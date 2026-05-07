package dev.twunk.hytale.interfaces.config;

import com.hypixel.hytale.component.dependency.Dependency;
import com.hypixel.hytale.server.core.universe.world.WorldProvider;
import java.util.Collections;
import java.util.Set;

public interface IDependencies<ECS_TYPE extends WorldProvider> {
    @SuppressWarnings("null")
    default Set<Dependency<ECS_TYPE>> getDependencies() {
        return Collections.emptySet();
    }

    default Set<Dependency<ECS_TYPE>> getDependencies(Class<?> clazz) {
        return this.getDependencies();
    }
}
