package com.eklesa.subscription.repository;

import com.eklesa.subscription.model.Subscription;
import com.eklesa.subscription.model.enums.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository para Subscription.
 * 
 * PATRÓN: Igual que en CRM (ContactRepository, CompanyRepository)
 */
@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    
    /**
     * Encuentra subscription por organization ID.
     * 
     * CRÍTICO: Una org solo tiene UNA subscription activa.
     */
    Optional<Subscription> findByOrganizationId(Long organizationId);
    
    /**
     * Encuentra subscriptions por status.
     */
    List<Subscription> findByStatus(SubscriptionStatus status);
    
    /**
     * Encuentra trials que expiran antes de una fecha.
     * 
     * USADO POR: Scheduled job para enviar notificaciones.
     */
    @Query("SELECT s FROM Subscription s WHERE s.status = 'TRIALING' AND s.trialEndDate < :date")
    List<Subscription> findTrialsExpiringBefore(@Param("date") LocalDateTime date);
    
    /**
     * Encuentra subscriptions en grace period.
     */
    List<Subscription> findByStatusAndUpdatedAtBefore(
        SubscriptionStatus status, 
        LocalDateTime date
    );
    
    /**
     * Cuenta subscriptions activas.
     */
    Long countByStatus(SubscriptionStatus status);
    
    /**
     * Encuentra por PayPal subscription ID.
     */
    Optional<Subscription> findByPaypalSubscriptionId(String paypalSubscriptionId);
    
    /**
     * Verifica si una org ya usó el trial.
     */
    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END " +
           "FROM Subscription s WHERE s.organizationId = :orgId AND s.isTrialUsed = true")
    Boolean hasUsedTrial(@Param("orgId") Long organizationId);
}
