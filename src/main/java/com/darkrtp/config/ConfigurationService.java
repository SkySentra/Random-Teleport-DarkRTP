package com.darkrtp.config;

import org.bukkit.World;

public interface ConfigurationService {

    GeneralSettings general();

    SafetySettings safety();

    WorldSettings worldSettings(World world);

    void reload();
}
