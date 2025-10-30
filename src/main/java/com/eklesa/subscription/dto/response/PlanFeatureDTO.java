package com.eklesa.subscription.dto.response;

import com.eklesa.subscription.model.enums.FeatureType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para features de un plan.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlanFeatureDTO {
    
    private Long id;
    private String featureCode;
    private String featureName;
    private String description;
    private FeatureType type;
    private Boolean enabled;
    private String limitValue;
    private Integer sortOrder;
}
