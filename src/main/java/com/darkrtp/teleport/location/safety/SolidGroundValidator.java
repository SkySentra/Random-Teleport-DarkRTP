package com.darkrtp.teleport.location.safety;

import com.darkrtp.config.SafetySettings;
import org.bukkit.Material;

public final class SolidGroundValidator implements SafetyValidator {

    @Override
    public boolean isSafe(SpotCandidate candidate, SafetySettings settings) {
        Material type = candidate.ground().getType();
        if (type.isAir()) {
            return false;
        }
        if (candidate.ground().isLiquid()) {
            return type == Material.WATER && settings.allowWaterLanding();
        }
        return type.isSolid();
    }
}
