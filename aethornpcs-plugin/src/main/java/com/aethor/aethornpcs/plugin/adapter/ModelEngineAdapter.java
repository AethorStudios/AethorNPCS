package com.aethor.aethornpcs.plugin.adapter;

import com.aethor.aethornpcs.plugin.AethorNPCSPlugin;
import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.model.ActiveModel;
import com.ticxo.modelengine.api.model.ModeledEntity;
import org.bukkit.entity.Entity;

import java.util.Optional;

/**
 * Adapter for ModelEngine integration.
 * Handles all direct interactions with the ModelEngine API.
 */
public class ModelEngineAdapter {

    private final AethorNPCSPlugin plugin;
    private final boolean available;

    public ModelEngineAdapter(AethorNPCSPlugin plugin) {
        this.plugin = plugin;
        this.available = plugin.getServer().getPluginManager().getPlugin("ModelEngine") != null;
    }

    public boolean isAvailable() {
        return available;
    }

    /**
     * Attach a model to an entity.
     *
     * @param entity The entity to attach the model to
     * @param modelId The ModelEngine model ID
     * @return true if successful
     */
    public boolean attachModel(Entity entity, String modelId) {
        if (!available) {
            plugin.debug("Cannot attach model - ModelEngine not available");
            return false;
        }

        try {
            ModeledEntity modeledEntity = ModelEngineAPI.createModeledEntity(entity);
            if (modeledEntity == null) {
                plugin.getLogger().warning("Failed to create ModeledEntity for " + entity.getUniqueId());
                return false;
            }

            ActiveModel activeModel = ModelEngineAPI.createActiveModel(modelId);
            if (activeModel == null) {
                plugin.getLogger().warning("Model ID '" + modelId + "' not found in ModelEngine");
                return false;
            }

            modeledEntity.addModel(activeModel, true);
            
            // Set default idle animation if it exists
            if (activeModel.getAnimationHandler().getAnimation("idle") != null) {
                activeModel.getAnimationHandler().playAnimation("idle", 0, 0, 1, true);
            }

            plugin.debug("Attached model " + modelId + " to entity " + entity.getUniqueId());
            return true;

        } catch (Exception e) {
            plugin.getLogger().severe("Failed to attach model " + modelId + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Remove model from entity.
     */
    public void removeModel(Entity entity) {
        if (!available) {
            return;
        }

        try {
            ModeledEntity modeledEntity = ModelEngineAPI.getModeledEntity(entity.getUniqueId());
            if (modeledEntity != null) {
                ModelEngineAPI.removeModeledEntity(entity.getUniqueId());
                plugin.debug("Removed model from entity " + entity.getUniqueId());
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Error removing model: " + e.getMessage());
        }
    }

    /**
     * Get ModeledEntity for an entity.
     */
    public Optional<ModeledEntity> getModeledEntity(Entity entity) {
        if (!available) {
            return Optional.empty();
        }

        try {
            return Optional.ofNullable(ModelEngineAPI.getModeledEntity(entity.getUniqueId()));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * Check if a model exists.
     */
    public boolean modelExists(String modelId) {
        if (!available) {
            return false;
        }

        try {
            return ModelEngineAPI.getBlueprint(modelId) != null;
        } catch (Exception e) {
            return false;
        }
    }
}
