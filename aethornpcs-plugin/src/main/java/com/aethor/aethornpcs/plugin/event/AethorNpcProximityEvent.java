package com.aethor.aethornpcs.plugin.event;

import com.aethor.aethornpcs.api.payload.ProximityPayload;
import com.aethor.aethornpcs.plugin.model.NpcImpl;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Fired when a player enters or exits proximity of an NPC.
 */
public class AethorNpcProximityEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Player player;
    private final NpcImpl npc;
    private final ProximityPayload payload;

    public AethorNpcProximityEvent(Player player, NpcImpl npc, ProximityPayload payload) {
        this.player = player;
        this.npc = npc;
        this.payload = payload;
    }

    public Player getPlayer() {
        return player;
    }

    public NpcImpl getNpc() {
        return npc;
    }

    public ProximityPayload getPayload() {
        return payload;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
