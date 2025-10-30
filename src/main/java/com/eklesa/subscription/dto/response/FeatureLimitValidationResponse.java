package com.eklesa.subscription.dto.response;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response de validación de límites.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeatureLimitValidationResponse {
    
    private Boolean allowed;
    private String featureCode;
    private Integer currentUsage;
    private Integer maxLimit;
    private Integer remaining;
    private BigDecimal usagePercentage;
    
    // Si no está permitido
    private String reason;
    private String upgradeMessage;
    private String recommendedPlan;
}
