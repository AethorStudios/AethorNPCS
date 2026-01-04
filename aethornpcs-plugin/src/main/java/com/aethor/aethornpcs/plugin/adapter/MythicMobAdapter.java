package com.aethor.aethornpcs.plugin.adapter;

import com.aethor.aethornpcs.plugin.AethorNPCSPlugin;
import io.lumine.mythic.api.MythicProvider;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.mobs.ActiveMob;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

import java.util.Optional;
import java.util.UUID;

/**
 * Adapter for MythicMobs integration.
 * Handles all direct interactions with the MythicMobs API.
 */
public class MythicMobAdapter {

    private final AethorNPCSPlugin plugin;
    private final boolean available;

    public MythicMobAdapter(AethorNPCSPlugin plugin) {
        this.plugin = plugin;
        this.available = plugin.getServer().getPluginManager().getPlugin("MythicMobs") != null;
    }

    public boolean isAvailable() {
        return available;
    }

    /**
     * Spawn a MythicMob at the given location.
     *
     * @param internalName MythicMobs mob type
     * @param location Spawn location
     * @param level Mob level
     * @return The spawned entity, or null if spawn failed
     */
    public Entity spawnMob(String internalName, Location location, int level) {
        if (!available) {
            plugin.getLogger().warning("Cannot spawn MythicMob - MythicMobs not available");
            return null;
        }

        try {
            ActiveMob activeMob = MythicBukkit.inst().getMobManager()
                    .spawnMob(internalName, BukkitAdapter.adapt(location), level);
            
            if (activeMob != null) {
                Entity entity = activeMob.getEntity().getBukkitEntity();
                plugin.debug("Spawned MythicMob " + internalName + " with UUID " + entity.getUniqueId());
                return entity;
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to spawn MythicMob " + internalName + ": " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Get ActiveMob by entity UUID.
     */
    public Optional<ActiveMob> getActiveMob(UUID entityUuid) {
        if (!available) {
            return Optional.empty();
        }

        try {
            return Optional.ofNullable(MythicBukkit.inst().getMobManager().getActiveMob(entityUuid).orElse(null));
        } catch (Exception e) {
            plugin.getLogger().warning("Error getting ActiveMob: " + e.getMessage());
            return Optional.empty();
        }
    }
    
    /**
     * Set the display name of a MythicMob using MythicMobs' API.
     * This ensures the name persists and isn't overridden by MythicMobs.
     */
    public void setDisplayName(UUID entityUuid, String displayName) {
        if (!available) {
            return;
        }
        
        try {
            Optional<ActiveMob> activeMob = getActiveMob(entityUuid);
            if (activeMob.isPresent()) {
                activeMob.get().setDisplayName(displayName);
                plugin.debug("Set MythicMob display name via API: " + displayName);
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Error setting MythicMob display name: " + e.getMessage());
        }
    }

    /**
     * Remove a MythicMob entity.
     */
    public void removeMob(UUID entityUuid) {
        if (!available) {
            return;
        }

        try {
            getActiveMob(entityUuid).ifPresent(activeMob -> {
                activeMob.remove();
                plugin.debug("Removed MythicMob with UUID " + entityUuid);
            });
        } catch (Exception e) {
            plugin.getLogger().warning("Error removing MythicMob: " + e.getMessage());
        }
    }

    /**
     * Check if a mob type exists.
     */
    public boolean mobTypeExists(String internalName) {
        if (!available) {
            return false;
        }

        try {
            return MythicBukkit.inst().getMobManager().getMythicMob(internalName).isPresent();
        } catch (Exception e) {
            return false;
        }
    }
}
