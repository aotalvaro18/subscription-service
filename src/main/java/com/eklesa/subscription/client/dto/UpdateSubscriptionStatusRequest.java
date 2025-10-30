package com.eklesa.subscription.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Request para actualizar status en auth-service.
 * 
 * ENVIADO POR: subscription-service a auth-service
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateSubscriptionStatusRequest {
    
    private Long organizationId;
    private String subscriptionStatus;
    private LocalDateTime trialEndsAt;
}

