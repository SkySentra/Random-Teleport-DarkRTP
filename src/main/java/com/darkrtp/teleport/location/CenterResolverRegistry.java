package com.darkrtp.teleport.location;

import com.darkrtp.config.WorldSettings;
import org.bukkit.World;

import java.util.List;

public final class CenterResolverRegistry {

    private final List<CenterResolver> resolvers;

    public CenterResolverRegistry(List<CenterResolver> resolvers) {
        this.resolvers = List.copyOf(resolvers);
    }

    public CenterResolver.Center resolve(World world, WorldSettings settings) {
        for (CenterResolver resolver : resolvers) {
            if (resolver.supports(settings.centerMode())) {
                return resolver.resolve(world, settings);
            }
        }
        return new SpawnCenterResolver().resolve(world, settings);
    }
}
