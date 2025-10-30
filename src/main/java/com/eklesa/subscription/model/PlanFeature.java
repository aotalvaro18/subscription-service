package com.eklesa.subscription.model;

import com.eklesa.subscription.model.enums.FeatureType;
import jakarta.persistence.*;
import lombok.*;

/**
 * Features incluidas en un plan.
 * 
 * Ejemplos:
 * - CRM_BASIC (MODULE)
 * - TURNS_SERVICE (MODULE)
 * - ADVANCED_REPORTS (CAPABILITY)
 * - API_ACCESS (CAPABILITY)
 */
@Entity
@Table(name = "plan_features", indexes = {
    @Index(name = "idx_plan_id", columnList = "plan_id"),
    @Index(name = "idx_feature_code", columnList = "feature_code")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlanFeature extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    private Plan plan;

    /**
     * Código único de la feature.
     */
    @Column(name = "feature_code", nullable = false, length = 100)
    private String featureCode;

    /**
     * Nombre de la feature (display).
     */
    @Column(name = "feature_name", nullable = false, length = 200)
    private String featureName;

    /**
     * Descripción de la feature.
     */
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * Tipo de feature.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private FeatureType type;

    /**
     * Si está habilitada en este plan.
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean enabled = true;

    /**
     * Valor de límite (si aplica).
     * Ejemplo: "5000" para maxContacts
     */
    @Column(name = "limit_value")
    private String limitValue;

    /**
     * Orden de presentación en UI.
     */
    @Column(name = "sort_order")
    private Integer sortOrder;
}
