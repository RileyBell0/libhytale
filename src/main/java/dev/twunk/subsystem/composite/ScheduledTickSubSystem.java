package dev.twunk.subsystem.composite;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.WorldProvider;
import dev.twunk.lib.component.INTERNAL_TickSchedulerComponent;
import dev.twunk.lib.lifetime.TrackedEntities;
import dev.twunk.subsystem.ISubSystem;
import dev.twunk.subsystem.SubSystemOwner;
import dev.twunk.subsystem.base.EntityLifetimeSubSystem;
import dev.twunk.subsystem.base.GlobalTickSubSystem;
import dev.twunk.subsystem.base.interfaces.IEntityLifetimeSystem;
import dev.twunk.subsystem.base.interfaces.IGlobalTickSystem;
import dev.twunk.subsystem.composite.interfaces.IRegistry;
import dev.twunk.subsystem.composite.interfaces.IScheduledTickSystem;
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
public class ScheduledTickSubSystem<ECS_STORE extends WorldProvider>
    extends SubSystemOwner<ECS_STORE>
    implements IEntityLifetimeSystem<ECS_STORE>, IGlobalTickSystem<ECS_STORE>, ISubSystem<ECS_STORE>
{

    @Nonnull
    private final TrackedEntities<ECS_STORE> entities;

    @Nonnull
    private final IScheduledTickSystem<ECS_STORE> parent;

    /**
     * Hytale expects a new "class" for each system you register. Thus, to have these composable modules
     * of subsystems, each one must secretly create a new class each and every time you call it
     */
    @SuppressWarnings("unchecked")
    @Nonnull
    public <T extends ScheduledTickSubSystem<ECS_STORE>> ScheduledTickSubSystem<ECS_STORE> newSubsystemFor(
        final @Nonnull IScheduledTickSystem<ECS_STORE> parent
    ) {
        return ISubSystem.__newSubSystem(ScheduledTickSubSystem.class, IScheduledTickSystem.class, parent);
    }

    protected ScheduledTickSubSystem(final @Nonnull IScheduledTickSystem<ECS_STORE> parent) {
        super(parent.getQuery());
        this.parent = parent;

        @SuppressWarnings("unchecked")
        final var componentType = parent
            .getRegistry()
            .getComponentType(
                (Class<INTERNAL_TickSchedulerComponent<ECS_STORE>>) (Class<?>) INTERNAL_TickSchedulerComponent.class
            );
        if (componentType == null) {
            throw new RuntimeException("Failed to get component type for " + INTERNAL_TickSchedulerComponent.class);
        }
        // Init our module for tracking and persisting how our entities are
        // ticking/sleeping/etc
        this.entities = new TrackedEntities<ECS_STORE>(parent.getId(), componentType);

        // IMPORTANTLY the order in which these subsystems are created
        this.appendSubSystem(EntityLifetimeSubSystem.newSubsystemFor(this));
        this.appendSubSystem(GlobalTickSubSystem.newSubsystemFor(this));
    }

    /**
     * Whenever an entity is added we need to manually track it, such that we can
     * later manually run the tick method ourselves on each of our entities.
     *
     * Thus, we kick this responsibility over to our `TrackedEntities` class
     * that'll set it up to be easily tickable for us later.
     */
    public void onEntityAdded(
        final @Nonnull Ref<ECS_STORE> ref,
        final @Nonnull AddReason reason,
        final @Nonnull Store<ECS_STORE> store,
        final @Nonnull CommandBuffer<ECS_STORE> commandBuffer
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
        final @Nonnull Ref<ECS_STORE> ref,
        final @Nonnull RemoveReason reason,
        final @Nonnull Store<ECS_STORE> store,
        final @Nonnull CommandBuffer<ECS_STORE> commandBuffer
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
        final float dt,
        final @Nonnull ArchetypeChunk<ECS_STORE> archetypeChunk,
        final @Nonnull Store<ECS_STORE> store,
        final @Nonnull CommandBuffer<ECS_STORE> commandBuffer
    ) {
        for (// Java doesn't believe us when we assert that items inside an arraylist are nonnull.
        // Don't worry, they are, that's the only reason we suppress null here
        @Nonnull
        @SuppressWarnings("null")
        final var ticker : entities.ticking) {
            // TODO - need to make it so that we check if the ref is still valid
            // at this stage (eventually)
            final var res = parent.onEntityTick(ticker.world, ticker.ref, dt, store, commandBuffer);

            // Transition to the state returned by the block
            if (res != null) {
                switch (res.getType()) {
                    case TickPlan.TYPE_BROKEN:
                    case TickPlan.TYPE_SLEEP:
                    case TickPlan.TYPE_STOP:
                    default:
                        break;
                }
            }
        }
    }

    @Override
    public IRegistry<ECS_STORE> getRegistry() {
        return parent.getRegistry();
    }
}
