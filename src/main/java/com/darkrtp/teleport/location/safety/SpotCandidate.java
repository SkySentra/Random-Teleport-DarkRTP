package com.darkrtp.teleport.location.safety;

import org.bukkit.World;
import org.bukkit.block.Block;

public record SpotCandidate(
        World world,
        int x,
        int groundY,
        int z,
        Block ground,
        Block feet,
        Block head
) {
}
