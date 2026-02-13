package dev.twunk.system.easy;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.ArchetypeTickingSystem;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

// TODO when making the builder make
// Set<Dependency<ChunkStore>>
// the argument, basically in what order this should run, but really just "after" x

/**
 * Subsystem for calling `onSystemTick` on the parent system every tick
 *
 * GOAL: run code ONCE per tick globally. not per element, just, run this once per tick
 */
public class GlobalTickSystem extends ArchetypeTickingSystem<ChunkStore> implements ISubSystem {

    private final @Nonnull ISystemTick parent;
    private final @Nullable Query<ChunkStore> query;

    public GlobalTickSystem(@Nonnull ISystemTick parent) {
        this.parent = parent;
        this.query = parent.getQuery();
    }

    /**
     * tick method that gets called by the `store`
     * this is pretty much just a shim to get into my code, as i don't want to touch
     * theirs wherever possible
     */
    @Override
    public void tick(
        float dt,
        @Nonnull ArchetypeChunk<ChunkStore> archetypeChunk,
        @Nonnull Store<ChunkStore> store,
        @Nonnull CommandBuffer<ChunkStore> commandBuffer
    ) {
        parent.onSystemTick(dt, archetypeChunk, store, commandBuffer);
    }

    @Override
    @Nullable
    public Query<ChunkStore> getQuery() {
        return this.query;
    }
}
