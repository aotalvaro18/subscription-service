package com.eklesa.subscription.model.enums;

/**
 * Tiers de planes.
 */
public enum PlanTier {
    STARTER(0),
    PROFESSIONAL(1),
    ENTERPRISE(2);

    private final int level;

    PlanTier(int level) {
        this.level = level;
    }

    public int getLevel() {
        return level;
    }

    public boolean isHigherThan(PlanTier other) {
        return this.level > other.level;
    }
}
