package dev.twunk.hytale.system;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.server.core.universe.world.WorldProvider;
import dev.twunk.interfaces.ISubSystem;
import dev.twunk.interfaces.methods.IOnAddRemove;
import dev.twunk.interfaces.methods.IOnScheduledTick;
import dev.twunk.interfaces.methods.IOnUniverseTick;
import dev.twunk.interfaces.methods.IRegistry;
import dev.twunk.lib.TickPlan;
import dev.twunk.lib.component.INTERNAL_TickSchedulerComponent;
import dev.twunk.lib.lifetime.TrackedEntities;

/**
 * TODO really not working that well atm, needs to be cleaned up BUT it worked
 * well in concept and practice. Just, yeah, needs to be cleaned up and extended to support
 * more advanced ticking ideas like "every x ticks" or "every x seconds" etc etc
 *
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
public class OnScheduledTickEventDriver<ECS_TYPE extends WorldProvider>
    extends SubSystemOwner<ECS_TYPE>
    implements IOnAddRemove<ECS_TYPE>, IOnUniverseTick<ECS_TYPE>, ISubSystem<ECS_TYPE>
{

    private final TrackedEntities<ECS_TYPE> entities;
    private final IOnScheduledTick<ECS_TYPE> listener;
    private final IRegistry<ECS_TYPE> registry;

    ///////////////////////////////////////////////////////////////////////////
    // \/======================\/-  Methods  -\/==========================\/ //
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Hytale expects a new "class" for each system you register. Thus, to have these composable modules
     * of subsystems, each one must secretly create a new class each and every time you call it
     */
    public OnScheduledTickEventDriver<ECS_TYPE> constructNewSystemClass(
        IOnScheduledTick<ECS_TYPE> listener,
        Query<ECS_TYPE> query,
        IRegistry<ECS_TYPE> registry
    ) {
        return ISubSystem.__construct(
            ISubSystem.__dupeClassAndGetConstructor(
                OnScheduledTickEventDriver.class,
                IOnScheduledTick.class,
                Query.class,
                IRegistry.class
            ),
            listener,
            query,
            registry
        );
    }

    protected OnScheduledTickEventDriver(
        IOnScheduledTick<ECS_TYPE> listener,
        Query<ECS_TYPE> query,
        IRegistry<ECS_TYPE> registry
    ) {
        super(query);
        this.listener = listener;
        this.registry = registry;

        @SuppressWarnings("unchecked")
        final var componentType = registry.getComponentType(
            (Class<INTERNAL_TickSchedulerComponent<ECS_TYPE>>) (Class<?>) INTERNAL_TickSchedulerComponent.class
        );
        if (componentType == null) {
            throw new RuntimeException("Failed to get component type for " + INTERNAL_TickSchedulerComponent.class);
        }
        // Init our module for tracking and persisting how our entities are
        // ticking/sleeping/etc
        this.entities = new TrackedEntities<ECS_TYPE>(listener.getId(), componentType);

        // IMPORTANTLY the order in which these subsystems are created
        this.appendSubSystem(OnAddRemoveEventDriver.ForListener.constructNewSystemClass(this, query, this.registry));
        this.appendSubSystem(OnUniverseTickEventDriver.constructNewSystemClass(this, query, this.registry));
    }

    /**
     * Whenever an entity is added we need to manually track it, such that we can
     * later manually run the tick method ourselves on each of our entities.
     *
     * Thus, we kick this responsibility over to our `TrackedEntities` class
     * that'll set it up to be easily tickable for us later.
     */
    public void onEntityAdded(
        final Ref<ECS_TYPE> ref,
        final AddReason reason,
        final Store<ECS_TYPE> store,
        final CommandBuffer<ECS_TYPE> commandBuffer
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
        final Ref<ECS_TYPE> ref,
        final RemoveReason reason,
        final Store<ECS_TYPE> store,
        final CommandBuffer<ECS_TYPE> commandBuffer
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
        final ArchetypeChunk<ECS_TYPE> archetypeChunk,
        final Store<ECS_TYPE> store,
        final CommandBuffer<ECS_TYPE> commandBuffer
    ) {
        for (// Java doesn't believe us when we assert that items inside an arraylist are nonnull.
        // Don't worry, they are, that's the only reason we suppress null here
        final var ticker : entities.ticking) {
            final var res = listener.onEntityTick(ticker.world, ticker.ref, dt, store, commandBuffer);

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
    public IRegistry<ECS_TYPE> getRegistry() {
        return this.registry;
    }
}
