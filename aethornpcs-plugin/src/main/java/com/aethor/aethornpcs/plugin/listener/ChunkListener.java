package com.aethor.aethornpcs.plugin.listener;

import com.aethor.aethornpcs.plugin.AethorNPCSPlugin;
import com.aethor.aethornpcs.plugin.model.NpcImpl;
import com.aethor.aethornpcs.plugin.persistence.NpcPersistence;
import com.aethor.aethornpcs.plugin.registry.NpcRegistry;
import com.aethor.aethornpcs.plugin.service.AethorNpcApiImpl;
import org.bukkit.Chunk;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

import java.util.Optional;

/**
 * Handles chunk load/unload events to spawn/despawn NPCs.
 */
public class ChunkListener implements Listener {

    private final AethorNPCSPlugin plugin;
    private final AethorNpcApiImpl api;
    private final NpcRegistry registry;

    public ChunkListener(AethorNPCSPlugin plugin, AethorNpcApiImpl api, NpcRegistry registry) {
        this.plugin = plugin;
        this.api = api;
        this.registry = registry;
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        Chunk chunk = event.getChunk();
        String world = chunk.getWorld().getName();
        int chunkX = chunk.getX();
        int chunkZ = chunk.getZ();

        plugin.debug("Chunk loaded: " + world + " [" + chunkX + ", " + chunkZ + "]");

        // Check all NPCs to see if any should spawn in this chunk
        for (NpcImpl npc : registry.getAllNpcs()) {
            // Skip if already spawned
            if (npc.isSpawned()) {
                continue;
            }

            // Check if NPC location is in this chunk
            if (!npc.getLocation().getWorld().equals(world)) {
                continue;
            }

            int npcChunkX = (int) Math.floor(npc.getLocation().getX() / 16);
            int npcChunkZ = (int) Math.floor(npc.getLocation().getZ() / 16);

            if (npcChunkX == chunkX && npcChunkZ == chunkZ) {
                plugin.debug("Spawning NPC " + npc.getId() + " in loaded chunk");
                Entity entity = api.spawnEntity(npc);
                if (entity != null) {
                    registry.linkEntity(npc.getId(), entity.getUniqueId());
                }
            }
        }
    }

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent event) {
        // Don't despawn NPCs on chunk unload - let them persist
        // This prevents NPCs from disappearing when chunks unload temporarily
        // They will respawn when chunks load via ChunkLoadEvent
        return;
        
        /* Old despawn logic - disabled
        // Only despawn if configured to do so
        if (!"DESPAWN_ON_UNLOAD".equals(plugin.getConfiguration().getChunkStrategy())) {
            return;
        }

        Chunk chunk = event.getChunk();
        plugin.debug("Chunk unloading: " + chunk.getWorld().getName() + " [" + chunk.getX() + ", " + chunk.getZ() + "]");

        // Find and despawn NPCs in this chunk
        for (Entity entity : chunk.getEntities()) {
            Optional<NpcImpl> npcOpt = registry.getNpcByEntity(entity.getUniqueId());
            if (npcOpt.isPresent()) {
                NpcImpl npc = npcOpt.get();
                plugin.debug("Despawning NPC " + npc.getId() + " due to chunk unload");
                api.despawnEntity(npc);
            }
        }
        */
    }
}
