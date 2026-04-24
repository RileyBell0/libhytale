package dev.twunk.lib.registry;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.WorldProvider;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;

public abstract class ComponentRegistryHelper<ECS_TYPE extends WorldProvider> {

    public final Map<
        Class<? extends Component<ECS_TYPE>>,
        ComponentType<ECS_TYPE, ? extends Component<ECS_TYPE>>
    > components = new HashMap<>();

    public final Map<String, ComponentType<ECS_TYPE, ? extends Component<ECS_TYPE>>> componentsById = new HashMap<>();

    protected ComponentRegistryHelper() {}

    /**
     * Registers the component type with the static map that stores
     * all that goodness for us. IRegisteredComponent gets to know
     * about EVERYTHING above it WOOOO
     */
    public final <T extends Component<ECS_TYPE>> void registerComponentType(
        ComponentType<ECS_TYPE, T> componentType,
        Class<T> myClass,
        String id
    ) {
        this.components.put(myClass, componentType);
        this.componentsById.put(id, componentType);
    }

    @Nullable
    public final <T extends Component<ECS_TYPE>> ComponentType<ECS_TYPE, T> getComponentType(Class<T> componentClass) {
        var componentType = this.components.get(componentClass);
        if (componentType == null) {
            return null;
        }

        // casting is safe as long as i haven't stuffed something up
        @SuppressWarnings("unchecked")
        var res = (ComponentType<ECS_TYPE, T>) componentType;

        return res;
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public final <T extends Component<ECS_TYPE>> ComponentType<ECS_TYPE, T> getComponentType(String componentId) {
        return (ComponentType<ECS_TYPE, T>) this.componentsById.get(componentId);
    }
}
