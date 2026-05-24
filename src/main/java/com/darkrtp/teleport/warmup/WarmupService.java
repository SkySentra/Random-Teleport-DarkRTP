package com.darkrtp.teleport.warmup;

import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.function.Consumer;

public interface WarmupService {

    enum CancelReason {
        MOVED,
        DAMAGED,
        QUIT
    }

    boolean isPending(UUID playerId);

    void begin(Player player, int seconds, Runnable onComplete, Consumer<CancelReason> onCancel);

    void cancel(UUID playerId);

    void cancelAll();
}
