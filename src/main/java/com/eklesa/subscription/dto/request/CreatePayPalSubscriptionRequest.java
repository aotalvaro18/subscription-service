package com.eklesa.subscription.dto.request;

import com.eklesa.subscription.model.enums.BillingPeriod;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request para crear suscripción en PayPal.
 * 
 * LLAMADO POR: Frontend cuando user selecciona un plan
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePayPalSubscriptionRequest {
    
    @NotNull(message = "Organization ID es requerido")
    private Long organizationId;
    
    @NotBlank(message = "Plan code es requerido")
    private String planCode;
    
    @NotNull(message = "Billing period es requerido")
    private BillingPeriod billingPeriod;
    
    /**
     * URLs de retorno después del checkout.
     */
    @NotBlank(message = "Return URL es requerida")
    private String returnUrl;
    
    @NotBlank(message = "Cancel URL es requerida")
    private String cancelUrl;
}

