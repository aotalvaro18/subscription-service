package com.eklesa.subscription.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Configuración de JPA.
 * 
 * IGUAL QUE CRM-SERVICE
 */
@Configuration
@EnableJpaRepositories(basePackages = "com.eklesa.subscription.repository")
@EnableJpaAuditing
@EnableTransactionManagement
public class JpaConfig {
}
