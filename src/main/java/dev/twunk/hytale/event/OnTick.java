package dev.twunk.hytale.event;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
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
import dev.twunk.hytale.ref.AnyRef;
import java.util.HashSet;
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

    private Set<Dependency<ECS_TYPE>> dependencies = new HashSet<>();

    @Nullable
    private SystemGroup<ECS_TYPE> group = null;

    private final IOnTick<ECS_TYPE> listener;
    private final Query<ECS_TYPE> query;
    private final IRegistry<ECS_TYPE> registry;

    protected OnTick(IRegistry<ECS_TYPE> registry, Query<ECS_TYPE> query, IOnTick<ECS_TYPE> listener) {
        this.query = query;
        this.registry = registry;
        this.listener = listener;
    }

    // ////////////////////////////////////////////////////////////////////////
    // \/======================\/-  Methods  -\/==========================\/ //
    // ////////////////////////////////////////////////////////////////////////

    public final void tick(
        float dt,
        int index,
        ArchetypeChunk<ECS_TYPE> archetypeChunk,
        Store<ECS_TYPE> store,
        CommandBuffer<ECS_TYPE> commandBuffer
    ) {
        listener.onTick(dt, AnyRef.of(archetypeChunk.getReferenceTo(index)), commandBuffer);
    }

    // ////////////////////////////////////////////////////////////////////////
    // \/==================\/-  Getters/setters  -\/======================\/ //
    // ////////////////////////////////////////////////////////////////////////
    // #region getters/setters

    @Override
    public Set<Dependency<ECS_TYPE>> getDependencies() {
        return this.dependencies;
    }

    @Override
    public void setDependencies(Set<Dependency<ECS_TYPE>> dependencies) {
        this.dependencies = new HashSet<>();
        this.dependencies.addAll(dependencies);
    }

    @Override
    public boolean addDependency(Dependency<ECS_TYPE> dependency) {
        return this.dependencies.add(dependency);
    }

    @Override
    @Nullable
    public SystemGroup<ECS_TYPE> getGroup() {
        return this.group;
    }

    public void setGroup(@Nullable SystemGroup<ECS_TYPE> group) {
        this.group = group;
    }

    @Override
    public final Query<ECS_TYPE> getQuery() {
        return this.query;
    }

    @Override
    public final IRegistry<ECS_TYPE> getRegistry() {
        return this.registry;
    }

    // #endregion getters/setters

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
            IEventDriver.__dupeClassAndGetConstructor(OnTick.class, IRegistry.class, Query.class, IOnTick.class),
            registry,
            query,
            listener
        );
    }

    public static final <ECS_TYPE extends WorldProvider> OnTick<ECS_TYPE> newDriverFor(
        IRegistry<ECS_TYPE> registry,
        IQuery<ECS_TYPE> queryProider,
        IOnTick<ECS_TYPE> listener
    ) {
        return IEventDriver.__construct(
            IEventDriver.__dupeClassAndGetConstructor(OnTick.class, IRegistry.class, Query.class, IOnTick.class),
            registry,
            queryProider.getQuery(IOnTick.class),
            listener
        );
    }

    // #endregion hide
}
