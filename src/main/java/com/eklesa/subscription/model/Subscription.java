package com.eklesa.subscription.model;

import com.eklesa.subscription.model.enums.BillingPeriod;
import com.eklesa.subscription.model.enums.SubscriptionStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad que representa una suscripción de una organización.
 * 
 * REGLAS DE NEGOCIO:
 * - Una organización solo puede tener UNA subscription activa
 * - Trial dura exactamente 21 días
 * - Grace period de 7 días después del trial
 * - PayPal fields son null durante trial
 */
@Entity
@Table(name = "subscriptions", indexes = {
    @Index(name = "idx_organization_id", columnList = "organization_id"),
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "idx_paypal_subscription_id", columnList = "paypal_subscription_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Subscription extends BaseEntity {

    /**
     * Organization ID de auth-service.
     * NO es FK en BD, solo referencia lógica.
     */
    @Column(name = "organization_id", nullable = false, unique = true)
    private Long organizationId;

    /**
     * Plan contratado.
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "plan_id", nullable = false)
    private Plan plan;

    /**
     * Estado actual de la suscripción.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private SubscriptionStatus status;

    /**
     * Período de facturación (mensual o anual).
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "billing_period", length = 20)
    private BillingPeriod billingPeriod;

    // ============================================
    // TRIAL FIELDS
    // ============================================

    @Column(name = "trial_start_date")
    private LocalDateTime trialStartDate;

    @Column(name = "trial_end_date")
    private LocalDateTime trialEndDate;

    @Column(name = "is_trial_used", nullable = false)
    private Boolean isTrialUsed = false;

    // ============================================
    // PAYPAL INTEGRATION FIELDS
    // ============================================

    /**
     * ID del cliente en PayPal.
     * Null durante trial.
     */
    @Column(name = "paypal_payer_id", length = 100)
    private String paypalPayerId;

    /**
     * ID de la suscripción en PayPal.
     * Null durante trial.
     */
    @Column(name = "paypal_subscription_id", length = 100)
    private String paypalSubscriptionId;

    /**
     * ID del agreement en PayPal.
     * Se usa para gestionar la suscripción.
     */
    @Column(name = "paypal_agreement_id", length = 100)
    private String paypalAgreementId;

    /**
     * Email de PayPal del usuario.
     */
    @Column(name = "paypal_email", length = 255)
    private String paypalEmail;

    // ============================================
    // BILLING DATES
    // ============================================

    @Column(name = "current_period_start")
    private LocalDateTime currentPeriodStart;

    @Column(name = "current_period_end")
    private LocalDateTime currentPeriodEnd;

    @Column(name = "next_billing_date")
    private LocalDateTime nextBillingDate;

    @Column(name = "canceled_at")
    private LocalDateTime canceledAt;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    // ============================================
    // PRICING
    // ============================================

    @Column(name = "amount", precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "currency", length = 3)
    private String currency = "COP"; // Default: Colombian Peso

    // ============================================
    // RELATIONSHIPS
    // ============================================

    @OneToMany(mappedBy = "subscription", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Invoice> invoices = new ArrayList<>();

    @OneToMany(mappedBy = "subscription", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<UsageRecord> usageRecords = new ArrayList<>();

    // ============================================
    // BUSINESS METHODS
    // ============================================

    /**
     * Verifica si la suscripción está activa.
     */
    public boolean isActive() {
        return status == SubscriptionStatus.ACTIVE;
    }

    /**
     * Verifica si está en trial.
     */
    public boolean isTrialing() {
        return status == SubscriptionStatus.TRIALING;
    }

    /**
     * Verifica si está en grace period.
     */
    public boolean isInGracePeriod() {
        return status == SubscriptionStatus.GRACE_PERIOD;
    }

    /**
     * Verifica si puede acceder al sistema (no suspendido).
     */
    public boolean canAccess() {
        return status == SubscriptionStatus.ACTIVE 
            || status == SubscriptionStatus.TRIALING 
            || status == SubscriptionStatus.GRACE_PERIOD;
    }

    /**
     * Verifica si tiene acceso read-only.
     */
    public boolean isReadOnly() {
        return status == SubscriptionStatus.GRACE_PERIOD;
    }

    /**
     * Calcula días restantes del trial.
     */
    public long getDaysLeftInTrial() {
        if (!isTrialing() || trialEndDate == null) {
            return 0;
        }
        return java.time.Duration.between(LocalDateTime.now(), trialEndDate).toDays();
    }

    /**
     * Inicia el trial.
     */
    public void startTrial(Plan starterPlan) {
        this.plan = starterPlan;
        this.status = SubscriptionStatus.TRIALING;
        this.trialStartDate = LocalDateTime.now();
        this.trialEndDate = trialStartDate.plusDays(21);
        this.isTrialUsed = true;
        this.billingPeriod = BillingPeriod.MONTHLY;
    }

    /**
     * Activa la suscripción después del pago.
     */
    public void activate(Plan newPlan, BillingPeriod period, String paypalSubId, String paypalPayerId) {
        this.plan = newPlan;
        this.status = SubscriptionStatus.ACTIVE;
        this.billingPeriod = period;
        this.paypalSubscriptionId = paypalSubId;
        this.paypalPayerId = paypalPayerId;
        this.currentPeriodStart = LocalDateTime.now();
        this.currentPeriodEnd = period == BillingPeriod.ANNUAL 
            ? currentPeriodStart.plusYears(1)
            : currentPeriodStart.plusMonths(1);
        this.nextBillingDate = currentPeriodEnd;
    }

    /**
     * Marca como expirado y entra en grace period.
     */
    public void enterGracePeriod() {
        this.status = SubscriptionStatus.GRACE_PERIOD;
    }

    /**
     * Suspende la suscripción.
     */
    public void suspend() {
        this.status = SubscriptionStatus.SUSPENDED;
    }

    /**
     * Cancela la suscripción.
     */
    public void cancel() {
        this.status = SubscriptionStatus.CANCELED;
        this.canceledAt = LocalDateTime.now();
    }
}
