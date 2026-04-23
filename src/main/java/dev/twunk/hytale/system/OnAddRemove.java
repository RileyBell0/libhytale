package dev.twunk.hytale.system;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.RefSystem;
import com.hypixel.hytale.component.system.tick.ArchetypeTickingSystem;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.universe.world.WorldProvider;
import dev.twunk.hytale.system.ignoreme.OnAddRemove__Component;
import dev.twunk.hytale.system.ignoreme.OnAddRemove__Listener;
import dev.twunk.interfaces.IEventDriver;
import dev.twunk.interfaces.events.IOnAddRemove;
import dev.twunk.interfaces.events.IOnTick;
import dev.twunk.interfaces.methods.IQuery;
import dev.twunk.interfaces.methods.IRegistry;

/**
 * Tiny Subsystem to simply tell our parent system when we added/removed entities
 * that match our parent's query
 *
 * GOAL: Need to know when entities load/unload (and optionally why they got added/removed)
 *
 * REQUIRES:
 * - N/A (this is a leaf)
 * PRODUCES:
 * - IOnAddRemoveSystem runner
 *
 * My code
 * @see IOnAddRemove       - Methods for listening to entity add/remove events
 * @see OnTick  - Underlying SubSystem that powers the IEntityTick methods
 *                             for IEntityTickSystems that register an EntityTickSubSystem
 * @see IOnTick           - Underlying method for ticking an entity
 *
 * Hytale's code
 * @see EntityTickingSystem    - Baseline hytale system for ticking entities.
 *                               It's the underlying driver of IEntityTickSubSystem
 * @see ArchetypeTickingSystem - Underlying sort of baseline ticking system (that i know how to implement).
 *                               Runs ONCE per tick (global, not per matching entity, just runs a single
 *                               time per tick) and has an inbuilt query
 */
public abstract class OnAddRemove<ECS_TYPE extends WorldProvider>
    extends RefSystem<ECS_TYPE>
    implements IEventDriver<ECS_TYPE>
{

    private final Query<ECS_TYPE> query;
    private final IRegistry<ECS_TYPE> registry;

    protected OnAddRemove(Query<ECS_TYPE> query, IRegistry<ECS_TYPE> registry) {
        this.query = query;
        this.registry = registry;
    }

    ///////////////////////////////////////////////////////////////////////////
    // \/======================\/-  Methods  -\/==========================\/ //
    ///////////////////////////////////////////////////////////////////////////

    /**
     * To be defined in the subclasses
     */
    @Override
    public abstract void onEntityAdded(
        Ref<ECS_TYPE> ref,
        AddReason reason,
        Store<ECS_TYPE> store,
        CommandBuffer<ECS_TYPE> commandBuffer
    );

    @Override
    public abstract void onEntityRemove(
        Ref<ECS_TYPE> ref,
        RemoveReason reason,
        Store<ECS_TYPE> store,
        CommandBuffer<ECS_TYPE> commandBuffer
    );

    @Override
    public final void onRegister(JavaPlugin plugin) {
        this.getRegistry().registerSystem(plugin, this);
    }

    @Override
    public Query<ECS_TYPE> getQuery() {
        return this.query;
    }

    @Override
    public IRegistry<ECS_TYPE> getRegistry() {
        return this.registry;
    }

    ///////////////////////////////////////////////////////////////////////////
    // \/==================\/-  Implementations  -\/======================\/ //
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Shim around other method for reducing boilerplate if i define a query on my class
     */
    public static final <ECS_TYPE extends WorldProvider, T extends IQuery<ECS_TYPE>> OnAddRemove<
        ECS_TYPE
    > newUninitialised(T listener, IRegistry<ECS_TYPE> registry) {
        @SuppressWarnings("unchecked")
        var asIOnAddRemove = (IOnAddRemove<ECS_TYPE>) listener;
        return newUninitialised(asIOnAddRemove, listener.getQuery(), registry);
    }

    /**
     * Hytale expects a new "class" for each system you register. Thus, to have these composable modules
     * of subsystems, each one must secretly create a new class each and every time you call it
     */
    public static <ECS_TYPE extends WorldProvider> OnAddRemove<ECS_TYPE> newUninitialised(
        IOnAddRemove<ECS_TYPE> listener,
        Query<ECS_TYPE> query,
        IRegistry<ECS_TYPE> registry
    ) {
        return IEventDriver.__construct(
            IEventDriver.__dupeClassAndGetConstructor(
                OnAddRemove__Listener.class,
                IOnAddRemove.class,
                Query.class,
                IRegistry.class
            ),
            listener,
            query,
            registry
        );
    }

    /**
     * Hytale expects a new "class" for each system you register. Thus, to have these composable modules
     * of subsystems, each one must secretly create a new class each and every time you call it
     *
     * Bound for T fully defined here
     */
    public static final <ECS_TYPE extends WorldProvider, T extends Component<ECS_TYPE>> OnAddRemove<
        ECS_TYPE
    > newUninitialised(ComponentType<ECS_TYPE, T> componentType, IRegistry<ECS_TYPE> registry) {
        return IEventDriver.__construct(
            IEventDriver.__dupeClassAndGetConstructor(
                OnAddRemove__Component.class,
                ComponentType.class,
                IRegistry.class
            ),
            componentType,
            registry
        );
    }
}
