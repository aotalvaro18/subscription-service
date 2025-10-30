package com.eklesa.subscription.dto.response;

import com.eklesa.subscription.model.enums.PlanTier;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO de respuesta para Plan.
 * 
 * USADO EN: Frontend para mostrar planes en /pricing
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlanDTO {
    
    private Long id;
    private String code;
    private String name;
    private PlanTier tier;
    private String description;
    
    // Pricing
    private BigDecimal monthlyPrice;
    private BigDecimal annualPrice;
    private String currency;
    
    // Limits
    private Integer maxContacts;
    private Integer maxUsers;
    private Integer maxPipelines;
    private Integer maxDeals;
    private Integer maxStorageGb;
    
    // Display
    private Boolean active;
    private Boolean isFeatured;
    private Integer sortOrder;
    
    // Features
    private List<PlanFeatureDTO> features;
    
    // Helpers
    private String monthlyPriceFormatted;
    private String annualPriceFormatted;
    private Boolean hasUnlimitedContacts;
    private Boolean hasUnlimitedUsers;
}

