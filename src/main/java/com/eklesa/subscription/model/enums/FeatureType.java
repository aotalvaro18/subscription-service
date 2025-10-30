package com.eklesa.subscription.model.enums;

/**
 * Tipos de features disponibles.
 */
public enum FeatureType {
    /**
     * Módulo completo (CRM, Turns, etc.).
     */
    MODULE,

    /**
     * Capacidad específica (API access, white-label).
     */
    CAPABILITY,

    /**
     * Límite numérico (max contacts, max users).
     */
    LIMIT,

    /**
     * Nivel de soporte.
     */
    SUPPORT
}
