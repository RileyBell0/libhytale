package dev.twunk.lib.events;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import dev.twunk.hytale.events.OnBlockTick;
import dev.twunk.hytale.interfaces.events.IOnBlockTick;
import dev.twunk.hytale.refs.BlockRef;

public abstract class OnBlockTick__Listener extends OnBlockTick {

    private final IOnBlockTick listener;

    public OnBlockTick__Listener(IOnBlockTick listener, Query<ChunkStore> query) {
        super(query);
        this.listener = listener;
    }

    ///////////////////////////////////////////////////////////////////////////
    // \/======================\/-  Methods  -\/==========================\/ //
    ///////////////////////////////////////////////////////////////////////////

    public final void tick(
        float dt,
        int index,
        ArchetypeChunk<ChunkStore> archetypeChunk,
        Store<ChunkStore> store,
        CommandBuffer<ChunkStore> commandBuffer
    ) {
        listener.onBlockTick(new BlockRef(archetypeChunk.getReferenceTo(index)), commandBuffer);
    }
}
