package dev.twunk.hytale.events.composite;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.universe.world.WorldProvider;
import dev.twunk.hytale.interfaces.IEventDriver;
import dev.twunk.hytale.interfaces.events.IOnAddRemove;
import dev.twunk.hytale.interfaces.events.IOnScheduledTick;
import dev.twunk.hytale.interfaces.events.IOnWorldTick;
import dev.twunk.hytale.interfaces.methods.IQuery;
import dev.twunk.hytale.interfaces.methods.IRegistry;
import dev.twunk.hytale.utils.ComponentUtils;
import dev.twunk.lib.TickPlan;
import dev.twunk.lib.lifetime.TrackedEntities;
import dev.twunk.lib.lifetime.TrackedEntity;
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
> implements IOnAddRemove<ECS_TYPE>, IOnWorldTick<ECS_TYPE>, IEventDriver<ECS_TYPE>, IQuery<ECS_TYPE> {

    private final TrackedEntities<ECS_TYPE> inMemoryTrackedEntities;
    private final IRegistry<ECS_TYPE> registry;
    private final Query<ECS_TYPE> query;

    protected OnScheduledTick(String id, Query<ECS_TYPE> query, IRegistry<ECS_TYPE> registry) {
        this.query = query;
        this.registry = registry;

        this.inMemoryTrackedEntities = new TrackedEntities<ECS_TYPE>(id);
    }

    ///////////////////////////////////////////////////////////////////////////
    // \/======================\/-  Methods  -\/==========================\/ //
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public Query<ECS_TYPE> getQuery() {
        return this.query;
    }

    @Override
    public final void onRegister(JavaPlugin plugin) {
        this.registry.bindEventListeners(plugin, this);
    }

    @Override
    public IRegistry<ECS_TYPE> getRegistry() {
        return this.registry;
    }

    @Nullable
    protected abstract TickPlan tickTheTicker(
        TrackedEntity<ECS_TYPE> ticker,
        float dt,
        ArchetypeChunk<ECS_TYPE> archetypeChunk,
        Store<ECS_TYPE> store,
        CommandBuffer<ECS_TYPE> commandBuffer
    );

    ///////////////////////////////////////////////////////////////////////////
    // \/======================\/-  Events  -\/===========================\/ //
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Track entity (add to the scheduler, or add BACK into the scheduler) when it's
     * created or re-loaded into the world
     */
    public final void onEntityAdded(
        Ref<ECS_TYPE> ref,
        AddReason reason,
        Store<ECS_TYPE> store,
        CommandBuffer<ECS_TYPE> commandBuffer
    ) {
        inMemoryTrackedEntities.track(ref, store, commandBuffer);
    }

    /**
     * Untrack entity (remove from scheduler) when the entity is removed/unloaded
     */
    public final void onEntityRemove(
        Ref<ECS_TYPE> ref,
        RemoveReason reason,
        Store<ECS_TYPE> store,
        CommandBuffer<ECS_TYPE> commandBuffer
    ) {
        inMemoryTrackedEntities.untrack(ref, store, reason);
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
        for (final @Nonnull var ticker : inMemoryTrackedEntities.ticking) {
            final var res = this.tickTheTicker(ticker, dt, archetypeChunk, store, commandBuffer);

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

    public static final <
        ECS_TYPE extends WorldProvider,
        T extends IOnScheduledTick<ECS_TYPE> & IQuery<ECS_TYPE>
    > OnScheduledTick<ECS_TYPE> newUninitialised(String id, T listener, IRegistry<ECS_TYPE> registry) {
        return new OnScheduledTick__Listener<ECS_TYPE>(id, listener, listener.getQuery(), registry);
    }

    public static final <ECS_TYPE extends WorldProvider> OnScheduledTick<ECS_TYPE> newUninitialised(
        String id,
        IOnScheduledTick<ECS_TYPE> listener,
        Query<ECS_TYPE> query,
        IRegistry<ECS_TYPE> registry
    ) {
        return new OnScheduledTick__Listener<ECS_TYPE>(id, listener, query, registry);
    }

    public static final <ECS_TYPE extends WorldProvider, T extends Component<ECS_TYPE>> OnScheduledTick<
        ECS_TYPE
    > newUninitialised(String id, ComponentType<ECS_TYPE, T> componentType, IRegistry<ECS_TYPE> registry) {
        return new OnScheduledTick__Component<ECS_TYPE, T>(id, componentType, registry);
    }
}

class OnScheduledTick__Listener<ECS_TYPE extends WorldProvider> extends OnScheduledTick<ECS_TYPE> {

    private final IOnScheduledTick<ECS_TYPE> listener;

    protected OnScheduledTick__Listener(
        String id,
        IOnScheduledTick<ECS_TYPE> listener,
        Query<ECS_TYPE> query,
        IRegistry<ECS_TYPE> registry
    ) {
        super(id, query, registry);
        this.listener = listener;
    }

    ///////////////////////////////////////////////////////////////////////////
    // \/======================\/-  Methods  -\/==========================\/ //
    ///////////////////////////////////////////////////////////////////////////

    @Override
    @Nullable
    public final TickPlan tickTheTicker(
        TrackedEntity<ECS_TYPE> ticker,
        float dt,
        ArchetypeChunk<ECS_TYPE> archetypeChunk,
        Store<ECS_TYPE> store,
        CommandBuffer<ECS_TYPE> commandBuffer
    ) {
        return listener.onScheduledTick(ticker.world, ticker.ref, dt, store, commandBuffer);
    }
}

class OnScheduledTick__Component<
    ECS_TYPE extends WorldProvider,
    T extends Component<ECS_TYPE>
> extends OnScheduledTick<ECS_TYPE> {

    private final ComponentType<ECS_TYPE, T> componentType;

    protected OnScheduledTick__Component(
        String id,
        ComponentType<ECS_TYPE, T> componentType,
        IRegistry<ECS_TYPE> registry
    ) {
        super(id, Query.and(componentType), registry);
        this.componentType = componentType;
    }

    ///////////////////////////////////////////////////////////////////////////
    // \/======================\/-  Methods  -\/==========================\/ //
    ///////////////////////////////////////////////////////////////////////////

    @SuppressWarnings("unchecked")
    @Override
    @Nullable
    public final TickPlan tickTheTicker(
        TrackedEntity<ECS_TYPE> ticker,
        float dt,
        ArchetypeChunk<ECS_TYPE> archetypeChunk,
        Store<ECS_TYPE> store,
        CommandBuffer<ECS_TYPE> commandBuffer
    ) {
        final var component = ComponentUtils.get(ticker.ref, componentType);
        if (component == null) {
            return null;
        }

        return ((IOnScheduledTick<ECS_TYPE>) component).onScheduledTick(
            ticker.world,
            ticker.ref,
            dt,
            store,
            commandBuffer
        );
    }
}
