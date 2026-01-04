package com.aethor.aethornpcs.api;

import com.aethor.aethornpcs.api.dto.Npc;
import com.aethor.aethornpcs.api.dto.NpcSpawnRequest;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

/**
 * Main API interface for AethorNPCS.
 * Quest plugins should retrieve this service via Bukkit's ServicesManager.
 * 
 * Example usage:
 * <pre>
 * RegisteredServiceProvider&lt;AethorNpcApi&gt; provider = 
 *     Bukkit.getServicesManager().getRegistration(AethorNpcApi.class);
 * if (provider != null) {
 *     AethorNpcApi api = provider.getProvider();
 *     // Use API...
 * }
 * </pre>
 */
public interface AethorNpcApi {

    /**
     * Get an NPC by its unique identifier.
     * 
     * @param id The NPC identifier
     * @return Optional containing the NPC if found
     */
    Optional<Npc> getNpc(String id);

    /**
     * Get an NPC by its spawned entity UUID.
     * 
     * @param entityUuid The entity UUID
     * @return Optional containing the NPC if found
     */
    Optional<Npc> getNpcByEntity(UUID entityUuid);

    /**
     * Get all registered NPCs (both spawned and unspawned).
     * 
     * @return Collection of all NPCs
     */
    Collection<Npc> getAllNpcs();

    /**
     * Spawn a new NPC at the specified location.
     * 
     * @param request The spawn request containing all NPC details
     * @return The spawned NPC
     * @throws IllegalArgumentException if NPC with same ID already exists
     * @throws IllegalStateException if spawn fails
     */
    Npc spawn(NpcSpawnRequest request);

    /**
     * Despawn and remove an NPC.
     * 
     * @param id The NPC identifier
     * @return true if NPC was found and removed
     */
    boolean despawn(String id);

    /**
     * Respawn an existing NPC (useful after modifications).
     * 
     * @param id The NPC identifier
     * @return true if NPC was found and respawned
     */
    boolean respawn(String id);

    /**
     * Check if an entity is an AethorNPC.
     * 
     * @param entityUuid The entity UUID
     * @return true if the entity is managed by AethorNPCS
     */
    boolean isNpc(UUID entityUuid);
}
