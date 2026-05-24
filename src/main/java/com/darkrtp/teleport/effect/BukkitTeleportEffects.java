package com.darkrtp.teleport.effect;

import com.darkrtp.config.ConfigurationService;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public final class BukkitTeleportEffects implements TeleportEffects {

    private static final int PARTICLE_COUNT = 40;

    private final ConfigurationService configuration;

    public BukkitTeleportEffects(ConfigurationService configuration) {
        this.configuration = configuration;
    }

    @Override
    public void playArrival(Player player) {
        try {
            Location location = player.getLocation();
            if (configuration.general().soundEffects()) {
                player.playSound(location, Sound.ENTITY_ENDERMAN_TELEPORT, 0.8f, 1.0f);
            }
            if (configuration.general().particleEffects()) {
                player.getWorld().spawnParticle(
                        Particle.PORTAL, location.clone().add(0, 1, 0), PARTICLE_COUNT, 0.5, 1.0, 0.5, 0.1);
            }
        } catch (RuntimeException ignored) {
        }
    }
}
