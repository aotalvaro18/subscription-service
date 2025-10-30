package com.eklesa.subscription.service;

import com.eklesa.subscription.dto.response.PlanDTO;
import com.eklesa.subscription.dto.response.PlanFeatureDTO;
import com.eklesa.subscription.exception.SubscriptionException;
import com.eklesa.subscription.model.Plan;
import com.eklesa.subscription.model.PlanFeature;
import com.eklesa.subscription.model.enums.PlanTier;
import com.eklesa.subscription.repository.PlanFeatureRepository;
import com.eklesa.subscription.repository.PlanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Servicio para gestión de planes.
 * 
 * RESPONSABILIDADES:
 * - Obtener planes disponibles
 * - Mapear a DTOs con features
 * - Formatear precios
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PlanService {
    
    private final PlanRepository planRepository;
    private final PlanFeatureRepository planFeatureRepository;
    
    /**
     * Obtiene todos los planes activos.
     * 
     * USADO EN: PricingPage del frontend
     */
    @Transactional(readOnly = true)
    public List<PlanDTO> getAllActivePlans() {
        log.debug("Fetching all active plans");
        
        List<Plan> plans = planRepository.findByActiveTrueOrderBySortOrder();
        
        return plans.stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList());
    }
    
    /**
     * Obtiene plan por código.
     */
    @Transactional(readOnly = true)
    public Plan getPlanByCode(String code) {
        return planRepository.findByCode(code)
            .orElseThrow(() -> new SubscriptionException("Plan not found: " + code));
    }
    
    /**
     * Obtiene plan por tier.
     */
    @Transactional(readOnly = true)
    public Plan getPlanByTier(PlanTier tier) {
        return planRepository.findByTier(tier)
            .orElseThrow(() -> new SubscriptionException("Plan not found for tier: " + tier));
    }
    
    /**
     * Obtiene plan destacado.
     */
    @Transactional(readOnly = true)
    public PlanDTO getFeaturedPlan() {
        Plan plan = planRepository.findByIsFeaturedTrue()
            .orElseThrow(() -> new SubscriptionException("No featured plan found"));
        
        return mapToDTO(plan);
    }
    
    /**
     * Mapea Plan a DTO con features incluidas.
     */
    public PlanDTO mapToDTO(Plan plan) {
        List<PlanFeature> features = planFeatureRepository
            .findByPlanIdAndEnabledTrueOrderBySortOrder(plan.getId());
        
        return PlanDTO.builder()
            .id(plan.getId())
            .code(plan.getCode())
            .name(plan.getName())
            .tier(plan.getTier())
            .description(plan.getDescription())
            .monthlyPrice(plan.getMonthlyPrice())
            .annualPrice(plan.getAnnualPrice())
            .currency(plan.getCurrency())
            .maxContacts(plan.getMaxContacts())
            .maxUsers(plan.getMaxUsers())
            .maxPipelines(plan.getMaxPipelines())
            .maxDeals(plan.getMaxDeals())
            .maxStorageGb(plan.getMaxStorageGb())
            .active(plan.getActive())
            .isFeatured(plan.getIsFeatured())
            .sortOrder(plan.getSortOrder())
            .features(features.stream()
                .map(this::mapFeatureToDTO)
                .collect(Collectors.toList()))
            .monthlyPriceFormatted(formatPrice(plan.getMonthlyPrice(), plan.getCurrency()))
            .annualPriceFormatted(formatPrice(plan.getAnnualPrice(), plan.getCurrency()))
            .hasUnlimitedContacts(plan.hasUnlimitedContacts())
            .hasUnlimitedUsers(plan.hasUnlimitedUsers())
            .build();
    }
    
    /**
     * Mapea PlanFeature a DTO.
     */
    private PlanFeatureDTO mapFeatureToDTO(PlanFeature feature) {
        return PlanFeatureDTO.builder()
            .id(feature.getId())
            .featureCode(feature.getFeatureCode())
            .featureName(feature.getFeatureName())
            .description(feature.getDescription())
            .type(feature.getType())
            .enabled(feature.getEnabled())
            .limitValue(feature.getLimitValue())
            .sortOrder(feature.getSortOrder())
            .build();
    }
    
    /**
     * Formatea precio según la moneda.
     */
    private String formatPrice(java.math.BigDecimal price, String currency) {
        if (price == null) {
            return "Gratis";
        }
        
        Locale locale = "COP".equals(currency) ? new Locale("es", "CO") : Locale.US;
        NumberFormat formatter = NumberFormat.getCurrencyInstance(locale);
        
        return formatter.format(price);
    }
}
