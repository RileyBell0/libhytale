package dev.twunk.subsystem.base;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.ArchetypeTickingSystem;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import dev.twunk.subsystem.ISubSystem;
import dev.twunk.subsystem.base.interfaces.IGlobalTickSystem;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Subsystem for calling `onSystemTick` on the parent system every tick
 *
 * GOAL: run code ONCE per tick globally. not per element, just, run this once per tick
 *
 * REQUIRES:
 * - N/A (this is a leaf)
 * PRODUCES:
 * - IGlobalTickSystem runner
 */
public class GlobalTickSubSystem extends ArchetypeTickingSystem<ChunkStore> implements ISubSystem {

    private final @Nonnull IGlobalTickSystem parent;
    private final @Nullable Query<ChunkStore> query;

    /**
     * Hytale expects a new "class" for each system you register. Thus, to have these composable modules
     * of subsystems, each one must secretly create a new class each and every time you call it
     */
    public static <T extends GlobalTickSubSystem> GlobalTickSubSystem create(@Nonnull final IGlobalTickSystem parent) {
        return ISubSystem.__newSubSystem(GlobalTickSubSystem.class, parent);
    }

    private GlobalTickSubSystem(@Nonnull IGlobalTickSystem parent) {
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
