package com.darkrtp.teleport.cooldown;

import java.util.UUID;

public interface CooldownService {

    long remainingSeconds(UUID playerId);

    void start(UUID playerId, int seconds);

    void clear(UUID playerId);
}
