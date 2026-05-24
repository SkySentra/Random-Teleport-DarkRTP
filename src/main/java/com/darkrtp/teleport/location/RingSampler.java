package com.darkrtp.teleport.location;

import java.util.concurrent.ThreadLocalRandom;

public final class RingSampler {

    public record Point(int x, int z) {
    }

    public Point sample(double centerX, double centerZ, int minRadius, int maxRadius) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        double angle = random.nextDouble() * Math.PI * 2;
        double minSquared = (double) minRadius * minRadius;
        double maxSquared = (double) maxRadius * maxRadius;
        double distance = Math.sqrt(random.nextDouble() * (maxSquared - minSquared) + minSquared);
        int x = (int) Math.round(centerX + Math.cos(angle) * distance);
        int z = (int) Math.round(centerZ + Math.sin(angle) * distance);
        return new Point(x, z);
    }
}
