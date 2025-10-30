package com.eklesa.subscription.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Registro de uso de features para tracking.
 * 
 * Se usa para analytics y alertas de límites.
 */
@Entity
@Table(name = "usage_records", indexes = {
    @Index(name = "idx_subscription_id", columnList = "subscription_id"),
    @Index(name = "idx_feature_code", columnList = "feature_code"),
    @Index(name = "idx_recorded_at", columnList = "recorded_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsageRecord extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_id", nullable = false)
    private Subscription subscription;

    /**
     * Código de la feature que se está usando.
     * Ej: "CONTACTS", "USERS", "DEALS"
     */
    @Column(name = "feature_code", nullable = false, length = 50)
    private String featureCode;

    /**
     * Cantidad usada.
     */
    @Column(name = "usage_count", nullable = false)
    private Integer usageCount;

    /**
     * Límite del plan.
     */
    @Column(name = "plan_limit")
    private Integer planLimit;

    /**
     * Porcentaje de uso.
     */
    @Column(name = "usage_percentage", precision = 5, scale = 2)
    private BigDecimal usagePercentage;

    /**
     * Fecha de registro.
     */
    @Column(name = "recorded_at", nullable = false)
    private LocalDateTime recordedAt;

    /**
     * Si se alcanzó el límite.
     */
    @Column(name = "limit_exceeded", nullable = false)
    @Builder.Default
    private Boolean limitExceeded = false;
}
