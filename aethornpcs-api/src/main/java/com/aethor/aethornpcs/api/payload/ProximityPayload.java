package com.aethor.aethornpcs.api.payload;

import java.util.Objects;
import java.util.UUID;

/**
 * Payload for NPC proximity events.
 * Fired when a player enters or exits the proximity radius of an NPC.
 */
public final class ProximityPayload {

    private final UUID playerUuid;
    private final String npcId;
    private final UUID entityUuid;
    private final ProximityAction action;
    private final double distance;

    public ProximityPayload(UUID playerUuid, String npcId, UUID entityUuid, ProximityAction action, double distance) {
        this.playerUuid = Objects.requireNonNull(playerUuid, "playerUuid cannot be null");
        this.npcId = Objects.requireNonNull(npcId, "npcId cannot be null");
        this.entityUuid = Objects.requireNonNull(entityUuid, "entityUuid cannot be null");
        this.action = Objects.requireNonNull(action, "action cannot be null");
        this.distance = distance;
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

    public ProximityAction getAction() {
        return action;
    }

    public double getDistance() {
        return distance;
    }

    public enum ProximityAction {
        ENTER,
        EXIT
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProximityPayload that = (ProximityPayload) o;
        return Double.compare(that.distance, distance) == 0 &&
               playerUuid.equals(that.playerUuid) &&
               npcId.equals(that.npcId) &&
               entityUuid.equals(that.entityUuid) &&
               action == that.action;
    }

    @Override
    public int hashCode() {
        return Objects.hash(playerUuid, npcId, entityUuid, action, distance);
    }

    @Override
    public String toString() {
        return String.format("ProximityPayload{player=%s, npc=%s, action=%s, distance=%.2f}",
                playerUuid, npcId, action, distance);
    }
}
