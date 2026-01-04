package com.aethor.aethornpcs.api.payload;

import java.util.Objects;
import java.util.UUID;

/**
 * Payload for NPC spawn events.
 */
public final class SpawnPayload {

    private final String npcId;
    private final UUID entityUuid;

    public SpawnPayload(String npcId, UUID entityUuid) {
        this.npcId = Objects.requireNonNull(npcId, "npcId cannot be null");
        this.entityUuid = Objects.requireNonNull(entityUuid, "entityUuid cannot be null");
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
        SpawnPayload that = (SpawnPayload) o;
        return npcId.equals(that.npcId) && entityUuid.equals(that.entityUuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(npcId, entityUuid);
    }

    @Override
    public String toString() {
        return String.format("SpawnPayload{npc=%s, entity=%s}", npcId, entityUuid);
    }
}
