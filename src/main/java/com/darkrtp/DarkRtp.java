package com.darkrtp;

import com.darkrtp.command.RandomTeleportCommand;
import com.darkrtp.config.BukkitConfigurationService;
import com.darkrtp.config.ConfigurationService;
import com.darkrtp.message.MessageService;
import com.darkrtp.message.MiniMessageService;
import com.darkrtp.teleport.DefaultRandomTeleportService;
import com.darkrtp.teleport.RandomTeleportService;
import com.darkrtp.teleport.cooldown.CooldownService;
import com.darkrtp.teleport.cooldown.InMemoryCooldownService;
import com.darkrtp.teleport.effect.BukkitTeleportEffects;
import com.darkrtp.teleport.effect.TeleportEffects;
import com.darkrtp.teleport.location.AsyncSafeLocationFinder;
import com.darkrtp.teleport.location.CenterResolverRegistry;
import com.darkrtp.teleport.location.FixedCenterResolver;
import com.darkrtp.teleport.location.RingSampler;
import com.darkrtp.teleport.location.SafeLocationFinder;
import com.darkrtp.teleport.location.SpawnCenterResolver;
import com.darkrtp.teleport.location.WorldBorderCenterResolver;
import com.darkrtp.teleport.location.safety.ColumnSafeSpotScanner;
import com.darkrtp.teleport.location.safety.HeightBoundsValidator;
import com.darkrtp.teleport.location.safety.OpenSpaceValidator;
import com.darkrtp.teleport.location.safety.SafeSpotScanner;
import com.darkrtp.teleport.location.safety.SolidGroundValidator;
import com.darkrtp.teleport.location.safety.UnsafeBlockValidator;
import com.darkrtp.teleport.warmup.PlayerWarmupService;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public final class DarkRtp extends JavaPlugin {

    private ConfigurationService configuration;
    private MessageService messages;
    private RandomTeleportService teleportService;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        this.configuration = new BukkitConfigurationService(this);
        this.messages = new MiniMessageService(this);

        CooldownService cooldowns = new InMemoryCooldownService();
        PlayerWarmupService warmups = new PlayerWarmupService(this, configuration);
        TeleportEffects effects = new BukkitTeleportEffects(configuration);
        SafeLocationFinder locationFinder = buildLocationFinder();

        this.teleportService = new DefaultRandomTeleportService(
                configuration, messages, cooldowns, warmups, locationFinder, effects);

        getServer().getPluginManager().registerEvents(warmups, this);

        if (!registerCommand()) {
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        getLogger().info("DarkRTP enabled.");
    }

    @Override
    public void onDisable() {
        if (teleportService != null) {
            teleportService.shutdown();
        }
    }

    private SafeLocationFinder buildLocationFinder() {
        CenterResolverRegistry centerResolvers = new CenterResolverRegistry(List.of(
                new SpawnCenterResolver(),
                new WorldBorderCenterResolver(),
                new FixedCenterResolver()));

        SafeSpotScanner scanner = new ColumnSafeSpotScanner(List.of(
                new HeightBoundsValidator(),
                new SolidGroundValidator(),
                new UnsafeBlockValidator(),
                new OpenSpaceValidator()));

        return new AsyncSafeLocationFinder(configuration, centerResolvers, new RingSampler(), scanner);
    }

    private boolean registerCommand() {
        PluginCommand command = getCommand("darkrtp");
        if (command == null) {
            getLogger().severe("Command 'darkrtp' is missing from plugin.yml; disabling DarkRTP.");
            return false;
        }
        RandomTeleportCommand executor =
                new RandomTeleportCommand(teleportService, messages, this::reloadPlugin);
        command.setExecutor(executor);
        command.setTabCompleter(executor);
        return true;
    }

    private void reloadPlugin() {
        configuration.reload();
        messages.reload();
    }
}
