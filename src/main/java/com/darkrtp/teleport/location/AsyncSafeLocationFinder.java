package com.darkrtp.teleport.location;

import com.darkrtp.config.ConfigurationService;
import com.darkrtp.config.WorldSettings;
import com.darkrtp.teleport.location.safety.SafeSpotScanner;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldBorder;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public final class AsyncSafeLocationFinder implements SafeLocationFinder {

    private static final int WORLD_BORDER_MARGIN = 2;

    private final ConfigurationService configuration;
    private final CenterResolverRegistry centerResolvers;
    private final RingSampler ringSampler;
    private final SafeSpotScanner scanner;

    public AsyncSafeLocationFinder(ConfigurationService configuration,
                                   CenterResolverRegistry centerResolvers,
                                   RingSampler ringSampler,
                                   SafeSpotScanner scanner) {
        this.configuration = configuration;
        this.centerResolvers = centerResolvers;
        this.ringSampler = ringSampler;
        this.scanner = scanner;
    }

    @Override
    public CompletableFuture<Optional<Location>> find(World world) {
        CompletableFuture<Optional<Location>> result = new CompletableFuture<>();
        attempt(world, configuration.worldSettings(world), 0, result);
        return result;
    }

    private void attempt(World world, WorldSettings settings, int tries,
                         CompletableFuture<Optional<Location>> result) {
        if (tries >= configuration.general().maxAttempts()) {
            result.complete(Optional.empty());
            return;
        }

        CenterResolver.Center center = centerResolvers.resolve(world, settings);
        int min = Math.max(0, settings.minRadius());
        int max = Math.max(min + 1, settings.maxRadius());
        RingSampler.Point point = ringSampler.sample(center.x(), center.z(), min, max);

        if (settings.respectWorldBorder() && !insideBorder(world, point.x(), point.z())) {
            attempt(world, settings, tries + 1, result);
            return;
        }

        world.getChunkAtAsync(point.x() >> 4, point.z() >> 4, true).thenAccept(chunk -> {
            Optional<Location> spot = scanner.scan(world, point.x(), point.z(), configuration.safety());
            if (spot.isPresent()) {
                result.complete(spot);
            } else {
                attempt(world, settings, tries + 1, result);
            }
        }).exceptionally(throwable -> {
            attempt(world, settings, tries + 1, result);
            return null;
        });
    }

    private boolean insideBorder(World world, int x, int z) {
        WorldBorder border = world.getWorldBorder();
        double half = border.getSize() / 2.0;
        double centerX = border.getCenter().getX();
        double centerZ = border.getCenter().getZ();
        return Math.abs(x - centerX) <= half - WORLD_BORDER_MARGIN
                && Math.abs(z - centerZ) <= half - WORLD_BORDER_MARGIN;
    }
}
