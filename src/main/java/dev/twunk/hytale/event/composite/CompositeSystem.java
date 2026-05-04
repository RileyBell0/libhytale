package dev.twunk.hytale.event.composite;

import com.hypixel.hytale.component.SystemGroup;
import com.hypixel.hytale.component.dependency.Dependency;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.universe.world.WorldProvider;
import dev.twunk.hytale.interfaces.IEventDriver;
import dev.twunk.hytale.interfaces.methods.IRegistry;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.Nullable;

public class CompositeSystem<ECS_TYPE extends WorldProvider> implements IEventDriver<ECS_TYPE> {

    private Set<Dependency<ECS_TYPE>> dependencies = new HashSet<>();

    @Nullable
    private SystemGroup<ECS_TYPE> group = null;

    protected final IRegistry<ECS_TYPE> registry;

    protected CompositeSystem(IRegistry<ECS_TYPE> registry) {
        this.registry = registry;
    }

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
    public final IRegistry<ECS_TYPE> getRegistry() {
        return this.registry;
    }

    @Override
    public void onRegister(JavaPlugin plugin) {
        this.getRegistry().registerEventListeners(plugin, this);
    }
}
