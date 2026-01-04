package com.aethor.aethornpcs.plugin.gui;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manages GUI screens and click events
 */
public class GuiManager implements Listener {
    
    private final Plugin plugin;
    private final Map<UUID, GuiScreen> openScreens = new HashMap<>();
    private final ChatInputHandler chatInputHandler;
    
    public GuiManager(Plugin plugin) {
        this.plugin = plugin;
        this.chatInputHandler = new ChatInputHandler(plugin, this);
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
    
    /**
     * Get the chat input handler
     */
    public ChatInputHandler getChatInputHandler() {
        return chatInputHandler;
    }
    
    /**
     * Open a GUI screen for a player
     */
    public void openScreen(Player player, GuiScreen screen) {
        screen.open(player);
        openScreens.put(player.getUniqueId(), screen);
    }
    
    /**
     * Get the currently open screen for a player
     */
    public GuiScreen getOpenScreen(Player player) {
        return openScreens.get(player.getUniqueId());
    }
    
    /**
     * Close and remove a player's GUI
     */
    public void closeScreen(Player player) {
        openScreens.remove(player.getUniqueId());
        player.closeInventory();
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        
        GuiScreen screen = openScreens.get(player.getUniqueId());
        if (screen == null) {
            return;
        }
        
        // Cancel all clicks in managed GUIs
        event.setCancelled(true);
        
        // Handle the click
        screen.handleClick(player, event.getSlot(), event.getClick());
    }
    
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) {
            return;
        }
        
        GuiScreen screen = openScreens.get(player.getUniqueId());
        if (screen != null) {
            screen.onClose(player);
            openScreens.remove(player.getUniqueId());
        }
    }
}
