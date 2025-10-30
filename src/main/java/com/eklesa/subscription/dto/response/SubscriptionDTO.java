package com.eklesa.subscription.dto.response;

import com.eklesa.subscription.model.enums.BillingPeriod;
import com.eklesa.subscription.model.enums.SubscriptionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO de respuesta para Subscription.
 * 
 * USADO EN: Frontend para mostrar info de suscripción
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionDTO {
    
    private Long id;
    private Long organizationId;
    
    // Plan info
    private PlanDTO plan;
    
    // Status
    private SubscriptionStatus status;
    private BillingPeriod billingPeriod;
    
    // Trial
    private LocalDateTime trialStartDate;
    private LocalDateTime trialEndDate;
    private Boolean isTrialUsed;
    private Long daysLeftInTrial;
    
    // PayPal
    private String paypalSubscriptionId;
    private String paypalEmail;
    
    // Billing
    private LocalDateTime currentPeriodStart;
    private LocalDateTime currentPeriodEnd;
    private LocalDateTime nextBillingDate;
    private BigDecimal amount;
    private String currency;
    
    // Dates
    private LocalDateTime canceledAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Computed
    private Boolean canAccess;
    private Boolean isReadOnly;
    private Boolean isActive;
}
