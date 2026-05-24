package com.darkrtp.teleport.location;

import com.darkrtp.config.WorldSettings;
import org.bukkit.World;

public interface CenterResolver {

    record Center(double x, double z) {
    }

    boolean supports(WorldSettings.CenterMode mode);

    Center resolve(World world, WorldSettings settings);
}
