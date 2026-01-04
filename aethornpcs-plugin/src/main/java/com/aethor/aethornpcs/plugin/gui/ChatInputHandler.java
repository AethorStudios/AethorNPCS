package com.aethor.aethornpcs.plugin.gui;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Handles chat input from players for GUI text entry
 */
public class ChatInputHandler implements Listener {
    
    private final Plugin plugin;
    private final GuiManager guiManager;
    private final Map<UUID, PendingInput> pendingInputs = new HashMap<>();
    
    public ChatInputHandler(Plugin plugin, GuiManager guiManager) {
        this.plugin = plugin;
        this.guiManager = guiManager;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
    
    /**
     * Request chat input from a player
     */
    public void requestInput(Player player, Consumer<String> callback, GuiScreen returnScreen) {
        pendingInputs.put(player.getUniqueId(), new PendingInput(callback, returnScreen));
    }
    
    /**
     * Cancel pending input for a player
     */
    public void cancelInput(Player player) {
        PendingInput pending = pendingInputs.remove(player.getUniqueId());
        if (pending != null && pending.returnScreen != null) {
            guiManager.openScreen(player, pending.returnScreen);
        }
    }
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        PendingInput pending = pendingInputs.remove(player.getUniqueId());
        
        if (pending != null) {
            event.setCancelled(true);
            String message = event.getMessage();
            
            // Check for cancellation
            if (message.equalsIgnoreCase("cancel") || message.equalsIgnoreCase("exit")) {
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    if (pending.returnScreen != null) {
                        guiManager.openScreen(player, pending.returnScreen);
                    }
                });
                return;
            }
            
            // Process input on main thread
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                pending.callback.accept(message);
            });
        }
    }
    
    private static class PendingInput {
        final Consumer<String> callback;
        final GuiScreen returnScreen;
        
        PendingInput(Consumer<String> callback, GuiScreen returnScreen) {
            this.callback = callback;
            this.returnScreen = returnScreen;
        }
    }
}
