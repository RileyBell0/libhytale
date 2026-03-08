package dev.twunk.utils;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import dev.twunk.component._TickSchedulerComponent;
import dev.twunk.plugin.ModPlugin;
import dev.twunk.subsystem.composite._EntityScheduledTickStateComponent;
import java.util.HashMap;
import javax.annotation.Nonnull;

public abstract class TwunkLib {

    public static final HashMap<
        Class<? extends Component<ChunkStore>>,
        ComponentType<ChunkStore, ? extends Component<ChunkStore>>
    > registeredComponents = new HashMap<
        Class<? extends Component<ChunkStore>>,
        ComponentType<ChunkStore, ? extends Component<ChunkStore>>
    >();

    private static boolean hasRegisteredTickComponents = false;

    // Called automatically when you load a mod plugin
    public static void init(ModPlugin plugin) {
        registerTickComponents(plugin);
    }

    private static void registerTickComponents(ModPlugin plugin) {
        if (hasRegisteredTickComponents) {
            return;
        }
        hasRegisteredTickComponents = true;

        // per-system ticking
        _TickSchedulerComponent.COMPONENT_TYPE = plugin.registerComponent(_TickSchedulerComponent.CODEC);

        // component-based ticking
        _EntityScheduledTickStateComponent.Active.COMPONENT_TYPE = plugin.registerComponent(
            _EntityScheduledTickStateComponent.Active.CODEC
        );
        _EntityScheduledTickStateComponent.Sleeping.COMPONENT_TYPE = plugin.registerComponent(
            _EntityScheduledTickStateComponent.Sleeping.CODEC
        );
        _EntityScheduledTickStateComponent.Stopped.COMPONENT_TYPE = plugin.registerComponent(
            _EntityScheduledTickStateComponent.Stopped.CODEC
        );
        _EntityScheduledTickStateComponent.Unknown.COMPONENT_TYPE = plugin.registerComponent(
            _EntityScheduledTickStateComponent.Unknown.CODEC
        );
        hasRegisteredTickComponents = true;
    }

    /**
     * WARNING: Only ever call this AFTER your plugin's setup function (e.g.
     * plugin's
     * start function, or really anywhere else in the code)
     *
     * This is designed to be easy to use throughout the code, so we assume it to
     * always succeed
     * and it WILL always succeed as long as you register your component before
     * calling it
     */
    @Nonnull
    @SuppressWarnings("unchecked")
    public static <T extends Component<ChunkStore>> ComponentType<ChunkStore, T> getComponentType(
        Class<T> componentClass
    ) {
        var componentType = registeredComponents.get(componentClass);
        if (componentType == null) {
            throw new RuntimeException(
                "Called getComponentType on class " + componentClass + " before initialising said class"
            );
        }

        // casting is safe as long as i haven't stuffed something up
        return (ComponentType<ChunkStore, T>) componentType;
    }

    /**
     * Registers the component type with the static map that stores
     * all that goodness for us. IRegisteredComponent gets to know
     * about EVERYTHING above it WOOOO
     */
    public static <T extends Component<ChunkStore>> void registerComponentType(
        Class<T> myClass,
        ComponentType<ChunkStore, T> componentType
    ) {
        registeredComponents.put(myClass, componentType);
    }
}
