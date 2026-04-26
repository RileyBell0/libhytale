package dev.twunk.hytale.event.composite;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.server.core.modules.block.BlockModule.BlockStateInfo;
import com.hypixel.hytale.server.core.universe.world.WorldProvider;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.twunk.hytale.LibHytaleException;
import dev.twunk.hytale.component.UUIDComponent;
import dev.twunk.hytale.interfaces.IQueryableEventDriver;
import dev.twunk.hytale.interfaces.event.IOnAddRemove;
import dev.twunk.hytale.interfaces.event.IOnTick.IOnTickQuery;
import dev.twunk.hytale.interfaces.event.IOnWorldTick;
import dev.twunk.hytale.interfaces.methods.IRegistry;
import dev.twunk.hytale.ref.AnyRef;
import dev.twunk.hytale.utils.BlockUtils;
import dev.twunk.hytale.utils.ChunkUtils;
import dev.twunk.hytale.utils.ComponentUtils;
import dev.twunk.lib.event.scheduled.TickSchedule;
import dev.twunk.lib.registry.ChunkRegisterProvider;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * You need to provide me an ID so that i can create, serialize and deserialize components UNIQUE to this system consistently
 *
 * i create one component
 * - 1x dupe of ActivelyTickingComponent -> means i can mark an entity as ticking for any number of ScheduledTick systems and their queries INDIVIDUALLY
 *
 * Originally i was also going to create a dupe of TickScheduleComponent, but i may as well just store all system tick info in there and let you fetch yours via
 * your system's ID
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
> implements IOnAddRemove<ECS_TYPE>, IOnWorldTick<ECS_TYPE>, IOnTickQuery<ECS_TYPE>, IQueryableEventDriver<ECS_TYPE> {

    @FunctionalInterface
    private static interface SleepingEntityCreator<ECS_TYPE extends WorldProvider> {
        SleepingEntity fromRef(Ref<ECS_TYPE> ref, UUID uuid, TickSchedule.Sleeping schedule);
    }

    // A unique and STABLE identifier for the system. you cannot change this.
    // once you decide on an ID your players REQUIRE it to be stable (or everything
    // in their worlds will break)
    //
    // This is what is used for entities to "remember" if they're sleeping, ticking etc
    // in your system when they're loaded back in.
    //
    // hence, if you change this ID all entities will still store but forever forget
    // what state they held
    private final TickSchedule defaultSchedule;
    private final String id;
    private final Query<ECS_TYPE> query;
    private final IRegistry<ECS_TYPE> registry;

    private ComponentType<ECS_TYPE, ActivelyTickingComponent<ECS_TYPE>> activeFlagComponentType;
    private ComponentType<ECS_TYPE, TickScheduleComponent<ECS_TYPE>> tickScheduleComponentType;
    private final ComponentType<ECS_TYPE, UUIDComponent<ECS_TYPE>> uuidComponentType;

    public static final SleepingEntity sleepingEntityCreator__ChunkStore(
        Ref<ChunkStore> ref,
        UUID uuid,
        TickSchedule.Sleeping schedule
    ) {
        // get the BlockStateInfo component -> this gives us access to the
        // local index directly with info.getIndex(), and gives us a chunk
        // ref that we can turn into a chunk index easily too
        @SuppressWarnings("null")
        @Nonnull
        final var info = ComponentUtils.get(ref, BlockStateInfo.getComponentType());

        final var blockIndex = info.getIndex();

        @SuppressWarnings("null")
        @Nonnull
        final var chunkIndex = ChunkUtils.Coords.Index.get(info);

        return new SleepingEntity__Block(uuid, schedule, chunkIndex, blockIndex);
    }

    private static final SleepingEntity sleepingEntityCreator__EntityStore(
        Ref<EntityStore> ref,
        UUID uuid,
        TickSchedule.Sleeping schedule
    ) {
        return new SleepingEntity(uuid, schedule);
    }

    private final Set<UUID> removedSleepers = new HashSet<>();
    private final PriorityQueue<SleepingEntity> nextToWake = new PriorityQueue<>();
    private final SleepingEntityCreator<ECS_TYPE> sleepingEntityCreator;

    protected OnScheduledTick(String id, Query<ECS_TYPE> query, IRegistry<ECS_TYPE> registry) {
        this.id = id;
        this.query = query;
        this.registry = registry;
        this.defaultSchedule = TickSchedule.ACTIVE;

        if (this.registry instanceof ChunkRegisterProvider) {
            @SuppressWarnings({ "unchecked", "rawtypes" })
            SleepingEntityCreator<ECS_TYPE> creator =
                (SleepingEntityCreator) OnScheduledTick::sleepingEntityCreator__ChunkStore;
            this.sleepingEntityCreator = creator;
        } else {
            @SuppressWarnings({ "unchecked", "rawtypes" })
            SleepingEntityCreator<ECS_TYPE> creator =
                (SleepingEntityCreator) OnScheduledTick::sleepingEntityCreator__EntityStore;
            this.sleepingEntityCreator = creator;
        }

        @SuppressWarnings({ "unchecked", "null" })
        @Nonnull
        final ComponentType<ECS_TYPE, ActivelyTickingComponent<ECS_TYPE>> activeComponent =
            this.registry.getComponentType(ActivelyTickingComponent.class);
        this.activeFlagComponentType = activeComponent;

        @SuppressWarnings({ "unchecked", "null" })
        @Nonnull
        ComponentType<ECS_TYPE, UUIDComponent<ECS_TYPE>> uuidComponent = this.registry.getComponentType(
            UUIDComponent.class
        );
        this.uuidComponentType = uuidComponent;

        @SuppressWarnings({ "unchecked", "null" })
        @Nonnull
        ComponentType<ECS_TYPE, TickScheduleComponent<ECS_TYPE>> tickScheduleComponent = this.registry.getComponentType(
            TickScheduleComponent.class
        );
        this.tickScheduleComponentType = tickScheduleComponent;
    }

    protected OnScheduledTick(
        String id,
        Query<ECS_TYPE> query,
        IRegistry<ECS_TYPE> registry,
        TickSchedule defaultSchedule
    ) {
        this.id = id;
        this.query = query;
        this.registry = registry;
        this.defaultSchedule = defaultSchedule;

        if (this.registry instanceof ChunkRegisterProvider) {
            @SuppressWarnings({ "unchecked", "rawtypes" })
            SleepingEntityCreator<ECS_TYPE> creator =
                (SleepingEntityCreator) OnScheduledTick::sleepingEntityCreator__ChunkStore;
            this.sleepingEntityCreator = creator;
        } else {
            @SuppressWarnings({ "unchecked", "rawtypes" })
            SleepingEntityCreator<ECS_TYPE> creator =
                (SleepingEntityCreator) OnScheduledTick::sleepingEntityCreator__EntityStore;
            this.sleepingEntityCreator = creator;
        }

        @SuppressWarnings({ "unchecked", "null" })
        @Nonnull
        final ComponentType<ECS_TYPE, ActivelyTickingComponent<ECS_TYPE>> activeComponent =
            this.registry.getComponentType(ActivelyTickingComponent.class);
        this.activeFlagComponentType = activeComponent;

        @SuppressWarnings({ "unchecked", "null" })
        @Nonnull
        ComponentType<ECS_TYPE, UUIDComponent<ECS_TYPE>> uuidComponent = this.registry.getComponentType(
            UUIDComponent.class
        );
        this.uuidComponentType = uuidComponent;

        @SuppressWarnings({ "unchecked", "null" })
        @Nonnull
        ComponentType<ECS_TYPE, TickScheduleComponent<ECS_TYPE>> tickScheduleComponent = this.registry.getComponentType(
            TickScheduleComponent.class
        );
        this.tickScheduleComponentType = tickScheduleComponent;
    }

    @Override
    public final Query<ECS_TYPE> getQuery() {
        return this.query;
    }

    @Override
    public final Query<ECS_TYPE> getIOnTickQuery() {
        return Query.and(this.query, this.activeFlagComponentType);
    }

    @Override
    public final IRegistry<ECS_TYPE> getRegistry() {
        return this.registry;
    }

    // ////////////////////////////////////////////////////////////////////////
    // \/===========\/-  Logic driving the tick scheduling  -\/===========\/ //
    // ////////////////////////////////////////////////////////////////////////
    // #region schedule

    /**
     * Load in tick schedule (or use default fallback)
     *
     * Guarantees that entities will have a UUID component after this point.
     */
    @Override
    public final void onEntityAdded(AnyRef<ECS_TYPE> ref, AddReason reason, CommandBuffer<ECS_TYPE> commandBuffer) {
        // get the UUID component, or force one onto the entity otherwise
        final UUIDComponent<ECS_TYPE> uuidComponent = commandBuffer.ensureAndGetComponent(ref, this.uuidComponentType);
        final UUID uuid = uuidComponent.getUuid();

        // Get the schedule holder component, or force said component to store our schedule onto the entity
        final var tickScheduleComponent = commandBuffer.ensureAndGetComponent(ref, this.tickScheduleComponentType);

        // finally: check the schedule. If it doesn't exist we'll use a default. If it's meant to be ticking, we'll also force a ticking component onto it
        var schedule = tickScheduleComponent.getSchedule(this.id);
        if (schedule == null) {
            // no schedule found: use default AND save schedule to our component for persistence
            schedule = this.defaultSchedule;
            tickScheduleComponent.setSchedule(this.id, schedule);
        }
        switch (schedule) {
            // it wants to be ticking so make sure its got that component that we query on for ticking entities and move along
            case TickSchedule.Active _ -> commandBuffer.ensureComponent(ref, this.activeFlagComponentType);
            // it wants to be sleeping, so just need to record when it wants to be awake so i can wake it up (as long as its still loaded when that happens)
            case TickSchedule.Sleeping sleeping -> this.nextToWake.add(
                this.sleepingEntityCreator.fromRef(ref, uuid, sleeping)
            );
            // ignore and discard other cases, they're not waiting for a tick or actively ticking
            // so i literally don't care about them
            default -> {
                // ^^
            }
        }
    }

    /**
     * Remove entity from the sleeping cache if sleeping
     * Actively ticking entities are handled automatically due to their components dipping from the world and thus the query
     */
    @Override
    public final void onEntityRemove(AnyRef<ECS_TYPE> ref, RemoveReason reason, CommandBuffer<ECS_TYPE> commandBuffer) {
        // get the UUID component
        @Nullable
        UUIDComponent<ECS_TYPE> uuidComponent = ComponentUtils.get(ref, this.uuidComponentType);

        // if it DIDNT exist there's no point in making a new one so we'll just dip.
        // sure, it SHOULD exist but hell i don't trust y'all enough to rely on that heavily
        if (uuidComponent == null) {
            return;
        }

        // mark that the sleeper handler should discard element next time it sees it, as we're just, yeah, done with it
        removedSleepers.add(uuidComponent.getUuid());
    }

    // #endregion schedule

    // ////////////////////////////////////////////////////////////////////////
    // \/====\/-  Wake up sleepers that are ready for their tick  -\/=====\/ //
    // ////////////////////////////////////////////////////////////////////////
    // #region awaken

    @Override
    public void onWorldTick(
        float dt,
        ArchetypeChunk<ECS_TYPE> archetypeChunk,
        Store<ECS_TYPE> store,
        CommandBuffer<ECS_TYPE> commandBuffer
    ) {
        // first: wake up elements that are ready to wake
        SleepingEntity next;
        final var currentTick = store.getExternalData().getWorld().getTick();
        // while the next sleeper exists and is waiting to be ticked (<= currentTick)
        while ((next = nextToWake.peek()) != null && next.nextTick <= currentTick) {
            // skip over & remove entites that are no longer loaded from our
            // next tick queue
            final var uuid = next.uuid;
            if (removedSleepers.contains(uuid)) {
                nextToWake.remove();
                removedSleepers.remove(uuid);
                continue;
            }

            // neat, this one's ready to go. We need to FIND the element and, heck ok. shit. need to store the coords and chunk and world
            // meaning, i need to also store with the nextToWake its coords and shit
            final var nowAwake = nextToWake.remove();

            // if we're in chunk mode we have to use the coords to get a ref
            final Ref<ECS_TYPE> ref = switch (nowAwake) {
                case SleepingEntity__Block asChunk -> {
                    // getRef based on its coords. We can't use UUID for this without modifying hytales ChunkStore to also record entites vs uuids and i so cbf doing that
                    @SuppressWarnings({ "unchecked", "rawtypes" })
                    final Ref<ECS_TYPE> blockRef = (Ref) BlockUtils.Refs.get(
                        (ChunkStore) store.getExternalData(),
                        asChunk.chunkCoords,
                        asChunk.localCoords
                    );
                    yield blockRef;
                }
                case SleepingEntity _ -> {
                    // getRef based on its UUID
                    final var entityStore = ((EntityStore) store.getExternalData());

                    @SuppressWarnings({ "unchecked", "rawtypes" })
                    final Ref<ECS_TYPE> entityRef = (Ref) entityStore.getRefFromUUID(uuid);

                    yield entityRef;
                }
            };
            if (ref == null) {
                throw new LibHytaleException(
                    "This shouldn't happen, means we failed to get a ref via coords for a block entity that's still loaded and ready for its scheduled tick"
                );
            }

            // waking it up is as easy as adding the activeFlagComponent to it
            commandBuffer.ensureComponent(ref, this.activeFlagComponentType);

            // then we just loop and keep going till we run out of things to tick
        }
    }

    // #endregion awaken

    @Nullable
    protected abstract TickSchedule _onScheduledTick(
        float dt,
        AnyRef<ECS_TYPE> ref,
        CommandBuffer<ECS_TYPE> commandBuffer
    );

    // ////////////////////////////////////////////////////////////////////////
    // \/======\/-  Tick, and potentially sleep/stop entities  -\/========\/ //
    // ////////////////////////////////////////////////////////////////////////
    /**
     * Tick any entities that are scheduled to run this tick. Those entities can
     * return a plan for when they should tick next, or null to just keep ticking
     */
    @Override
    public void onTick(float dt, AnyRef<ECS_TYPE> ref, CommandBuffer<ECS_TYPE> commandBuffer) {
        // run your tick method
        final var res = this._onScheduledTick(dt, ref, commandBuffer);
        if (res == null) {
            return;
        }

        final var tickScheduleComponent = commandBuffer.ensureAndGetComponent(ref, this.tickScheduleComponentType);

        // Configure future schedule for this entity according to your returned tick schedule
        switch (res) {
            case TickSchedule.Sleeping newSchedule -> {
                // if they're planning to run next tick, we'll just, ignore that new
                // schedule request. silly dummie
                final var currentTick = ref.getStore().getExternalData().getWorld().getTick();
                if (newSchedule.wakeUpAt == currentTick + 1) {
                    break;
                }

                // write us into the sleep queue
                final var uuidComponent = commandBuffer.getComponent(ref, this.uuidComponentType);
                final var uuid = uuidComponent.getUuid();
                this.nextToWake.add(this.sleepingEntityCreator.fromRef(ref, uuid, newSchedule));

                // persist our new schedule
                tickScheduleComponent.setSchedule(this.id, newSchedule);

                // finally: remove the flag that says we're ticking (so we don't anymore)
                // (occurs AFTER our system finishes running all ticks)
                commandBuffer.tryRemoveComponent(ref, this.activeFlagComponentType);
            }
            case TickSchedule.Stopped newSchedule -> {
                // persist our new schedule
                tickScheduleComponent.setSchedule(this.id, newSchedule);

                // finally: remove the flag that says we're ticking (so we don't anymore)
                // (occurs AFTER our system finishes running all ticks)
                commandBuffer.tryRemoveComponent(ref, this.activeFlagComponentType);
            }
            default -> {
                // default behaviour is to continue ticking, so can sip this
            }
        }
    }
}
