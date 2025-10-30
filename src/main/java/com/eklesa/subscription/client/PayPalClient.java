package com.eklesa.subscription.client;

import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.OAuthTokenCredential;
import com.paypal.base.rest.PayPalRESTException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuración del cliente de PayPal.
 * 
 * DOCS: https://developer.paypal.com/docs/api/overview/
 */
@Configuration
@Slf4j
public class PayPalClient {
    
    @Value("${paypal.client-id}")
    private String clientId;
    
    @Value("${paypal.client-secret}")
    private String clientSecret;
    
    @Value("${paypal.mode}")
    private String mode; // sandbox o live
    
    /**
     * Crea el contexto de API de PayPal.
     */
    @Bean
    public APIContext apiContext() throws PayPalRESTException {
        log.info("Initializing PayPal API Context in mode: {}", mode);
        
        Map<String, String> config = new HashMap<>();
        config.put("mode", mode);
        
        OAuthTokenCredential credential = new OAuthTokenCredential(clientId, clientSecret, config);
        APIContext context = new APIContext(credential.getAccessToken());
        context.setConfigurationMap(config);
        
        log.info("PayPal API Context initialized successfully");
        
        return context;
    }
}
