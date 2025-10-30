package com.eklesa.subscription.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.eklesa.subscription.dto.error.ErrorResponse;
import com.eklesa.subscription.dto.error.FeatureLimitErrorResponse;
import com.eklesa.subscription.dto.error.ValidationErrorResponse;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Manejador global de excepciones.
 * 
 * SIGUIENDO PATRÓN DEL CRM: Respuestas consistentes
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    
    /**
     * Maneja errores de suscripción.
     */
    @ExceptionHandler(SubscriptionException.class)
    public ResponseEntity<ErrorResponse> handleSubscriptionException(SubscriptionException ex) {
        log.error("Subscription error: {}", ex.getMessage());
        
        ErrorResponse error = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.BAD_REQUEST.value())
            .error("Subscription Error")
            .message(ex.getMessage())
            .build();
        
        return ResponseEntity.badRequest().body(error);
    }
    
    /**
     * Maneja errores de límites excedidos.
     * 
     * CRÍTICO: Frontend detecta este error y muestra UpgradeModal
     */
    @ExceptionHandler(FeatureLimitExceededException.class)
    public ResponseEntity<FeatureLimitErrorResponse> handleFeatureLimitExceeded(FeatureLimitExceededException ex) {
        log.warn("Feature limit exceeded: {}", ex.getMessage());
        
        FeatureLimitErrorResponse error = FeatureLimitErrorResponse.featureLimitBuilder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.FORBIDDEN.value())
            .error("Feature Limit Exceeded")
            .message(ex.getMessage())
            .code("FEATURE_LIMIT_EXCEEDED") // Ahora este método sí existe
            .featureCode(ex.getFeatureCode())
            .currentUsage(ex.getCurrentUsage())
            .maxLimit(ex.getMaxLimit())
            .build();
        
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }
    
    /**
     * Maneja errores de procesamiento de pagos.
     */
    @ExceptionHandler(PaymentProcessingException.class)
    public ResponseEntity<ErrorResponse> handlePaymentProcessingException(PaymentProcessingException ex) {
        log.error("Payment processing error: {}", ex.getMessage());
        
        ErrorResponse error = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.PAYMENT_REQUIRED.value())
            .error("Payment Processing Error")
            .message(ex.getMessage())
            .build();
        
        return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED).body(error);
    }
    
    /**
     * Maneja errores de validación.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
        log.error("Validation error: {}", ex.getMessage());
        
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        ValidationErrorResponse error = ValidationErrorResponse.validationErrorBuilder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.BAD_REQUEST.value())
            .error("Validation Error")
            .message("Invalid request parameters")
            .fieldErrors(errors)
            .build();
        
        return ResponseEntity.badRequest().body(error);
    }
    
    /**
     * Maneja errores generales.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        log.error("Unexpected error", ex);
        
        ErrorResponse error = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
            .error("Internal Server Error")
            .message("An unexpected error occurred")
            .build();
        
        return ResponseEntity.internalServerError().body(error);
    }
}