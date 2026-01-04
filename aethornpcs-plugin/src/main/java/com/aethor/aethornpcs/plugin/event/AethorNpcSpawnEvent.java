package com.aethor.aethornpcs.plugin.event;

import com.aethor.aethornpcs.api.payload.SpawnPayload;
import com.aethor.aethornpcs.plugin.model.NpcImpl;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Fired when an NPC is spawned.
 */
public class AethorNpcSpawnEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final NpcImpl npc;
    private final SpawnPayload payload;

    public AethorNpcSpawnEvent(NpcImpl npc) {
        this.npc = npc;
        this.payload = new SpawnPayload(npc.getId(), npc.getEntityUuid());
    }

    public NpcImpl getNpc() {
        return npc;
    }

    public SpawnPayload getPayload() {
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
