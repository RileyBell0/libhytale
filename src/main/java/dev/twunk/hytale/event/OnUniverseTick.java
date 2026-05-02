package dev.twunk.hytale.event;

import com.hypixel.hytale.component.SystemGroup;
import com.hypixel.hytale.component.dependency.Dependency;
import com.hypixel.hytale.component.system.tick.ArchetypeTickingSystem;
import com.hypixel.hytale.component.system.tick.TickingSystem;
import com.hypixel.hytale.server.core.universe.world.WorldProvider;
import dev.twunk.hytale.interfaces.IEventDriver;
import dev.twunk.hytale.interfaces.ISystemEventDriver;
import dev.twunk.hytale.interfaces.event.IOnUniverseTick;
import dev.twunk.hytale.interfaces.methods.IRegistry;
import dev.twunk.lib.event.OnUniverseTick__Listener;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.Nullable;

/**
 * Subsystem for calling `onSystemTick` on the parent system every tick
 *
 * GOAL: run code ONCE per tick globally. not per element, just, run this once per tick
 *
 * REQUIRES:
 * - N/A (this is a leaf)
 * PRODUCES:
 * - IGlobalTickSystem runner
 *
 * My code
 * @see IOnUniverseTick - Something that this subsystem can call and run.
 *
 * Hytale's code
 * @see ArchetypeTickingSystem - I use this to run the subsystem. Only way i currently know
 *                               of for getting a commandBuffer in a global tick
 */
public abstract class OnUniverseTick<ECS_TYPE extends WorldProvider>
    extends TickingSystem<ECS_TYPE> // hytale's underlying driver for my code
    implements ISystemEventDriver<ECS_TYPE>
{

    private final IRegistry<ECS_TYPE> registry;

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

    @Nullable
    private SystemGroup<ECS_TYPE> group = null;

    @Override
    @Nullable
    public SystemGroup<ECS_TYPE> getGroup() {
        return this.group;
    }

    public void setGroup(@Nullable SystemGroup<ECS_TYPE> group) {
        this.group = group;
    }

    protected OnUniverseTick(IRegistry<ECS_TYPE> registry) {
        this.registry = registry;
    }

    @Override
    public final IRegistry<ECS_TYPE> getRegistry() {
        return this.registry;
    }

    // ////////////////////////////////////////////////////////////////////////
    // \/==================\/-  Implementations  -\/======================\/ //
    // ////////////////////////////////////////////////////////////////////////
    // #region hide

    public static final <ECS_TYPE extends WorldProvider> OnUniverseTick<ECS_TYPE> newDriverFor(
        IRegistry<ECS_TYPE> registry,
        IOnUniverseTick<ECS_TYPE> listener
    ) {
        return IEventDriver.__construct(
            IEventDriver.__dupeClassAndGetConstructor(
                OnUniverseTick__Listener.class,
                IRegistry.class,
                IOnUniverseTick.class
            ),
            registry,
            listener
        );
    }

    // #endregion hide
}
