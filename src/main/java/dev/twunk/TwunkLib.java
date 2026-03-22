package dev.twunk;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
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
    public static final EntityRegisterProvider ENTITY_REGISTRY = new EntityRegisterProvider();

    @Nonnull
    public static final ChunkRegisterProvider CHUNK_REGISTRY = new ChunkRegisterProvider();

    @Nonnull
    public static final HashMap<
        Class<? extends Component<ChunkStore>>,
        ComponentType<ChunkStore, ? extends Component<ChunkStore>>
    > registeredChunkComponents = new HashMap<>();

    @Nonnull
    public static final HashMap<
        String,
        ComponentType<ChunkStore, ? extends Component<ChunkStore>>
    > registeredChunkComponentsById = new HashMap<>();

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

    private static boolean hasRegisteredLibComponents = false;

    // Called automatically when you load a mod plugin
    public static void init(@Nonnull final ModPlugin plugin) {
        if (hasRegisteredLibComponents) {
            return;
        }
        hasRegisteredLibComponents = true;

        // per-system ticking
        plugin.registerChunkComponent(INTERNAL_TickSchedulerComponent.CHUNK_CODEC);
        plugin.registerEntityComponent(INTERNAL_TickSchedulerComponent.ENTITY_CODEC);

        // i use this to tset some stuff
        TwunkDevTestComponent.COMPONENT_TYPE = plugin.registerChunkComponent(TwunkDevTestComponent.CODEC);
        plugin.registerChunkComponent(ContainerComponent.CHUNK_CODEC);
        plugin.registerEntityComponent(ContainerComponent.ENTITY_CODEC);
        plugin.registerChunkComponent(TrashComponent.CHUNK_CODEC);
        plugin.registerEntityComponent(TrashComponent.ENTITY_CODEC);

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
    public static <T extends Component<ChunkStore>> ComponentType<ChunkStore, T> getChunkComponentType(
        @Nonnull final Class<T> componentClass
    ) {
        var componentType = registeredChunkComponents.get(componentClass);
        if (componentType == null) {
            throw new RuntimeException(
                "Called getComponentType on class " + componentClass + " before initialising said class"
            );
        }

        // casting is safe as long as i haven't stuffed something up
        return (ComponentType<ChunkStore, T>) componentType;
    }

    @Nullable
    public static ComponentType<ChunkStore, ? extends Component<ChunkStore>> getChunkComponentType(
        @Nonnull final String componentId
    ) {
        return registeredChunkComponentsById.get(componentId);
    }

    @Nonnull
    @SuppressWarnings("unchecked")
    public static <T extends Component<EntityStore>> ComponentType<EntityStore, T> getEntityComponentType(
        @Nonnull final Class<T> componentClass
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
    public static ComponentType<EntityStore, ? extends Component<EntityStore>> getEntityComponentType(
        @Nonnull final String componentId
    ) {
        return registeredEntityComponentsById.get(componentId);
    }

    /**
     * Registers the component type with the static map that stores
     * all that goodness for us. IRegisteredComponent gets to know
     * about EVERYTHING above it WOOOO
     */
    public static <T extends Component<ChunkStore>> void registerChunkComponentType(
        @Nonnull final Class<T> myClass,
        @Nonnull final String id,
        @Nonnull final ComponentType<ChunkStore, T> componentType
    ) {
        registeredChunkComponents.put(myClass, componentType);
        registeredChunkComponentsById.put(id, componentType);
    }

    /**
     * Registers the component type with the static map that stores
     * all that goodness for us. IRegisteredComponent gets to know
     * about EVERYTHING above it WOOOO
     */
    public static <T extends Component<EntityStore>> void registerEntityComponentType(
        @Nonnull final Class<T> myClass,
        @Nonnull final String id,
        @Nonnull final ComponentType<EntityStore, T> componentType
    ) {
        registeredEntityComponents.put(myClass, componentType);
        registeredEntityComponentsById.put(id, componentType);
    }
}
