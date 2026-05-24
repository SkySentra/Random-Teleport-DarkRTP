package com.darkrtp.config;

public record WorldSettings(
        boolean enabled,
        int minRadius,
        int maxRadius,
        CenterMode centerMode,
        double centerX,
        double centerZ,
        boolean respectWorldBorder
) {

    public enum CenterMode {
        WORLD_SPAWN,
        WORLD_BORDER,
        FIXED
    }
}
