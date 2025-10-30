package com.eklesa.subscription.exception;

/**
 * Excepción cuando se excede el límite de una feature.
 * 
 * USADO PARA: Trigger upgrade modal en frontend
 */
public class FeatureLimitExceededException extends RuntimeException {
    
    private final String featureCode;
    private final Integer currentUsage;
    private final Integer maxLimit;
    
    public FeatureLimitExceededException(String message) {
        super(message);
        this.featureCode = null;
        this.currentUsage = null;
        this.maxLimit = null;
    }
    
    public FeatureLimitExceededException(String message, String featureCode, Integer currentUsage, Integer maxLimit) {
        super(message);
        this.featureCode = featureCode;
        this.currentUsage = currentUsage;
        this.maxLimit = maxLimit;
    }
    
    public String getFeatureCode() {
        return featureCode;
    }
    
    public Integer getCurrentUsage() {
        return currentUsage;
    }
    
    public Integer getMaxLimit() {
        return maxLimit;
    }
}
