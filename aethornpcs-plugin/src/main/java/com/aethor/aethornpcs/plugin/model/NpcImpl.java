package com.aethor.aethornpcs.plugin.model;

import com.aethor.aethornpcs.api.dto.Npc;
import com.aethor.aethornpcs.api.dto.NpcLocation;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Implementation of the Npc interface.
 */
public class NpcImpl implements Npc {

    private final String id;
    private UUID entityUuid;
    private final NpcLocation location;
    private final String mythicMobInternalName;
    private final String modelEngineModelId;
    private final Map<String, String> metadata;
    private final boolean invulnerable;
    private final boolean silent;
    private final boolean noAI;
    private final boolean lookAtPlayers;
    private final String displayName;

    public NpcImpl(
            String id,
            NpcLocation location,
            String mythicMobInternalName,
            String modelEngineModelId,
            Map<String, String> metadata,
            boolean invulnerable,
            boolean silent,
            boolean noAI,
            boolean lookAtPlayers,
            String displayName) {
        this.id = id;
        this.location = location;
        this.mythicMobInternalName = mythicMobInternalName;
        this.modelEngineModelId = modelEngineModelId;
        this.metadata = new HashMap<>(metadata);
        this.invulnerable = invulnerable;
        this.silent = silent;
        this.noAI = noAI;
        this.displayName = displayName;
        this.lookAtPlayers = lookAtPlayers;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public UUID getEntityUuid() {
        return entityUuid;
    }

    public void setEntityUuid(UUID entityUuid) {
        this.entityUuid = entityUuid;
    }

    @Override
    public NpcLocation getLocation() {
        return location;
    }

    @Override
    public String getMythicMobInternalName() {
        return mythicMobInternalName;
    }

    @Override
    public String getModelEngineModelId() {
        return modelEngineModelId;
    }

    @Override
    public Map<String, String> getMetadata() {
        return Collections.unmodifiableMap(metadata);
    }

    @Override
    public boolean isSpawned() {
        return entityUuid != null;
    }

    @Override
    public boolean isInvulnerable() {
        return invulnerable;
    }

    @Override
    public boolean isSilent() {
        return silent;
    }

    @Override
    public boolean hasNoAI() {
        return noAI;
    }

    @Override
    public boolean shouldLookAtPlayers() {
        return lookAtPlayers;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }
}
