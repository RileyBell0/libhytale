package dev.twunk.hytale.event;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.SystemGroup;
import com.hypixel.hytale.component.dependency.Dependency;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.ArchetypeTickingSystem;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.universe.world.WorldProvider;
import dev.twunk.hytale.interfaces.IEventDriver;
import dev.twunk.hytale.interfaces.ISystemEventDriver;
import dev.twunk.hytale.interfaces.config.IQuery;
import dev.twunk.hytale.interfaces.event.IOnTick;
import dev.twunk.hytale.interfaces.methods.IRegistry;
import dev.twunk.lib.event.OnTick__Component;
import dev.twunk.lib.event.OnTick__Listener;
import java.util.Collections;
import java.util.Set;
import javax.annotation.Nullable;

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
    implements ISystemEventDriver<ECS_TYPE>
{

    private final Query<ECS_TYPE> query;
    private final IRegistry<ECS_TYPE> registry;

    @SuppressWarnings("null")
    private Set<Dependency<ECS_TYPE>> dependencies = Collections.emptySet();

    @Override
    public Set<Dependency<ECS_TYPE>> getDependencies() {
        return this.dependencies;
    }

    public void setDependencies(Set<Dependency<ECS_TYPE>> dependencies) {
        this.dependencies = dependencies;
    }

    @Nullable
    private SystemGroup<ECS_TYPE> group = null;

    @Override
    @Nullable
    public SystemGroup<ECS_TYPE> getGroup() {
        return this.group;
    }

    public void setGroup(SystemGroup<ECS_TYPE> group) {
        this.group = group;
    }

    protected OnTick(IRegistry<ECS_TYPE> registry, Query<ECS_TYPE> query) {
        this.query = query;
        this.registry = registry;
    }

    @Override
    public final Query<ECS_TYPE> getQuery() {
        return this.query;
    }

    @Override
    public final IRegistry<ECS_TYPE> getRegistry() {
        return this.registry;
    }

    // ////////////////////////////////////////////////////////////////////////
    // \/==================\/-  Implementations  -\/======================\/ //
    // ////////////////////////////////////////////////////////////////////////
    // #region hide

    /**
     * Shim around other method for reducing boilerplate if i define a query on my class
     */
    public static final <ECS_TYPE extends WorldProvider, T extends IOnTick<ECS_TYPE> & IQuery<ECS_TYPE>> OnTick<
        ECS_TYPE
    > newDriverFor(IRegistry<ECS_TYPE> registry, T listener) {
        return newDriverFor(registry, listener.getQuery(IOnTick.class), listener);
    }

    public static final <ECS_TYPE extends WorldProvider> OnTick<ECS_TYPE> newDriverFor(
        IRegistry<ECS_TYPE> registry,
        Query<ECS_TYPE> query,
        IOnTick<ECS_TYPE> listener
    ) {
        return IEventDriver.__construct(
            IEventDriver.__dupeClassAndGetConstructor(
                OnTick__Listener.class,
                IRegistry.class,
                Query.class,
                IOnTick.class
            ),
            registry,
            query,
            listener
        );
    }

    /**
     * Bound for T fully defined here
     */
    public static final <ECS_TYPE extends WorldProvider, T extends Component<ECS_TYPE>> OnTick<ECS_TYPE> newDriverFor(
        IRegistry<ECS_TYPE> registry,
        Query<ECS_TYPE> query,
        ComponentType<ECS_TYPE, T> componentType
    ) {
        return IEventDriver.__construct(
            IEventDriver.__dupeClassAndGetConstructor(
                OnTick__Component.class,
                IRegistry.class,
                Query.class,
                ComponentType.class
            ),
            registry,
            query,
            componentType
        );
    }
    // #endregion hide
}
