package com.aethor.aethornpcs.plugin.listener;

import com.aethor.aethornpcs.api.payload.InteractPayload;
import com.aethor.aethornpcs.plugin.AethorNPCSPlugin;
import com.aethor.aethornpcs.plugin.event.AethorNpcInteractEvent;
import com.aethor.aethornpcs.plugin.model.NpcImpl;
import com.aethor.aethornpcs.plugin.registry.NpcRegistry;
import com.aethor.aethornpcs.plugin.service.AethorNpcApiImpl;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.persistence.PersistentDataType;

import java.util.Optional;

/**
 * Listens for player interactions with NPC entities.
 */
public class InteractionListener implements Listener {

    private final AethorNPCSPlugin plugin;
    private final AethorNpcApiImpl api;
    private final NpcRegistry registry;

    public InteractionListener(AethorNPCSPlugin plugin, AethorNpcApiImpl api, NpcRegistry registry) {
        this.plugin = plugin;
        this.api = api;
        this.registry = registry;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Entity entity = event.getRightClicked();
        
        // Check if entity is an NPC
        if (!isNpc(entity)) {
            return;
        }

        Optional<NpcImpl> npcOpt = registry.getNpcByEntity(entity.getUniqueId());
        if (npcOpt.isEmpty()) {
            return;
        }

        NpcImpl npc = npcOpt.get();
        Player player = event.getPlayer();

        // Create payload
        InteractPayload payload = new InteractPayload(
                player.getUniqueId(),
                npc.getId(),
                entity.getUniqueId(),
                InteractPayload.ClickType.RIGHT_CLICK
        );

        // Fire event
        AethorNpcInteractEvent npcEvent = new AethorNpcInteractEvent(player, npc, payload);
        Bukkit.getPluginManager().callEvent(npcEvent);

        // Cancel original event if NPC event was cancelled
        if (npcEvent.isCancelled()) {
            event.setCancelled(true);
        }

        plugin.debug("Player " + player.getName() + " right-clicked NPC " + npc.getId());
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        Entity entity = event.getEntity();
        
        // Check if entity is an NPC
        if (!isNpc(entity)) {
            return;
        }

        // Check if damager is a player
        if (!(event.getDamager() instanceof Player)) {
            return;
        }

        Optional<NpcImpl> npcOpt = registry.getNpcByEntity(entity.getUniqueId());
        if (npcOpt.isEmpty()) {
            return;
        }

        NpcImpl npc = npcOpt.get();
        Player player = (Player) event.getDamager();

        // Create payload
        InteractPayload payload = new InteractPayload(
                player.getUniqueId(),
                npc.getId(),
                entity.getUniqueId(),
                InteractPayload.ClickType.LEFT_CLICK
        );

        // Fire event
        AethorNpcInteractEvent npcEvent = new AethorNpcInteractEvent(player, npc, payload);
        Bukkit.getPluginManager().callEvent(npcEvent);

        // Always cancel damage to NPCs
        event.setCancelled(true);

        plugin.debug("Player " + player.getName() + " left-clicked NPC " + npc.getId());
    }

    private boolean isNpc(Entity entity) {
        // Check scoreboard tag
        if (entity.getScoreboardTags().contains("aethornpcs:npc")) {
            return true;
        }

        // Check PDC
        return entity.getPersistentDataContainer().has(api.getNpcIdKey(), PersistentDataType.STRING);
    }
}
