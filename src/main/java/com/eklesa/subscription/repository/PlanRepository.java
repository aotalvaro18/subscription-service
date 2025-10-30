package com.eklesa.subscription.repository;

import com.eklesa.subscription.model.Plan;
import com.eklesa.subscription.model.enums.PlanTier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository para Plan.
 */
@Repository
public interface PlanRepository extends JpaRepository<Plan, Long> {
    
    /**
     * Encuentra plan por código.
     * 
     * EJEMPLO: findByCode("PROFESSIONAL")
     */
    Optional<Plan> findByCode(String code);
    
    /**
     * Encuentra plan por tier.
     */
    Optional<Plan> findByTier(PlanTier tier);
    
    /**
     * Encuentra planes activos ordenados.
     */
    List<Plan> findByActiveTrueOrderBySortOrder();
    
    /**
     * Encuentra plan destacado.
     */
    Optional<Plan> findByIsFeaturedTrue();
}
