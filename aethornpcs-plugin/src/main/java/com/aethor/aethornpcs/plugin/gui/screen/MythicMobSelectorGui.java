package com.aethor.aethornpcs.plugin.gui.screen;

import com.aethor.aethornpcs.plugin.gui.GuiManager;
import com.aethor.aethornpcs.plugin.gui.GuiScreen;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.mobs.MobExecutor;
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
 * GUI for selecting a MythicMob
 */
public class MythicMobSelectorGui extends GuiScreen {
    
    private final GuiManager guiManager;
    private final Consumer<String> onSelect;
    private final GuiScreen previousScreen;
    private int page = 0;
    private static final int MOBS_PER_PAGE = 45;
    
    public MythicMobSelectorGui(GuiManager guiManager, Consumer<String> onSelect, GuiScreen previousScreen) {
        super(6, "§6§lSelect MythicMob");
        this.guiManager = guiManager;
        this.onSelect = onSelect;
        this.previousScreen = previousScreen;
    }
    
    @Override
    protected void populate() {
        List<String> mobIds = new ArrayList<>();
        
        try {
            MobExecutor mobManager = MythicBukkit.inst().getMobManager();
            mobIds = mobManager.getMobNames().stream()
                .sorted()
                .collect(Collectors.toList());
        } catch (Exception e) {
            // MythicMobs not loaded or error
        }
        
        int totalPages = (int) Math.ceil(mobIds.size() / (double) MOBS_PER_PAGE);
        
        // Display mobs for current page
        int startIdx = page * MOBS_PER_PAGE;
        int endIdx = Math.min(startIdx + MOBS_PER_PAGE, mobIds.size());
        
        for (int i = startIdx; i < endIdx; i++) {
            String mobId = mobIds.get(i);
            int slot = i - startIdx;
            setItem(slot, createMobItem(mobId));
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
            "§7Total MythicMobs: §f" + mobIds.size()
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
        } else if (slot >= 0 && slot < MOBS_PER_PAGE) {
            // Get the mob at this position
            List<String> mobIds = new ArrayList<>();
            try {
                MobExecutor mobManager = MythicBukkit.inst().getMobManager();
                mobIds = mobManager.getMobNames().stream()
                    .sorted()
                    .collect(Collectors.toList());
            } catch (Exception e) {
                return;
            }
            
            int mobIdx = (page * MOBS_PER_PAGE) + slot;
            if (mobIdx < mobIds.size()) {
                String selectedMob = mobIds.get(mobIdx);
                onSelect.accept(selectedMob);
                
                if (previousScreen != null) {
                    guiManager.openScreen(player, previousScreen);
                } else {
                    guiManager.closeScreen(player);
                }
            }
        }
    }
    
    private ItemStack createMobItem(String mobId) {
        ItemStack item = new ItemStack(Material.SPAWNER);
        ItemMeta meta = item.getItemMeta();
        
        meta.displayName(Component.text("§e§l" + mobId).decoration(TextDecoration.ITALIC, false));
        meta.lore(List.of(
            Component.text("§7Click to select this MythicMob").decoration(TextDecoration.ITALIC, false)
        ));
        
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
