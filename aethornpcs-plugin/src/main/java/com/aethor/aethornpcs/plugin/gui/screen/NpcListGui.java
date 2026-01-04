package com.aethor.aethornpcs.plugin.gui.screen;

import com.aethor.aethornpcs.api.dto.Npc;
import com.aethor.aethornpcs.plugin.AethorNPCSPlugin;
import com.aethor.aethornpcs.plugin.gui.GuiManager;
import com.aethor.aethornpcs.plugin.gui.GuiScreen;
import com.aethor.aethornpcs.plugin.registry.NpcRegistry;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * GUI showing all NPCs with pagination
 */
public class NpcListGui extends GuiScreen {
    
    private final GuiManager guiManager;
    private final NpcRegistry registry;
    private int page = 0;
    private static final int NPCS_PER_PAGE = 45;
    
    public NpcListGui(GuiManager guiManager) {
        super(6, "§6§lNPC List");
        this.guiManager = guiManager;
        this.registry = AethorNPCSPlugin.getInstance().getNpcRegistry();
    }
    
    @Override
    protected void populate() {
        List<Npc> allNpcs = new ArrayList<>(registry.getAllNpcs());
        int totalPages = (int) Math.ceil(allNpcs.size() / (double) NPCS_PER_PAGE);
        
        // Display NPCs for current page
        int startIdx = page * NPCS_PER_PAGE;
        int endIdx = Math.min(startIdx + NPCS_PER_PAGE, allNpcs.size());
        
        for (int i = startIdx; i < endIdx; i++) {
            Npc npc = allNpcs.get(i);
            int slot = i - startIdx;
            setItem(slot, createNpcItem(npc));
        }
        
        // Navigation buttons
        if (page > 0) {
            setItem(48, createButton(Material.ARROW, "§aPrevious Page"));
        }
        
        if (page < totalPages - 1) {
            setItem(50, createButton(Material.ARROW, "§aNext Page"));
        }
        
        // Back button
        setItem(49, createButton(Material.BARRIER, "§cBack to Menu"));
        
        // Page indicator
        setItem(45, createButton(
            Material.PAPER,
            "§ePage " + (page + 1) + " / " + Math.max(1, totalPages),
            "§7Total NPCs: §f" + allNpcs.size()
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
            guiManager.openScreen(player, new MainMenuGui(guiManager));
        } else if (slot >= 0 && slot < NPCS_PER_PAGE) {
            // Clicked an NPC item
            List<Npc> allNpcs = new ArrayList<>(registry.getAllNpcs());
            int npcIdx = (page * NPCS_PER_PAGE) + slot;
            
            if (npcIdx < allNpcs.size()) {
                Npc npc = allNpcs.get(npcIdx);
                guiManager.openScreen(player, new EditNpcGui(guiManager, npc));
            }
        }
    }
    
    private ItemStack createNpcItem(Npc npc) {
        ItemStack item = new ItemStack(Material.ARMOR_STAND);
        ItemMeta meta = item.getItemMeta();
        
        String displayName = npc.getDisplayName() != null && !npc.getDisplayName().isEmpty() 
            ? npc.getDisplayName() 
            : npc.getId();
        
        meta.displayName(Component.text("§e§l" + displayName).decoration(TextDecoration.ITALIC, false));
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("§7ID: §f" + npc.getId()).decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("§7MythicMob: §f" + npc.getMythicMobInternalName()).decoration(TextDecoration.ITALIC, false));
        
        if (npc.getModelEngineModelId() != null) {
            lore.add(Component.text("§7Model: §f" + npc.getModelEngineModelId()).decoration(TextDecoration.ITALIC, false));
        }
        
        lore.add(Component.empty());
        lore.add(Component.text("§aLeft-click §7to edit").decoration(TextDecoration.ITALIC, false));
        
        meta.lore(lore);
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
