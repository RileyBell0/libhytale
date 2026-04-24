package dev.twunk.lib.event;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import dev.twunk.hytale.event.OnBlockTick;
import dev.twunk.hytale.interfaces.event.IOnBlockTick;
import dev.twunk.hytale.ref.BlockRef;

public abstract class OnBlockTick__Listener extends OnBlockTick {

    private final IOnBlockTick listener;

    protected OnBlockTick__Listener(IOnBlockTick listener, Query<ChunkStore> query) {
        super(query);
        this.listener = listener;
    }

    // ////////////////////////////////////////////////////////////////////////
    // \/======================\/-  Methods  -\/==========================\/ //
    // ////////////////////////////////////////////////////////////////////////

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
