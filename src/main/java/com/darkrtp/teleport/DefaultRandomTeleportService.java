package com.darkrtp.teleport;

import com.darkrtp.config.ConfigurationService;
import com.darkrtp.config.WorldSettings;
import com.darkrtp.message.Message;
import com.darkrtp.message.MessageService;
import com.darkrtp.teleport.cooldown.CooldownService;
import com.darkrtp.teleport.effect.TeleportEffects;
import com.darkrtp.teleport.location.SafeLocationFinder;
import com.darkrtp.teleport.warmup.WarmupService;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public final class DefaultRandomTeleportService implements RandomTeleportService {

    private final ConfigurationService configuration;
    private final MessageService messages;
    private final CooldownService cooldowns;
    private final WarmupService warmups;
    private final SafeLocationFinder locationFinder;
    private final TeleportEffects effects;

    private final Set<UUID> searching = ConcurrentHashMap.newKeySet();

    public DefaultRandomTeleportService(ConfigurationService configuration,
                                        MessageService messages,
                                        CooldownService cooldowns,
                                        WarmupService warmups,
                                        SafeLocationFinder locationFinder,
                                        TeleportEffects effects) {
        this.configuration = configuration;
        this.messages = messages;
        this.cooldowns = cooldowns;
        this.warmups = warmups;
        this.locationFinder = locationFinder;
        this.effects = effects;
    }

    @Override
    public CompletableFuture<TeleportResult> teleport(TeleportRequest request) {
        Player target = request.target();
        UUID id = target.getUniqueId();
        WorldSettings settings = configuration.worldSettings(target.getWorld());

        if (!settings.enabled()) {
            messages.send(request.initiator(), Message.WORLD_DISABLED,
                    MessageService.placeholder("world", target.getWorld().getName()));
            return CompletableFuture.completedFuture(TeleportResult.WORLD_DISABLED);
        }
        if (searching.contains(id) || warmups.isPending(id)) {
            messages.send(request.initiator(), Message.ALREADY_TELEPORTING);
            return CompletableFuture.completedFuture(TeleportResult.ALREADY_IN_PROGRESS);
        }
        if (!request.bypassCooldown()) {
            long remaining = cooldowns.remainingSeconds(id);
            if (remaining > 0) {
                messages.send(request.initiator(), Message.COOLDOWN,
                        MessageService.placeholder("seconds", Long.toString(remaining)));
                return CompletableFuture.completedFuture(TeleportResult.ON_COOLDOWN);
            }
        }

        CompletableFuture<TeleportResult> outcome = new CompletableFuture<>();
        int warmupSeconds = request.bypassWarmup() ? 0 : configuration.general().warmupSeconds();
        if (warmupSeconds <= 0) {
            beginSearch(request, outcome);
        } else {
            messages.send(target, Message.WARMUP,
                    MessageService.placeholder("seconds", Integer.toString(warmupSeconds)));
            warmups.begin(target, warmupSeconds,
                    () -> beginSearch(request, outcome),
                    reason -> onWarmupCancelled(request, reason, outcome));
        }
        return outcome;
    }

    private void onWarmupCancelled(TeleportRequest request, WarmupService.CancelReason reason,
                                   CompletableFuture<TeleportResult> outcome) {
        switch (reason) {
            case MOVED -> messages.send(request.target(), Message.CANCELLED_MOVE);
            case DAMAGED -> messages.send(request.target(), Message.CANCELLED_DAMAGE);
            case QUIT -> {  }
        }
        outcome.complete(TeleportResult.CANCELLED);
    }

    private void beginSearch(TeleportRequest request, CompletableFuture<TeleportResult> outcome) {
        Player target = request.target();
        if (!target.isOnline()) {
            outcome.complete(TeleportResult.CANCELLED);
            return;
        }
        UUID id = target.getUniqueId();
        // Guard the player for the whole operation, not just the search: clearing the
        // flag here (when the outcome resolves) keeps it set through the teleport itself,
        // so a second /rtp can't slip in during the teleportAsync window.
        searching.add(id);
        outcome.whenComplete((result, throwable) -> searching.remove(id));
        messages.send(request.initiator(), Message.SEARCHING);

        locationFinder.find(target.getWorld()).whenComplete((found, throwable) -> {
            if (throwable != null || found == null || found.isEmpty()) {
                messages.send(request.initiator(), Message.NO_LOCATION);
                outcome.complete(TeleportResult.NO_SAFE_LOCATION);
                return;
            }
            if (!target.isOnline()) {
                outcome.complete(TeleportResult.CANCELLED);
                return;
            }
            performTeleport(request, found.get(), outcome);
        });
    }

    private void performTeleport(TeleportRequest request, Location destination,
                                 CompletableFuture<TeleportResult> outcome) {
        Player target = request.target();
        Location current = target.getLocation();
        destination.setYaw(current.getYaw());
        destination.setPitch(current.getPitch());

        target.teleportAsync(destination).thenAccept(success -> {
            if (!Boolean.TRUE.equals(success)) {
                messages.send(request.initiator(), Message.NO_LOCATION);
                outcome.complete(TeleportResult.NO_SAFE_LOCATION);
                return;
            }
            if (!request.bypassCooldown()) {
                cooldowns.start(target.getUniqueId(), configuration.general().cooldownSeconds());
            }
            announceSuccess(request, destination);
            effects.playArrival(target);
            outcome.complete(TeleportResult.SUCCESS);
        });
    }

    private void announceSuccess(TeleportRequest request, Location destination) {
        Player target = request.target();
        messages.send(target, Message.SUCCESS,
                MessageService.placeholder("x", Integer.toString(destination.getBlockX())),
                MessageService.placeholder("y", Integer.toString(destination.getBlockY())),
                MessageService.placeholder("z", Integer.toString(destination.getBlockZ())),
                MessageService.placeholder("world", target.getWorld().getName()));
        if (!request.selfRequest()) {
            messages.send(request.initiator(), Message.TELEPORTED_OTHER,
                    MessageService.placeholder("player", target.getName()));
        }
    }

    @Override
    public void shutdown() {
        warmups.cancelAll();
        searching.clear();
    }
}
