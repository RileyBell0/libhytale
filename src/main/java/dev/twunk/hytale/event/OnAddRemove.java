package dev.twunk.hytale.event;

import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.dependency.Dependency;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.RefSystem;
import com.hypixel.hytale.component.system.tick.ArchetypeTickingSystem;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.universe.world.WorldProvider;
import dev.twunk.hytale.interfaces.IEventDriver;
import dev.twunk.hytale.interfaces.ISystemEventDriver;
import dev.twunk.hytale.interfaces.config.IQuery;
import dev.twunk.hytale.interfaces.event.IOnAddRemove;
import dev.twunk.hytale.interfaces.event.IOnTick;
import dev.twunk.hytale.interfaces.methods.IRegistry;
import dev.twunk.hytale.ref.AnyRef;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;

/// WARNING: dependencies don't seem to really matter for this, always seems to get called at the start of the tick
///
/// Tiny Subsystem to simply tell our parent system when we added/removed entities
/// that match our parent's query
///
/// GOAL: Need to know when entities load/unload (and optionally why they got added/removed)
///
/// REQUIRES:
/// - N/A (this is a leaf)
/// PRODUCES:
/// - IOnAddRemoveSystem runner
///
/// My code
///
/// @see IOnAddRemove       - Methods for listening to entity add/remove events
/// @see OnTick  - Underlying SubSystem that powers the IEntityTick methods
///                             for IEntityTickSystems that register an EntityTickSubSystem
/// @see IOnTick           - Underlying method for ticking an entity
///
/// Hytale's code
/// @see EntityTickingSystem    - Baseline hytale system for ticking entities.
///                               It's the underlying driver of IEntityTickSubSystem
/// @see ArchetypeTickingSystem - Underlying sort of baseline ticking system (that i know how to implement).
///                               Runs ONCE per tick (global, not per matching entity, just runs a single
///                               time per tick) and has an inbuilt query
public abstract class OnAddRemove<ECS_TYPE extends WorldProvider>
        extends RefSystem<ECS_TYPE>
        implements ISystemEventDriver<ECS_TYPE> {

    private final Query<ECS_TYPE> query;
    private final IRegistry<ECS_TYPE> registry;
    private final IOnAddRemove<ECS_TYPE> listener;

    @Nullable
    private SystemGroup<ECS_TYPE> group = null;

    /**
     * WARNING: dependencies don't seem to really matter for OnAddRemove, always seems to get called at the start of the tick
     */
    private Set<Dependency<ECS_TYPE>> dependencies = new HashSet<>();

    protected OnAddRemove(IRegistry<ECS_TYPE> registry, Query<ECS_TYPE> query, IOnAddRemove<ECS_TYPE> listener) {
        this.query = query;
        this.registry = registry;
        this.listener = listener;
    }

    // ////////////////////////////////////////////////////////////////////////
    // \/======================\/-  Methods  -\/==========================\/ //
    // ////////////////////////////////////////////////////////////////////////

    @Override
    public void onEntityAdded(
            Ref<ECS_TYPE> ref,
            AddReason reason,
            Store<ECS_TYPE> store,
            CommandBuffer<ECS_TYPE> commandBuffer
    ) {
        listener.onAdd(AnyRef.of(ref), reason, commandBuffer);
    }

    @Override
    public void onEntityRemove(
            Ref<ECS_TYPE> ref,
            RemoveReason reason,
            Store<ECS_TYPE> store,
            CommandBuffer<ECS_TYPE> commandBuffer
    ) {
        listener.onRemove(AnyRef.of(ref), reason, commandBuffer);
    }

    // ////////////////////////////////////////////////////////////////////////
    // \/==================\/-  Getters/setters  -\/======================\/ //
    // ////////////////////////////////////////////////////////////////////////
    // #region getters/setters

    /**
     * WARNING: dependencies don't seem to really matter for OnAddRemove, always seems to get called at the start of the tick
     */
    @Override
    public Set<Dependency<ECS_TYPE>> getDependencies() {
        return this.dependencies;
    }

    /**
     * WARNING: dependencies don't seem to really matter for OnAddRemove, always seems to get called at the start of the tick
     */
    @Override
    public void setDependencies(Set<Dependency<ECS_TYPE>> dependencies) {
        this.dependencies = new HashSet<>();
        this.dependencies.addAll(dependencies);
    }

    /**
     * WARNING: dependencies don't seem to really matter for OnAddRemove, always seems to get called at the start of the tick
     */
    @Override
    public boolean addDependency(Dependency<ECS_TYPE> dependency) {
        return this.dependencies.add(dependency);
    }

    @Override
    @Nullable
    public SystemGroup<ECS_TYPE> getGroup() {
        return this.group;
    }

    @Override
    public void setGroup(@Nullable SystemGroup<ECS_TYPE> group) {
        this.group = group;
    }

    @Override
    public Query<ECS_TYPE> getQuery() {
        return this.query;
    }

    @Override
    public IRegistry<ECS_TYPE> getRegistry() {
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
    public static <
            ECS_TYPE extends WorldProvider,
            T extends IOnAddRemove<ECS_TYPE> & IQuery<ECS_TYPE>
            > OnAddRemove<ECS_TYPE> newDriverFor(IRegistry<ECS_TYPE> registry, T listener) {
        return newDriverFor(registry, listener.getQuery(IOnAddRemove.class), listener);
    }

    public static <ECS_TYPE extends WorldProvider> OnAddRemove<ECS_TYPE> newDriverFor(
            IRegistry<ECS_TYPE> registry,
            IQuery<ECS_TYPE> queryProvider,
            IOnAddRemove<ECS_TYPE> listener
    ) {
        return newDriverFor(registry, queryProvider.getQuery(IOnAddRemove.class), listener);
    }

    /**
     * Hytale expects a new "class" for each system you register. Thus, to have these composable modules
     * of subsystems, each one must secretly create a new class each and every time you call it
     */
    public static <ECS_TYPE extends WorldProvider> OnAddRemove<ECS_TYPE> newDriverFor(
            IRegistry<ECS_TYPE> registry,
            Query<ECS_TYPE> query,
            IOnAddRemove<ECS_TYPE> listener
    ) {
        return IEventDriver.__construct(
                IEventDriver.__dupeClassAndGetConstructor(
                        OnAddRemove.class,
                        IRegistry.class,
                        Query.class,
                        IOnAddRemove.class
                ),
                registry,
                query,
                listener
        );
    }

    // #endregion hide
}
