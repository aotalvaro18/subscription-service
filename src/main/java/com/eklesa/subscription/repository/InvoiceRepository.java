package com.eklesa.subscription.repository;

import com.eklesa.subscription.model.Invoice;
import com.eklesa.subscription.model.enums.InvoiceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository para Invoice.
 */
@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    
    /**
     * Encuentra invoices de una subscription (paginado).
     */
    Page<Invoice> findBySubscriptionIdOrderByCreatedAtDesc(
        Long subscriptionId, 
        Pageable pageable
    );
    
    /**
     * Encuentra invoice por PayPal ID.
     */
    Optional<Invoice> findByPaypalInvoiceId(String paypalInvoiceId);
    
    /**
     * Encuentra invoices por status.
     */
    List<Invoice> findByStatus(InvoiceStatus status);
    
    /**
     * Encuentra invoices pendientes antes de una fecha.
     */
    List<Invoice> findByStatusAndDueDateBefore(
        InvoiceStatus status, 
        LocalDateTime date
    );
}
