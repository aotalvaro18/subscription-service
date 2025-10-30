package com.eklesa.subscription.dto.request;

import com.eklesa.subscription.model.enums.BillingPeriod;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request para upgrade/downgrade de plan.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpgradePlanRequest {
    
    @NotNull(message = "Organization ID es requerido")
    private Long organizationId;
    
    @NotBlank(message = "Plan code es requerido")
    private String planCode; // "PROFESSIONAL", "ENTERPRISE"
    
    @NotNull(message = "Billing period es requerido")
    private BillingPeriod billingPeriod;
    
    /**
     * Si es un upgrade inmediato (con proration).
     */
    private Boolean immediate = true;
}
