package com.eklesa.subscription.dto.response;

import com.eklesa.subscription.model.enums.InvoiceStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO para Invoice.
 * 
 * USADO EN: BillingPage para historial de pagos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceDTO {
    
    private Long id;
    private String paypalInvoiceId;
    private String paypalTransactionId;
    
    private BigDecimal amount;
    private String currency;
    private InvoiceStatus status;
    
    private LocalDateTime periodStart;
    private LocalDateTime periodEnd;
    private LocalDateTime paidAt;
    private LocalDateTime dueDate;
    
    private String receiptUrl;
    private String notes;
    
    private LocalDateTime createdAt;
    
    // Formatted
    private String amountFormatted;
    private String statusDisplay;
}

