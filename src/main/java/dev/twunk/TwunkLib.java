package dev.twunk;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import dev.twunk.components.ContainerComponent;
import dev.twunk.components.TrashComponent;
import dev.twunk.interactions.LogInteraction;
import dev.twunk.interactions.OpenContainerComponentInteraction;
import dev.twunk.interactions.SpawnItemInteraction;
import dev.twunk.lib.component.INTERNAL_TickSchedulerComponent;
import dev.twunk.lib.component.TwunkDevTestComponent;
import dev.twunk.plugin.ModPlugin;
import java.util.HashMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class TwunkLib {

    @Nonnull
    public static final HashMap<
        Class<? extends Component<ChunkStore>>,
        ComponentType<ChunkStore, ? extends Component<ChunkStore>>
    > registeredComponents = new HashMap<>();

    @Nonnull
    public static final HashMap<
        String,
        ComponentType<ChunkStore, ? extends Component<ChunkStore>>
    > registeredComponentsById = new HashMap<>();

    private static boolean hasRegisteredLibComponents = false;

    // Called automatically when you load a mod plugin
    public static void init(@Nonnull final ModPlugin plugin) {
        if (hasRegisteredLibComponents) {
            return;
        }
        hasRegisteredLibComponents = true;

        // per-system ticking
        INTERNAL_TickSchedulerComponent.COMPONENT_TYPE = plugin.registerComponent(
            INTERNAL_TickSchedulerComponent.CODEC
        );

        // i use this to tset some stuff
        TwunkDevTestComponent.COMPONENT_TYPE = plugin.registerComponent(TwunkDevTestComponent.CODEC);
        ContainerComponent.COMPONENT_TYPE = plugin.registerComponent(ContainerComponent.CODEC);
        TrashComponent.COMPONENT_TYPE = plugin.registerComponent(TrashComponent.CODEC);

        // Register interactions
        plugin.registerInteraction(LogInteraction.CODEC, "Log");
        plugin.registerInteraction(SpawnItemInteraction.CODEC, "SpawnItem");
        plugin.registerInteraction(OpenContainerComponentInteraction.CODEC, "OpenMahContainerPls");
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
        @Nonnull final Class<T> componentClass
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

    @Nullable
    public static ComponentType<ChunkStore, ? extends Component<ChunkStore>> getComponentType(
        @Nonnull final String componentId
    ) {
        return registeredComponentsById.get(componentId);
    }

    /**
     * Registers the component type with the static map that stores
     * all that goodness for us. IRegisteredComponent gets to know
     * about EVERYTHING above it WOOOO
     */
    public static <T extends Component<ChunkStore>> void registerComponentType(
        @Nonnull final Class<T> myClass,
        @Nonnull final String id,
        @Nonnull final ComponentType<ChunkStore, T> componentType
    ) {
        registeredComponents.put(myClass, componentType);
        registeredComponentsById.put(id, componentType);
    }
}
