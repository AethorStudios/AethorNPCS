package com.aethor.aethornpcs.api.dto;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Request object for spawning a new NPC.
 */
public final class NpcSpawnRequest {

    private final String id;
    private final NpcLocation location;
    private final String mythicMobInternalName;
    private final String modelEngineModelId;
    private final Map<String, String> metadata;
    private final boolean invulnerable;
    private final boolean silent;
    private final boolean noAI;
    private final boolean lookAtPlayers;
    private final String displayName;

    private NpcSpawnRequest(Builder builder) {
        this.id = Objects.requireNonNull(builder.id, "id cannot be null");
        this.location = Objects.requireNonNull(builder.location, "location cannot be null");
        this.mythicMobInternalName = Objects.requireNonNull(builder.mythicMobInternalName, "mythicMobInternalName cannot be null");
        this.modelEngineModelId = builder.modelEngineModelId;
        this.metadata = Collections.unmodifiableMap(new HashMap<>(builder.metadata));
        this.invulnerable = builder.invulnerable;
        this.silent = builder.silent;
        this.noAI = builder.noAI;
        this.lookAtPlayers = builder.lookAtPlayers;
        this.displayName = builder.displayName;
    }

    public String getId() {
        return id;
    }

    public NpcLocation getLocation() {
        return location;
    }

    public String getMythicMobInternalName() {
        return mythicMobInternalName;
    }

    public String getModelEngineModelId() {
        return modelEngineModelId;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public boolean isInvulnerable() {
        return invulnerable;
    }

    public boolean isSilent() {
        return silent;
    }

    public boolean hasNoAI() {
        return noAI;
    }

    public boolean shouldLookAtPlayers() {
        return lookAtPlayers;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String id;
        private NpcLocation location;
        private String mythicMobInternalName;
        private String modelEngineModelId;
        private Map<String, String> metadata = new HashMap<>();
        private String displayName;
        private boolean invulnerable = true;
        private boolean silent = true;
        private boolean noAI = true;
        private boolean lookAtPlayers = true;

        private Builder() {}

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder location(NpcLocation location) {
            this.location = location;
            return this;
        }

        public Builder mythicMobInternalName(String mythicMobInternalName) {
            this.mythicMobInternalName = mythicMobInternalName;
            return this;
        }

        public Builder modelEngineModelId(String modelEngineModelId) {
            this.modelEngineModelId = modelEngineModelId;
            return this;
        }

        public Builder metadata(Map<String, String> metadata) {
            this.metadata = new HashMap<>(metadata);
            return this;
        }

        public Builder addMetadata(String key, String value) {
            this.metadata.put(key, value);
            return this;
        }

        public Builder invulnerable(boolean invulnerable) {
            this.invulnerable = invulnerable;
            return this;
        }

        public Builder silent(boolean silent) {
            this.silent = silent;
            return this;
        }

        public Builder noAI(boolean noAI) {
            this.noAI = noAI;
            return this;
        }

        public Builder displayName(String displayName) {
            this.displayName = displayName;
            return this;
        }

        public Builder lookAtPlayers(boolean lookAtPlayers) {
            this.lookAtPlayers = lookAtPlayers;
            return this;
        }

        public NpcSpawnRequest build() {
            return new NpcSpawnRequest(this);
        }
    }
}
