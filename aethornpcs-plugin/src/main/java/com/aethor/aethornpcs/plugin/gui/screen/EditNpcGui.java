package com.aethor.aethornpcs.plugin.gui.screen;

import com.aethor.aethornpcs.api.dto.Npc;
import com.aethor.aethornpcs.plugin.AethorNPCSPlugin;
import com.aethor.aethornpcs.plugin.gui.GuiManager;
import com.aethor.aethornpcs.plugin.gui.GuiScreen;
import com.aethor.aethornpcs.plugin.service.AethorNpcApiImpl;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;

/**
 * GUI for editing an existing NPC
 */
public class EditNpcGui extends GuiScreen {
    
    private final GuiManager guiManager;
    private final AethorNpcApiImpl apiImpl;
    private final Npc npc;
    
    public EditNpcGui(GuiManager guiManager, Npc npc) {
        super(4, "§6§lEdit NPC: §f" + npc.getId());
        this.guiManager = guiManager;
        this.apiImpl = (AethorNpcApiImpl) AethorNPCSPlugin.getInstance().getApiImpl();
        this.npc = npc;
    }
    
    @Override
    protected void populate() {
        // NPC info display
        setItem(4, createInfoItem(
            Material.NAME_TAG,
            "§e§l" + npc.getId(),
            "§7MythicMob: §f" + npc.getMythicMobInternalName(),
            npc.getModelEngineModelId() != null ? "§7Model: §f" + npc.getModelEngineModelId() : "",
            npc.getDisplayName() != null ? "§7Display: " + npc.getDisplayName() : ""
        ));
        
        // Move NPC button
        setItem(11, createButton(
            Material.ENDER_PEARL,
            "§b§lMove to Me",
            "§7Teleport this NPC to",
            "§7your current location"
        ));
        
        // Respawn NPC button
        setItem(13, createButton(
            Material.TOTEM_OF_UNDYING,
            "§a§lRespawn",
            "§7Despawn and respawn",
            "§7this NPC"
        ));
        
        // Delete NPC button
        setItem(15, createButton(
            Material.TNT,
            "§c§lDelete NPC",
            "§7Permanently remove",
            "§7this NPC",
            "",
            "§c§lShift-click to confirm"
        ));
        
        // Teleport to NPC button
        setItem(20, createButton(
            Material.COMPASS,
            "§e§lTeleport to NPC",
            "§7Go to this NPC's location"
        ));
        
        // Change MythicMob button  
        setItem(22, createButton(
            Material.SPAWNER,
            "§a§lChange MythicMob",
            "§7Current: §f" + npc.getMythicMobInternalName(),
            "",
            "§7Click to select new MythicMob"
        ));
        
        // Change Model button
        setItem(24, createButton(
            Material.ARMOR_STAND,
            "§b§lChange Model",
            npc.getModelEngineModelId() != null 
                ? "§7Current: §f" + npc.getModelEngineModelId()
                : "§7Current: §7§oNone",
            "",
            "§7Click to select new model"
        ));
        
        // Back button
        setItem(31, createButton(
            Material.BARRIER,
            "§c§lBack",
            "§7Return to NPC list"
        ));
    }
    
    @Override
    public void handleClick(Player player, int slot, ClickType clickType) {
        switch (slot) {
            case 11: // Move to player
                moveNpc(player);
                break;
                
            case 13: // Respawn
                respawnNpc(player);
                break;
                
            case 15: // Delete
                if (clickType.isShiftClick()) {
                    deleteNpc(player);
                } else {
                    player.sendMessage(Component.text("Shift-click to confirm deletion", NamedTextColor.YELLOW));
                }
                break;
                
            case 20: // Teleport to NPC
                teleportToNpc(player);
                break;
                
            case 22: // Change MythicMob
                player.sendMessage(Component.text("Feature coming soon: Edit MythicMob", NamedTextColor.YELLOW));
                break;
                
            case 24: // Change Model
                player.sendMessage(Component.text("Feature coming soon: Edit Model", NamedTextColor.YELLOW));
                break;
                
            case 31: // Back
                guiManager.openScreen(player, new NpcListGui(guiManager));
                break;
        }
    }
    
    private void moveNpc(Player player) {
        Location loc = player.getLocation();
        
        try {
            apiImpl.moveNpc(npc.getId(), loc.getWorld().getName(), loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
            player.sendMessage(Component.text("✔ Moved NPC to your location", NamedTextColor.GREEN));
            guiManager.closeScreen(player);
        } catch (Exception e) {
            player.sendMessage(Component.text("✘ Failed to move NPC: " + e.getMessage(), NamedTextColor.RED));
        }
    }
    
    private void respawnNpc(Player player) {
        try {
            apiImpl.respawnNpc(npc.getId());
            player.sendMessage(Component.text("✔ Respawned NPC", NamedTextColor.GREEN));
            guiManager.closeScreen(player);
        } catch (Exception e) {
            player.sendMessage(Component.text("✘ Failed to respawn NPC: " + e.getMessage(), NamedTextColor.RED));
        }
    }
    
    private void deleteNpc(Player player) {
        try {
            apiImpl.removeNpc(npc.getId());
            player.sendMessage(Component.text("✔ Deleted NPC: ", NamedTextColor.GREEN)
                .append(Component.text(npc.getId(), NamedTextColor.WHITE)));
            guiManager.openScreen(player, new NpcListGui(guiManager));
        } catch (Exception e) {
            player.sendMessage(Component.text("✘ Failed to delete NPC: " + e.getMessage(), NamedTextColor.RED));
        }
    }
    
    private void teleportToNpc(Player player) {
        try {
            Location loc = new Location(
                player.getServer().getWorld(npc.getLocation().getWorld()),
                npc.getLocation().getX(),
                npc.getLocation().getY(),
                npc.getLocation().getZ(),
                npc.getLocation().getYaw(),
                npc.getLocation().getPitch()
            );
            
            player.teleport(loc);
            player.sendMessage(Component.text("✔ Teleported to NPC", NamedTextColor.GREEN));
            guiManager.closeScreen(player);
        } catch (Exception e) {
            player.sendMessage(Component.text("✘ Failed to teleport: " + e.getMessage(), NamedTextColor.RED));
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
    
    private ItemStack createButton(Material material, String name, String... lore) {
        return createInfoItem(material, name, lore);
    }
}
