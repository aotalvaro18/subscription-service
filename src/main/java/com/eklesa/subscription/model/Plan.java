package com.eklesa.subscription.model;

import com.eklesa.subscription.model.enums.PlanTier;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad que representa un plan de suscripción.
 * 
 * PLANES:
 * - STARTER (trial): $0
 * - PROFESSIONAL: COP $49.000/mes
 * - ENTERPRISE: COP $199.000/mes
 */
@Entity
@Table(name = "plans", indexes = {
    @Index(name = "idx_code", columnList = "code", unique = true),
    @Index(name = "idx_tier", columnList = "tier")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Plan extends BaseEntity {

    /**
     * Código único del plan.
     */
    @Column(nullable = false, unique = true, length = 50)
    private String code;

    /**
     * Nombre del plan (display).
     */
    @Column(nullable = false, length = 100)
    private String name;

    /**
     * Tier/nivel del plan.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PlanTier tier;

    /**
     * Descripción del plan.
     */
    @Column(columnDefinition = "TEXT")
    private String description;

    // ============================================
    // PRICING
    // ============================================

    @Column(name = "monthly_price", precision = 10, scale = 2)
    private BigDecimal monthlyPrice;

    @Column(name = "annual_price", precision = 10, scale = 2)
    private BigDecimal annualPrice;

    @Column(length = 3)
    private String currency = "COP";

    // ============================================
    // FEATURE LIMITS
    // ============================================

    /**
     * Límites de features.
     * null = ilimitado
     */
    @Column(name = "max_contacts")
    private Integer maxContacts;

    @Column(name = "max_users")
    private Integer maxUsers;

    @Column(name = "max_pipelines")
    private Integer maxPipelines;

    @Column(name = "max_deals")
    private Integer maxDeals;

    @Column(name = "max_storage_gb")
    private Integer maxStorageGb;

    // ============================================
    // PAYPAL INTEGRATION
    // ============================================

    /**
     * ID del plan en PayPal (mensual).
     */
    @Column(name = "paypal_monthly_plan_id", length = 100)
    private String paypalMonthlyPlanId;

    /**
     * ID del plan en PayPal (anual).
     */
    @Column(name = "paypal_annual_plan_id", length = 100)
    private String paypalAnnualPlanId;

    // ============================================
    // STATUS
    // ============================================

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    @Column(name = "is_featured")
    @Builder.Default
    private Boolean isFeatured = false;

    @Column(name = "sort_order")
    private Integer sortOrder;

    // ============================================
    // RELATIONSHIPS
    // ============================================

    @OneToMany(mappedBy = "plan", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PlanFeature> features = new ArrayList<>();

    // ============================================
    // BUSINESS METHODS
    // ============================================

    public boolean isStarter() {
        return tier == PlanTier.STARTER;
    }

    public boolean isProfessional() {
        return tier == PlanTier.PROFESSIONAL;
    }

    public boolean isEnterprise() {
        return tier == PlanTier.ENTERPRISE;
    }

    public boolean hasUnlimitedContacts() {
        return maxContacts == null;
    }

    public boolean hasUnlimitedUsers() {
        return maxUsers == null;
    }

    public BigDecimal getPrice(String billingPeriod) {
        return "ANNUAL".equals(billingPeriod) ? annualPrice : monthlyPrice;
    }
}
