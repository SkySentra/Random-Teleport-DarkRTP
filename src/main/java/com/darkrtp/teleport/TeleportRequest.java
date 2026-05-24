package com.darkrtp.teleport;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public record TeleportRequest(
        Player target,
        CommandSender initiator,
        boolean bypassCooldown,
        boolean bypassWarmup
) {

    public boolean selfRequest() {
        return initiator.equals(target);
    }
}
