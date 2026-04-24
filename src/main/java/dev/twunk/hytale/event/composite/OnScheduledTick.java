package dev.twunk.hytale.event.composite;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.server.core.universe.world.WorldProvider;
import dev.twunk.hytale.interfaces.IQueryableEventDriver;
import dev.twunk.hytale.interfaces.event.IOnAddRemove;
import dev.twunk.hytale.interfaces.event.IOnScheduledTick;
import dev.twunk.hytale.interfaces.event.IOnWorldTick;
import dev.twunk.hytale.interfaces.methods.IQuery;
import dev.twunk.hytale.interfaces.methods.IRegistry;
import dev.twunk.hytale.ref.AnyRef;
import dev.twunk.hytale.ref.TrackedRef;
import dev.twunk.lib.event.OnScheduledTick__Component;
import dev.twunk.lib.event.OnScheduledTick__Listener;
import dev.twunk.lib.event.scheduled.LoadedEntities;
import dev.twunk.lib.event.scheduled.TickPlan;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

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
public abstract class OnScheduledTick<
    ECS_TYPE extends WorldProvider
> implements IOnAddRemove<ECS_TYPE>, IOnWorldTick<ECS_TYPE>, IQueryableEventDriver<ECS_TYPE> {

    private final LoadedEntities<ECS_TYPE> entities;
    private final IRegistry<ECS_TYPE> registry;
    private final Query<ECS_TYPE> query;

    protected OnScheduledTick(String id, Query<ECS_TYPE> query, IRegistry<ECS_TYPE> registry) {
        this.query = query;
        this.registry = registry;
        this.entities = new LoadedEntities<>(id, registry);
    }

    @Override
    public Query<ECS_TYPE> getQuery() {
        return this.query;
    }

    @Override
    public IRegistry<ECS_TYPE> getRegistry() {
        return this.registry;
    }

    // ////////////////////////////////////////////////////////////////////////
    // \/======================\/-  Methods  -\/==========================\/ //
    // ////////////////////////////////////////////////////////////////////////

    @Nullable
    protected abstract TickPlan runScheduledTick(
        float dt,
        TrackedRef<ECS_TYPE> ticker,
        CommandBuffer<ECS_TYPE> commandBuffer
    );

    // ////////////////////////////////////////////////////////////////////////
    // \/===========\/-  Logic driving the tick scheduling  -\/===========\/ //
    // ////////////////////////////////////////////////////////////////////////

    /**
     * Track entity (add to the scheduler, or add BACK into the scheduler) when it's
     * created or re-loaded into the world
     */
    @Override
    public final void onEntityAdded(AnyRef<ECS_TYPE> ref, AddReason reason, CommandBuffer<ECS_TYPE> commandBuffer) {
        entities.track(ref, commandBuffer);
    }

    /**
     * Untrack entity (remove from scheduler) when the entity is removed/unloaded
     */
    @Override
    public final void onEntityRemove(AnyRef<ECS_TYPE> ref, RemoveReason reason, CommandBuffer<ECS_TYPE> commandBuffer) {
        entities.untrack(ref, reason);
    }

    /**
     * Tick any entities that are scheduled to run this tick. Those entities can
     * return a plan for when they should tick next, or null to just keep ticking
     */
    @SuppressWarnings("null")
    @Override
    public final void onWorldTick(
        float dt,
        ArchetypeChunk<ECS_TYPE> archetypeChunk,
        Store<ECS_TYPE> store,
        CommandBuffer<ECS_TYPE> commandBuffer
    ) {
        // For all entities scheduled to be ticked right now
        for (final @Nonnull var ticker : entities.ticking) {
            // Tick
            final var res = this.runScheduledTick(dt, ticker, commandBuffer);

            // Configure schedule for future tick(s)
            if (res == null) {
                continue;
            }
            switch (res.getType()) {
                case
                    TickPlan.ScheduleType.ACTIVE,
                    TickPlan.ScheduleType.SLEEP,
                    TickPlan.ScheduleType.STOP,
                    TickPlan.ScheduleType.UNKNOWN -> {
                    // TODO haven't configured TYPE_BROKEN
                    break;
                }
            }
        }
    }

    // ////////////////////////////////////////////////////////////////////////
    // \/==================\/-  Implementations  -\/======================\/ //
    // ////////////////////////////////////////////////////////////////////////
    // #region hide

    public static final <
        ECS_TYPE extends WorldProvider,
        T extends IOnScheduledTick<ECS_TYPE> & IQuery<ECS_TYPE>
    > OnScheduledTick<ECS_TYPE> newUninitialised(String id, T listener, IRegistry<ECS_TYPE> registry) {
        return new OnScheduledTick__Listener<>(id, listener, listener.getQuery(), registry);
    }

    public static final <ECS_TYPE extends WorldProvider> OnScheduledTick<ECS_TYPE> newUninitialised(
        String id,
        IOnScheduledTick<ECS_TYPE> listener,
        Query<ECS_TYPE> query,
        IRegistry<ECS_TYPE> registry
    ) {
        return new OnScheduledTick__Listener<>(id, listener, query, registry);
    }

    public static final <ECS_TYPE extends WorldProvider, T extends Component<ECS_TYPE>> OnScheduledTick<
        ECS_TYPE
    > newUninitialised(String id, ComponentType<ECS_TYPE, T> componentType, IRegistry<ECS_TYPE> registry) {
        return new OnScheduledTick__Component<>(id, componentType, registry);
    }
    // #endregion hide
}
