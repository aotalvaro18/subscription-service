package com.eklesa.subscription.scheduler;

import com.eklesa.subscription.event.publisher.SubscriptionEventPublisher;
import com.eklesa.subscription.model.Subscription;
import com.eklesa.subscription.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Job que envía recordatorios de trial próximo a expirar.
 * 
 * SCHEDULE: Diario a las 8 AM
 * 
 * NOTIFICACIONES:
 * - Día 14: "Te quedan 7 días"
 * - Día 18: "Te quedan 3 días"
 * - Día 20: "Mañana expira tu prueba"
 * - Día 21: "Tu prueba expiró hoy"
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TrialReminderJob {
    
    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionEventPublisher eventPublisher;
    
    /**
     * Ejecuta diariamente a las 8 AM.
     */
    @Scheduled(cron = "${app.scheduler.trial-reminder-cron:0 0 8 * * ?}")
    public void sendTrialReminders() {
        log.info("Starting trial reminder job");
        
        LocalDateTime now = LocalDateTime.now();
        
        // Recordatorio: 7 días restantes
        sendRemindersForDaysLeft(7, now);
        
        // Recordatorio: 3 días restantes
        sendRemindersForDaysLeft(3, now);
        
        // Recordatorio: 1 día restante
        sendRemindersForDaysLeft(1, now);
        
        log.info("Trial reminder job completed");
    }
    
    private void sendRemindersForDaysLeft(int daysLeft, LocalDateTime now) {
        LocalDateTime targetDate = now.plusDays(daysLeft);
        LocalDateTime startOfDay = targetDate.toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);
        
        // Buscar trials que expiran exactamente en X días
        List<Subscription> subscriptions = subscriptionRepository
            .findTrialsExpiringBefore(endOfDay)
            .stream()
            .filter(s -> s.getTrialEndDate().isAfter(startOfDay))
            .toList();
        
        log.info("Found {} trials expiring in {} days", subscriptions.size(), daysLeft);
        
        for (Subscription subscription : subscriptions) {
            try {
                eventPublisher.publishTrialExpiring(subscription, daysLeft);
                log.info("Sent trial reminder for org: {}, days left: {}", 
                    subscription.getOrganizationId(), daysLeft);
            } catch (Exception e) {
                log.error("Error sending trial reminder for subscription: {}", 
                    subscription.getId(), e);
            }
        }
    }
}
