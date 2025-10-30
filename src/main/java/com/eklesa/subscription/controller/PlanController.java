package com.eklesa.subscription.controller;

import com.eklesa.subscription.dto.response.PlanDTO;
import com.eklesa.subscription.service.PlanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller para gestión de planes.
 * 
 * ENDPOINTS:
 * - GET /api/plans
 * - GET /api/plans/{code}
 * - GET /api/plans/featured
 * 
 * PÚBLICO: No requiere autenticación (para landing page)
 */
@RestController
@RequestMapping("/api/plans")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Plans", description = "Consulta de planes disponibles")
public class PlanController {
    
    private final PlanService planService;
    
    /**
     * Obtiene todos los planes activos.
     * 
     * LLAMADO POR: Frontend en PricingPage
     * 
     * PÚBLICO: Cualquiera puede ver los planes
     */
    @GetMapping
    @Operation(summary = "Listar planes", description = "Obtiene todos los planes de suscripción disponibles")
    public ResponseEntity<List<PlanDTO>> getAllPlans() {
        log.info("REST request to get all plans");
        
        List<PlanDTO> plans = planService.getAllActivePlans();
        
        return ResponseEntity.ok(plans);
    }
    
    /**
     * Obtiene plan destacado.
     * 
     * USADO EN: Landing page para mostrar plan recomendado
     */
    @GetMapping("/featured")
    @Operation(summary = "Plan destacado", description = "Obtiene el plan recomendado/destacado")
    public ResponseEntity<PlanDTO> getFeaturedPlan() {
        log.info("REST request to get featured plan");
        
        PlanDTO plan = planService.getFeaturedPlan();
        
        return ResponseEntity.ok(plan);
    }
}
