package dev.twunk.subsystem.composite.interfaces;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.WorldProvider;
import dev.twunk.plugin.ModPlugin;
import dev.twunk.subsystem.ISubSystem;
import javax.annotation.Nonnull;

public interface IRegistry<ECS_STORE extends WorldProvider> {
    public <T extends Component<ECS_STORE>> ComponentType<ECS_STORE, T> getComponentType(
        @Nonnull final Class<T> componentClass
    );
    public ComponentType<ECS_STORE, ? extends Component<ECS_STORE>> getComponentType(
        @Nonnull final String componentClass
    );

    public <T extends Component<ECS_STORE>> void registerComponentType(
        @Nonnull final Class<T> myClass,
        @Nonnull final String id,
        @Nonnull final ComponentType<ECS_STORE, T> componentType
    );

    public void registerSystem(@Nonnull final ModPlugin plugin, @Nonnull final ISubSystem<ECS_STORE> system);
}
