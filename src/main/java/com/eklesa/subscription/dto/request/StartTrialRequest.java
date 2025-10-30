package com.eklesa.subscription.dto.request;

import com.eklesa.subscription.model.enums.OrganizationType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request para iniciar un trial.
 * 
 * LLAMADO POR: auth-service después de crear una organización
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StartTrialRequest {
    
    @NotNull(message = "Organization ID es requerido")
    private Long organizationId;
    
    @NotNull(message = "Organization type es requerido")
    private OrganizationType organizationType;
    
    /**
     * Email del owner para notificaciones.
     */
    private String ownerEmail;
    
    /**
     * Cognito sub del owner.
     */
    private String ownerCognitoSub;
}
