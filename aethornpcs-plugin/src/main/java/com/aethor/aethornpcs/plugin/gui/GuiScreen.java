package com.aethor.aethornpcs.plugin.gui;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * Base class for all GUI screens
 */
public abstract class GuiScreen {
    
    protected final int size;
    protected final String title;
    protected Inventory inventory;
    
    public GuiScreen(int rows, String title) {
        this.size = rows * 9;
        this.title = title;
    }
    
    /**
     * Open this GUI for a player
     */
    public void open(Player player) {
        this.inventory = Bukkit.createInventory(null, size, title);
        populate();
        player.openInventory(inventory);
    }
    
    /**
     * Populate the inventory with items
     */
    protected abstract void populate();
    
    /**
     * Handle a click in this GUI
     */
    public abstract void handleClick(Player player, int slot, ClickType clickType);
    
    /**
     * Called when the GUI is closed
     */
    public void onClose(Player player) {
        // Override if needed
    }
    
    /**
     * Set an item in the inventory
     */
    protected void setItem(int slot, ItemStack item) {
        if (slot >= 0 && slot < size) {
            inventory.setItem(slot, item);
        }
    }
    
    /**
     * Clear the inventory
     */
    protected void clear() {
        inventory.clear();
    }
    
    /**
     * Refresh the GUI content
     */
    public void refresh() {
        clear();
        populate();
    }
}
