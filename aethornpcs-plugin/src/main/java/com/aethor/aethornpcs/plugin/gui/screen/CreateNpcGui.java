package com.aethor.aethornpcs.plugin.gui.screen;

import com.aethor.aethornpcs.api.dto.NpcLocation;
import com.aethor.aethornpcs.api.dto.NpcSpawnRequest;
import com.aethor.aethornpcs.plugin.AethorNPCSPlugin;
import com.aethor.aethornpcs.plugin.gui.GuiManager;
import com.aethor.aethornpcs.plugin.gui.GuiScreen;
import com.aethor.aethornpcs.plugin.service.AethorNpcApiImpl;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * GUI for creating a new NPC
 */
public class CreateNpcGui extends GuiScreen {
    
    private final GuiManager guiManager;
    private final AethorNpcApiImpl apiImpl;
    
    private String npcId;
    private String mythicMob;
    private String model;
    private String displayName;
    
    public CreateNpcGui(GuiManager guiManager, String presetId) {
        super(4, "§6§lCreate NPC");
        this.guiManager = guiManager;
        this.apiImpl = (AethorNpcApiImpl) AethorNPCSPlugin.getInstance().getApiImpl();
        this.npcId = presetId != null ? presetId : "npc_" + UUID.randomUUID().toString().substring(0, 8);
    }
    
    @Override
    protected void populate() {
        // NPC ID display/edit
        setItem(10, createInfoItem(
            Material.NAME_TAG,
            "§e§lNPC ID",
            "§f" + npcId,
            "",
            "§7This ID must be unique",
            "§8(Auto-generated)"
        ));
        
        // MythicMob selector
        setItem(12, createSelectorItem(
            Material.SPAWNER,
            "§a§lMythicMob",
            mythicMob != null ? "§f" + mythicMob : "§c§oNot selected",
            "",
            "§7Click to select a MythicMob",
            mythicMob == null ? "§c§lRequired" : ""
        ));
        
        // Model selector
        setItem(14, createSelectorItem(
            Material.ARMOR_STAND,
            "§b§lModelEngine Model",
            model != null ? "§f" + model : "§7§oNone",
            "",
            "§7Click to select a model",
            "§7(Optional)"
        ));
        
        // Display name
        setItem(16, createInfoItem(
            Material.PAPER,
            "§d§lDisplay Name",
            displayName != null ? displayName : "§7§oNone",
            "",
            "§7Use §e/npc setname §7in chat",
            "§7Supports MiniMessage formatting"
        ));
        
        // Create button
        boolean canCreate = mythicMob != null && !mythicMob.isEmpty();
        setItem(31, createButton(
            canCreate ? Material.EMERALD_BLOCK : Material.RED_STAINED_GLASS_PANE,
            canCreate ? "§a§l✔ Create NPC" : "§c§l✘ Cannot Create",
            canCreate ? "§7Click to create the NPC" : "§cMust select a MythicMob",
            canCreate ? "§7at your current location" : ""
        ));
        
        // Cancel button
        setItem(22, createButton(
            Material.BARRIER,
            "§c§lCancel",
            "§7Return to main menu"
        ));
    }
    
    @Override
    public void handleClick(Player player, int slot, ClickType clickType) {
        switch (slot) {
            case 12: // MythicMob selector
                guiManager.openScreen(player, new MythicMobSelectorGui(guiManager, selectedMob -> {
                    this.mythicMob = selectedMob;
                }, this));
                break;
                
            case 14: // Model selector
                guiManager.openScreen(player, new ModelSelectorGui(guiManager, selectedModel -> {
                    this.model = selectedModel;
                }, this));
                break;
                
            case 16: // Display name
                player.closeInventory();
                player.sendMessage(Component.text("Enter display name in chat:", NamedTextColor.YELLOW));
                player.sendMessage(Component.text("Supports: <#FF0000>, <gradient:red:blue>, <rainbow>, etc.", NamedTextColor.GRAY));
                player.sendMessage(Component.text("Type 'cancel' to abort", NamedTextColor.GRAY));
                
                // Request chat input
                guiManager.getChatInputHandler().requestInput(player, input -> {
                    this.displayName = input;
                    refresh();
                    guiManager.openScreen(player, this);
                }, this);
                break;
                
            case 31: // Create NPC
                if (mythicMob != null && !mythicMob.isEmpty()) {
                    createNpc(player);
                }
                break;
                
            case 22: // Cancel
                guiManager.openScreen(player, new MainMenuGui(guiManager));
                break;
        }
    }
    
    private void createNpc(Player player) {
        Location loc = player.getLocation();
        
        NpcLocation npcLoc = new NpcLocation(
            loc.getWorld().getName(),
            loc.getX(),
            loc.getY(),
            loc.getZ(),
            loc.getYaw(),
            loc.getPitch()
        );
        
        NpcSpawnRequest.Builder builder = NpcSpawnRequest.builder()
            .id(npcId)
            .mythicMobInternalName(mythicMob)
            .location(npcLoc);
        
        if (model != null && !model.isEmpty()) {
            builder.modelEngineModelId(model);
        }
        
        if (displayName != null && !displayName.isEmpty()) {
            builder.displayName(displayName);
        }
        
        try {
            apiImpl.createNpc(builder.build());
            player.sendMessage(Component.text("✔ Created NPC: ", NamedTextColor.GREEN)
                .append(Component.text(npcId, NamedTextColor.WHITE)));
            guiManager.closeScreen(player);
        } catch (Exception e) {
            player.sendMessage(Component.text("✘ Failed to create NPC: " + e.getMessage(), NamedTextColor.RED));
        }
    }
    
    private ItemStack createInfoItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        
        meta.displayName(Component.text(name).decoration(TextDecoration.ITALIC, false));
        
        if (lore.length > 0) {
            meta.lore(List.of(
                Arrays.stream(lore)
                    .filter(line -> !line.isEmpty())
                    .map(line -> Component.text(line).decoration(TextDecoration.ITALIC, false))
                    .toArray(Component[]::new)
            ));
        }
        
        item.setItemMeta(meta);
        return item;
    }
    
    private ItemStack createSelectorItem(Material material, String name, String... lore) {
        return createInfoItem(material, name, lore);
    }
    
    private ItemStack createButton(Material material, String name, String... lore) {
        return createInfoItem(material, name, lore);
    }
}
