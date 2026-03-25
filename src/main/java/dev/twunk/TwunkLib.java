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
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class TwunkLib {

    @Nonnull
    public static final EntityRegisterProvider ENTITY_REGISTRY = new EntityRegisterProvider();

    @Nonnull
    public static final ChunkRegisterProvider CHUNK_REGISTRY = new ChunkRegisterProvider();

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
     * WARNING: Only ever call this AFTER your plugin's setup function. Component
     *          Types are stored on registration, thus, they're not there before
     *          registration...
     */
    @Nonnull
    public static <T extends Component<ChunkStore>> ComponentType<ChunkStore, T> getChunkComponentType(
            @Nonnull final Class<T> componentClass) {
        return CHUNK_REGISTRY.getComponentType(componentClass);
    }

    /**
     * WARNING: Only ever call this AFTER your plugin's setup function. Component
     *          Types are stored on registration, thus, they're not there before
     *          registration...
     */
    @Nullable
    public static ComponentType<ChunkStore, ? extends Component<ChunkStore>> getChunkComponentType(
            @Nonnull final String componentId) {
        return CHUNK_REGISTRY.getComponentType(componentId);
    }

    /**
     * WARNING: Only ever call this AFTER your plugin's setup function. Component
     *          Types are stored on registration, thus, they're not there before
     *          registration...
     */
    @Nonnull
    public static <T extends Component<EntityStore>> ComponentType<EntityStore, T> getEntityComponentType(
            @Nonnull final Class<T> componentClass) {
        return ENTITY_REGISTRY.getComponentType(componentClass);
    }

    /**
     * WARNING: Only ever call this AFTER your plugin's setup function. Component
     *          Types are stored on registration, thus, they're not there before
     *          registration...
     */
    @Nullable
    public static ComponentType<EntityStore, ? extends Component<EntityStore>> getEntityComponentType(
            @Nonnull final String componentId) {
        return ENTITY_REGISTRY.getComponentType(componentId);
    }

    /**
     * Register component type to both its Class, and to its ID
     */
    public static <T extends Component<ChunkStore>> void registerChunkComponentType(
            @Nonnull final ComponentType<ChunkStore, T> componentType,
            @Nonnull final Class<T> myClass,
            @Nonnull final String id) {
        CHUNK_REGISTRY.registerComponentType(componentType, myClass, id);
    }

    public static <T extends Component<EntityStore>> void registerEntityComponentType(
            @Nonnull final ComponentType<EntityStore, T> componentType,
            @Nonnull final Class<T> myClass,
            @Nonnull final String id) {
        ENTITY_REGISTRY.registerComponentType(componentType, myClass, id);
    }
}
