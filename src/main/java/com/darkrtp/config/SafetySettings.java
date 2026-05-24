package com.darkrtp.config;

import org.bukkit.Material;

import java.util.Set;

public record SafetySettings(
        Set<Material> unsafeBlocks,
        boolean allowWaterLanding,
        boolean avoidNetherRoof,
        int minY
) {

    public SafetySettings {
        unsafeBlocks = Set.copyOf(unsafeBlocks);
    }
}
