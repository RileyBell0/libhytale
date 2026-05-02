package dev.twunk.hytale.interfaces.config;

import com.hypixel.hytale.component.SystemGroup;
import com.hypixel.hytale.server.core.universe.world.WorldProvider;
import javax.annotation.Nullable;

public interface IGroup<ECS_TYPE extends WorldProvider> {
    @Nullable
    public default SystemGroup<ECS_TYPE> getGroup() {
        return null;
    }

    @Nullable
    public default SystemGroup<ECS_TYPE> getGroup(Class<?> clazz) {
        return this.getGroup();
    }
}
