package com.eklesa.subscription.controller;

import com.eklesa.subscription.dto.request.CancelSubscriptionRequest;
import com.eklesa.subscription.dto.request.StartTrialRequest;
import com.eklesa.subscription.dto.request.UpgradePlanRequest;
import com.eklesa.subscription.dto.response.SubscriptionDTO;
import com.eklesa.subscription.service.SubscriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Controller para gestión de suscripciones.
 * 
 * ENDPOINTS:
 * - POST   /api/subscriptions/start-trial
 * - GET    /api/subscriptions/organization/{orgId}
 * - POST   /api/subscriptions/upgrade
 * - POST   /api/subscriptions/cancel
 * 
 * SEGURIDAD:
 * - Requiere autenticación (JWT)
 * - Solo OWNER puede modificar suscripción
 */
@RestController
@RequestMapping("/api/subscriptions")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Subscriptions", description = "Gestión de suscripciones")
public class SubscriptionController {
    
    private final SubscriptionService subscriptionService;
    
    /**
     * Inicia un trial para una organización.
     * 
     * LLAMADO POR: auth-service después de crear organization
     * 
     * SECURITY: Internal API call (API key en header)
     */
    @PostMapping("/start-trial")
    @Operation(summary = "Iniciar trial", description = "Crea una suscripción trial para una organización nueva")
    public ResponseEntity<SubscriptionDTO> startTrial(
        @Valid @RequestBody StartTrialRequest request
    ) {
        log.info("REST request to start trial for organization: {}", request.getOrganizationId());
        
        SubscriptionDTO subscription = subscriptionService.startTrial(request);
        
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(subscription);
    }
    
    /**
     * Obtiene la suscripción de una organización.
     * 
     * LLAMADO POR: Frontend en todas las páginas (para TrialBanner, etc.)
     */
    @GetMapping("/organization/{organizationId}")
    @PreAuthorize("hasRole('OWNER') or hasRole('ADMIN')")
    @Operation(summary = "Obtener suscripción", description = "Obtiene la suscripción activa de una organización")
    public ResponseEntity<SubscriptionDTO> getSubscriptionByOrganization(
        @PathVariable Long organizationId
    ) {
        log.info("REST request to get subscription for organization: {}", organizationId);
        
        SubscriptionDTO subscription = subscriptionService.getByOrganizationId(organizationId);
        
        return ResponseEntity.ok(subscription);
    }
    
    /**
     * Upgrade/downgrade de plan.
     * 
     * LLAMADO POR: Frontend cuando user selecciona nuevo plan
     */
    @PostMapping("/upgrade")
    @PreAuthorize("hasRole('OWNER')")
    @Operation(summary = "Cambiar plan", description = "Upgrade o downgrade de plan de suscripción")
    public ResponseEntity<SubscriptionDTO> upgradePlan(
        @Valid @RequestBody UpgradePlanRequest request
    ) {
        log.info("REST request to upgrade plan for organization: {} to plan: {}", 
            request.getOrganizationId(), request.getPlanCode());
        
        SubscriptionDTO subscription = subscriptionService.upgradePlan(request);
        
        return ResponseEntity.ok(subscription);
    }
    
    /**
     * Cancela una suscripción.
     * 
     * LLAMADO POR: Frontend desde BillingPage
     */
    @PostMapping("/cancel")
    @PreAuthorize("hasRole('OWNER')")
    @Operation(summary = "Cancelar suscripción", description = "Cancela la suscripción activa")
    public ResponseEntity<Void> cancelSubscription(
        @Valid @RequestBody CancelSubscriptionRequest request
    ) {
        log.info("REST request to cancel subscription for organization: {}", request.getOrganizationId());
        
        subscriptionService.cancelSubscription(request);
        
        return ResponseEntity.noContent().build();
    }
}
