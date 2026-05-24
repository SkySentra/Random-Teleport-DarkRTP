package com.darkrtp.teleport.location;

import com.darkrtp.config.WorldSettings;
import org.bukkit.Location;
import org.bukkit.World;

public final class WorldBorderCenterResolver implements CenterResolver {

    @Override
    public boolean supports(WorldSettings.CenterMode mode) {
        return mode == WorldSettings.CenterMode.WORLD_BORDER;
    }

    @Override
    public Center resolve(World world, WorldSettings settings) {
        Location center = world.getWorldBorder().getCenter();
        return new Center(center.getX(), center.getZ());
    }
}
