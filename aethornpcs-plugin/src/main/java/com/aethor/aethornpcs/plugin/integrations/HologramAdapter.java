package com.aethor.aethornpcs.plugin.integrations;

import com.aethor.aethornpcs.plugin.AethorNPCSPlugin;
import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Adapter for DecentHolograms integration
 * Creates and manages floating holograms above NPC heads for display names
 */
public class HologramAdapter {
    private final AethorNPCSPlugin plugin;
    private final boolean enabled;
    
    // Patterns for MiniMessage to DecentHolograms conversion
    // Handle both </rainbow> and <rainbow> as closing tags (user error)
    private static final Pattern RAINBOW_PATTERN = Pattern.compile("<rainbow>(.+?)(?:</rainbow>|<rainbow>)", Pattern.CASE_INSENSITIVE);
    private static final Pattern GRADIENT_PATTERN = Pattern.compile("<gradient:([^:>]+):([^>]+)>(.+?)</gradient>", Pattern.CASE_INSENSITIVE);
    private static final Pattern HEX_PATTERN = Pattern.compile("<#([0-9A-Fa-f]{6})>");

    public HologramAdapter(AethorNPCSPlugin plugin) {
        this.plugin = plugin;
        this.enabled = plugin.getServer().getPluginManager().isPluginEnabled("DecentHolograms");
        if (enabled) {
            plugin.getLogger().info("DecentHolograms integration enabled!");
        } else {
            plugin.getLogger().warning("DecentHolograms not found - display names will not appear above NPCs");
        }
    }

    public boolean isEnabled() {
        return enabled;
    }
    
    /**
     * Convert MiniMessage format to DecentHolograms format
     * @param miniMessageText Text with MiniMessage formatting
     * @return Text with DecentHolograms formatting
     */
    private String convertToDecentHologramsFormat(String miniMessageText) {
        if (miniMessageText == null || miniMessageText.isEmpty()) {
            return miniMessageText;
        }
        
        String original = miniMessageText;
        String result = miniMessageText;
        
        // Convert <rainbow>text</rainbow> or <rainbow>text<rainbow> to <RAINBOW1>text</RAINBOW>
        result = RAINBOW_PATTERN.matcher(result).replaceAll("<RAINBOW1>$1</RAINBOW>");
        
        // Convert <gradient:color1:color2>text</gradient> to <#color1>text</#color2>
        Matcher gradientMatcher = GRADIENT_PATTERN.matcher(result);
        StringBuffer sb = new StringBuffer();
        while (gradientMatcher.find()) {
            String color1 = convertNamedColorToHex(gradientMatcher.group(1));
            String color2 = convertNamedColorToHex(gradientMatcher.group(2));
            String text = gradientMatcher.group(3);
            gradientMatcher.appendReplacement(sb, "<" + color1 + ">" + text + "</" + color2 + ">");
        }
        gradientMatcher.appendTail(sb);
        result = sb.toString();
        
        // Hex colors are already compatible: <#RRGGBB> works in both
        
        // Log conversion if changed
        if (!original.equals(result)) {
            plugin.debug("Converted display name format: '" + original + "' -> '" + result + "'");
        }
        
        return result;
    }
    
    /**
     * Convert named color to hex code
     * @param colorName Named color (e.g., "red", "blue")
     * @return Hex color code (e.g., "#FF0000")
     */
    private String convertNamedColorToHex(String colorName) {
        if (colorName.startsWith("#")) {
            return colorName;
        }
        
        // Common color mappings
        switch (colorName.toLowerCase()) {
            case "red": return "#FF0000";
            case "blue": return "#0000FF";
            case "green": return "#00FF00";
            case "yellow": return "#FFFF00";
            case "aqua": return "#00FFFF";
            case "gold": return "#FFD700";
            case "gray": case "grey": return "#808080";
            case "dark_gray": case "dark_grey": return "#555555";
            case "light_purple": return "#FF55FF";
            case "dark_purple": return "#AA00AA";
            case "white": return "#FFFFFF";
            case "black": return "#000000";
            default: return colorName; // Return as-is if unknown
        }
    }

    /**
     * Create a hologram above an entity's head with the specified display name
     * @param entityUuid The UUID of the entity
     * @param entity The entity to attach the hologram to
     * @param displayName The display name to show (supports MiniMessage formatting)
     * @return true if hologram was created successfully
     */
    public boolean createHologram(UUID entityUuid, Entity entity, String displayName) {
        if (!enabled || displayName == null || displayName.isEmpty()) {
            return false;
        }

        try {
            String hologramName = "aethornpc_" + entityUuid.toString();
            
            // Remove existing hologram if it exists
            removeHologram(entityUuid);
            
            // Get location above entity's head (add 2.3 blocks to Y for proper positioning)
            Location hologramLocation = entity.getLocation().clone().add(0, 2.3, 0);
            
            // Convert MiniMessage format to DecentHolograms format
            String decentHologramsText = convertToDecentHologramsFormat(displayName);
            
            // Create hologram with the display name as the first line
            // false = non-persistent (won't be saved to file)
            DHAPI.createHologram(hologramName, hologramLocation, false, Collections.singletonList(decentHologramsText));
            
            plugin.debug("Created hologram '" + hologramName + "' with display name: " + decentHologramsText);
            return true;
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to create hologram for NPC " + entityUuid + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * Update the display name of an existing hologram
     * @param entityUuid The UUID of the entity
     * @param entity The entity (for location updates)
     * @param displayName The new display name
     * @return true if hologram was updated successfully
     */
    public boolean updateHologram(UUID entityUuid, Entity entity, String displayName) {
        if (!enabled || displayName == null || displayName.isEmpty()) {
            return false;
        }

        try {
            String hologramName = "aethornpc_" + entityUuid.toString();
            Hologram hologram = DHAPI.getHologram(hologramName);
            
            if (hologram == null) {
                // Hologram doesn't exist, create it
                return createHologram(entityUuid, entity, displayName);
            }
            
            // Convert MiniMessage format to DecentHolograms format
            String decentHologramsText = convertToDecentHologramsFormat(displayName);
            
            // Update the hologram's text
            DHAPI.setHologramLine(hologram, 0, decentHologramsText);
            
            // Update location to follow the entity
            Location newLocation = entity.getLocation().clone().add(0, 2.3, 0);
            hologram.setLocation(newLocation);
            
            plugin.debug("Updated hologram '" + hologramName + "' with display name: " + decentHologramsText);
            return true;
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to update hologram for NPC " + entityUuid + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * Remove a hologram associated with an NPC
     * @param entityUuid The UUID of the entity
     */
    public void removeHologram(UUID entityUuid) {
        if (!enabled) {
            return;
        }

        try {
            String hologramName = "aethornpc_" + entityUuid.toString();
            Hologram hologram = DHAPI.getHologram(hologramName);
            
            if (hologram != null) {
                hologram.delete();
                plugin.debug("Removed hologram '" + hologramName + "'");
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to remove hologram for NPC " + entityUuid + ": " + e.getMessage());
        }
    }

    /**
     * Update the position of a hologram to follow its entity
     * Should be called periodically or when the entity moves
     * @param entityUuid The UUID of the entity
     * @param entity The entity to follow
     */
    public void updateHologramLocation(UUID entityUuid, Entity entity) {
        if (!enabled) {
            return;
        }

        try {
            String hologramName = "aethornpc_" + entityUuid.toString();
            Hologram hologram = DHAPI.getHologram(hologramName);
            
            if (hologram != null) {
                Location newLocation = entity.getLocation().clone().add(0, 2.3, 0);
                hologram.setLocation(newLocation);
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to update hologram location for NPC " + entityUuid + ": " + e.getMessage());
        }
    }

    /**
     * Remove all AethorNPC holograms (useful for cleanup on disable)
     * Note: This method is a no-op since DHAPI doesn't provide a way to list all holograms.
     * Individual holograms will be removed when NPCs despawn.
     */
    public void removeAllHolograms() {
        // Individual holograms are removed in despawnEntity() via removeHologram()
        // No need for bulk cleanup since holograms are non-persistent
        if (enabled) {
            plugin.debug("Hologram cleanup - individual holograms removed on NPC despawn");
        }
    }
    
    /**
     * Remove orphaned holograms that don't belong to any active NPC entity
     * @param activeEntityUuids Collection of entity UUIDs that should have holograms
     * @return The number of holograms removed
     */
    public int removeOrphanedHolograms(Collection<UUID> activeEntityUuids) {
        if (!enabled) {
            return 0;
        }
        
        int removed = 0;
        try {
            // Get all worlds and scan for entities to find all possible hologram names
            // This is a workaround since DHAPI doesn't provide getAllHolograms()
            for (org.bukkit.World world : plugin.getServer().getWorlds()) {
                for (org.bukkit.entity.Entity entity : world.getEntities()) {
                    UUID entityUuid = entity.getUniqueId();
                    
                    // Check if this entity has an aethornpc hologram
                    String hologramName = "aethornpc_" + entityUuid.toString();
                    Hologram hologram = DHAPI.getHologram(hologramName);
                    
                    if (hologram != null) {
                        // Hologram exists - check if it belongs to an active NPC
                        if (!activeEntityUuids.contains(entityUuid)) {
                            // Orphaned hologram - remove it
                            hologram.delete();
                            removed++;
                            plugin.debug("Removed orphaned hologram: " + hologramName);
                        }
                    }
                }
            }
            
            if (removed > 0) {
                plugin.getLogger().info("Removed " + removed + " orphaned hologram(s)");
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Error during hologram cleanup: " + e.getMessage());
        }
        
        return removed;
    }
}
