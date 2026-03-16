package dev.twunk.subsystem.composite;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import dev.twunk.subsystem.ISubSystem;
import dev.twunk.subsystem.SubSystemOwner;
import dev.twunk.subsystem.base.EntityLifetimeSubSystem;
import dev.twunk.subsystem.base.GlobalTickSubSystem;
import dev.twunk.subsystem.base.interfaces.IEntityLifetimeSystem;
import dev.twunk.subsystem.base.interfaces.IGlobalTickSystem;
import dev.twunk.subsystem.composite.interfaces.IScheduledTickSystem;
import dev.twunk.utils.lifetime.TrackedEntities;
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
public class ScheduledTickSubSystem
    extends SubSystemOwner
    implements IEntityLifetimeSystem, IGlobalTickSystem, ISubSystem
{

    @Nonnull
    private final TrackedEntities entities;

    @Nonnull
    private final IScheduledTickSystem parent;

    /**
     * Hytale expects a new "class" for each system you register. Thus, to have these composable modules
     * of subsystems, each one must secretly create a new class each and every time you call it
     */
    public static <T extends ScheduledTickSubSystem> ScheduledTickSubSystem create(
        @Nonnull final IScheduledTickSystem parent
    ) {
        return ISubSystem.__newSubSystem(ScheduledTickSubSystem.class, parent);
    }

    private ScheduledTickSubSystem(@Nonnull final IScheduledTickSystem parent) {
        super(parent.getQuery());
        this.parent = parent;

        // Init our module for tracking and persisting how our entities are
        // ticking/sleeping/etc
        this.entities = new TrackedEntities(parent.getId());

        // IMPORTANTLY the order in which these subsystems are created
        this.appendSubSystem(EntityLifetimeSubSystem.create(this));
        this.appendSubSystem(GlobalTickSubSystem.create(this));
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
                    case _EntityScheduledTickStateComponent.TYPE_BROKEN:
                    case _EntityScheduledTickStateComponent.TYPE_SLEEP:
                    case _EntityScheduledTickStateComponent.TYPE_STOP:
                    default:
                        break;
                }
            }
        }
    }
}
