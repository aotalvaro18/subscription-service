package com.eklesa.subscription.client.dto;

import com.eklesa.subscription.model.enums.OrganizationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO de Organization del auth-service.
 * 
 * SOLO para comunicación vía Feign Client.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationDTO {
    
    private Long id;
    private String name;
    private String code;
    
    // Subscription fields (que agregamos en auth-service)
    private String subscriptionStatus;
    private OrganizationType organizationType;
    private LocalDateTime trialEndsAt;
    
    private Boolean active;
    private LocalDateTime createdAt;
}
