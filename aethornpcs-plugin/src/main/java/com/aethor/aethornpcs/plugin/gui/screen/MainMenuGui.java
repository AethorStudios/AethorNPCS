package com.aethor.aethornpcs.plugin.gui.screen;

import com.aethor.aethornpcs.plugin.gui.GuiManager;
import com.aethor.aethornpcs.plugin.gui.GuiScreen;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;

/**
 * Main menu GUI for NPC management
 */
public class MainMenuGui extends GuiScreen {
    
    private final GuiManager guiManager;
    
    public MainMenuGui(GuiManager guiManager) {
        super(3, "§6§lAethorNPCS §8- Main Menu");
        this.guiManager = guiManager;
    }
    
    @Override
    protected void populate() {
        // Create NPC button
        setItem(11, createButton(
            Material.PLAYER_HEAD,
            "§a§lCreate NPC",
            "§7Click to create a new NPC",
            "§7at your current location"
        ));
        
        // List NPCs button
        setItem(13, createButton(
            Material.BOOK,
            "§e§lManage NPCs",
            "§7View and manage all NPCs",
            "§7Click to open the NPC list"
        ));
        
        // Settings button
        setItem(15, createButton(
            Material.COMPARATOR,
            "§b§lSettings",
            "§7Configure plugin settings",
            "§8(Coming soon)"
        ));
        
        // Close button
        setItem(22, createButton(
            Material.BARRIER,
            "§c§lClose",
            "§7Close this menu"
        ));
    }
    
    @Override
    public void handleClick(Player player, int slot, ClickType clickType) {
        switch (slot) {
            case 11: // Create NPC
                guiManager.openScreen(player, new CreateNpcGui(guiManager, null));
                break;
            case 13: // List NPCs
                guiManager.openScreen(player, new NpcListGui(guiManager));
                break;
            case 15: // Settings
                player.sendMessage(Component.text("Settings coming soon!", NamedTextColor.YELLOW));
                break;
            case 22: // Close
                guiManager.closeScreen(player);
                break;
        }
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
