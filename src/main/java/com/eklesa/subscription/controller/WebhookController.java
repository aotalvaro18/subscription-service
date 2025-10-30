package com.eklesa.subscription.controller;

import com.eklesa.subscription.service.PayPalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controller para recibir webhooks de PayPal.
 * 
 * ENDPOINT:
 * - POST /webhooks/paypal
 * 
 * SEGURIDAD:
 * - Valida signature de PayPal
 * - NO requiere autenticación (viene de PayPal)
 * 
 * EVENTOS MANEJADOS:
 * - BILLING.SUBSCRIPTION.ACTIVATED
 * - BILLING.SUBSCRIPTION.CANCELLED
 * - PAYMENT.SALE.COMPLETED
 * - PAYMENT.SALE.DENIED
 */
@RestController
@RequestMapping("/webhooks")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Webhooks", description = "Recepción de eventos de PayPal")
public class WebhookController {
    
    private final PayPalService paypalService;
    
    /**
     * Recibe webhooks de PayPal.
     * 
     * CONFIGURACIÓN EN PAYPAL:
     * - URL: https://subscription-service.app-runner.aws.com/webhooks/paypal
     * - Eventos suscritos:
     *   * BILLING.SUBSCRIPTION.ACTIVATED
     *   * BILLING.SUBSCRIPTION.CANCELLED
     *   * PAYMENT.SALE.COMPLETED
     *   * PAYMENT.SALE.DENIED
     * 
     * IMPORTANTE:
     * - PayPal firma los webhooks con headers
     * - Debemos validar la firma antes de procesar
     * - Retornar 200 OK inmediatamente (procesar async)
     */
    @PostMapping("/paypal")
    @Operation(summary = "Webhook PayPal", description = "Recibe eventos de PayPal")
    public ResponseEntity<Void> handlePayPalWebhook(
        @RequestBody Map<String, Object> payload,
        @RequestHeader Map<String, String> headers
    ) {
        log.info("Received PayPal webhook: {}", payload.get("event_type"));
        
        try {
            // Validar y procesar webhook
            paypalService.processWebhook(payload, headers);
            
            // Retornar OK inmediatamente
            return ResponseEntity.ok().build();
            
        } catch (Exception e) {
            log.error("Error processing PayPal webhook", e);
            
            // Retornar 500 para que PayPal reintente
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Health check para webhooks.
     * 
     * PayPal verifica que el endpoint esté disponible.
     */
    @GetMapping("/paypal/health")
    @Operation(summary = "Health check", description = "Verifica que el endpoint de webhooks esté activo")
    public ResponseEntity<Map<String, String>> webhookHealth() {
        return ResponseEntity.ok(Map.of(
            "status", "healthy",
            "service", "subscription-service-webhooks"
        ));
    }
}
