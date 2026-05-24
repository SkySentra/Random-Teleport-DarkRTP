package com.darkrtp.teleport.location.safety;

import com.darkrtp.config.SafetySettings;

import java.util.Set;
import org.bukkit.Material;

public final class UnsafeBlockValidator implements SafetyValidator {

    @Override
    public boolean isSafe(SpotCandidate candidate, SafetySettings settings) {
        Set<Material> unsafe = settings.unsafeBlocks();
        return !unsafe.contains(candidate.ground().getType())
                && !unsafe.contains(candidate.feet().getType())
                && !unsafe.contains(candidate.head().getType());
    }
}
