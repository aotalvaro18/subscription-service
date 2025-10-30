package com.eklesa.subscription.controller;

import com.eklesa.subscription.dto.request.CreatePayPalSubscriptionRequest;
import com.eklesa.subscription.dto.response.InvoiceDTO;
import com.eklesa.subscription.dto.response.PayPalCheckoutResponse;
import com.eklesa.subscription.service.BillingService;
import com.eklesa.subscription.service.PayPalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Controller para facturación y pagos.
 * 
 * ENDPOINTS:
 * - POST /api/billing/paypal/create-subscription
 * - POST /api/billing/paypal/execute
 * - GET  /api/billing/invoices/{orgId}
 * 
 * SEGURIDAD: Requiere autenticación
 */
@RestController
@RequestMapping("/api/billing")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Billing", description = "Facturación y pagos")
public class BillingController {
    
    private final PayPalService paypalService;
    private final BillingService billingService;
    
    /**
     * Crea una suscripción en PayPal.
     * 
     * FLOW:
     * 1. Frontend llama este endpoint
     * 2. Backend crea subscription en PayPal
     * 3. Retorna approval URL
     * 4. Frontend redirige a PayPal
     * 5. User aprueba
     * 6. PayPal redirige a returnUrl
     * 7. Frontend llama /execute
     * 
     * LLAMADO POR: Frontend cuando user hace click en "Suscribirse"
     */
    @PostMapping("/paypal/create-subscription")
    @PreAuthorize("hasRole('OWNER')")
    @Operation(summary = "Crear suscripción PayPal", description = "Inicia el flujo de pago con PayPal")
    public ResponseEntity<PayPalCheckoutResponse> createPayPalSubscription(
        @Valid @RequestBody CreatePayPalSubscriptionRequest request
    ) {
        log.info("REST request to create PayPal subscription for org: {}", request.getOrganizationId());
        
        PayPalCheckoutResponse response = paypalService.createPayPalSubscription(request);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Ejecuta el agreement después de aprobación.
     * 
     * LLAMADO POR: Frontend en CheckoutSuccessPage
     */
    @PostMapping("/paypal/execute")
    @PreAuthorize("hasRole('OWNER')")
    @Operation(summary = "Ejecutar agreement PayPal", description = "Completa el pago después de la aprobación")
    public ResponseEntity<Void> executeAgreement(
        @RequestParam String token,
        @RequestParam Long organizationId
    ) {
        log.info("REST request to execute PayPal agreement for org: {}", organizationId);
        
        paypalService.executeAgreement(token, organizationId);
        
        return ResponseEntity.ok().build();
    }
    
    /**
     * Obtiene historial de facturas.
     * 
     * LLAMADO POR: Frontend en BillingPage
     */
    @GetMapping("/invoices/{organizationId}")
    @PreAuthorize("hasRole('OWNER') or hasRole('ADMIN')")
    @Operation(summary = "Historial de facturas", description = "Obtiene las facturas de una organización")
    public ResponseEntity<Page<InvoiceDTO>> getInvoices(
        @PathVariable Long organizationId,
        Pageable pageable
    ) {
        log.info("REST request to get invoices for org: {}", organizationId);
        
        Page<InvoiceDTO> invoices = billingService.getInvoiceHistory(organizationId, pageable);
        
        return ResponseEntity.ok(invoices);
    }
}
