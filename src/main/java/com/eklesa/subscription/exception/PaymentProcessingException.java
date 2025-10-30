package com.eklesa.subscription.exception;

/**
 * Excepción para errores de procesamiento de pagos.
 */
public class PaymentProcessingException extends RuntimeException {
    
    public PaymentProcessingException(String message) {
        super(message);
    }
    
    public PaymentProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
