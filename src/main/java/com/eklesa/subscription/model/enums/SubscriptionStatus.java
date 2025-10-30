package com.eklesa.subscription.model.enums;

/**
 * Estados posibles de una suscripción.
 */
public enum SubscriptionStatus {
    /**
     * Trial activo (21 días).
     */
    TRIALING,

    /**
     * Suscripción activa y pagada.
     */
    ACTIVE,

    /**
     * Trial expiró, en grace period (7 días read-only).
     */
    GRACE_PERIOD,

    /**
     * Suscripción suspendida (no puede acceder).
     */
    SUSPENDED,

    /**
     * Usuario canceló la suscripción.
     */
    CANCELED,

    /**
     * Pago fallido, esperando retry.
     */
    PAST_DUE,

    /**
     * Suscripción terminada permanentemente.
     */
    ENDED
}
