package com.eklesa.subscription.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response con URL de checkout de PayPal.
 * 
 * USADO POR: Frontend para redirigir a PayPal
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayPalCheckoutResponse {
    
    /**
     * URL de aprobación de PayPal.
     * Frontend redirige aquí.
     */
    private String approvalUrl;
    
    /**
     * ID de la suscripción en PayPal.
     */
    private String paypalSubscriptionId;
    
    /**
     * Token para tracking.
     */
    private String token;
}
