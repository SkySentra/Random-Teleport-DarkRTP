package com.darkrtp.teleport.warmup;

import com.darkrtp.config.ConfigurationService;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public final class PlayerWarmupService implements WarmupService, Listener {

    private static final long TICKS_PER_SECOND = 20L;

    private record Pending(BukkitTask task, Location origin, Consumer<CancelReason> onCancel) {
    }

    private final Plugin plugin;
    private final ConfigurationService configuration;
    private final Map<UUID, Pending> pending = new ConcurrentHashMap<>();

    public PlayerWarmupService(Plugin plugin, ConfigurationService configuration) {
        this.plugin = plugin;
        this.configuration = configuration;
    }

    @Override
    public boolean isPending(UUID playerId) {
        return pending.containsKey(playerId);
    }

    @Override
    public void begin(Player player, int seconds, Runnable onComplete, Consumer<CancelReason> onCancel) {
        UUID id = player.getUniqueId();
        BukkitTask task = plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            pending.remove(id);
            onComplete.run();
        }, seconds * TICKS_PER_SECOND);
        pending.put(id, new Pending(task, player.getLocation(), onCancel));
    }

    @Override
    public void cancel(UUID playerId) {
        Pending removed = pending.remove(playerId);
        if (removed != null) {
            removed.task().cancel();
        }
    }

    @Override
    public void cancelAll() {
        pending.values().forEach(p -> p.task().cancel());
        pending.clear();
    }

    private void interrupt(UUID playerId, CancelReason reason) {
        Pending removed = pending.remove(playerId);
        if (removed != null) {
            removed.task().cancel();
            removed.onCancel().accept(reason);
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (!configuration.general().cancelOnMove() || pending.isEmpty()) {
            return;
        }
        Pending warmup = pending.get(event.getPlayer().getUniqueId());
        if (warmup == null) {
            return;
        }
        Location from = warmup.origin();
        Location to = event.getTo();
        if (from.getBlockX() != to.getBlockX()
                || from.getBlockY() != to.getBlockY()
                || from.getBlockZ() != to.getBlockZ()) {
            interrupt(event.getPlayer().getUniqueId(), CancelReason.MOVED);
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (!configuration.general().cancelOnDamage()) {
            return;
        }
        if (event.getEntity() instanceof Player player && pending.containsKey(player.getUniqueId())) {
            interrupt(player.getUniqueId(), CancelReason.DAMAGED);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        if (pending.containsKey(event.getPlayer().getUniqueId())) {
            interrupt(event.getPlayer().getUniqueId(), CancelReason.QUIT);
        }
    }
}
