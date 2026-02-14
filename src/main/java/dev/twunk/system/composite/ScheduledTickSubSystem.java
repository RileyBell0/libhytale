package dev.twunk.system.composite;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import dev.twunk.system.SubSystemOwner;
import dev.twunk.system.base.EntityLifetimeSubSystem;
import dev.twunk.system.base.GlobalTickSubSystem;
import dev.twunk.system.base.IEntityLifetimeSystem;
import dev.twunk.system.base.IGlobalTickSystem;
import dev.twunk.system.response.TickResponse;
import dev.twunk.utils.TrackedEntities;
import javax.annotation.Nonnull;

/**
 * Composite subsystem to allow the parent to run code on its elements every
 * tick in a smarter way
 *
 * GOAL: need to tick my entities that match the query but not necessarily EVERY
 *       tick. Need logic to determine if my entity should sleep/continue ticking
 *       without changing its components constantly
 *
 * REQUIRES:
 * - LifetimeSystem   -> keep track of existing entities ourselves manually
 * - GlobalTickSystem -> loop through ticking entities we've tracked manually
 *                       and run their tick method (tick entities)
 * PRODUCES:
 * - IScheduledTickSystem runner
 */
public class ScheduledTickSubSystem extends SubSystemOwner implements IEntityLifetimeSystem, IGlobalTickSystem {

    @Nonnull
    private final TrackedEntities entities;

    @Nonnull
    private final IScheduledTickSystem parent;

    public ScheduledTickSubSystem(@Nonnull final IScheduledTickSystem parent, @Nonnull final Query<ChunkStore> query) {
        super(query);
        this.parent = parent;

        // Init our module for tracking and persisting how our entities are
        // ticking/sleeping/etc
        this.entities = new TrackedEntities(parent.getId());

        // IMPORTANTLY the order in which these subsystems are created
        this.appendSubSystem(new EntityLifetimeSubSystem(this));
        this.appendSubSystem(new GlobalTickSubSystem(this));
    }

    /**
     * Whenever an entity is added we need to manually track it, such that we can
     * later manually run the tick method ourselves on each of our entities.
     *
     * Thus, we kick this responsibility over to our `TrackedEntities` class
     * that'll set it up to be easily tickable for us later.
     */
    public void onEntityAdded(
        @Nonnull final Ref<ChunkStore> ref,
        @Nonnull final AddReason reason,
        @Nonnull final Store<ChunkStore> store,
        @Nonnull final CommandBuffer<ChunkStore> commandBuffer
    ) {
        entities.track(ref, store, commandBuffer);
    }

    /**
     * As above, we're maintaining the list of existing entities that match our
     * query MANUALLY, so, equally, we have to remove them manually too (else we'll
     * have invalid refs around the place)
     *
     * Removes the entity from our TrackedEntities tracker.
     */
    public void onEntityRemove(
        @Nonnull final Ref<ChunkStore> ref,
        @Nonnull final RemoveReason reason,
        @Nonnull final Store<ChunkStore> store,
        @Nonnull final CommandBuffer<ChunkStore> commandBuffer
    ) {
        // drop the entity from our tracker
        entities.untrack(ref, store, reason);
    }

    /**
     * Runs once globally AFTER the lifetime subsystem has finished
     *
     * This bit is the `IScheduledTickSystem` runner. Pretty much just runs through
     * all ticking entities calling the parent's `onEntityTick` method for
     * their scheduled tick
     */
    public void onSystemTick(
        float dt,
        @Nonnull ArchetypeChunk<ChunkStore> archetypeChunk,
        @Nonnull Store<ChunkStore> store,
        @Nonnull CommandBuffer<ChunkStore> commandBuffer
    ) {
        // tick all our refs (ugly loop syntaxt basically just to assert that it's not
        // null. trust me, it's not, i guarantee it on its way in elsewhere, just want
        // to avoid having to check something the java type system can't pick up for
        // some reason)
        for (@Nonnull
        @SuppressWarnings("null")
        var ticker : entities.ticking) {
            // need to make it so that we check if the ref is still valid at this stage
            // (eventually)
            var res = parent.onEntityTick(
                ticker.world,
                ticker.chunk,
                ticker.ref,
                ticker.pos,
                ticker.blockId,
                dt,
                store,
                commandBuffer
            );

            // Transition to the state returned by the block
            if (res != null) {
                switch (res.getType()) {
                    case TickResponse.TYPE_BROKEN:
                    case TickResponse.TYPE_SLEEP:
                    case TickResponse.TYPE_STOP:
                    default:
                        break;
                }
            }
        }
    }
}
