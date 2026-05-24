package com.darkrtp.teleport.location;

import com.darkrtp.config.WorldSettings;
import org.bukkit.World;

public final class FixedCenterResolver implements CenterResolver {

    @Override
    public boolean supports(WorldSettings.CenterMode mode) {
        return mode == WorldSettings.CenterMode.FIXED;
    }

    @Override
    public Center resolve(World world, WorldSettings settings) {
        return new Center(settings.centerX(), settings.centerZ());
    }
}
