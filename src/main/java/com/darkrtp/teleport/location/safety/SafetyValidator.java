package com.darkrtp.teleport.location.safety;

import com.darkrtp.config.SafetySettings;

public interface SafetyValidator {

    boolean isSafe(SpotCandidate candidate, SafetySettings settings);
}
