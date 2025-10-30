package com.eklesa.subscription.repository;

import com.eklesa.subscription.model.PlanFeature;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository para PlanFeature.
 */
@Repository
public interface PlanFeatureRepository extends JpaRepository<PlanFeature, Long> {
    
    /**
     * Encuentra features de un plan.
     */
    List<PlanFeature> findByPlanIdOrderBySortOrder(Long planId);
    
    /**
     * Encuentra features habilitadas de un plan.
     */
    List<PlanFeature> findByPlanIdAndEnabledTrueOrderBySortOrder(Long planId);
}
