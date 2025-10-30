package com.eklesa.subscription.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request para cancelar suscripción.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CancelSubscriptionRequest {
    
    @NotNull(message = "Organization ID es requerido")
    private Long organizationId;
    
    /**
     * Si cancela inmediatamente o al final del período.
     */
    private Boolean immediate = false;
    
    /**
     * Razón de cancelación (opcional).
     */
    private String reason;
    
    /**
     * Feedback adicional.
     */
    private String feedback;
}

