package com.darkrtp.teleport.cooldown;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class InMemoryCooldownService implements CooldownService {

    private static final long MILLIS_PER_SECOND = 1000L;

    private final Map<UUID, Long> expiryMillis = new ConcurrentHashMap<>();

    @Override
    public long remainingSeconds(UUID playerId) {
        Long expiry = expiryMillis.get(playerId);
        if (expiry == null) {
            return 0;
        }
        long remaining = expiry - System.currentTimeMillis();
        if (remaining <= 0) {
            expiryMillis.remove(playerId);
            return 0;
        }
        return (remaining + MILLIS_PER_SECOND - 1) / MILLIS_PER_SECOND;
    }

    @Override
    public void start(UUID playerId, int seconds) {
        if (seconds <= 0) {
            expiryMillis.remove(playerId);
            return;
        }
        expiryMillis.put(playerId, System.currentTimeMillis() + seconds * MILLIS_PER_SECOND);
    }

    @Override
    public void clear(UUID playerId) {
        expiryMillis.remove(playerId);
    }
}
