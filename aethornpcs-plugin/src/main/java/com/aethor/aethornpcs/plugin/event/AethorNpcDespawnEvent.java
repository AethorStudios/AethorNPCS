package com.aethor.aethornpcs.plugin.event;

import com.aethor.aethornpcs.api.payload.DespawnPayload;
import com.aethor.aethornpcs.plugin.model.NpcImpl;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Fired when an NPC is despawned.
 */
public class AethorNpcDespawnEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final NpcImpl npc;
    private final DespawnPayload payload;

    public AethorNpcDespawnEvent(NpcImpl npc) {
        this.npc = npc;
        this.payload = new DespawnPayload(npc.getId(), npc.getEntityUuid());
    }

    public NpcImpl getNpc() {
        return npc;
    }

    public DespawnPayload getPayload() {
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
