package dev.twunk.lib.registry;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Resource;
import com.hypixel.hytale.component.ResourceType;
import com.hypixel.hytale.server.core.universe.world.WorldProvider;
import dev.twunk.hytale.interfaces.methods.IRegistry;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;

public abstract class ComponentRegistryHelper<ECS_TYPE extends WorldProvider> implements IRegistry<ECS_TYPE> {

    public final Map<
        Class<? extends Component<ECS_TYPE>>,
        ComponentType<ECS_TYPE, ? extends Component<ECS_TYPE>>
    > components = new HashMap<>();

    public final Map<String, ComponentType<ECS_TYPE, ? extends Component<ECS_TYPE>>> componentsById = new HashMap<>();

    public final Map<
        Class<? extends Resource<ECS_TYPE>>,
        ResourceType<ECS_TYPE, ? extends Resource<ECS_TYPE>>
    > resources = new HashMap<>();

    public final Map<String, ResourceType<ECS_TYPE, ? extends Resource<ECS_TYPE>>> resourcesById = new HashMap<>();

    protected ComponentRegistryHelper() {}

    /**
     * Registers the component type with the static map that stores
     * all that goodness for us. IRegisteredComponent gets to know
     * about EVERYTHING above it WOOOO
     */
    @Override
    public final <T extends Component<ECS_TYPE>> void cacheComponentType(
        ComponentType<ECS_TYPE, T> componentType,
        Class<T> myClass,
        String id
    ) {
        this.components.put(myClass, componentType);
        this.componentsById.put(id, componentType);
    }

    @Nullable
    @Override
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
    @Override
    public final <T extends Component<ECS_TYPE>> ComponentType<ECS_TYPE, T> getComponentType(String componentId) {
        return (ComponentType<ECS_TYPE, T>) this.componentsById.get(componentId);
    }

    @Override
    public final <T extends Resource<ECS_TYPE>> void cacheResourceType(
        ResourceType<ECS_TYPE, T> resourceType,
        Class<T> myClass,
        String id
    ) {
        this.resources.put(myClass, resourceType);
        this.resourcesById.put(id, resourceType);
    }

    @Nullable
    @Override
    public final <T extends Resource<ECS_TYPE>> ResourceType<ECS_TYPE, T> getResourceType(Class<T> resourceClass) {
        var resourceType = this.resources.get(resourceClass);
        if (resourceType == null) {
            return null;
        }

        // casting is safe as long as i haven't stuffed something up
        @SuppressWarnings("unchecked")
        var res = (ResourceType<ECS_TYPE, T>) resourceType;

        return res;
    }

    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    public final <T extends Resource<ECS_TYPE>> ResourceType<ECS_TYPE, T> getResourceType(String resourceId) {
        return (ResourceType<ECS_TYPE, T>) this.resourcesById.get(resourceId);
    }
}
