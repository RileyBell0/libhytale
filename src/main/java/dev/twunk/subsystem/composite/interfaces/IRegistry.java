package dev.twunk.subsystem.composite.interfaces;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.WorldProvider;
import dev.twunk.plugin.ModPlugin;
import dev.twunk.subsystem.ISubSystem;
import javax.annotation.Nonnull;

public interface IRegistry<ECS_STORE extends WorldProvider> {
    public <T extends Component<ECS_STORE>> ComponentType<ECS_STORE, T> getComponentType(
        final @Nonnull Class<T> componentClass
    );

    public ComponentType<ECS_STORE, ? extends Component<ECS_STORE>> getComponentType(
        final @Nonnull String componentClass
    );

    public <T extends Component<ECS_STORE>> void registerComponentType(
        final @Nonnull ComponentType<ECS_STORE, T> componentType,
        final @Nonnull Class<T> myClass,
        final @Nonnull String id
    );

    public void registerSystem(final @Nonnull ModPlugin plugin, final @Nonnull ISubSystem<ECS_STORE> system);
}
