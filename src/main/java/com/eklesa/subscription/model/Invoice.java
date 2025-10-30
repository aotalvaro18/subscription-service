package com.eklesa.subscription.model;

import com.eklesa.subscription.model.enums.InvoiceStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Registro de cada pago/intento de pago.
 */
@Entity
@Table(name = "invoices", indexes = {
    @Index(name = "idx_subscription_id", columnList = "subscription_id"),
    @Index(name = "idx_paypal_invoice_id", columnList = "paypal_invoice_id"),
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "idx_created_at", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Invoice extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_id", nullable = false)
    private Subscription subscription;

    /**
     * ID de la factura en PayPal.
     */
    @Column(name = "paypal_invoice_id", length = 100)
    private String paypalInvoiceId;

    /**
     * ID de la transacción en PayPal.
     */
    @Column(name = "paypal_transaction_id", length = 100)
    private String paypalTransactionId;

    /**
     * Monto facturado.
     */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency;

    /**
     * Estado de la factura.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private InvoiceStatus status;

    /**
     * Período facturado.
     */
    @Column(name = "period_start")
    private LocalDateTime periodStart;

    @Column(name = "period_end")
    private LocalDateTime periodEnd;

    /**
     * Fecha de pago.
     */
    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    /**
     * Fecha de vencimiento.
     */
    @Column(name = "due_date")
    private LocalDateTime dueDate;

    /**
     * URL del recibo en PayPal.
     */
    @Column(name = "receipt_url", length = 500)
    private String receiptUrl;

    /**
     * Nota adicional.
     */
    @Column(columnDefinition = "TEXT")
    private String notes;
}
