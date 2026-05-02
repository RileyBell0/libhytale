package dev.twunk.hytale.interfaces.config;

import com.hypixel.hytale.component.SystemGroup;
import com.hypixel.hytale.component.dependency.Dependency;
import com.hypixel.hytale.component.system.ISystem;
import com.hypixel.hytale.server.core.universe.world.WorldProvider;
import java.util.Collections;
import java.util.Set;
import javax.annotation.Nullable;

public interface IEventConfig<
    ECS_TYPE extends WorldProvider
> extends IGroup<ECS_TYPE>, IDependencies<ECS_TYPE>, ISystem<ECS_TYPE> {
    @Override
    @Nullable
    public default SystemGroup<ECS_TYPE> getGroup() {
        return null;
    }

    @Override
    @SuppressWarnings("null")
    public default Set<Dependency<ECS_TYPE>> getDependencies() {
        return Collections.emptySet();
    }
}
