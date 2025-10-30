package com.eklesa.subscription.service;

import com.eklesa.subscription.client.AuthServiceClient;
import com.eklesa.subscription.client.dto.UpdateSubscriptionStatusRequest;
import com.eklesa.subscription.dto.request.CancelSubscriptionRequest;
import com.eklesa.subscription.dto.request.StartTrialRequest;
import com.eklesa.subscription.dto.request.UpgradePlanRequest;
import com.eklesa.subscription.dto.response.SubscriptionDTO;
import com.eklesa.subscription.event.publisher.SubscriptionEventPublisher;
import com.eklesa.subscription.exception.SubscriptionException;
import com.eklesa.subscription.model.Plan;
import com.eklesa.subscription.model.Subscription;
import com.eklesa.subscription.model.enums.PlanTier;
import com.eklesa.subscription.model.enums.SubscriptionStatus;
import com.eklesa.subscription.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Servicio principal para gestión de suscripciones.
 * 
 * RESPONSABILIDADES:
 * - Iniciar trials
 * - Activar suscripciones
 * - Upgrades/downgrades
 * - Cancelaciones
 * - Sincronización con auth-service
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionService {
    
    private final SubscriptionRepository subscriptionRepository;
    private final PlanService planService;
    private final AuthServiceClient authServiceClient;
    private final SubscriptionEventPublisher eventPublisher;
    
    private static final int TRIAL_DAYS = 21;
    private static final int GRACE_PERIOD_DAYS = 7;
    
    /**
     * Inicia un trial para una organización nueva.
     * 
     * LLAMADO POR: auth-service después de crear org
     * 
     * FLOW:
     * 1. Verifica que no exista subscription previa
     * 2. Crea subscription con plan STARTER
     * 3. Status = TRIALING
     * 4. trialEndDate = now + 21 días
     * 5. Sincroniza con auth-service
     * 6. Publica evento TrialStartedEvent
     */
    @Transactional
    public SubscriptionDTO startTrial(StartTrialRequest request) {
        log.info("Starting trial for organization: {}", request.getOrganizationId());
        
        // Validar que no existe subscription previa
        if (subscriptionRepository.findByOrganizationId(request.getOrganizationId()).isPresent()) {
            throw new SubscriptionException("Organization already has a subscription");
        }
        
        // Obtener plan STARTER
        Plan starterPlan = planService.getPlanByTier(PlanTier.STARTER);
        
        // Crear subscription
        Subscription subscription = Subscription.builder()
            .organizationId(request.getOrganizationId())
            .plan(starterPlan)
            .status(SubscriptionStatus.TRIALING)
            .trialStartDate(LocalDateTime.now())
            .trialEndDate(LocalDateTime.now().plusDays(TRIAL_DAYS))
            .isTrialUsed(true)
            .build();
        
        subscription = subscriptionRepository.save(subscription);
        
        // Sincronizar con auth-service
        syncWithAuthService(subscription);
        
        // Publicar evento
        eventPublisher.publishTrialStarted(subscription, request.getOwnerEmail());
        
        log.info("Trial started successfully for org: {}", request.getOrganizationId());
        
        return mapToDTO(subscription);
    }
    
    /**
     * Obtiene subscription por organization ID.
     */
    @Transactional(readOnly = true)
    public SubscriptionDTO getByOrganizationId(Long organizationId) {
        Subscription subscription = subscriptionRepository
            .findByOrganizationId(organizationId)
            .orElseThrow(() -> new SubscriptionException("Subscription not found for organization: " + organizationId));
        
        return mapToDTO(subscription);
    }
    
    /**
     * Activa una suscripción después de pago exitoso.
     * 
     * LLAMADO POR: PayPalService después de webhook de pago
     */
    @Transactional
    public SubscriptionDTO activateSubscription(
        Long organizationId, 
        String planCode,
        String paypalSubscriptionId,
        String paypalPayerId,
        String billingPeriod
    ) {
        log.info("Activating subscription for org: {}", organizationId);
        
        Subscription subscription = subscriptionRepository
            .findByOrganizationId(organizationId)
            .orElseThrow(() -> new SubscriptionException("Subscription not found"));
        
        Plan newPlan = planService.getPlanByCode(planCode);
        
        // Activar
        subscription.activate(
            newPlan, 
            com.eklesa.subscription.model.enums.BillingPeriod.valueOf(billingPeriod),
            paypalSubscriptionId,
            paypalPayerId
        );
        
        subscription = subscriptionRepository.save(subscription);
        
        // Sincronizar con auth-service
        syncWithAuthService(subscription);
        
        // Publicar evento
        eventPublisher.publishSubscriptionActivated(subscription);
        
        log.info("Subscription activated for org: {}", organizationId);
        
        return mapToDTO(subscription);
    }
    
    /**
     * Upgrade/downgrade de plan.
     */
    @Transactional
    public SubscriptionDTO upgradePlan(UpgradePlanRequest request) {
        log.info("Upgrading plan for org: {} to {}", request.getOrganizationId(), request.getPlanCode());
        
        Subscription subscription = subscriptionRepository
            .findByOrganizationId(request.getOrganizationId())
            .orElseThrow(() -> new SubscriptionException("Subscription not found"));
        
        Plan newPlan = planService.getPlanByCode(request.getPlanCode());
        
        // Validar upgrade
        if (!canUpgrade(subscription.getPlan(), newPlan)) {
            throw new SubscriptionException("Cannot downgrade from " + subscription.getPlan().getCode() + " to " + newPlan.getCode());
        }
        
        // Actualizar plan
        subscription.setPlan(newPlan);
        subscription.setBillingPeriod(request.getBillingPeriod());
        
        subscription = subscriptionRepository.save(subscription);
        
        // Sincronizar con auth-service
        syncWithAuthService(subscription);
        
        log.info("Plan upgraded successfully for org: {}", request.getOrganizationId());
        
        return mapToDTO(subscription);
    }
    
    /**
     * Cancela una suscripción.
     */
    @Transactional
    public void cancelSubscription(CancelSubscriptionRequest request) {
        log.info("Canceling subscription for org: {}", request.getOrganizationId());
        
        Subscription subscription = subscriptionRepository
            .findByOrganizationId(request.getOrganizationId())
            .orElseThrow(() -> new SubscriptionException("Subscription not found"));
        
        if (request.getImmediate()) {
            // Cancelación inmediata
            subscription.cancel();
            subscription.setEndedAt(LocalDateTime.now());
        } else {
            // Cancelar al final del período
            subscription.setStatus(SubscriptionStatus.CANCELED);
            subscription.setCanceledAt(LocalDateTime.now());
            // endedAt = currentPeriodEnd (se procesa después)
        }
        
        subscriptionRepository.save(subscription);
        
        // Sincronizar con auth-service
        syncWithAuthService(subscription);
        
        // Publicar evento
        eventPublisher.publishSubscriptionCanceled(subscription, request.getReason());
        
        log.info("Subscription canceled for org: {}", request.getOrganizationId());
    }
    
    /**
     * Marca trial como expirado y entra en grace period.
     * 
     * LLAMADO POR: Scheduled job
     */
    @Transactional
    public void expireTrial(Long subscriptionId) {
        Subscription subscription = subscriptionRepository
            .findById(subscriptionId)
            .orElseThrow(() -> new SubscriptionException("Subscription not found"));
        
        if (!subscription.isTrialing()) {
            log.warn("Subscription {} is not in trial, skipping expiration", subscriptionId);
            return;
        }
        
        log.info("Expiring trial for subscription: {}", subscriptionId);
        
        subscription.enterGracePeriod();
        subscriptionRepository.save(subscription);
        
        // Sincronizar con auth-service
        syncWithAuthService(subscription);
        
        // Publicar evento
        eventPublisher.publishTrialExpired(subscription);
    }
    
    /**
     * Suspende subscription después de grace period.
     * 
     * LLAMADO POR: Scheduled job
     */
    @Transactional
    public void suspendSubscription(Long subscriptionId) {
        Subscription subscription = subscriptionRepository
            .findById(subscriptionId)
            .orElseThrow(() -> new SubscriptionException("Subscription not found"));
        
        log.info("Suspending subscription: {}", subscriptionId);
        
        subscription.suspend();
        subscriptionRepository.save(subscription);
        
        // Sincronizar con auth-service
        syncWithAuthService(subscription);
        
        // Publicar evento
        eventPublisher.publishSubscriptionSuspended(subscription);
    }
    
    // ============================================
    // HELPERS
    // ============================================
    
    /**
     * Sincroniza status con auth-service.
     */
    private void syncWithAuthService(Subscription subscription) {
        try {
            UpdateSubscriptionStatusRequest request = UpdateSubscriptionStatusRequest.builder()
                .organizationId(subscription.getOrganizationId())
                .subscriptionStatus(subscription.getStatus().name())
                .trialEndsAt(subscription.getTrialEndDate())
                .build();
            
            authServiceClient.updateSubscriptionStatus(request);
            
            log.debug("Synced subscription status with auth-service for org: {}", subscription.getOrganizationId());
        } catch (Exception e) {
            log.error("Failed to sync with auth-service for org: {}", subscription.getOrganizationId(), e);
            // No lanzar excepción, solo loggear
        }
    }
    
    /**
     * Valida si puede hacer upgrade.
     */
    private boolean canUpgrade(Plan currentPlan, Plan newPlan) {
        return newPlan.getTier().getLevel() >= currentPlan.getTier().getLevel();
    }
    
    /**
     * Mapea Subscription a DTO.
     */
    private SubscriptionDTO mapToDTO(Subscription subscription) {
        return SubscriptionDTO.builder()
            .id(subscription.getId())
            .organizationId(subscription.getOrganizationId())
            .plan(planService.mapToDTO(subscription.getPlan()))
            .status(subscription.getStatus())
            .billingPeriod(subscription.getBillingPeriod())
            .trialStartDate(subscription.getTrialStartDate())
            .trialEndDate(subscription.getTrialEndDate())
            .isTrialUsed(subscription.getIsTrialUsed())
            .daysLeftInTrial(subscription.getDaysLeftInTrial())
            .paypalSubscriptionId(subscription.getPaypalSubscriptionId())
            .paypalEmail(subscription.getPaypalEmail())
            .currentPeriodStart(subscription.getCurrentPeriodStart())
            .currentPeriodEnd(subscription.getCurrentPeriodEnd())
            .nextBillingDate(subscription.getNextBillingDate())
            .amount(subscription.getAmount())
            .currency(subscription.getCurrency())
            .canceledAt(subscription.getCanceledAt())
            .createdAt(subscription.getCreatedAt())
            .updatedAt(subscription.getUpdatedAt())
            .canAccess(subscription.canAccess())
            .isReadOnly(subscription.isReadOnly())
            .isActive(subscription.isActive())
            .build();
    }
}
