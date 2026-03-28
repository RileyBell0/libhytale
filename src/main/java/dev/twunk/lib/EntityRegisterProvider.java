package dev.twunk.lib;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.twunk.hytale.HytalePlugin;
import dev.twunk.interfaces.ISubSystem;
import dev.twunk.interfaces.methods.IRegistry;
import java.util.HashMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class EntityRegisterProvider implements IRegistry<EntityStore> {

    @Nonnull
    public static final HashMap<
        Class<? extends Component<EntityStore>>,
        ComponentType<EntityStore, ? extends Component<EntityStore>>
    > registeredEntityComponents = new HashMap<>();

    @Nonnull
    public static final HashMap<
        String,
        ComponentType<EntityStore, ? extends Component<EntityStore>>
    > registeredEntityComponentsById = new HashMap<>();

    @Nonnull
    @SuppressWarnings("unchecked")
    public final <T extends Component<EntityStore>> ComponentType<EntityStore, T> getComponentType(
        final @Nonnull Class<T> componentClass
    ) {
        var componentType = registeredEntityComponents.get(componentClass);
        if (componentType == null) {
            throw new RuntimeException(
                "Called getComponentType on class " + componentClass + " before initialising said class"
            );
        }

        // casting is safe as long as i haven't stuffed something up
        return (ComponentType<EntityStore, T>) componentType;
    }

    @Nullable
    public final ComponentType<EntityStore, ? extends Component<EntityStore>> getComponentType(
        final @Nonnull String componentId
    ) {
        return registeredEntityComponentsById.get(componentId);
    }

    /**
     * Registers the component type with the static map that stores
     * all that goodness for us. IRegisteredComponent gets to know
     * about EVERYTHING above it WOOOO
     */
    public final <T extends Component<EntityStore>> void registerComponentType(
        final @Nonnull ComponentType<EntityStore, T> componentType,
        final @Nonnull Class<T> myClass,
        final @Nonnull String id
    ) {
        registeredEntityComponents.put(myClass, componentType);
        registeredEntityComponentsById.put(id, componentType);
    }

    public final void registerSystem(
        final @Nonnull HytalePlugin plugin,
        final @Nonnull ISubSystem<EntityStore> system
    ) {
        plugin.getEntityStoreRegistry().registerSystem(system);
    }
}
