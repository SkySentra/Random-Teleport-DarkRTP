package com.darkrtp.teleport.location.safety;

import com.darkrtp.config.SafetySettings;
import org.bukkit.World;

public final class HeightBoundsValidator implements SafetyValidator {

    private static final int NETHER_ROOF_Y = 125;

    @Override
    public boolean isSafe(SpotCandidate candidate, SafetySettings settings) {
        int groundY = candidate.groundY();
        if (groundY + 1 < settings.minY()) {
            return false;
        }
        if (groundY >= candidate.world().getMaxHeight() - 2) {
            return false;
        }
        return !settings.avoidNetherRoof()
                || candidate.world().getEnvironment() != World.Environment.NETHER
                || groundY < NETHER_ROOF_Y;
    }
}
