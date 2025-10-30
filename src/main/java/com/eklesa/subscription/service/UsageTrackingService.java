package com.eklesa.subscription.service;

import com.eklesa.subscription.model.Subscription;
import com.eklesa.subscription.model.UsageRecord;
import com.eklesa.subscription.repository.SubscriptionRepository;
import com.eklesa.subscription.repository.UsageRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

/**
 * Servicio para tracking de uso de features.
 * 
 * RESPONSABILIDADES:
 * - Registrar uso actual de features
 * - Calcular porcentajes
 * - Detectar cuando se alcanza un límite
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UsageTrackingService {
    
    private final UsageRecordRepository usageRecordRepository;
    private final SubscriptionRepository subscriptionRepository;
    
    /**
     * Registra el uso actual de una feature.
     * 
     * LLAMADO POR: crm-service después de crear/eliminar recursos
     */
    @Transactional
    public void recordUsage(Long organizationId, String featureCode, Integer currentCount) {
        log.debug("Recording usage for org: {}, feature: {}, count: {}", 
            organizationId, featureCode, currentCount);
        
        Subscription subscription = subscriptionRepository
            .findByOrganizationId(organizationId)
            .orElseThrow(() -> new RuntimeException("Subscription not found"));
        
        // Obtener límite del plan
        Integer planLimit = getLimit(subscription.getPlan(), featureCode);
        
        // Calcular porcentaje
        BigDecimal usagePercentage = calculatePercentage(currentCount, planLimit);
        
        // Verificar si excede límite
        boolean limitExceeded = planLimit != null && currentCount > planLimit;
        
        UsageRecord record = UsageRecord.builder()
            .subscription(subscription)
            .featureCode(featureCode)
            .usageCount(currentCount)
            .planLimit(planLimit)
            .usagePercentage(usagePercentage)
            .recordedAt(LocalDateTime.now())
            .limitExceeded(limitExceeded)
            .build();
        
        usageRecordRepository.save(record);
        
        if (limitExceeded) {
            log.warn("Limit exceeded for org: {}, feature: {}, usage: {}, limit: {}", 
                organizationId, featureCode, currentCount, planLimit);
        }
    }
    
    /**
     * Obtiene el uso actual de una feature.
     */
    @Transactional(readOnly = true)
    public Integer getCurrentUsage(Long subscriptionId, String featureCode) {
        return usageRecordRepository
            .findTopBySubscriptionIdAndFeatureCodeOrderByRecordedAtDesc(subscriptionId, featureCode)
            .map(UsageRecord::getUsageCount)
            .orElse(0);
    }
    
    // ============================================
    // HELPERS
    // ============================================
    
    private Integer getLimit(com.eklesa.subscription.model.Plan plan, String featureCode) {
        switch (featureCode.toUpperCase()) {
            case "CONTACTS": return plan.getMaxContacts();
            case "USERS": return plan.getMaxUsers();
            case "PIPELINES": return plan.getMaxPipelines();
            case "DEALS": return plan.getMaxDeals();
            default: return null;
        }
    }
    
    private BigDecimal calculatePercentage(Integer current, Integer max) {
        if (max == null || max == 0) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(current)
            .divide(BigDecimal.valueOf(max), 4, RoundingMode.HALF_UP)
            .multiply(BigDecimal.valueOf(100));
    }
}
