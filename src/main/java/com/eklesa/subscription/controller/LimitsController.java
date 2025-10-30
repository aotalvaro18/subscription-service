package com.eklesa.subscription.controller;

import com.eklesa.subscription.dto.request.ValidateFeatureLimitRequest;
import com.eklesa.subscription.dto.response.FeatureLimitValidationResponse;
import com.eklesa.subscription.dto.response.UsageLimitsDTO;
import com.eklesa.subscription.service.FeatureLimitService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller para validación de límites de features.
 * 
 * ENDPOINTS:
 * - POST /api/limits/validate
 * - GET  /api/limits/{orgId}
 * 
 * LLAMADO POR: crm-service antes de crear recursos
 */
@RestController
@RequestMapping("/api/limits")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Limits", description = "Validación de límites de features")
public class LimitsController {
    
    private final FeatureLimitService featureLimitService;
    
    /**
     * Valida si puede crear un recurso.
     * 
     * CRÍTICO: Este endpoint es llamado por crm-service
     * antes de crear contactos, deals, etc.
     * 
     * EJEMPLO:
     * POST /api/limits/validate
     * {
     *   "organizationId": 123,
     *   "featureCode": "CONTACTS",
     *   "currentCount": 450,
     *   "incrementBy": 1
     * }
     * 
     * RESPONSE:
     * {
     *   "allowed": true,
     *   "currentUsage": 450,
     *   "maxLimit": 500,
     *   "remaining": 50,
     *   "usagePercentage": 90.0
     * }
     */
    @PostMapping("/validate")
    @Operation(summary = "Validar límite", description = "Valida si puede usar una feature según el plan")
    public ResponseEntity<FeatureLimitValidationResponse> validateFeatureLimit(
        @Valid @RequestBody ValidateFeatureLimitRequest request
    ) {
        log.debug("REST request to validate feature limit for org: {}, feature: {}", 
            request.getOrganizationId(), request.getFeatureCode());
        
        FeatureLimitValidationResponse response = featureLimitService.validateFeatureLimit(request);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Obtiene los límites actuales de una organización.
     * 
     * LLAMADO POR: Frontend para mostrar usage bars
     */
    @GetMapping("/{organizationId}")
    @Operation(summary = "Obtener límites", description = "Obtiene los límites y uso actual de todas las features")
    public ResponseEntity<UsageLimitsDTO> getCurrentLimits(
        @PathVariable Long organizationId
    ) {
        log.debug("REST request to get current limits for org: {}", organizationId);
        
        UsageLimitsDTO limits = featureLimitService.getCurrentLimits(organizationId);
        
        return ResponseEntity.ok(limits);
    }
}
