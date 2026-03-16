package dev.twunk.subsystem.base;

import com.hypixel.hytale.builtin.blocktick.system.ChunkBlockTickSystem;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import dev.twunk.subsystem.ISubSystem;
import dev.twunk.subsystem.base.interfaces.IEntityTickSystem;
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

    /**
     * Hytale expects a new "class" for each system you register. Thus, to have these composable modules
     * of subsystems, each one must secretly create a new class each and every time you call it
     */
    public static <T extends EntityTickSubSystem> EntityTickSubSystem create(@Nonnull final IEntityTickSystem parent) {
        return ISubSystem.__newSubSystem(EntityTickSubSystem.class, IEntityTickSystem.class, parent);
    }

    protected EntityTickSubSystem(@Nonnull IEntityTickSystem parent) {
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
