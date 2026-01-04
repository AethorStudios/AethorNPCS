package com.aethor.aethornpcs.plugin.registry;

import com.aethor.aethornpcs.plugin.model.NpcImpl;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Runtime registry for tracking NPCs and their entities.
 */
public class NpcRegistry {

    private final Map<String, NpcImpl> npcById = new ConcurrentHashMap<>();
    private final Map<UUID, String> npcIdByEntityUuid = new ConcurrentHashMap<>();

    /**
     * Register an NPC.
     */
    public void register(NpcImpl npc) {
        npcById.put(npc.getId(), npc);
    }

    /**
     * Unregister an NPC.
     */
    public void unregister(String id) {
        NpcImpl npc = npcById.remove(id);
        if (npc != null && npc.getEntityUuid() != null) {
            npcIdByEntityUuid.remove(npc.getEntityUuid());
        }
    }

    /**
     * Get NPC by ID.
     */
    public Optional<NpcImpl> getNpc(String id) {
        return Optional.ofNullable(npcById.get(id));
    }

    /**
     * Get NPC by entity UUID.
     */
    public Optional<NpcImpl> getNpcByEntity(UUID entityUuid) {
        String id = npcIdByEntityUuid.get(entityUuid);
        return id != null ? getNpc(id) : Optional.empty();
    }

    /**
     * Get all NPCs.
     */
    public Collection<NpcImpl> getAllNpcs() {
        return Collections.unmodifiableCollection(npcById.values());
    }

    /**
     * Link entity UUID to NPC.
     */
    public void linkEntity(String npcId, UUID entityUuid) {
        npcIdByEntityUuid.put(entityUuid, npcId);
        getNpc(npcId).ifPresent(npc -> npc.setEntityUuid(entityUuid));
    }

    /**
     * Unlink entity from NPC.
     */
    public void unlinkEntity(UUID entityUuid) {
        String npcId = npcIdByEntityUuid.remove(entityUuid);
        if (npcId != null) {
            getNpc(npcId).ifPresent(npc -> npc.setEntityUuid(null));
        }
    }

    /**
     * Check if entity is an NPC.
     */
    public boolean isNpc(UUID entityUuid) {
        return npcIdByEntityUuid.containsKey(entityUuid);
    }

    /**
     * Clear all registrations.
     */
    public void clear() {
        npcById.clear();
        npcIdByEntityUuid.clear();
    }
}
