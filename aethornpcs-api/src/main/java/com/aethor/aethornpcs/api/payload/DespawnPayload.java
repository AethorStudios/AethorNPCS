package com.aethor.aethornpcs.api.payload;

import java.util.Objects;
import java.util.UUID;

/**
 * Payload for NPC despawn events.
 */
public final class DespawnPayload {

    private final String npcId;
    private final UUID entityUuid;

    public DespawnPayload(String npcId, UUID entityUuid) {
        this.npcId = Objects.requireNonNull(npcId, "npcId cannot be null");
        this.entityUuid = entityUuid; // Can be null if never spawned
    }

    public String getNpcId() {
        return npcId;
    }

    public UUID getEntityUuid() {
        return entityUuid;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DespawnPayload that = (DespawnPayload) o;
        return npcId.equals(that.npcId) && Objects.equals(entityUuid, that.entityUuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(npcId, entityUuid);
    }

    @Override
    public String toString() {
        return String.format("DespawnPayload{npc=%s, entity=%s}", npcId, entityUuid);
    }
}
