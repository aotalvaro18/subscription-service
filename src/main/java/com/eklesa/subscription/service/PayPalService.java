package com.eklesa.subscription.service;

import com.eklesa.subscription.dto.request.CreatePayPalSubscriptionRequest;
import com.eklesa.subscription.dto.response.PayPalCheckoutResponse;
import com.eklesa.subscription.exception.PaymentProcessingException;
import com.eklesa.subscription.model.Plan;
import com.eklesa.subscription.model.Subscription;
import com.eklesa.subscription.repository.SubscriptionRepository;
import com.eklesa.subscription.util.PayPalWebhookValidator;
import com.paypal.api.payments.*;
import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.PayPalRESTException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Servicio para integración con PayPal Subscriptions API.
 * 
 * RESPONSABILIDADES:
 * - Crear suscripciones en PayPal
 * - Procesar webhooks de PayPal
 * - Cancelar suscripciones
 * - Verificar firmas de webhooks
 * 
 * DOCS: https://developer.paypal.com/docs/subscriptions/
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PayPalService {
    
    private final APIContext apiContext;
    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionService subscriptionService;
    private final PlanService planService;
    private final PayPalWebhookValidator webhookValidator;
    
    @Value("${app.frontend-url}")
    private String frontendUrl;
    
    /**
     * Crea una suscripción en PayPal y retorna URL de aprobación.
     * 
     * FLOW:
     * 1. Obtiene plan y pricing
     * 2. Crea billing agreement en PayPal
     * 3. Retorna approval URL para redirect
     * 
     * LLAMADO POR: Frontend cuando user hace click en "Suscribirse"
     */
    public PayPalCheckoutResponse createPayPalSubscription(CreatePayPalSubscriptionRequest request) {
        log.info("Creating PayPal subscription for org: {}", request.getOrganizationId());
        
        // ✅ CORRECCIÓN: El bloque 'try' ahora captura 'Exception' para manejar todos
        // los errores posibles, incluyendo 'PayPalRESTException' y las excepciones
        // de Java requeridas ('MalformedURLException', etc.).
        try {
            // --- PASO 1: Preparar la información del Agreement ---

            // Obtener el plan de nuestra base de datos
            Plan plan = planService.getPlanByCode(request.getPlanCode());
            
            // Obtener el ID del plan de PayPal correspondiente al período de facturación
            String paypalPlanId = "MONTHLY".equals(request.getBillingPeriod().name()) 
                ? plan.getPaypalMonthlyPlanId() 
                : plan.getPaypalAnnualPlanId();
            
            if (paypalPlanId == null) {
                throw new PaymentProcessingException("PayPal plan ID not configured for: " + request.getPlanCode());
            }
            
            // --- PASO 2: Construir el objeto 'Agreement' ---

            Agreement agreement = new Agreement();
            agreement.setName(plan.getName() + " Subscription");
            agreement.setDescription("Subscription to " + plan.getName() + " plan");
            agreement.setStartDate(getStartDate());
            
            // Configurar el pagador (Payer)
            Payer payer = new Payer();
            payer.setPaymentMethod("paypal");
            agreement.setPayer(payer);
            
            // Asociar el plan de PayPal
            com.paypal.api.payments.Plan agreementPlan = new com.paypal.api.payments.Plan();
            agreementPlan.setId(paypalPlanId);
            agreement.setPlan(agreementPlan);
            
            // --- PASO 3: Crear el Agreement en PayPal ---
            
            // Esta es la llamada a la API de PayPal que puede lanzar las excepciones
            Agreement createdAgreement = agreement.create(apiContext);
            
            // --- PASO 4: Extraer la URL de aprobación de la respuesta ---

            // ✅ CORRECCIÓN: La URL de aprobación se extrae de la lista de 'links',
            // no de un método getApprovalUrl().
            String approvalUrl = "";
            for (Links links : createdAgreement.getLinks()) {
                if ("approval_url".equalsIgnoreCase(links.getRel())) {
                    approvalUrl = links.getHref();
                    break;
                }
            }

            // Validar que se obtuvo la URL
            if (approvalUrl.isEmpty()) {
                log.error("Failed to get approval URL from PayPal response for org: {}", request.getOrganizationId());
                throw new PaymentProcessingException("Could not get approval URL from PayPal");
            }
            
            String token = extractToken(approvalUrl);
            
            log.info("PayPal subscription created successfully for org: {}", request.getOrganizationId());
            
            // --- PASO 5: Devolver la respuesta al frontend ---
            
            return PayPalCheckoutResponse.builder()
                .approvalUrl(approvalUrl)
                .paypalSubscriptionId(createdAgreement.getId())
                .token(token)
                .build();
                
        } catch (Exception e) {
            // Este bloque 'catch' ahora maneja todos los errores
            log.error("Error creating PayPal subscription for org: {}", request.getOrganizationId(), e);
            throw new PaymentProcessingException("Failed to create PayPal subscription: " + e.getMessage(), e);
        }
    }
    
    /**
     * Ejecuta el agreement después de que user aprueba en PayPal.
     * 
     * FLOW:
     * 1. User es redirigido de PayPal con token
     * 2. Backend ejecuta el agreement
     * 3. Activa la subscription
     * 
     * LLAMADO POR: Frontend en CheckoutSuccessPage
     */
    public void executeAgreement(String token, Long organizationId) {
        log.info("Executing PayPal agreement for org: {}", organizationId);
        
        try {
            // Ejecutar agreement
            Agreement agreement = new Agreement();
            agreement.setToken(token);
            Agreement executedAgreement = agreement.execute(apiContext, agreement.getToken());
            
            // Obtener subscription de BD
            Subscription subscription = subscriptionRepository
                .findByOrganizationId(organizationId)
                .orElseThrow(() -> new PaymentProcessingException("Subscription not found"));
            
            // Activar subscription
            String planCode = subscription.getPlan().getCode();
            String paypalSubId = executedAgreement.getId();
            String paypalPayerId = executedAgreement.getPayer().getPayerInfo().getPayerId();
            String billingPeriod = subscription.getBillingPeriod().name();
            
            subscriptionService.activateSubscription(
                organizationId,
                planCode,
                paypalSubId,
                paypalPayerId,
                billingPeriod
            );
            
            log.info("PayPal agreement executed successfully for org: {}", organizationId);
            
        } catch (PayPalRESTException e) {
            log.error("Error executing PayPal agreement for org: {}", organizationId, e);
            throw new PaymentProcessingException("Failed to execute agreement: " + e.getMessage());
        }
    }
    
    /**
     * Cancela una suscripción en PayPal.
     */
    public void cancelPayPalSubscription(String paypalSubscriptionId) {
        log.info("Canceling PayPal subscription: {}", paypalSubscriptionId);
        
        try {
            Agreement agreement = Agreement.get(apiContext, paypalSubscriptionId);
            
            AgreementStateDescriptor stateDescriptor = new AgreementStateDescriptor();
            stateDescriptor.setNote("Subscription canceled by user");
            
            agreement.cancel(apiContext, stateDescriptor);
            
            log.info("PayPal subscription canceled: {}", paypalSubscriptionId);
            
        } catch (PayPalRESTException e) {
            log.error("Error canceling PayPal subscription: {}", paypalSubscriptionId, e);
            throw new PaymentProcessingException("Failed to cancel PayPal subscription: " + e.getMessage());
        }
    }
    
    /**
     * Procesa webhook de PayPal.
     * 
     * EVENTOS CRÍTICOS:
     * - BILLING.SUBSCRIPTION.ACTIVATED
     * - BILLING.SUBSCRIPTION.CANCELLED
     * - PAYMENT.SALE.COMPLETED
     * - PAYMENT.SALE.DENIED
     */
    public void processWebhook(Map<String, Object> payload, Map<String, String> headers) {
        log.info("Processing PayPal webhook");
        
        // Validar firma
        if (!webhookValidator.validateSignature(payload, headers)) {
            log.error("Invalid webhook signature");
            throw new PaymentProcessingException("Invalid webhook signature");
        }
        
        String eventType = (String) payload.get("event_type");
        
        log.info("Webhook event type: {}", eventType);
        
        switch (eventType) {
            case "BILLING.SUBSCRIPTION.ACTIVATED":
                handleSubscriptionActivated(payload);
                break;
                
            case "BILLING.SUBSCRIPTION.CANCELLED":
                handleSubscriptionCancelled(payload);
                break;
                
            case "PAYMENT.SALE.COMPLETED":
                handlePaymentCompleted(payload);
                break;
                
            case "PAYMENT.SALE.DENIED":
                handlePaymentDenied(payload);
                break;
                
            default:
                log.info("Unhandled webhook event type: {}", eventType);
        }
    }
    
    // ============================================
    // WEBHOOK HANDLERS
    // ============================================
    
    private void handleSubscriptionActivated(Map<String, Object> payload) {
        log.info("Handling BILLING.SUBSCRIPTION.ACTIVATED");
        
        Map<String, Object> resource = (Map<String, Object>) payload.get("resource");
        String paypalSubscriptionId = (String) resource.get("id");
        
        // La activación ya fue manejada en executeAgreement
        // Este webhook es confirmación adicional
        
        log.info("Subscription activated webhook confirmed: {}", paypalSubscriptionId);
    }
    
    private void handleSubscriptionCancelled(Map<String, Object> payload) {
        log.info("Handling BILLING.SUBSCRIPTION.CANCELLED");
        
        Map<String, Object> resource = (Map<String, Object>) payload.get("resource");
        String paypalSubscriptionId = (String) resource.get("id");
        
        // Buscar subscription y marcar como cancelada
        subscriptionRepository.findByPaypalSubscriptionId(paypalSubscriptionId)
            .ifPresent(subscription -> {
                subscription.cancel();
                subscriptionRepository.save(subscription);
                log.info("Subscription canceled via webhook: {}", subscription.getId());
            });
    }
    
    private void handlePaymentCompleted(Map<String, Object> payload) {
        log.info("Handling PAYMENT.SALE.COMPLETED");
        
        Map<String, Object> resource = (Map<String, Object>) payload.get("resource");
        String paypalSubscriptionId = (String) resource.get("billing_agreement_id");
        
        // Crear invoice record
        // TODO: Implementar creación de invoice
        
        log.info("Payment completed for subscription: {}", paypalSubscriptionId);
    }
    
    private void handlePaymentDenied(Map<String, Object> payload) {
        log.error("Handling PAYMENT.SALE.DENIED");
        
        Map<String, Object> resource = (Map<String, Object>) payload.get("resource");
        String paypalSubscriptionId = (String) resource.get("billing_agreement_id");
        
        // Marcar subscription como PAST_DUE
        subscriptionRepository.findByPaypalSubscriptionId(paypalSubscriptionId)
            .ifPresent(subscription -> {
                subscription.setStatus(com.eklesa.subscription.model.enums.SubscriptionStatus.PAST_DUE);
                subscriptionRepository.save(subscription);
                log.error("Subscription marked as PAST_DUE: {}", subscription.getId());
            });
    }
    
    // ============================================
    // HELPERS
    // ============================================
    
    private String getStartDate() {
        // PayPal requiere start date en el futuro (al menos 1 minuto)
        LocalDateTime startDate = LocalDateTime.now().plusMinutes(2);
        return startDate.format(DateTimeFormatter.ISO_DATE_TIME);
    }
    
    private String extractToken(String approvalUrl) {
        // Extraer token de URL: ?token=XXX
        String[] parts = approvalUrl.split("token=");
        return parts.length > 1 ? parts[1] : null;
    }
}
