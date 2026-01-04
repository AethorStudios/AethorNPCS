package com.aethor.aethornpcs.plugin.persistence;

import com.aethor.aethornpcs.api.dto.NpcLocation;
import com.aethor.aethornpcs.plugin.AethorNPCSPlugin;
import com.aethor.aethornpcs.plugin.model.NpcImpl;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Handles persistence of NPC definitions to disk.
 */
public class NpcPersistence {

    private final AethorNPCSPlugin plugin;
    private final File dataFile;
    private final Map<String, NpcDefinition> definitions = new HashMap<>();

    public NpcPersistence(AethorNPCSPlugin plugin, File dataFolder) {
        this.plugin = plugin;
        dataFolder.mkdirs();
        this.dataFile = new File(dataFolder, "npcs.yml");
    }

    /**
     * Load NPCs from disk.
     */
    public void load() {
        if (!dataFile.exists()) {
            plugin.getLogger().info("No npcs.yml found, starting fresh.");
            return;
        }

        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(dataFile);
        ConfigurationSection npcsSection = yaml.getConfigurationSection("npcs");

        if (npcsSection == null) {
            plugin.getLogger().info("No NPCs defined in npcs.yml");
            return;
        }

        int count = 0;
        for (String id : npcsSection.getKeys(false)) {
            ConfigurationSection npcSection = npcsSection.getConfigurationSection(id);
            if (npcSection == null) continue;

            try {
                NpcDefinition def = new NpcDefinition();
                def.id = id;
                def.world = npcSection.getString("world");
                def.x = npcSection.getDouble("x");
                def.y = npcSection.getDouble("y");
                def.z = npcSection.getDouble("z");
                def.yaw = (float) npcSection.getDouble("yaw", 0.0);
                def.pitch = (float) npcSection.getDouble("pitch", 0.0);
                def.mythicMobInternalName = npcSection.getString("mythicMob");
                def.modelEngineModelId = npcSection.getString("model");
                
                // Load metadata
                ConfigurationSection metaSection = npcSection.getConfigurationSection("metadata");
                if (metaSection != null) {
                    for (String key : metaSection.getKeys(false)) {
                        def.metadata.put(key, metaSection.getString(key));
                    }
                }

                // Load flags
                def.invulnerable = npcSection.getBoolean("flags.invulnerable", true);
                def.silent = npcSection.getBoolean("flags.silent", true);
                def.noAI = npcSection.getBoolean("flags.noAI", true);
                def.lookAtPlayers = npcSection.getBoolean("flags.lookAtPlayers", true);
                
                // Load display name
                def.displayName = npcSection.getString("displayName");

                definitions.put(id, def);
                count++;

            } catch (Exception e) {
                plugin.getLogger().severe("Failed to load NPC " + id + ": " + e.getMessage());
            }
        }

        plugin.getLogger().info("Loaded " + count + " NPC definition(s) from disk.");
    }

    /**
     * Save NPCs to disk.
     */
    public void save() {
        YamlConfiguration yaml = new YamlConfiguration();

        for (NpcDefinition def : definitions.values()) {
            String path = "npcs." + def.id;
            yaml.set(path + ".world", def.world);
            yaml.set(path + ".x", def.x);
            yaml.set(path + ".y", def.y);
            yaml.set(path + ".z", def.z);
            yaml.set(path + ".yaw", def.yaw);
            yaml.set(path + ".pitch", def.pitch);
            yaml.set(path + ".mythicMob", def.mythicMobInternalName);
            
            
            if (def.displayName != null) {
                yaml.set(path + ".displayName", def.displayName);
            }
            if (def.modelEngineModelId != null) {
                yaml.set(path + ".model", def.modelEngineModelId);
            }

            // Save metadata
            if (!def.metadata.isEmpty()) {
                for (Map.Entry<String, String> entry : def.metadata.entrySet()) {
                    yaml.set(path + ".metadata." + entry.getKey(), entry.getValue());
                }
            }

            // Save flags
            yaml.set(path + ".flags.invulnerable", def.invulnerable);
            yaml.set(path + ".flags.silent", def.silent);
            yaml.set(path + ".flags.noAI", def.noAI);
            yaml.set(path + ".flags.lookAtPlayers", def.lookAtPlayers);
        }

        try {
            yaml.save(dataFile);
            plugin.getLogger().info("Saved " + definitions.size() + " NPC definition(s) to disk.");
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save npcs.yml: " + e.getMessage());
        }
    }

    /**
     * Add or update an NPC definition.
     */
    public void saveDefinition(NpcImpl npc) {
        NpcDefinition def = new NpcDefinition();
        def.id = npc.getId();
        def.world = npc.getLocation().getWorld();
        def.x = npc.getLocation().getX();
        def.y = npc.getLocation().getY();
        def.z = npc.getLocation().getZ();
        def.yaw = npc.getLocation().getYaw();
        def.pitch = npc.getLocation().getPitch();
        def.mythicMobInternalName = npc.getMythicMobInternalName();
        def.modelEngineModelId = npc.getModelEngineModelId();
        def.metadata.putAll(npc.getMetadata());
        def.displayName = npc.getDisplayName();
        def.invulnerable = npc.isInvulnerable();
        def.silent = npc.isSilent();
        def.noAI = npc.hasNoAI();
        def.lookAtPlayers = npc.shouldLookAtPlayers();

        definitions.put(npc.getId(), def);
    }

    /**
     * Remove an NPC definition.
     */
    public void removeDefinition(String id) {
        definitions.remove(id);
    }

    /**
     * Get all definitions.
     */
    public Collection<NpcDefinition> getAllDefinitions() {
        return Collections.unmodifiableCollection(definitions.values());
    }

    /**
     * Get a specific definition.
     */
    public Optional<NpcDefinition> getDefinition(String id) {
        return Optional.ofNullable(definitions.get(id));
    }

    /**
     * Internal storage class for NPC definitions.
     */
    public static class NpcDefinition {
        public String id;
        public String world;
        public double x, y, z;
        public float yaw, pitch;
        public String mythicMobInternalName;
        public String modelEngineModelId;
        public Map<String, String> metadata = new HashMap<>();
        public boolean invulnerable;
        public boolean silent;
        public boolean noAI;
        public String displayName;
        public boolean lookAtPlayers;

        public NpcLocation toLocation() {
            return new NpcLocation(world, x, y, z, yaw, pitch);
        }
    }
}
