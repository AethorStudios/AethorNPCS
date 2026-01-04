package com.aethor.aethornpcs.plugin.gui.screen;

import com.aethor.aethornpcs.plugin.gui.GuiManager;
import com.aethor.aethornpcs.plugin.gui.GuiScreen;
import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.generator.blueprint.ModelBlueprint;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * GUI for selecting a ModelEngine model
 */
public class ModelSelectorGui extends GuiScreen {
    
    private final GuiManager guiManager;
    private final Consumer<String> onSelect;
    private final GuiScreen previousScreen;
    private int page = 0;
    private static final int MODELS_PER_PAGE = 45;
    
    public ModelSelectorGui(GuiManager guiManager, Consumer<String> onSelect, GuiScreen previousScreen) {
        super(6, "§6§lSelect Model");
        this.guiManager = guiManager;
        this.onSelect = onSelect;
        this.previousScreen = previousScreen;
    }
    
    @Override
    protected void populate() {
        List<String> modelIds = new ArrayList<>();
        modelIds.add("§7§oNone"); // Add option for no model
        
        try {
            modelIds.addAll(
                ModelEngineAPI.getAPI().getModelRegistry().getKeys().stream()
                    .sorted()
                    .collect(Collectors.toList())
            );
        } catch (Exception e) {
            // ModelEngine not loaded or error
        }
        
        int totalPages = (int) Math.ceil(modelIds.size() / (double) MODELS_PER_PAGE);
        
        // Display models for current page
        int startIdx = page * MODELS_PER_PAGE;
        int endIdx = Math.min(startIdx + MODELS_PER_PAGE, modelIds.size());
        
        for (int i = startIdx; i < endIdx; i++) {
            String modelId = modelIds.get(i);
            int slot = i - startIdx;
            setItem(slot, createModelItem(modelId));
        }
        
        // Navigation buttons
        if (page > 0) {
            setItem(48, createButton(Material.ARROW, "§aPrevious Page"));
        }
        
        if (page < totalPages - 1) {
            setItem(50, createButton(Material.ARROW, "§aNext Page"));
        }
        
        // Back button
        setItem(49, createButton(Material.BARRIER, "§cBack"));
        
        // Page indicator
        setItem(45, createButton(
            Material.PAPER,
            "§ePage " + (page + 1) + " / " + Math.max(1, totalPages),
            "§7Total Models: §f" + (modelIds.size() - 1)
        ));
    }
    
    @Override
    public void handleClick(Player player, int slot, ClickType clickType) {
        if (slot == 48 && page > 0) {
            page--;
            refresh();
        } else if (slot == 50) {
            page++;
            refresh();
        } else if (slot == 49) {
            if (previousScreen != null) {
                guiManager.openScreen(player, previousScreen);
            } else {
                guiManager.closeScreen(player);
            }
        } else if (slot >= 0 && slot < MODELS_PER_PAGE) {
            // Get the model at this position
            List<String> modelIds = new ArrayList<>();
            modelIds.add("§7§oNone");
            
            try {
                modelIds.addAll(
                    ModelEngineAPI.getAPI().getModelRegistry().getKeys().stream()
                        .sorted()
                        .collect(Collectors.toList())
                );
            } catch (Exception e) {
                return;
            }
            
            int modelIdx = (page * MODELS_PER_PAGE) + slot;
            if (modelIdx < modelIds.size()) {
                String selectedModel = modelIds.get(modelIdx);
                
                // Pass null if "None" was selected
                if (selectedModel.equals("§7§oNone")) {
                    onSelect.accept(null);
                } else {
                    onSelect.accept(selectedModel);
                }
                
                if (previousScreen != null) {
                    guiManager.openScreen(player, previousScreen);
                } else {
                    guiManager.closeScreen(player);
                }
            }
        }
    }
    
    private ItemStack createModelItem(String modelId) {
        Material material = modelId.equals("§7§oNone") ? Material.BARRIER : Material.ARMOR_STAND;
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        
        meta.displayName(Component.text("§e§l" + modelId).decoration(TextDecoration.ITALIC, false));
        
        if (modelId.equals("§7§oNone")) {
            meta.lore(List.of(
                Component.text("§7No ModelEngine model").decoration(TextDecoration.ITALIC, false)
            ));
        } else {
            meta.lore(List.of(
                Component.text("§7Click to select this model").decoration(TextDecoration.ITALIC, false)
            ));
        }
        
        item.setItemMeta(meta);
        return item;
    }
    
    private ItemStack createButton(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        
        meta.displayName(Component.text(name).decoration(TextDecoration.ITALIC, false));
        
        if (lore.length > 0) {
            meta.lore(List.of(
                Arrays.stream(lore)
                    .map(line -> Component.text(line).decoration(TextDecoration.ITALIC, false))
                    .toArray(Component[]::new)
            ));
        }
        
        item.setItemMeta(meta);
        return item;
    }
}
