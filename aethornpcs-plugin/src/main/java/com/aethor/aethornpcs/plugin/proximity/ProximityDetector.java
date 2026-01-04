package com.aethor.aethornpcs.plugin.proximity;

import com.aethor.aethornpcs.api.payload.ProximityPayload;
import com.aethor.aethornpcs.plugin.AethorNPCSPlugin;
import com.aethor.aethornpcs.plugin.config.PluginConfiguration;
import com.aethor.aethornpcs.plugin.event.AethorNpcProximityEvent;
import com.aethor.aethornpcs.plugin.model.NpcImpl;
import com.aethor.aethornpcs.plugin.registry.NpcRegistry;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

/**
 * Detects when players enter or exit proximity of NPCs.
 */
public class ProximityDetector {

    private final AethorNPCSPlugin plugin;
    private final NpcRegistry registry;
    private final PluginConfiguration config;
    private final Map<UUID, Set<String>> playerNearNpcs = new HashMap<>();
    private BukkitTask task;

    public ProximityDetector(AethorNPCSPlugin plugin, NpcRegistry registry, PluginConfiguration config) {
        this.plugin = plugin;
        this.registry = registry;
        this.config = config;
    }

    public void start() {
        int period = config.getProximityPeriodTicks();
        
        task = Bukkit.getScheduler().runTaskTimer(plugin, this::checkProximity, period, period);
        plugin.getLogger().info("Proximity detector started (period: " + period + " ticks, radius: " + config.getProximityRadius() + ")");
    }

    public void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
        playerNearNpcs.clear();
    }

    private void checkProximity() {
        double radius = config.getProximityRadius();
        double radiusSquared = radius * radius;

        Map<UUID, Set<String>> currentProximity = new HashMap<>();

        // Check each spawned NPC
        for (NpcImpl npc : registry.getAllNpcs()) {
            if (!npc.isSpawned()) {
                continue;
            }

            Entity entity = Bukkit.getEntity(npc.getEntityUuid());
            if (entity == null || !entity.isValid()) {
                continue;
            }

            Location npcLocation = entity.getLocation();

            // Check each online player
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (!player.getWorld().equals(entity.getWorld())) {
                    continue;
                }

                double distanceSquared = player.getLocation().distanceSquared(npcLocation);

                if (distanceSquared <= radiusSquared) {
                    // Player is near this NPC
                    currentProximity.computeIfAbsent(player.getUniqueId(), k -> new HashSet<>())
                            .add(npc.getId());
                }
            }
        }

        // Compare with previous state to detect enter/exit
        for (Player player : Bukkit.getOnlinePlayers()) {
            UUID playerId = player.getUniqueId();
            Set<String> previousNpcs = playerNearNpcs.getOrDefault(playerId, Collections.emptySet());
            Set<String> currentNpcs = currentProximity.getOrDefault(playerId, Collections.emptySet());

            // Check for ENTER events
            for (String npcId : currentNpcs) {
                if (!previousNpcs.contains(npcId)) {
                    fireProximityEvent(player, npcId, ProximityPayload.ProximityAction.ENTER);
                }
            }

            // Check for EXIT events
            for (String npcId : previousNpcs) {
                if (!currentNpcs.contains(npcId)) {
                    fireProximityEvent(player, npcId, ProximityPayload.ProximityAction.EXIT);
                }
            }
        }

        // Update state
        playerNearNpcs.clear();
        playerNearNpcs.putAll(currentProximity);
    }

    private void fireProximityEvent(Player player, String npcId, ProximityPayload.ProximityAction action) {
        Optional<NpcImpl> npcOpt = registry.getNpc(npcId);
        if (npcOpt.isEmpty()) {
            return;
        }

        NpcImpl npc = npcOpt.get();
        Entity entity = Bukkit.getEntity(npc.getEntityUuid());
        if (entity == null) {
            return;
        }

        double distance = player.getLocation().distance(entity.getLocation());

        ProximityPayload payload = new ProximityPayload(
                player.getUniqueId(),
                npc.getId(),
                npc.getEntityUuid(),
                action,
                distance
        );

        AethorNpcProximityEvent event = new AethorNpcProximityEvent(player, npc, payload);
        Bukkit.getPluginManager().callEvent(event);

        plugin.debug("Player " + player.getName() + " " + action + " proximity of NPC " + npc.getId() + " (distance: " + String.format("%.2f", distance) + ")");
    }
}
