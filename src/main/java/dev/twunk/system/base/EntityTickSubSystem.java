package dev.twunk.system.base;

import com.hypixel.hytale.builtin.blocktick.system.ChunkBlockTickSystem;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import dev.twunk.system.interfaces.ISubSystem;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Subsystem for calling `onEntityTick` on the parent system every tick
 *
 * GOAL: run code on entities every tick
 *
 * REQUIRES:
 * - N/A (this is a leaf)
 * PRODUCES:
 * - IEntityTickSystem runner
 */
public class EntityTickSubSystem extends ChunkBlockTickSystem.Ticking implements ISubSystem {

    private final @Nonnull IEntityTickSystem parent;
    private final @Nullable Query<ChunkStore> query;

    public EntityTickSubSystem(@Nonnull IEntityTickSystem parent) {
        this.parent = parent;
        this.query = parent.getQuery();
    }

    public void tick(
        float dt,
        int index,
        @Nonnull ArchetypeChunk<ChunkStore> archetypeChunk,
        @Nonnull Store<ChunkStore> store,
        @Nonnull CommandBuffer<ChunkStore> commandBuffer
    ) {
        parent.onEntityTick(dt, index, archetypeChunk, store, commandBuffer);
    }

    @Override
    public Query<ChunkStore> getQuery() {
        return this.query;
    }
}
