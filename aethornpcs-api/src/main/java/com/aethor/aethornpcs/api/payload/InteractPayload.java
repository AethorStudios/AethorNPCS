package com.aethor.aethornpcs.api.payload;

import java.util.Objects;
import java.util.UUID;

/**
 * Payload for NPC interaction events.
 * Immutable data object representing an interaction between a player and an NPC.
 */
public final class InteractPayload {

    private final UUID playerUuid;
    private final String npcId;
    private final UUID entityUuid;
    private final ClickType clickType;

    public InteractPayload(UUID playerUuid, String npcId, UUID entityUuid, ClickType clickType) {
        this.playerUuid = Objects.requireNonNull(playerUuid, "playerUuid cannot be null");
        this.npcId = Objects.requireNonNull(npcId, "npcId cannot be null");
        this.entityUuid = Objects.requireNonNull(entityUuid, "entityUuid cannot be null");
        this.clickType = Objects.requireNonNull(clickType, "clickType cannot be null");
    }

    public UUID getPlayerUuid() {
        return playerUuid;
    }

    public String getNpcId() {
        return npcId;
    }

    public UUID getEntityUuid() {
        return entityUuid;
    }

    public ClickType getClickType() {
        return clickType;
    }

    public enum ClickType {
        LEFT_CLICK,
        RIGHT_CLICK
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InteractPayload that = (InteractPayload) o;
        return playerUuid.equals(that.playerUuid) &&
               npcId.equals(that.npcId) &&
               entityUuid.equals(that.entityUuid) &&
               clickType == that.clickType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(playerUuid, npcId, entityUuid, clickType);
    }

    @Override
    public String toString() {
        return String.format("InteractPayload{player=%s, npc=%s, entity=%s, click=%s}",
                playerUuid, npcId, entityUuid, clickType);
    }
}
