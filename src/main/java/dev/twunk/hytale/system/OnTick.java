package dev.twunk.hytale.system;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.ArchetypeTickingSystem;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.universe.world.WorldProvider;
import dev.twunk.hytale.system.ignoreme.OnTick__Component;
import dev.twunk.hytale.system.ignoreme.OnTick__Listener;
import dev.twunk.interfaces.IEventDriver;
import dev.twunk.interfaces.events.IOnTick;
import dev.twunk.interfaces.methods.IQuery;
import dev.twunk.interfaces.methods.IRegistry;

/**
 * Subsystem for calling `onEntityTick` on the parent system every tick
 *
 * GOAL: run code on entities every tick
 *
 * REQUIRES:
 * - N/A (this is a leaf)
 * PRODUCES:
 * - IEntityTickSystem runner
 *
 *
 * My code
 * @see IOnTick       - Underlying method for ticking an entity
 *
 * Hytale's code
 * @see EntityTickingSystem    - Baseline hytale system for ticking entities.
 *                               It's the underlying driver of IEntityTickSubSystem
 * @see ArchetypeTickingSystem - Underlying sort of baseline ticking system (that i know how to implement).
 *                               Runs ONCE per tick (global, not per matching entity, just runs a single
 *                               time per tick) and has an inbuilt query
 */
public abstract class OnTick<ECS_TYPE extends WorldProvider>
    extends EntityTickingSystem<ECS_TYPE> // EntityTickingSystem is hytale's underlying code that powers this
    implements IEventDriver<ECS_TYPE>
{

    private final Query<ECS_TYPE> query;
    private final IRegistry<ECS_TYPE> registry;

    protected OnTick(Query<ECS_TYPE> query, IRegistry<ECS_TYPE> registry) {
        this.query = query;
        this.registry = registry;
    }

    ///////////////////////////////////////////////////////////////////////////
    // \/======================\/-  Methods  -\/==========================\/ //
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public abstract void tick(
        float dt,
        int index,
        ArchetypeChunk<ECS_TYPE> archetypeChunk,
        Store<ECS_TYPE> store,
        CommandBuffer<ECS_TYPE> commandBuffer
    );

    @Override
    public final void onRegister(JavaPlugin plugin) {
        this.getRegistry().registerSystem(plugin, this);
    }

    @Override
    public final Query<ECS_TYPE> getQuery() {
        return this.query;
    }

    @Override
    public final IRegistry<ECS_TYPE> getRegistry() {
        return this.registry;
    }

    ///////////////////////////////////////////////////////////////////////////
    // \/==================\/-  Implementations  -\/======================\/ //
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Shim around other method for reducing boilerplate if i define a query on my class
     */
    public static final <ECS_TYPE extends WorldProvider, T extends IOnTick<ECS_TYPE> & IQuery<ECS_TYPE>> OnTick<
        ECS_TYPE
    > newUninitialised(T listener, IRegistry<ECS_TYPE> registry) {
        return newUninitialised(listener, listener.getQuery(), registry);
    }

    public static final <ECS_TYPE extends WorldProvider> OnTick<ECS_TYPE> newUninitialised(
        IOnTick<ECS_TYPE> listener,
        Query<ECS_TYPE> query,
        IRegistry<ECS_TYPE> registry
    ) {
        return IEventDriver.__construct(
            IEventDriver.__dupeClassAndGetConstructor(
                OnTick__Listener.class,
                IOnTick.class,
                Query.class,
                IRegistry.class
            ),
            listener,
            query,
            registry
        );
    }

    /**
     * Bound for T fully defined here
     */
    public static final <ECS_TYPE extends WorldProvider, T extends Component<ECS_TYPE>> OnTick<
        ECS_TYPE
    > newUninitialised(ComponentType<ECS_TYPE, T> componentType, IRegistry<ECS_TYPE> registry) {
        return IEventDriver.__construct(
            IEventDriver.__dupeClassAndGetConstructor(OnTick__Component.class, ComponentType.class, IRegistry.class),
            componentType,
            registry
        );
    }
}
