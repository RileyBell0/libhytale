package dev.twunk.hytale.event.composite;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.ResourceType;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.server.core.universe.world.WorldProvider;
import dev.twunk.hytale.component.UUIDComponent;
import dev.twunk.hytale.component.UUIDLookupResource;
import dev.twunk.hytale.interfaces.IEventDriver;
import dev.twunk.hytale.interfaces.config.IQuery;
import dev.twunk.hytale.interfaces.event.IOnAddRemove;
import dev.twunk.hytale.interfaces.event.IOnScheduledTick;
import dev.twunk.hytale.interfaces.event.IOnTick;
import dev.twunk.hytale.interfaces.event.IOnWorldTick;
import dev.twunk.hytale.interfaces.methods.IRegistry;
import dev.twunk.hytale.ref.AnyRef;
import dev.twunk.hytale.resource.CurrentWorldTick;
import dev.twunk.lib.component.ActivelyTickingComponent;
import dev.twunk.lib.component.TickScheduleComponent;
import dev.twunk.lib.event.scheduled.SleepingEntity;
import dev.twunk.lib.event.scheduled.TickSchedule;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.UUID;
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
public class OnScheduledTick<ECS_TYPE extends WorldProvider>
    extends QueryableCompositeSystem<ECS_TYPE>
    implements IOnAddRemove<ECS_TYPE>, IOnWorldTick<ECS_TYPE>, IOnTick<ECS_TYPE>
{

    protected final IOnScheduledTick<ECS_TYPE> listener;

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

    private final Set<UUID> unloadedEntities = new HashSet<>();
    private final PriorityQueue<SleepingEntity> sleeping = new PriorityQueue<>();
    private final ComponentType<ECS_TYPE, ActivelyTickingComponent<ECS_TYPE>> activeFlagComponentType;
    private final ComponentType<ECS_TYPE, TickScheduleComponent<ECS_TYPE>> tickScheduleComponentType;
    private final ResourceType<ECS_TYPE, CurrentWorldTick<ECS_TYPE>> worldTickResourceType;
    private final ResourceType<ECS_TYPE, UUIDLookupResource<ECS_TYPE>> uuidLookupResourceType;
    private final ComponentType<ECS_TYPE, UUIDComponent<ECS_TYPE>> uuidComponentType;

    @SuppressWarnings({ "unchecked", "null" })
    protected OnScheduledTick(
        IRegistry<ECS_TYPE> registry,
        Query<ECS_TYPE> query,
        IOnScheduledTick<ECS_TYPE> listener,
        String id,
        TickSchedule defaultSchedule
    ) {
        super(registry, query);
        this.listener = listener;
        this.id = id;
        this.defaultSchedule = defaultSchedule;
        this.worldTickResourceType = this.registry.getResourceType(CurrentWorldTick.class);
        this.uuidLookupResourceType = this.registry.getResourceType(UUIDLookupResource.class);
        this.activeFlagComponentType = this.registry.getComponentType(ActivelyTickingComponent.class);
        this.uuidComponentType = this.registry.getComponentType(UUIDComponent.class);
        this.tickScheduleComponentType = this.registry.getComponentType(TickScheduleComponent.class);
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
    public final void onAdd(AnyRef<ECS_TYPE> ref, AddReason reason, CommandBuffer<ECS_TYPE> commandBuffer) {
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

        // when added to the ECS we need to make sure its got the components its meant to have
        // -> add the ticking component if it is active and doesn't have it
        // -> add it to the sleeping queue if it's sleeping
        switch (schedule) {
            // it wants to be ticking so make sure its got that component that we query on for ticking entities and move along
            // something about the combination of adding or removing a block + editing components + ticking = race condition
            case TickSchedule.Active _ -> commandBuffer.ensureComponent(ref, this.activeFlagComponentType);
            // it wants to be sleeping, so just need to record when it wants to be awake so i can wake it up (as long as its still loaded when that happens)
            case TickSchedule.Sleeping s -> {
                this.sleeping.add(new SleepingEntity(uuid, s));

                // trash an active flag if im asleep
                if (commandBuffer.getComponent(ref, this.activeFlagComponentType) != null) {
                    commandBuffer.removeComponent(ref, this.activeFlagComponentType);
                }
            }
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
    public final void onRemove(AnyRef<ECS_TYPE> ref, RemoveReason reason, CommandBuffer<ECS_TYPE> commandBuffer) {
        // mark that the sleeper handler should discard element next time it sees it, as we're just, yeah, done with it
        unloadedEntities.add(commandBuffer.ensureAndGetComponent(ref, this.uuidComponentType).getUuid());
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
        final var currentTick = commandBuffer.getResource(this.worldTickResourceType).getTick();

        // first: wake up elements that are ready to wake
        SleepingEntity next;
        // while the next sleeper exists and is waiting to be ticked (<= currentTick)
        while ((next = sleeping.peek()) != null && currentTick + 1 >= next.nextTick) {
            sleeping.remove();

            if (unloadedEntities.contains(next.uuid)) {
                unloadedEntities.remove(next.uuid);
                continue;
            }

            // waking it up is as easy as adding the activeFlagComponent to it.
            // then we just loop and keep going till we run out of things to tick
            final Ref<ECS_TYPE> ref = commandBuffer.getResource(this.uuidLookupResourceType).getRefByUUID(next.uuid);
            if (ref != null && ref.isValid()) {
                commandBuffer.ensureComponent(ref, this.activeFlagComponentType);
            }
        }
    }

    // #endregion awaken

    // ////////////////////////////////////////////////////////////////////////
    // \/======\/-  Tick, and potentially sleep/stop entities  -\/========\/ //
    // ////////////////////////////////////////////////////////////////////////
    /**
     * Tick any entities that are scheduled to run this tick. Those entities can
     * return a plan for when they should tick next, or null to just keep ticking
     */
    @Override
    public void onTick(float dt, AnyRef<ECS_TYPE> ref, CommandBuffer<ECS_TYPE> commandBuffer) {
        if (!ref.isValid()) {
            return;
        }

        final var currentTick = commandBuffer.getResource(this.worldTickResourceType).getTick();

        // run your tick method
        final var res = listener.onScheduledTick(dt, currentTick, ref, commandBuffer);
        if (res == null) {
            return;
        }

        final var tickScheduleComponent = ref.getComponent(this.tickScheduleComponentType);
        if (tickScheduleComponent == null) {
            return;
        }

        // Configure future schedule for this entity according to your returned tick schedule
        switch (res) {
            case TickSchedule.Sleeping newSchedule -> {
                // if they're planning to run next tick, we'll just, ignore that new
                // schedule request. silly dummie
                newSchedule.nextTick += currentTick;
                if (newSchedule.nextTick == currentTick + 1) {
                    break;
                }

                // write us into the sleep queue
                final var uuid = commandBuffer.getComponent(ref, this.uuidComponentType).getUuid();
                this.sleeping.add(new SleepingEntity(uuid, newSchedule));

                // persist our new schedule
                tickScheduleComponent.setSchedule(this.id, newSchedule);
                commandBuffer.putComponent(ref, this.tickScheduleComponentType, tickScheduleComponent);

                // finally: remove the flag that says we're ticking (so we don't anymore)
                // (occurs AFTER our system finishes running all ticks)
                commandBuffer.tryRemoveComponent(ref, this.activeFlagComponentType);
            }
            case TickSchedule.Stopped newSchedule -> {
                // persist our new schedule
                tickScheduleComponent.setSchedule(this.id, newSchedule);
                commandBuffer.putComponent(ref, this.tickScheduleComponentType, tickScheduleComponent);

                // finally: remove the flag that says we're ticking (so we don't anymore)
                // (occurs AFTER our system finishes running all ticks)
                commandBuffer.tryRemoveComponent(ref, this.activeFlagComponentType);
            }
            default -> {
                // default behaviour is to continue ticking, so can sip this
            }
        }
    }

    // ////////////////////////////////////////////////////////////////////////
    // \/==================\/-  Implementations  -\/======================\/ //
    // ////////////////////////////////////////////////////////////////////////
    // #region hide

    /**
     * Hytale expects a new "class" for each system you register. Thus, to have these composable modules
     * of subsystems, each one must secretly create a new class each and every time you call it
     */
    public static <ECS_TYPE extends WorldProvider> OnScheduledTick<ECS_TYPE> newDriverFor(
        IRegistry<ECS_TYPE> registry,
        IQuery<ECS_TYPE> queryProider,
        IOnScheduledTick<ECS_TYPE> listener,
        String id
    ) {
        return newDriverFor(registry, queryProider.getQuery(IOnScheduledTick.class), listener, id);
    }

    /**
     * Hytale expects a new "class" for each system you register. Thus, to have these composable modules
     * of subsystems, each one must secretly create a new class each and every time you call it
     */
    public static <ECS_TYPE extends WorldProvider> OnScheduledTick<ECS_TYPE> newDriverFor(
        IRegistry<ECS_TYPE> registry,
        IQuery<ECS_TYPE> queryProider,
        IOnScheduledTick<ECS_TYPE> listener,
        String id,
        @Nullable TickSchedule defaultSchedule
    ) {
        return newDriverFor(registry, queryProider.getQuery(IOnScheduledTick.class), listener, id, defaultSchedule);
    }

    /**
     * Hytale expects a new "class" for each system you register. Thus, to have these composable modules
     * of subsystems, each one must secretly create a new class each and every time you call it
     */
    public static <ECS_TYPE extends WorldProvider> OnScheduledTick<ECS_TYPE> newDriverFor(
        IRegistry<ECS_TYPE> registry,
        Query<ECS_TYPE> query,
        IOnScheduledTick<ECS_TYPE> listener,
        String id
    ) {
        return newDriverFor(registry, query, listener, id, TickSchedule.ACTIVE);
    }

    /**
     * Hytale expects a new "class" for each system you register. Thus, to have these composable modules
     * of subsystems, each one must secretly create a new class each and every time you call it
     */
    public static <ECS_TYPE extends WorldProvider> OnScheduledTick<ECS_TYPE> newDriverFor(
        IRegistry<ECS_TYPE> registry,
        Query<ECS_TYPE> query,
        IOnScheduledTick<ECS_TYPE> listener,
        String id,
        @Nullable TickSchedule defaultSchedule
    ) {
        return IEventDriver.__construct(
            IEventDriver.__dupeClassAndGetConstructor(
                OnScheduledTick.class,
                IRegistry.class,
                Query.class,
                IOnScheduledTick.class,
                String.class,
                TickSchedule.class
            ),
            registry,
            query,
            listener,
            id,
            defaultSchedule
        );
    }

    // #endregion hide
}
