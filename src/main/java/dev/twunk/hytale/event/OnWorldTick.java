package dev.twunk.hytale.event;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.SystemGroup;
import com.hypixel.hytale.component.dependency.Dependency;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.ArchetypeTickingSystem;
import com.hypixel.hytale.server.core.universe.world.WorldProvider;
import dev.twunk.hytale.interfaces.IEventDriver;
import dev.twunk.hytale.interfaces.ISystemEventDriver;
import dev.twunk.hytale.interfaces.config.IQuery;
import dev.twunk.hytale.interfaces.event.IOnWorldTick;
import dev.twunk.hytale.interfaces.methods.IRegistry;
import dev.twunk.lib.event.OnWorldTick__Listener;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import javax.annotation.Nullable;

public class OnWorldTick<ECS_TYPE extends WorldProvider>
    extends ArchetypeTickingSystem<ECS_TYPE>
    implements ISystemEventDriver<ECS_TYPE>
{

    private final Query<ECS_TYPE> query;
    private final IRegistry<ECS_TYPE> registry;

    protected OnWorldTick(IRegistry<ECS_TYPE> registry, Query<ECS_TYPE> query) {
        this.query = query;
        this.registry = registry;
    }

    @Nullable
    private SystemGroup<ECS_TYPE> group = null;

    private Set<Dependency<ECS_TYPE>> dependencies = new HashSet<>();

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

    @Override
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

    @Override
    public void tick(
        float dt,
        ArchetypeChunk<ECS_TYPE> archetypeChunk,
        Store<ECS_TYPE> store,
        CommandBuffer<ECS_TYPE> commandBuffer
    ) {
        // stub
    }

    // ////////////////////////////////////////////////////////////////////////
    // \/==================\/-  Implementations  -\/======================\/ //
    // ////////////////////////////////////////////////////////////////////////
    // #region hide

    /**
     * Shim around other method for reducing boilerplate if i define a query on my class
     */
    public static final <
        ECS_TYPE extends WorldProvider,
        T extends IOnWorldTick<ECS_TYPE> & IQuery<ECS_TYPE>
    > OnWorldTick<ECS_TYPE> newDriverFor(IRegistry<ECS_TYPE> registry, T listener) {
        return newDriverFor(registry, listener.getQuery(IOnWorldTick.class), listener);
    }

    public static final <ECS_TYPE extends WorldProvider> OnWorldTick<ECS_TYPE> newDriverFor(
        IRegistry<ECS_TYPE> registry,
        Query<ECS_TYPE> query,
        IOnWorldTick<ECS_TYPE> listener
    ) {
        return IEventDriver.__construct(
            IEventDriver.__dupeClassAndGetConstructor(
                OnWorldTick__Listener.class,
                IRegistry.class,
                Query.class,
                IOnWorldTick.class
            ),
            registry,
            query,
            listener
        );
    }

    public static final <ECS_TYPE extends WorldProvider> OnWorldTick<ECS_TYPE> newDriverFor(
        IRegistry<ECS_TYPE> registry,
        IQuery<ECS_TYPE> queryProider,
        IOnWorldTick<ECS_TYPE> listener
    ) {
        return IEventDriver.__construct(
            IEventDriver.__dupeClassAndGetConstructor(
                OnWorldTick__Listener.class,
                IRegistry.class,
                Query.class,
                IOnWorldTick.class
            ),
            registry,
            queryProider.getQuery(IOnWorldTick.class),
            listener
        );
    }

    public static final <ECS_TYPE extends WorldProvider> OnWorldTick<ECS_TYPE> newDriverFor(
        IRegistry<ECS_TYPE> registry,
        Function<Class<?>, Query<ECS_TYPE>> queryProider,
        IOnWorldTick<ECS_TYPE> listener
    ) {
        return IEventDriver.__construct(
            IEventDriver.__dupeClassAndGetConstructor(
                OnWorldTick__Listener.class,
                IRegistry.class,
                Query.class,
                IOnWorldTick.class
            ),
            registry,
            queryProider.apply(IOnWorldTick.class),
            listener
        );
    }

    // #endregion hide
}
