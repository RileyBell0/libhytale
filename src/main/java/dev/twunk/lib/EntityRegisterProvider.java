package dev.twunk.lib;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.system.ISystem;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.twunk.hytale.HytalePlugin;
import dev.twunk.interfaces.methods.IRegistry;
import java.util.HashMap;
import javax.annotation.Nullable;

public final class EntityRegisterProvider implements IRegistry<EntityStore> {

    public static final HashMap<
        Class<? extends Component<EntityStore>>,
        ComponentType<EntityStore, ? extends Component<EntityStore>>
    > registeredEntityComponents = new HashMap<>();

    public static final HashMap<
        String,
        ComponentType<EntityStore, ? extends Component<EntityStore>>
    > registeredEntityComponentsById = new HashMap<>();

    ///////////////////////////////////////////////////////////////////////////
    // \/======================\/-  Methods  -\/==========================\/ //
    ///////////////////////////////////////////////////////////////////////////

    @SuppressWarnings("unchecked")
    public final <T extends Component<EntityStore>> ComponentType<EntityStore, T> getComponentType(
        final Class<T> componentClass
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
        final String componentId
    ) {
        return registeredEntityComponentsById.get(componentId);
    }

    /**
     * Registers the component type with the static map that stores
     * all that goodness for us. IRegisteredComponent gets to know
     * about EVERYTHING above it WOOOO
     */
    public final <T extends Component<EntityStore>> void registerComponentType(
        final ComponentType<EntityStore, T> componentType,
        final Class<T> myClass,
        final String id
    ) {
        registeredEntityComponents.put(myClass, componentType);
        registeredEntityComponentsById.put(id, componentType);
    }

    public final void registerSystem(final HytalePlugin plugin, final ISystem<EntityStore> system) {
        plugin.getEntityStoreRegistry().registerSystem(system);
    }

    public void bindEventListeners(HytalePlugin plugin, Object unknown) {}
}
