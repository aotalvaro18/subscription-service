package com.eklesa.subscription.exception;

/**
 * Excepción general para errores de suscripción.
 */
public class SubscriptionException extends RuntimeException {
    
    public SubscriptionException(String message) {
        super(message);
    }
    
    public SubscriptionException(String message, Throwable cause) {
        super(message, cause);
    }
}

