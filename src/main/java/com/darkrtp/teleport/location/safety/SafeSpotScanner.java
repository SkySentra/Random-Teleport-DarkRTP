package com.darkrtp.teleport.location.safety;

import com.darkrtp.config.SafetySettings;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.Optional;

public interface SafeSpotScanner {

    Optional<Location> scan(World world, int x, int z, SafetySettings settings);
}
