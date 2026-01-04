package com.aethor.aethornpcs.plugin.service;

import com.aethor.aethornpcs.api.AethorNpcApi;
import com.aethor.aethornpcs.api.dto.Npc;
import com.aethor.aethornpcs.api.dto.NpcLocation;
import com.aethor.aethornpcs.api.dto.NpcSpawnRequest;
import com.aethor.aethornpcs.plugin.AethorNPCSPlugin;
import com.aethor.aethornpcs.plugin.adapter.ModelEngineAdapter;
import com.aethor.aethornpcs.plugin.adapter.MythicMobAdapter;
import com.aethor.aethornpcs.plugin.integrations.HologramAdapter;
import com.aethor.aethornpcs.plugin.config.PluginConfiguration;
import com.aethor.aethornpcs.plugin.event.AethorNpcSpawnEvent;
import com.aethor.aethornpcs.plugin.event.AethorNpcDespawnEvent;
import com.aethor.aethornpcs.plugin.model.NpcImpl;
import com.aethor.aethornpcs.plugin.persistence.NpcPersistence;
import com.aethor.aethornpcs.plugin.registry.NpcRegistry;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.persistence.PersistentDataType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of the AethorNpcApi interface.
 */
public class AethorNpcApiImpl implements AethorNpcApi {

    private final AethorNPCSPlugin plugin;
    private final NpcRegistry registry;
    private final NpcPersistence persistence;
    private final MythicMobAdapter mythicMobAdapter;
    private final ModelEngineAdapter modelEngineAdapter;
    private final HologramAdapter hologramAdapter;
    private final PluginConfiguration config;
    private final NamespacedKey npcIdKey;

    public AethorNpcApiImpl(
            AethorNPCSPlugin plugin,
            NpcRegistry registry,
            NpcPersistence persistence,
            MythicMobAdapter mythicMobAdapter,
            ModelEngineAdapter modelEngineAdapter,
            HologramAdapter hologramAdapter,
            PluginConfiguration config) {
        this.plugin = plugin;
        this.registry = registry;
        this.persistence = persistence;
        this.mythicMobAdapter = mythicMobAdapter;
        this.modelEngineAdapter = modelEngineAdapter;
        this.hologramAdapter = hologramAdapter;
        this.config = config;
        this.npcIdKey = new NamespacedKey(plugin, "npcId");
    }

    @Override
    public Optional<Npc> getNpc(String id) {
        return registry.getNpc(id).map(npc -> (Npc) npc);
    }

    @Override
    public Optional<Npc> getNpcByEntity(UUID entityUuid) {
        return registry.getNpcByEntity(entityUuid).map(npc -> (Npc) npc);
    }

    @Override
    public Collection<Npc> getAllNpcs() {
        return registry.getAllNpcs().stream()
                .map(npc -> (Npc) npc)
                .collect(Collectors.toList());
    }

    @Override
    public Npc spawn(NpcSpawnRequest request) {
        if (registry.getNpc(request.getId()).isPresent()) {
            throw new IllegalArgumentException("NPC with ID " + request.getId() + " already exists");
        }

        // Create NPC instance
        NpcImpl npc = new NpcImpl(
                request.getId(),
                request.getLocation(),
                request.getMythicMobInternalName(),
                request.getModelEngineModelId(),
                request.getMetadata(),
                request.isInvulnerable(),
                request.isSilent(),
                request.hasNoAI(),
                request.shouldLookAtPlayers(),
                request.getDisplayName()
        );

        // Register NPC
        registry.register(npc);

        // Spawn entity
        Entity entity = spawnEntity(npc);
        if (entity == null) {
            registry.unregister(npc.getId());
            throw new IllegalStateException("Failed to spawn entity for NPC " + request.getId());
        }

        // Save to persistence
        persistence.saveDefinition(npc);
        persistence.save();

        // Fire event
        AethorNpcSpawnEvent event = new AethorNpcSpawnEvent(npc);
        Bukkit.getPluginManager().callEvent(event);

        plugin.getLogger().info("Spawned NPC " + npc.getId() + " at " + npc.getLocation());
        return npc;
    }

    @Override
    public boolean despawn(String id) {
        Optional<NpcImpl> npcOpt = registry.getNpc(id);
        if (npcOpt.isEmpty()) {
            return false;
        }

        NpcImpl npc = npcOpt.get();
        despawnEntity(npc);

        // Fire event
        AethorNpcDespawnEvent event = new AethorNpcDespawnEvent(npc);
        Bukkit.getPluginManager().callEvent(event);

        // Remove from registry and persistence
        registry.unregister(id);
        persistence.removeDefinition(id);
        persistence.save();

        plugin.getLogger().info("Despawned and removed NPC " + id);
        return true;
    }

    @Override
    public boolean respawn(String id) {
        Optional<NpcImpl> npcOpt = registry.getNpc(id);
        if (npcOpt.isEmpty()) {
            return false;
        }

        NpcImpl npc = npcOpt.get();
        
        // Despawn existing entity
        despawnEntity(npc);

        // Spawn new entity
        Entity entity = spawnEntity(npc);
        
        plugin.getLogger().info("Respawned NPC " + id);
        return entity != null;
    }

    @Override
    public boolean isNpc(UUID entityUuid) {
        return registry.isNpc(entityUuid);
    }

    /**
     * Despawn all NPCs (used on plugin disable).
     */
    public void despawnAll() {
        for (NpcImpl npc : registry.getAllNpcs()) {
            despawnEntity(npc);
        }
    }
    
    /**
     * Load and spawn all NPCs from persistence.
     * This should be called after the plugin is initialized.
     */
    public void loadAndSpawnAll() {
        int spawned = 0;
        int skipped = 0;
        int failed = 0;
        
        for (NpcPersistence.NpcDefinition def : persistence.getAllDefinitions()) {
            try {
                // Create NPC instance from definition
                NpcImpl npc = new NpcImpl(
                    def.id,
                    def.toLocation(),
                    def.mythicMobInternalName,
                    def.modelEngineModelId,
                    def.metadata,
                    def.invulnerable,
                    def.silent,
                    def.noAI,
                    def.lookAtPlayers,
                    def.displayName
                );
                
                // Register NPC first (so ChunkListener can find it)
                registry.register(npc);
                
                // Check if chunk is loaded
                Location location = toBukkitLocation(def.toLocation());
                if (location == null) {
                    plugin.getLogger().warning("World not loaded for NPC " + def.id);
                    failed++;
                    registry.unregister(def.id);
                    continue;
                }
                
                if (!location.getChunk().isLoaded()) {
                    plugin.debug("Chunk not loaded for NPC " + def.id + ", will spawn when chunk loads");
                    skipped++;
                    continue;
                }
                
                // Spawn entity if chunk is loaded
                Entity entity = spawnEntity(npc);
                if (entity == null) {
                    plugin.getLogger().warning("Failed to spawn entity for NPC " + def.id);
                    registry.unregister(def.id);
                    failed++;
                    continue;
                }
                
                spawned++;
                plugin.debug("Loaded and spawned NPC: " + def.id);
                
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to load NPC " + def.id + ": " + e.getMessage());
                e.printStackTrace();
                failed++;
            }
        }
        
        if (spawned > 0 || skipped > 0) {
            plugin.getLogger().info("Loaded NPCs: " + spawned + " spawned, " + skipped + " waiting for chunks" + 
                (failed > 0 ? ", " + failed + " failed" : ""));
        } else if (failed > 0) {
            plugin.getLogger().warning("Failed to load " + failed + " NPC(s)");
        }
    }
    
    /**
     * Cleanup orphaned holograms that don't belong to any active NPC.
     * @return The number of holograms removed
     */
    public int cleanupOrphanedHolograms() {
        if (!hologramAdapter.isEnabled()) {
            return 0;
        }
        
        // Get all entity UUIDs that are currently registered as NPCs
        Collection<UUID> activeEntityUuids = registry.getAllNpcs().stream()
            .filter(NpcImpl::isSpawned)
            .map(NpcImpl::getEntityUuid)
            .collect(Collectors.toSet());
        
        // Remove holograms that don't belong to any active NPC
        return hologramAdapter.removeOrphanedHolograms(activeEntityUuids);
    }

    /**
     * Spawn entity for an NPC.
     */
    public Entity spawnEntity(NpcImpl npc) {
        plugin.debug("Attempting to spawn NPC " + npc.getId() + " at " + npc.getLocation());
        
        Location location = toBukkitLocation(npc.getLocation());
        if (location == null) {
            plugin.getLogger().warning("World " + npc.getLocation().getWorld() + " not loaded for NPC " + npc.getId());
            return null;
        }

        plugin.debug("Spawning MythicMob: " + npc.getMythicMobInternalName() + " at " + location);
        
        // Spawn via MythicMobs
        Entity entity = mythicMobAdapter.spawnMob(npc.getMythicMobInternalName(), location, 1);
        if (entity == null) {
            plugin.getLogger().severe("Failed to spawn MythicMob '" + npc.getMythicMobInternalName() + "' for NPC " + npc.getId() + " - Check if the MythicMob exists!");
            return null;
        }
        
        plugin.debug("Successfully spawned entity " + entity.getUniqueId() + " for NPC " + npc.getId());

        // Apply flags
        entity.setInvulnerable(npc.isInvulnerable());
        entity.setSilent(npc.isSilent());
        
        // Disable collision using scoreboard team to prevent players from pushing the NPC
        try {
            org.bukkit.scoreboard.Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
            org.bukkit.scoreboard.Team team = scoreboard.getTeam("aethornpcs_nocollide");
            
            if (team == null) {
                team = scoreboard.registerNewTeam("aethornpcs_nocollide");
                team.setOption(org.bukkit.scoreboard.Team.Option.COLLISION_RULE, 
                              org.bukkit.scoreboard.Team.OptionStatus.NEVER);
            }
            
            team.addEntry(entity.getUniqueId().toString());
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to disable collision for NPC " + npc.getId() + ": " + e.getMessage());
        }
        
        if (entity instanceof org.bukkit.entity.Mob) {
            org.bukkit.entity.Mob mob = (org.bukkit.entity.Mob) entity;
            if (npc.hasNoAI()) {
                mob.setAware(false);
            }
        }

        // Tag entity
        entity.addScoreboardTag("aethornpcs:npc");
        
        // Attach model if configured (do this before name to avoid conflicts)
        if (npc.getModelEngineModelId() != null && modelEngineAdapter.isAvailable()) {
            modelEngineAdapter.attachModel(entity, npc.getModelEngineModelId());
        }
        
        // Create hologram for display name if configured
        if (npc.getDisplayName() != null && !npc.getDisplayName().isEmpty()) {
            if (hologramAdapter.isEnabled()) {
                hologramAdapter.createHologram(entity.getUniqueId(), entity, npc.getDisplayName());
                plugin.debug("Created hologram for NPC " + npc.getId() + " with display name: " + npc.getDisplayName());
            } else {
                // Fallback to Bukkit's custom name if DecentHolograms is not available
                MiniMessage miniMessage = MiniMessage.miniMessage();
                Component displayName = miniMessage.deserialize(npc.getDisplayName());
                entity.customName(displayName);
                entity.setCustomNameVisible(true);
                plugin.debug("Set Bukkit custom name for NPC " + npc.getId() + " (DecentHolograms not available)");
            }
        }

        // entity.getPersistentDataContainer().set(npcIdKey, PersistentDataType.STRING, npc.getId());

        // Link entity to NPC
        registry.linkEntity(npc.getId(), entity.getUniqueId());

        plugin.debug("Spawned entity " + entity.getUniqueId() + " for NPC " + npc.getId());
        return entity;
    }

    /**
     * Despawn entity for an NPC.
     */
    public void despawnEntity(NpcImpl npc) {
        if (!npc.isSpawned()) {
            return;
        }

        UUID entityUuid = npc.getEntityUuid();
        Entity entity = Bukkit.getEntity(entityUuid);

        if (entity != null) {
            // Remove hologram if present
            if (hologramAdapter.isEnabled()) {
                hologramAdapter.removeHologram(entityUuid);
            }
            
            // Remove model if present
            if (modelEngineAdapter.isAvailable()) {
                modelEngineAdapter.removeModel(entity);
            }
            
            // Remove from no-collision team
            try {
                org.bukkit.scoreboard.Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
                org.bukkit.scoreboard.Team team = scoreboard.getTeam("aethornpcs_nocollide");
                if (team != null) {
                    team.removeEntry(entityUuid.toString());
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to remove entity from collision team: " + e.getMessage());
            }

            // Remove entity
            mythicMobAdapter.removeMob(entityUuid);
            entity.remove();
        }

        // Unlink entity
        registry.unlinkEntity(entityUuid);

        plugin.debug("Despawned entity " + entityUuid + " for NPC " + npc.getId());
    }

    /**
     * Convert NpcLocation to Bukkit Location.
     */
    private Location toBukkitLocation(NpcLocation npcLocation) {
        World world = Bukkit.getWorld(npcLocation.getWorld());
        if (world == null) {
            return null;
        }
        return new Location(world, npcLocation.getX(), npcLocation.getY(), npcLocation.getZ(),
                npcLocation.getYaw(), npcLocation.getPitch());
    }

    /**
     * Get the NPC ID key for PDC.
     */
    public NamespacedKey getNpcIdKey() {
        return npcIdKey;
    }
    
    /**
     * Create a new NPC from a spawn request.
     * This is a convenience method that combines spawn() and persistence.
     */
    public Npc createNpc(NpcSpawnRequest request) {
        Npc npc = spawn(request);
        persistence.save();
        return npc;
    }
    
    /**
     * Remove an NPC by ID.
     */
    public void removeNpc(String id) {
        Optional<NpcImpl> npcOpt = registry.getNpc(id);
        if (!npcOpt.isPresent()) {
            throw new IllegalArgumentException("NPC with ID " + id + " does not exist");
        }
        
        NpcImpl npc = npcOpt.get();
        
        // Despawn if spawned
        if (npc.isSpawned()) {
            despawn(id);
        }
        
        // Unregister
        registry.unregister(id);
        persistence.save();
    }
    
    /**
     * Move an NPC to a new location.
     */
    public void moveNpc(String id, String world, double x, double y, double z, float yaw, float pitch) {
        Optional<NpcImpl> npcOpt = registry.getNpc(id);
        if (!npcOpt.isPresent()) {
            throw new IllegalArgumentException("NPC with ID " + id + " does not exist");
        }
        
        NpcImpl npc = npcOpt.get();
        NpcLocation newLocation = new NpcLocation(world, x, y, z, yaw, pitch);
        
        // Update location
        npc.setLocation(newLocation);
        
        // Respawn if currently spawned
        if (npc.isSpawned()) {
            despawnEntity(npc);
            Entity entity = spawnEntity(npc);
            registry.linkEntity(npc.getId(), entity.getUniqueId());
        }
        
        persistence.save();
    }
    
    /**
     * Respawn an NPC (despawn and spawn again at same location).
     */
    public void respawnNpc(String id) {
        Optional<NpcImpl> npcOpt = registry.getNpc(id);
        if (!npcOpt.isPresent()) {
            throw new IllegalArgumentException("NPC with ID " + id + " does not exist");
        }
        
        NpcImpl npc = npcOpt.get();
        
        // Despawn if spawned
        if (npc.isSpawned()) {
            despawnEntity(npc);
        }
        
        // Spawn again
        Entity entity = spawnEntity(npc);
        registry.linkEntity(npc.getId(), entity.getUniqueId());
    }
}
