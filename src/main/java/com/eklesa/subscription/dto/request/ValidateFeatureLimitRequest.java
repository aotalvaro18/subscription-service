package com.eklesa.subscription.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request para validar si puede usar una feature.
 * 
 * LLAMADO POR: crm-service antes de crear contactos, deals, etc.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidateFeatureLimitRequest {
    
    @NotNull(message = "Organization ID es requerido")
    private Long organizationId;
    
    @NotBlank(message = "Feature code es requerido")
    private String featureCode; // "CONTACTS", "USERS", "DEALS", etc.
    
    /**
     * Cantidad actual de uso.
     */
    @NotNull(message = "Current count es requerido")
    private Integer currentCount;
    
    /**
     * Cantidad que se quiere añadir.
     */
    private Integer incrementBy = 1;
}
