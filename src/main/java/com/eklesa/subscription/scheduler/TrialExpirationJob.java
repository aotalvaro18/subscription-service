package com.eklesa.subscription.scheduler;

import com.eklesa.subscription.model.Subscription;
import com.eklesa.subscription.model.enums.SubscriptionStatus;
import com.eklesa.subscription.repository.SubscriptionRepository;
import com.eklesa.subscription.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Job que verifica trials expirados.
 * 
 * SCHEDULE: Diario a las 2 AM
 * 
 * FLOW:
 * 1. Busca trials con trialEndDate < now
 * 2. Los marca como GRACE_PERIOD
 * 3. Sincroniza con auth-service
 * 4. Publica evento TrialExpired
 * 5. Envía email de notificación
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TrialExpirationJob {
    
    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionService subscriptionService;
    
    /**
     * Ejecuta diariamente a las 2 AM.
     */
    @Scheduled(cron = "${app.scheduler.trial-expiration-cron:0 0 2 * * ?}")
    public void checkExpiredTrials() {
        log.info("Starting trial expiration job");
        
        LocalDateTime now = LocalDateTime.now();
        
        // Buscar trials expirados
        List<Subscription> expiredTrials = subscriptionRepository
            .findTrialsExpiringBefore(now);
        
        log.info("Found {} expired trials", expiredTrials.size());
        
        // Procesar cada uno
        for (Subscription subscription : expiredTrials) {
            try {
                subscriptionService.expireTrial(subscription.getId());
                log.info("Expired trial for org: {}", subscription.getOrganizationId());
            } catch (Exception e) {
                log.error("Error expiring trial for subscription: {}", subscription.getId(), e);
            }
        }
        
        // Buscar subscriptions en grace period que deben ser suspendidas
        LocalDateTime gracePeriodEnd = now.minusDays(7);
        List<Subscription> gracePeriodExpired = subscriptionRepository
            .findByStatusAndUpdatedAtBefore(SubscriptionStatus.GRACE_PERIOD, gracePeriodEnd);
        
        log.info("Found {} subscriptions to suspend", gracePeriodExpired.size());
        
        for (Subscription subscription : gracePeriodExpired) {
            try {
                subscriptionService.suspendSubscription(subscription.getId());
                log.info("Suspended subscription for org: {}", subscription.getOrganizationId());
            } catch (Exception e) {
                log.error("Error suspending subscription: {}", subscription.getId(), e);
            }
        }
        
        log.info("Trial expiration job completed");
    }
}
