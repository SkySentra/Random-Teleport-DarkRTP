package com.darkrtp.teleport.location.safety;

import com.darkrtp.config.SafetySettings;
import org.bukkit.Material;
import org.bukkit.block.Block;

public final class OpenSpaceValidator implements SafetyValidator {

    @Override
    public boolean isSafe(SpotCandidate candidate, SafetySettings settings) {
        return isClear(candidate.feet(), settings) && isClear(candidate.head(), settings);
    }

    private boolean isClear(Block block, SafetySettings settings) {
        Material type = block.getType();
        if (type.isAir()) {
            return true;
        }
        if (block.isLiquid()) {
            return type == Material.WATER && settings.allowWaterLanding();
        }
        return block.isPassable();
    }
}
