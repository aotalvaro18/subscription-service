package com.eklesa.subscription.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.auth.credentials.InstanceProfileCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ses.SesClient;

/**
 * Configuración de AWS SES para emails.
 * NOTA: Deshabilitado temporalmente hasta configurar SES en AWS.
 */
@Configuration
public class SesConfig {
    
    @Value("${cloud.aws.region.static:us-east-2}")  // ✅ Valor por defecto
    private String region;
    
    @Bean
    public SesClient sesClient() {
        return SesClient.builder()
            .region(Region.of(region))
            .build();
    }
}