package com.eklesa.subscription.client;

import com.eklesa.subscription.client.dto.OrganizationDTO;
import com.eklesa.subscription.client.dto.UpdateSubscriptionStatusRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

/**
 * Feign Client para comunicación con auth-service.
 * 
 * <p>Este cliente permite al subscription-service mantener sincronizado el estado
 * de las suscripciones con el auth-service, que es la fuente de verdad para 
 * información de organizaciones y usuarios.</p>
 * 
 * <h3>Responsabilidades:</h3>
 * <ul>
 *   <li>Actualizar el campo subscriptionStatus en la tabla organizations</li>
 *   <li>Obtener información de organizaciones cuando sea necesario</li>
 *   <li>Mantener la sincronización entre servicios mediante REST calls</li>
 * </ul>
 * 
 * <h3>Configuración:</h3>
 * <p>La URL se resuelve desde la variable de entorno AUTH_SERVICE_URL:</p>
 * <ul>
 *   <li><b>Desarrollo local:</b> http://localhost:8081 (valor por defecto)</li>
 *   <li><b>AWS:</b> Se inyecta desde variables de entorno de App Runner</li>
 * </ul>
 * 
 * <h3>Seguridad:</h3>
 * <p>Usa FeignConfig que agrega el header X-API-Key para autenticación
 * entre servicios internos.</p>
 * 
 * @see FeignConfig
 * @see UpdateSubscriptionStatusRequest
 * @see OrganizationDTO
 * 
 * @author Eklesa Team
 * @version 1.0
 */
@FeignClient(
    name = "auth-service",
    // ✅ CORRECCIÓN: Usa AUTH_SERVICE_URL con valor por defecto para desarrollo local
    url = "${AUTH_SERVICE_URL:http://localhost:8081}",
    // ✅ CORRECCIÓN: Nombre correcto de la clase (sin "Client" al final)
    configuration = FeignConfig.class
)
public interface AuthServiceClient {
    
    /**
     * Actualiza el estado de suscripción de una organización en auth-service.
     * 
     * <p><b>CRÍTICO:</b> Este endpoint mantiene sincronizado el campo 
     * Organization.subscriptionStatus que es usado por todos los servicios
     * del ecosistema Eklesa para validar acceso y permisos.</p>
     * 
     * <h3>Flujo típico:</h3>
     * <pre>
     * 1. subscription-service cambia el status de una suscripción
     * 2. Llama a este método para sincronizar con auth-service
     * 3. auth-service actualiza Organization.subscriptionStatus
     * 4. Otros servicios (CRM, Turns) consultan este campo para validar acceso
     * </pre>
     * 
     * <h3>Ejemplo de uso:</h3>
     * <pre>
     * UpdateSubscriptionStatusRequest request = UpdateSubscriptionStatusRequest.builder()
     *     .organizationId(123L)
     *     .subscriptionStatus(SubscriptionStatus.ACTIVE)
     *     .build();
     * 
     * authServiceClient.updateSubscriptionStatus(request);
     * </pre>
     * 
     * @param request DTO con organizationId y nuevo subscriptionStatus
     * @throws FeignException si hay error en la comunicación con auth-service
     * @throws IllegalArgumentException si el request es inválido
     */
    @PutMapping("/api/organizations/subscription-status")
    void updateSubscriptionStatus(@RequestBody UpdateSubscriptionStatusRequest request);
    
    /**
     * Obtiene la información completa de una organización.
     * 
     * <p>Usado principalmente para validaciones o cuando necesitamos
     * información adicional de la organización (owner, tipo, etc.)</p>
     * 
     * <h3>Casos de uso:</h3>
     * <ul>
     *   <li>Validar que la organización existe antes de crear una suscripción</li>
     *   <li>Obtener email del owner para notificaciones</li>
     *   <li>Verificar el tipo de organización (CHURCH, BUSINESS, NONPROFIT)</li>
     * </ul>
     * 
     * <h3>Ejemplo de uso:</h3>
     * <pre>
     * OrganizationDTO org = authServiceClient.getOrganization(123L);
     * String ownerEmail = org.getOwnerEmail();
     * // Enviar email de bienvenida al trial...
     * </pre>
     * 
     * @param id ID de la organización a consultar
     * @return DTO con la información completa de la organización
     * @throws FeignException.NotFound si la organización no existe (404)
     * @throws FeignException si hay error en la comunicación con auth-service
     */
    @GetMapping("/api/organizations/{id}")
    OrganizationDTO getOrganization(@PathVariable("id") Long id);
}