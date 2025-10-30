package com.eklesa.subscription.service;

import com.eklesa.subscription.dto.request.ValidateFeatureLimitRequest;
import com.eklesa.subscription.dto.response.FeatureLimitValidationResponse;
import com.eklesa.subscription.dto.response.UsageLimitsDTO;
import com.eklesa.subscription.exception.FeatureLimitExceededException;
import com.eklesa.subscription.model.Plan;
import com.eklesa.subscription.model.Subscription;
import com.eklesa.subscription.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Servicio para validación de límites de features.
 * 
 * RESPONSABILIDADES:
 * - Validar si una org puede crear más recursos
 * - Calcular usage percentage
 * - Retornar información de límites
 * 
 * LLAMADO POR: crm-service antes de crear contactos, deals, etc.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FeatureLimitService {
    
    private final SubscriptionRepository subscriptionRepository;
    private final UsageTrackingService usageTrackingService;
    
    private static final BigDecimal SOFT_LIMIT_GRACE = new BigDecimal("1.10"); // 110%
    
    /**
     * Valida si puede usar una feature.
     * 
     * RETORNA:
     * - allowed: true/false
     * - reason: si no está permitido
     * - upgradeMessage: mensaje para mostrar al user
     */
    @Transactional(readOnly = true)
    public FeatureLimitValidationResponse validateFeatureLimit(ValidateFeatureLimitRequest request) {
        log.debug("Validating feature limit for org: {}, feature: {}", 
            request.getOrganizationId(), request.getFeatureCode());
        
        Subscription subscription = subscriptionRepository
            .findByOrganizationId(request.getOrganizationId())
            .orElseThrow(() -> new FeatureLimitExceededException("No active subscription found"));
        
        Plan plan = subscription.getPlan();
        
        // Si está en grace period o suspended, no permitir
        if (subscription.isReadOnly() || !subscription.canAccess()) {
            return FeatureLimitValidationResponse.builder()
                .allowed(false)
                .featureCode(request.getFeatureCode())
                .reason("Subscription is not active")
                .upgradeMessage("Tu suscripción ha expirado. Por favor, renueva tu plan para continuar.")
                .build();
        }
        
        // Obtener límite del plan
        Integer maxLimit = getLimit(plan, request.getFeatureCode());
        
        // Si es unlimited, permitir
        if (maxLimit == null) {
            return FeatureLimitValidationResponse.builder()
                .allowed(true)
                .featureCode(request.getFeatureCode())
                .currentUsage(request.getCurrentCount())
                .maxLimit(null)
                .remaining(null)
                .usagePercentage(BigDecimal.ZERO)
                .build();
        }
        
        // Calcular nuevo uso
        int newUsage = request.getCurrentCount() + request.getIncrementBy();
        
        // Soft limit: permitir hasta 110% del límite
        int softLimit = (int) (maxLimit * SOFT_LIMIT_GRACE.doubleValue());
        
        if (newUsage > softLimit) {
            // Hard limit exceeded
            return FeatureLimitValidationResponse.builder()
                .allowed(false)
                .featureCode(request.getFeatureCode())
                .currentUsage(request.getCurrentCount())
                .maxLimit(maxLimit)
                .remaining(0)
                .usagePercentage(calculatePercentage(request.getCurrentCount(), maxLimit))
                .reason("Feature limit exceeded")
                .upgradeMessage(buildUpgradeMessage(request.getFeatureCode(), plan))
                .recommendedPlan(getRecommendedPlan(plan))
                .build();
        }
        
        // Permitir pero con warning si está cerca del límite
        boolean isNearLimit = newUsage > maxLimit;
        
        return FeatureLimitValidationResponse.builder()
            .allowed(true)
            .featureCode(request.getFeatureCode())
            .currentUsage(request.getCurrentCount())
            .maxLimit(maxLimit)
            .remaining(Math.max(0, maxLimit - newUsage))
            .usagePercentage(calculatePercentage(newUsage, maxLimit))
            .upgradeMessage(isNearLimit ? "Estás alcanzando el límite de tu plan. Considera actualizar." : null)
            .recommendedPlan(isNearLimit ? getRecommendedPlan(plan) : null)
            .build();
    }
    
    /**
     * Obtiene límites actuales de una organización.
     * 
     * USADO EN: Frontend para mostrar usage bars
     */
    @Transactional(readOnly = true)
    public UsageLimitsDTO getCurrentLimits(Long organizationId) {
        log.debug("Getting current limits for org: {}", organizationId);
        
        Subscription subscription = subscriptionRepository
            .findByOrganizationId(organizationId)
            .orElseThrow(() -> new FeatureLimitExceededException("No active subscription found"));
        
        Plan plan = subscription.getPlan();
        
        // Obtener usage actual de cada feature
        Integer currentContacts = usageTrackingService.getCurrentUsage(subscription.getId(), "CONTACTS");
        Integer currentUsers = usageTrackingService.getCurrentUsage(subscription.getId(), "USERS");
        Integer currentPipelines = usageTrackingService.getCurrentUsage(subscription.getId(), "PIPELINES");
        Integer currentDeals = usageTrackingService.getCurrentUsage(subscription.getId(), "DEALS");
        
        return UsageLimitsDTO.builder()
            // Contacts
            .maxContacts(plan.getMaxContacts())
            .currentContacts(currentContacts != null ? currentContacts : 0)
            .canCreateContact(canCreate(currentContacts, plan.getMaxContacts()))
            .contactsRemaining(calculateRemaining(currentContacts, plan.getMaxContacts()))
            
            // Users
            .maxUsers(plan.getMaxUsers())
            .currentUsers(currentUsers != null ? currentUsers : 0)
            .canCreateUser(canCreate(currentUsers, plan.getMaxUsers()))
            .usersRemaining(calculateRemaining(currentUsers, plan.getMaxUsers()))
            
            // Pipelines
            .maxPipelines(plan.getMaxPipelines())
            .currentPipelines(currentPipelines != null ? currentPipelines : 0)
            .canCreatePipeline(canCreate(currentPipelines, plan.getMaxPipelines()))
            
            // Deals
            .maxDeals(plan.getMaxDeals())
            .currentDeals(currentDeals != null ? currentDeals : 0)
            .canCreateDeal(canCreate(currentDeals, plan.getMaxDeals()))
            
            // General
            .planCode(plan.getCode())
            .planName(plan.getName())
            .isReadOnly(subscription.isReadOnly())
            .build();
    }
    
    // ============================================
    // HELPERS
    // ============================================
    
    private Integer getLimit(Plan plan, String featureCode) {
        switch (featureCode.toUpperCase()) {
            case "CONTACTS":
                return plan.getMaxContacts();
            case "USERS":
                return plan.getMaxUsers();
            case "PIPELINES":
                return plan.getMaxPipelines();
            case "DEALS":
                return plan.getMaxDeals();
            default:
                return null; // Unlimited
        }
    }
    
    private BigDecimal calculatePercentage(int current, Integer max) {
        if (max == null || max == 0) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(current)
            .divide(BigDecimal.valueOf(max), 4, RoundingMode.HALF_UP)
            .multiply(BigDecimal.valueOf(100));
    }
    
    private boolean canCreate(Integer current, Integer max) {
        if (max == null) {
            return true; // Unlimited
        }
        int actualCurrent = current != null ? current : 0;
        int softLimit = (int) (max * SOFT_LIMIT_GRACE.doubleValue());
        return actualCurrent < softLimit;
    }
    
    private Integer calculateRemaining(Integer current, Integer max) {
        if (max == null) {
            return null; // Unlimited
        }
        int actualCurrent = current != null ? current : 0;
        return Math.max(0, max - actualCurrent);
    }
    
    private String buildUpgradeMessage(String featureCode, Plan plan) {
        String featureName = getFeatureName(featureCode);
        return String.format(
            "Has alcanzado el límite de %s de tu plan %s. Actualiza a un plan superior para continuar.",
            featureName,
            plan.getName()
        );
    }
    
    private String getFeatureName(String featureCode) {
        switch (featureCode.toUpperCase()) {
            case "CONTACTS": return "contactos";
            case "USERS": return "usuarios";
            case "PIPELINES": return "pipelines";
            case "DEALS": return "deals";
            default: return "recursos";
        }
    }
    
    private String getRecommendedPlan(Plan currentPlan) {
        switch (currentPlan.getTier()) {
            case STARTER:
                return "PROFESSIONAL";
            case PROFESSIONAL:
                return "ENTERPRISE";
            default:
                return null;
        }
    }
}
