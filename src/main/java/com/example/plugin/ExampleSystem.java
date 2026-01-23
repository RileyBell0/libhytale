package com.example.plugin;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.universe.world.chunk.section.BlockSection;
import com.hypixel.hytale.server.core.universe.world.chunk.section.ChunkSection;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;

public class ExampleSystem extends EntityTickingSystem<ChunkStore> {

    private static final Query<ChunkStore> QUERY = com.hypixel.hytale.component.query.Query.and(
            BlockSection.getComponentType(),
            ChunkSection.getComponentType());

    public void tick(float dt, int index, @Nonnull ArchetypeChunk<ChunkStore> archetypeChunk,
            @Nonnull Store<ChunkStore> store,
            @Nonnull CommandBuffer<ChunkStore> commandBuffer) {
    }

    @Nullable
    public Query<ChunkStore> getQuery() {
        return QUERY;
    }
}