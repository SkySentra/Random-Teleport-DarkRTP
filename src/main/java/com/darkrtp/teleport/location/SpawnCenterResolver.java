package com.darkrtp.teleport.location;

import com.darkrtp.config.WorldSettings;
import org.bukkit.Location;
import org.bukkit.World;

public final class SpawnCenterResolver implements CenterResolver {

    @Override
    public boolean supports(WorldSettings.CenterMode mode) {
        return mode == WorldSettings.CenterMode.WORLD_SPAWN;
    }

    @Override
    public Center resolve(World world, WorldSettings settings) {
        Location spawn = world.getSpawnLocation();
        return new Center(spawn.getX(), spawn.getZ());
    }
}
