package com.aethor.aethornpcs.plugin.service;

import com.aethor.aethornpcs.api.AethorNpcApi;
import com.aethor.aethornpcs.api.dto.Npc;
import com.aethor.aethornpcs.api.dto.NpcLocation;
import com.aethor.aethornpcs.api.dto.NpcSpawnRequest;
import com.aethor.aethornpcs.plugin.AethorNPCSPlugin;
import com.aethor.aethornpcs.plugin.adapter.ModelEngineAdapter;
import com.aethor.aethornpcs.plugin.adapter.MythicMobAdapter;
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
    private final PluginConfiguration config;
    private final NamespacedKey npcIdKey;

    public AethorNpcApiImpl(
            AethorNPCSPlugin plugin,
            NpcRegistry registry,
            NpcPersistence persistence,
            MythicMobAdapter mythicMobAdapter,
            ModelEngineAdapter modelEngineAdapter,
            PluginConfiguration config) {
        this.plugin = plugin;
        this.registry = registry;
        this.persistence = persistence;
        this.mythicMobAdapter = mythicMobAdapter;
        this.modelEngineAdapter = modelEngineAdapter;
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
     * Spawn entity for an NPC.
     */
    public Entity spawnEntity(NpcImpl npc) {
        Location location = toBukkitLocation(npc.getLocation());
        if (location == null) {
            plugin.getLogger().warning("World " + npc.getLocation().getWorld() + " not loaded for NPC " + npc.getId());
            return null;
        }

        // Spawn via MythicMobs
        Entity entity = mythicMobAdapter.spawnMob(npc.getMythicMobInternalName(), location, 1);
        if (entity == null) {
            plugin.getLogger().severe("Failed to spawn MythicMob " + npc.getMythicMobInternalName());
            return null;
        }

        // Apply flags
        entity.setInvulnerable(npc.isInvulnerable());
        entity.setSilent(npc.isSilent());
        
        if (entity instanceof org.bukkit.entity.Mob) {
            org.bukkit.entity.Mob mob = (org.bukkit.entity.Mob) entity;
            if (npc.hasNoAI()) {
                mob.setAware(false);
            }
        }

        // Tag entity
        entity.addScoreboardTag("aethornpcs:npc");
        
        // Set display name if configured
        if (npc.getDisplayName() != null && !npc.getDisplayName().isEmpty()) {
            MiniMessage miniMessage = MiniMessage.miniMessage();
            Component displayName = miniMessage.deserialize(npc.getDisplayName());
            entity.customName(displayName);
            entity.setCustomNameVisible(true);
        }

        // ity.getPersistentDataContainer().set(npcIdKey, PersistentDataType.STRING, npc.getId());

        // Attach model if configured
        if (npc.getModelEngineModelId() != null && modelEngineAdapter.isAvailable()) {
            modelEngineAdapter.attachModel(entity, npc.getModelEngineModelId());
        }

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
            // Remove model if present
            if (modelEngineAdapter.isAvailable()) {
                modelEngineAdapter.removeModel(entity);
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
}
