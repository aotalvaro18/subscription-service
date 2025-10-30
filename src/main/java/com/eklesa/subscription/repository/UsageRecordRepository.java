package com.eklesa.subscription.repository;

import com.eklesa.subscription.model.UsageRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository para UsageRecord.
 */
@Repository
public interface UsageRecordRepository extends JpaRepository<UsageRecord, Long> {
    
    /**
     * Encuentra último registro de uso de una feature.
     */
    Optional<UsageRecord> findTopBySubscriptionIdAndFeatureCodeOrderByRecordedAtDesc(
        Long subscriptionId, 
        String featureCode
    );
    
    /**
     * Encuentra registros de una subscription.
     */
    List<UsageRecord> findBySubscriptionIdOrderByRecordedAtDesc(Long subscriptionId);
    
    /**
     * Encuentra registros que excedieron límite.
     */
    List<UsageRecord> findByLimitExceededTrueAndRecordedAtAfter(LocalDateTime date);
    
    /**
     * Obtiene uso actual de una feature.
     */
    @Query("SELECT ur.usageCount FROM UsageRecord ur " +
           "WHERE ur.subscription.id = :subscriptionId " +
           "AND ur.featureCode = :featureCode " +
           "ORDER BY ur.recordedAt DESC " +
           "LIMIT 1")
    Optional<Integer> getCurrentUsage(
        @Param("subscriptionId") Long subscriptionId, 
        @Param("featureCode") String featureCode
    );
}
