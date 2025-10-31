package com.eklesa.subscription.event.publisher;

import com.eklesa.subscription.event.payload.*;
import com.eklesa.subscription.model.Subscription;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

/**
 * Publicador de eventos de suscripción via SQS.
 * 
 * EVENTOS:
 * - TrialStartedEvent
 * - TrialExpiringEvent
 * - TrialExpiredEvent
 * - SubscriptionActivatedEvent
 * - SubscriptionCanceledEvent
 * - PaymentFailedEvent
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SubscriptionEventPublisher {
    
    private final SqsClient sqsClient;
    private final ObjectMapper objectMapper;
    
    @Value("${app.sqs.subscription-events-queue-url}")
    private String queueUrl;
    
    /**
     * Publica evento de trial iniciado.
     */
    public void publishTrialStarted(Subscription subscription, String ownerEmail) {
        TrialStartedEvent event = TrialStartedEvent.builder()
            .organizationId(subscription.getOrganizationId())
            .subscriptionId(subscription.getId())
            .trialEndDate(subscription.getTrialEndDate())
            .ownerEmail(ownerEmail)
            .timestamp(java.time.LocalDateTime.now())
            .build();
        
        publishEvent("TRIAL_STARTED", event);
    }
    
    /**
     * Publica evento de trial próximo a expirar.
     */
    public void publishTrialExpiring(Subscription subscription, long daysLeft) {
        TrialExpiringEvent event = TrialExpiringEvent.builder()
            .organizationId(subscription.getOrganizationId())
            .subscriptionId(subscription.getId())
            .daysLeft(daysLeft)
            .trialEndDate(subscription.getTrialEndDate())
            .timestamp(java.time.LocalDateTime.now())
            .build();
        
        publishEvent("TRIAL_EXPIRING", event);
    }
    
    /**
     * Publica evento de trial expirado.
     */
    public void publishTrialExpired(Subscription subscription) {
        TrialExpiredEvent event = TrialExpiredEvent.builder()
            .organizationId(subscription.getOrganizationId())
            .subscriptionId(subscription.getId())
            .timestamp(java.time.LocalDateTime.now())
            .build();
        
        publishEvent("TRIAL_EXPIRED", event);
    }
    
    /**
     * Publica evento de suscripción activada.
     */
    public void publishSubscriptionActivated(Subscription subscription) {
        SubscriptionActivatedEvent event = SubscriptionActivatedEvent.builder()
            .organizationId(subscription.getOrganizationId())
            .subscriptionId(subscription.getId())
            .planCode(subscription.getPlan().getCode())
            .billingPeriod(subscription.getBillingPeriod().name())
            .amount(subscription.getAmount())
            .timestamp(java.time.LocalDateTime.now())
            .build();
        
        publishEvent("SUBSCRIPTION_ACTIVATED", event);
    }
    
    /**
     * Publica evento de suscripción cancelada.
     */
    public void publishSubscriptionCanceled(Subscription subscription, String reason) {
        SubscriptionCanceledEvent event = SubscriptionCanceledEvent.builder()
            .organizationId(subscription.getOrganizationId())
            .subscriptionId(subscription.getId())
            .reason(reason)
            .timestamp(java.time.LocalDateTime.now())
            .build();
        
        publishEvent("SUBSCRIPTION_CANCELED", event);
    }
    
    /**
     * Publica evento de suscripción suspendida.
     */
    public void publishSubscriptionSuspended(Subscription subscription) {
        SubscriptionSuspendedEvent event = SubscriptionSuspendedEvent.builder()
            .organizationId(subscription.getOrganizationId())
            .subscriptionId(subscription.getId())
            .timestamp(java.time.LocalDateTime.now())
            .build();
        
        publishEvent("SUBSCRIPTION_SUSPENDED", event);
    }
    
    /**
     * Publica evento de pago fallido.
     */
    public void publishPaymentFailed(Subscription subscription, String reason) {
        PaymentFailedEvent event = PaymentFailedEvent.builder()
            .organizationId(subscription.getOrganizationId())
            .subscriptionId(subscription.getId())
            .paypalSubscriptionId(subscription.getPaypalSubscriptionId())
            .reason(reason)
            .timestamp(java.time.LocalDateTime.now())
            .build();
        
        publishEvent("PAYMENT_FAILED", event);
    }
    
    // ============================================
    // HELPER
    // ============================================
    
    private void publishEvent(String eventType, Object event) {
        try {
            String messageBody = objectMapper.writeValueAsString(event);
            
            SendMessageRequest request = SendMessageRequest.builder()
                .queueUrl(queueUrl)
                .messageBody(messageBody)
                .messageAttributes(Map.of(
                    "eventType", software.amazon.awssdk.services.sqs.model.MessageAttributeValue.builder()
                        .dataType("String")
                        .stringValue(eventType)
                        .build()
                ))
                .build();
            
            sqsClient.sendMessage(request);
            
            log.info("Published event: {} for org: {}", eventType, 
                ((BaseEvent) event).getOrganizationId());
            
        } catch (Exception e) {
            log.error("Failed to publish event: {}", eventType, e);
        }
    }
}