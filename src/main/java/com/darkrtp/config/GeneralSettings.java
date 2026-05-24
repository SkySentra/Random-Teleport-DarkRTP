package com.darkrtp.config;

public record GeneralSettings(
        int maxAttempts,
        int warmupSeconds,
        boolean cancelOnMove,
        boolean cancelOnDamage,
        int cooldownSeconds,
        boolean soundEffects,
        boolean particleEffects
) {
}
