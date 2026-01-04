package com.aethor.aethornpcs.plugin.config;

import org.bukkit.configuration.file.FileConfiguration;

/**
 * Plugin configuration wrapper.
 */
public class PluginConfiguration {

    private final FileConfiguration config;

    public PluginConfiguration(FileConfiguration config) {
        this.config = config;
    }

    public boolean isProximityEnabled() {
        return config.getBoolean("proximity.enabled", true);
    }

    public double getProximityRadius() {
        return config.getDouble("proximity.radius", 3.0);
    }

    public int getProximityPeriodTicks() {
        return config.getInt("proximity.periodTicks", 10);
    }

    public String getChunkStrategy() {
        return config.getString("chunkStrategy", "DESPAWN_ON_UNLOAD");
    }

    public boolean getDefaultInvulnerable() {
        return config.getBoolean("defaultFlags.invulnerable", true);
    }

    public boolean getDefaultSilent() {
        return config.getBoolean("defaultFlags.silent", true);
    }

    public boolean getDefaultNoAI() {
        return config.getBoolean("defaultFlags.noAI", true);
    }

    public boolean getDefaultLookAtPlayers() {
        return config.getBoolean("defaultFlags.lookAtPlayers", true);
    }

    public boolean isDebugLogging() {
        return config.getBoolean("debugLogging", false);
    }
}
