package com.darkrtp.teleport.location;

import org.bukkit.Location;
import org.bukkit.World;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface SafeLocationFinder {

    CompletableFuture<Optional<Location>> find(World world);
}
