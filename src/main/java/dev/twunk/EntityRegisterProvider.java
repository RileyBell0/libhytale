package dev.twunk;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.twunk.plugin.ModPlugin;
import dev.twunk.subsystem.ISubSystem;
import dev.twunk.subsystem.composite.interfaces.IRegistry;
import java.util.HashMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class EntityRegisterProvider implements IRegistry<EntityStore> {

    @Nonnull
    public static final HashMap<Class<? extends Component<EntityStore>>, ComponentType<EntityStore, ? extends Component<EntityStore>>> registeredEntityComponents = new HashMap<>();

    @Nonnull
    public static final HashMap<String, ComponentType<EntityStore, ? extends Component<EntityStore>>> registeredEntityComponentsById = new HashMap<>();

    @Nonnull
    public final <T extends Component<EntityStore>> ComponentType<EntityStore, T> getComponentType(
            @Nonnull final Class<T> componentClass) {
        return TwunkLib.getEntityComponentType(componentClass);
    }

    @Nullable
    public final ComponentType<EntityStore, ? extends Component<EntityStore>> getComponentType(
            @Nonnull final String componentId) {
        return TwunkLib.getEntityComponentType(componentId);
    }

    /**
     * Registers the component type with the static map that stores
     * all that goodness for us. IRegisteredComponent gets to know
     * about EVERYTHING above it WOOOO
     */
    public final <T extends Component<EntityStore>> void registerComponentType(
            @Nonnull final ComponentType<EntityStore, T> componentType,
            @Nonnull final Class<T> myClass,
            @Nonnull final String id) {
        TwunkLib.registerEntityComponentType(componentType, myClass, id);
    }

    public final void registerSystem(@Nonnull final ModPlugin plugin, @Nonnull final ISubSystem<EntityStore> system) {
        plugin.getEntityStoreRegistry().registerSystem(system);
    }
}
