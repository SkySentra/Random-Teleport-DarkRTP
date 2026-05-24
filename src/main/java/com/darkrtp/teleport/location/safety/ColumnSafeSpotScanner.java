package com.darkrtp.teleport.location.safety;

import com.darkrtp.config.SafetySettings;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.List;
import java.util.Optional;

public final class ColumnSafeSpotScanner implements SafeSpotScanner {

    private static final int NETHER_SCAN_START = 122;

    private final List<SafetyValidator> validators;

    public ColumnSafeSpotScanner(List<SafetyValidator> validators) {
        this.validators = List.copyOf(validators);
    }

    @Override
    public Optional<Location> scan(World world, int x, int z, SafetySettings settings) {
        if (world.getEnvironment() == World.Environment.NETHER) {
            return scanDownward(world, x, z, settings);
        }
        return evaluate(world, x, world.getHighestBlockYAt(x, z), z, settings);
    }

    private Optional<Location> scanDownward(World world, int x, int z, SafetySettings settings) {
        int start = Math.min(world.getMaxHeight() - 3, NETHER_SCAN_START);
        int floor = Math.max(settings.minY(), world.getMinHeight());
        for (int groundY = start; groundY > floor; groundY--) {
            Optional<Location> spot = evaluate(world, x, groundY, z, settings);
            if (spot.isPresent()) {
                return spot;
            }
        }
        return Optional.empty();
    }

    private Optional<Location> evaluate(World world, int x, int groundY, int z, SafetySettings settings) {
        SpotCandidate candidate = new SpotCandidate(
                world, x, groundY, z,
                world.getBlockAt(x, groundY, z),
                world.getBlockAt(x, groundY + 1, z),
                world.getBlockAt(x, groundY + 2, z));

        for (SafetyValidator validator : validators) {
            if (!validator.isSafe(candidate, settings)) {
                return Optional.empty();
            }
        }
        return Optional.of(new Location(world, x + 0.5, groundY + 1, z + 0.5));
    }
}
