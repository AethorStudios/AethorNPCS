package com.aethor.aethornpcs.plugin.event;

import com.aethor.aethornpcs.api.payload.InteractPayload;
import com.aethor.aethornpcs.plugin.model.NpcImpl;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Fired when a player interacts with an NPC.
 */
public class AethorNpcInteractEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();
    private boolean cancelled = false;

    private final Player player;
    private final NpcImpl npc;
    private final InteractPayload payload;

    public AethorNpcInteractEvent(Player player, NpcImpl npc, InteractPayload payload) {
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

    public InteractPayload getPayload() {
        return payload;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
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
