package com.aethor.aethornpcs.api.dto;

import java.util.Map;
import java.util.UUID;

/**
 * Represents an NPC in the AethorNPCS system.
 * This is a read-only view of an NPC's current state.
 */
public interface Npc {

    /**
     * Get the unique identifier of this NPC.
     * 
     * @return NPC identifier
     */
    String getId();

    /**
     * Get the spawned entity's UUID.
     * 
     * @return Entity UUID, or null if not currently spawned
     */
    UUID getEntityUuid();

    /**
     * Get the NPC's location.
     * 
     * @return Location information
     */
    NpcLocation getLocation();

    /**
     * Get the MythicMobs internal name used for this NPC.
     * 
     * @return MythicMobs mob name
     */
    String getMythicMobInternalName();

    /**
     * Get the ModelEngine model ID, if configured.
     * 
     * @return ModelEngine model ID, or null
     */
    String getModelEngineModelId();

    /**
     * Get custom metadata associated with this NPC.
     * Quest plugins can store arbitrary string data here.
     * 
     * @return Metadata map (immutable)
     */
    Map<String, String> getMetadata();

    /**
     * Check if the NPC is currently spawned.
     * 
     * @return true if spawned
     */
    boolean isSpawned();

    /**
     * Check if the NPC is invulnerable.
     * 
     * @return true if invulnerable
     */
    boolean isInvulnerable();

    /**
     * Check if the NPC is silent (no sounds).
     * 
     * @return true if silent
     */
    boolean isSilent();

    /**
     * Check if the NPC has AI disabled.
     * 
     * @return true if no AI
     */
    boolean hasNoAI();

    /**
     * Check if the NPC should look at nearby players.
     * 
     * @return true if should look at players
     */
    boolean shouldLookAtPlayers();

    /**
     * Get the display name shown above the NPC.
     * Supports MiniMessage formatting including hex colors.
     * 
     * @return Display name, or null if not set
     */
    String getDisplayName();
}
