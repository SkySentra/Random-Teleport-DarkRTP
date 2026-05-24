package com.darkrtp.config;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

import java.util.EnumSet;
import java.util.Locale;

public final class BukkitConfigurationService implements ConfigurationService {

    private final Plugin plugin;
    private volatile GeneralSettings general;
    private volatile SafetySettings safety;

    public BukkitConfigurationService(Plugin plugin) {
        this.plugin = plugin;
        reload();
    }

    @Override
    public void reload() {
        plugin.reloadConfig();
        FileConfiguration config = plugin.getConfig();
        this.general = readGeneral(config);
        this.safety = readSafety(config);
    }

    @Override
    public GeneralSettings general() {
        return general;
    }

    @Override
    public SafetySettings safety() {
        return safety;
    }

    @Override
    public WorldSettings worldSettings(World world) {
        FileConfiguration config = plugin.getConfig();
        String name = world.getName();
        int min = Math.max(0, integer(config, name, "min-radius", 100));
        int max = Math.max(min + 1, integer(config, name, "max-radius", 5000));
        return new WorldSettings(
                bool(config, name, "enabled", true),
                min,
                max,
                centerMode(string(config, name, "center.mode", "world-spawn")),
                real(config, name, "center.x", 0),
                real(config, name, "center.z", 0),
                bool(config, name, "respect-world-border", true));
    }

    private GeneralSettings readGeneral(FileConfiguration config) {
        return new GeneralSettings(
                Math.max(1, config.getInt("settings.max-attempts", 50)),
                Math.max(0, config.getInt("settings.warmup-seconds", 3)),
                config.getBoolean("settings.cancel-on-move", true),
                config.getBoolean("settings.cancel-on-damage", true),
                Math.max(0, config.getInt("settings.cooldown-seconds", 60)),
                config.getBoolean("settings.effects.sound", true),
                config.getBoolean("settings.effects.particles", true));
    }

    private SafetySettings readSafety(FileConfiguration config) {
        EnumSet<Material> unsafe = EnumSet.noneOf(Material.class);
        for (String entry : config.getStringList("safety.unsafe-blocks")) {
            Material material = Material.matchMaterial(entry.trim());
            if (material != null) {
                unsafe.add(material);
            } else {
                plugin.getLogger().warning("Unknown material in safety.unsafe-blocks: " + entry);
            }
        }
        return new SafetySettings(
                unsafe,
                config.getBoolean("safety.allow-water-landing", false),
                config.getBoolean("safety.avoid-nether-roof", true),
                config.getInt("safety.min-y", -64));
    }

    private WorldSettings.CenterMode centerMode(String value) {
        return switch (value.toLowerCase(Locale.ROOT).replace('_', '-')) {
            case "world-border", "border" -> WorldSettings.CenterMode.WORLD_BORDER;
            case "fixed", "manual" -> WorldSettings.CenterMode.FIXED;
            default -> WorldSettings.CenterMode.WORLD_SPAWN;
        };
    }

    private int integer(FileConfiguration config, String world, String key, int def) {
        String worldPath = "worlds." + world + "." + key;
        if (config.isSet(worldPath)) {
            return config.getInt(worldPath);
        }
        String defaultPath = "defaults." + key;
        return config.isSet(defaultPath) ? config.getInt(defaultPath) : def;
    }

    private boolean bool(FileConfiguration config, String world, String key, boolean def) {
        String worldPath = "worlds." + world + "." + key;
        if (config.isSet(worldPath)) {
            return config.getBoolean(worldPath);
        }
        String defaultPath = "defaults." + key;
        return config.isSet(defaultPath) ? config.getBoolean(defaultPath) : def;
    }

    private double real(FileConfiguration config, String world, String key, double def) {
        String worldPath = "worlds." + world + "." + key;
        if (config.isSet(worldPath)) {
            return config.getDouble(worldPath);
        }
        String defaultPath = "defaults." + key;
        return config.isSet(defaultPath) ? config.getDouble(defaultPath) : def;
    }

    private String string(FileConfiguration config, String world, String key, String def) {
        String worldPath = "worlds." + world + "." + key;
        if (config.isSet(worldPath)) {
            return config.getString(worldPath, def);
        }
        String defaultPath = "defaults." + key;
        return config.isSet(defaultPath) ? config.getString(defaultPath, def) : def;
    }
}
