package com.eklesa.subscription.service;

import com.eklesa.subscription.dto.response.InvoiceDTO;
import com.eklesa.subscription.model.Invoice;
import com.eklesa.subscription.model.Subscription;
import com.eklesa.subscription.model.enums.InvoiceStatus;
import com.eklesa.subscription.repository.InvoiceRepository;
import com.eklesa.subscription.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.util.Locale;

/**
 * Servicio para gestión de facturación.
 * 
 * RESPONSABILIDADES:
 * - Crear invoices
 * - Historial de pagos
 * - Generar receipts
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BillingService {
    
    private final InvoiceRepository invoiceRepository;
    private final SubscriptionRepository subscriptionRepository;
    
    /**
     * Obtiene historial de invoices de una organización.
     * 
     * USADO EN: BillingPage del frontend
     */
    @Transactional(readOnly = true)
    public Page<InvoiceDTO> getInvoiceHistory(Long organizationId, Pageable pageable) {
        log.debug("Fetching invoice history for org: {}", organizationId);
        
        Subscription subscription = subscriptionRepository
            .findByOrganizationId(organizationId)
            .orElseThrow(() -> new RuntimeException("Subscription not found"));
        
        Page<Invoice> invoices = invoiceRepository
            .findBySubscriptionIdOrderByCreatedAtDesc(subscription.getId(), pageable);
        
        return invoices.map(this::mapToDTO);
    }
    
    /**
     * Crea un invoice después de un pago exitoso.
     * 
     * LLAMADO POR: PayPalService después de webhook
     */
    @Transactional
    public InvoiceDTO createInvoice(
        Long subscriptionId,
        String paypalInvoiceId,
        String paypalTransactionId,
        BigDecimal amount,
        String currency,
        LocalDateTime periodStart,
        LocalDateTime periodEnd
    ) {
        log.info("Creating invoice for subscription: {}", subscriptionId);
        
        Subscription subscription = subscriptionRepository
            .findById(subscriptionId)
            .orElseThrow(() -> new RuntimeException("Subscription not found"));
        
        Invoice invoice = Invoice.builder()
            .subscription(subscription)
            .paypalInvoiceId(paypalInvoiceId)
            .paypalTransactionId(paypalTransactionId)
            .amount(amount)
            .currency(currency)
            .status(InvoiceStatus.PAID)
            .periodStart(periodStart)
            .periodEnd(periodEnd)
            .paidAt(LocalDateTime.now())
            .build();
        
        invoice = invoiceRepository.save(invoice);
        
        log.info("Invoice created: {}", invoice.getId());
        
        return mapToDTO(invoice);
    }
    
    /**
     * Marca invoice como fallido.
     */
    @Transactional
    public void markInvoiceAsFailed(Long invoiceId, String reason) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
            .orElseThrow(() -> new RuntimeException("Invoice not found"));
        
        invoice.setStatus(InvoiceStatus.FAILED);
        invoice.setNotes(reason);
        
        invoiceRepository.save(invoice);
        
        log.error("Invoice marked as failed: {}", invoiceId);
    }
    
    // ============================================
    // MAPPERS
    // ============================================
    
    private InvoiceDTO mapToDTO(Invoice invoice) {
        return InvoiceDTO.builder()
            .id(invoice.getId())
            .paypalInvoiceId(invoice.getPaypalInvoiceId())
            .paypalTransactionId(invoice.getPaypalTransactionId())
            .amount(invoice.getAmount())
            .currency(invoice.getCurrency())
            .status(invoice.getStatus())
            .periodStart(invoice.getPeriodStart())
            .periodEnd(invoice.getPeriodEnd())
            .paidAt(invoice.getPaidAt())
            .dueDate(invoice.getDueDate())
            .receiptUrl(invoice.getReceiptUrl())
            .notes(invoice.getNotes())
            .createdAt(invoice.getCreatedAt())
            .amountFormatted(formatAmount(invoice.getAmount(), invoice.getCurrency()))
            .statusDisplay(getStatusDisplay(invoice.getStatus()))
            .build();
    }
    
    private String formatAmount(BigDecimal amount, String currency) {
        Locale locale = "COP".equals(currency) ? new Locale("es", "CO") : Locale.US;
        NumberFormat formatter = NumberFormat.getCurrencyInstance(locale);
        return formatter.format(amount);
    }
    
    private String getStatusDisplay(InvoiceStatus status) {
        switch (status) {
            case PAID: return "Pagado";
            case PENDING: return "Pendiente";
            case FAILED: return "Fallido";
            case REFUNDED: return "Reembolsado";
            case VOID: return "Anulado";
            default: return status.name();
        }
    }
}
