package dev.twunk.hytale.interfaces.config;

import com.hypixel.hytale.component.SystemGroup;
import com.hypixel.hytale.component.dependency.Dependency;
import com.hypixel.hytale.component.system.ISystem;
import com.hypixel.hytale.server.core.universe.world.WorldProvider;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Set;

public interface IEventConfig<
        ECS_TYPE extends WorldProvider
        > extends IGroup<ECS_TYPE>, IDependencies<ECS_TYPE>, ISystem<ECS_TYPE> {
    @Override
    @Nullable
    default SystemGroup<ECS_TYPE> getGroup() {
        return null;
    }

    @Override
    @SuppressWarnings("null")
    @Nonnull
    default Set<Dependency<ECS_TYPE>> getDependencies() {
        return Collections.emptySet();
    }
}
