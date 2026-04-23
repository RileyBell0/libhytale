package dev.twunk.lib.registry;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.system.ISystem;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.twunk.hytale.HytalePlugin;
import dev.twunk.hytale.LibHytale;
import dev.twunk.hytale.interfaces.methods.IQuery;
import dev.twunk.hytale.interfaces.methods.IRegistry;
import dev.twunk.lib.codec.AutoSerializeParser;
import java.util.HashMap;
import javax.annotation.Nullable;

public final class EntityRegisterProvider implements IRegistry<EntityStore> {

    @SuppressWarnings("null")
    private static final HytaleLogger.Api console = HytaleLogger.forEnclosingClass().atInfo();

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

    public final void registerSystem(final JavaPlugin plugin, final ISystem<EntityStore> system) {
        plugin.getEntityStoreRegistry().registerSystem(system);
    }

    @Override
    public <T extends Component<EntityStore>> void bindEventListeners(JavaPlugin plugin, Class<T> listener) {}

    @Override
    public <T extends IQuery<EntityStore>> void bindEventListeners(JavaPlugin plugin, T listener) {}

    @Override
    public <T extends Component<EntityStore>> ComponentType<EntityStore, T> registerComponent(
        JavaPlugin plugin,
        BuilderCodec<T> codec
    ) {
        final Class<T> clazz = codec.getInnerClass();
        final var defaultId = clazz.getName();

        console.log("COMPONENT  \"" + clazz.getSimpleName());
        console.log(" --ID:     \"" + defaultId);
        if (defaultId == null) {
            throw new RuntimeException("Failed to get classname while registering component with codec " + codec);
        }

        final ComponentType<EntityStore, T> component = plugin
            .getEntityStoreRegistry()
            .registerComponent(clazz, defaultId, codec);

        // Store our component in the global register
        LibHytale.registerEntityComponentType(component, clazz, defaultId);

        HytalePlugin.initCommonSystemsFor(plugin, clazz, component);

        return component;
    }

    @Override
    public <T extends Component<EntityStore>> ComponentType<EntityStore, T> registerComponent(
        JavaPlugin plugin,
        Class<T> clazz
    ) {
        final BuilderCodec<T> codec = AutoSerializeParser.tryGetCodec(clazz);
        if (codec == null || !BuilderCodec.class.isAssignableFrom(codec.getClass())) {
            throw new RuntimeException("Failed to get codec for class " + clazz);
        }

        return this.registerComponent(plugin, codec);
    }
}
