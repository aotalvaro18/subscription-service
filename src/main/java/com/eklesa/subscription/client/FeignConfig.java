package com.eklesa.subscription.client;

import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de Feign para agregar API key.
 */
@Configuration
public class FeignConfig {
    
    @Value("${subscription-service.api-key:local-dev-key}")
    private String apiKey;
    
    @Bean
    public RequestInterceptor requestInterceptor() {
        return template -> {
            template.header("X-API-Key", apiKey);
        };
    }
}