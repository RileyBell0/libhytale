package dev.twunk.hytale.system.composite;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.WorldProvider;
import dev.twunk.hytale.HytalePlugin;
import dev.twunk.interfaces.IEventDriver;
import dev.twunk.interfaces.events.IOnAddRemove;
import dev.twunk.interfaces.events.IOnScheduledTick;
import dev.twunk.interfaces.events.IOnWorldTick;
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
public class OnScheduledTickEventDriver<
    ECS_TYPE extends WorldProvider
> implements IOnAddRemove<ECS_TYPE>, IOnWorldTick<ECS_TYPE>, IEventDriver<ECS_TYPE> {

    private final TrackedEntities<ECS_TYPE> entities;
    private final IOnScheduledTick<ECS_TYPE> listener;
    private final IRegistry<ECS_TYPE> registry;

    public OnScheduledTickEventDriver<ECS_TYPE> newUninitialised(
        IOnScheduledTick<ECS_TYPE> listener,
        IRegistry<ECS_TYPE> registry
    ) {
        return new OnScheduledTickEventDriver<>(listener, registry);
    }

    protected OnScheduledTickEventDriver(IOnScheduledTick<ECS_TYPE> listener, IRegistry<ECS_TYPE> registry) {
        this.listener = listener;
        this.registry = registry;

        @SuppressWarnings("unchecked")
        final ComponentType<ECS_TYPE, INTERNAL_TickSchedulerComponent<ECS_TYPE>> scheduledTickInfo =
            registry.getComponentType(INTERNAL_TickSchedulerComponent.class);
        if (scheduledTickInfo == null) {
            throw new RuntimeException("Failed to get component type for " + INTERNAL_TickSchedulerComponent.class);
        }

        // Init our module for tracking and persisting how our entities are
        // ticking/sleeping/etc
        this.entities = new TrackedEntities<ECS_TYPE>(listener.getId(), scheduledTickInfo);
    }

    ///////////////////////////////////////////////////////////////////////////
    // \/======================\/-  Events  -\/===========================\/ //
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Track entity (add to the scheduler, or add BACK into the scheduler) when it's
     * created or re-loaded into the world
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
     * Untrack entity (remove from scheduler) when the entity is removed/unloaded
     */
    public void onEntityRemove(
        final Ref<ECS_TYPE> ref,
        final RemoveReason reason,
        final Store<ECS_TYPE> store,
        final CommandBuffer<ECS_TYPE> commandBuffer
    ) {
        entities.untrack(ref, store, reason);
    }

    /**
     * Tick any entities that are scheduled to run this tick. Those entities can
     * return a plan for when they should tick next, or null to just keep ticking
     */
    public void onWorldTick(
        final float dt,
        final ArchetypeChunk<ECS_TYPE> archetypeChunk,
        final Store<ECS_TYPE> store,
        final CommandBuffer<ECS_TYPE> commandBuffer
    ) {
        for (final var ticker : entities.ticking) {
            final var res = listener.onScheduledTick(ticker.world, ticker.ref, dt, store, commandBuffer);

            // Transition to the state returned by the block
            if (res == null) {
                continue;
            }

            switch (res.getType()) {
                case TickPlan.TYPE_BROKEN:
                case TickPlan.TYPE_SLEEP:
                case TickPlan.TYPE_STOP:
                default:
                    break;
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // \/======================\/-  Methods  -\/==========================\/ //
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public final void onRegister(HytalePlugin plugin) {
        this.getRegistry().bindEventListeners(plugin, this);
    }

    @Override
    public IRegistry<ECS_TYPE> getRegistry() {
        return this.registry;
    }
}
