package dev.twunk.hytale;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.twunk.hytale.components.ContainerComponent;
import dev.twunk.hytale.components.TrashComponent;
import dev.twunk.hytale.interactions.LogInteraction;
import dev.twunk.hytale.interactions.OpenContainerComponentInteraction;
import dev.twunk.hytale.interactions.SpawnItemInteraction;
import dev.twunk.hytale.plugin.ModPlugin;
import dev.twunk.lib.ChunkRegisterProvider;
import dev.twunk.lib.EntityRegisterProvider;
import dev.twunk.lib.component.INTERNAL_TickSchedulerComponent;
import dev.twunk.lib.test.TwunkDevTestComponent;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * General need to knows for hytale's internals
 *
 * Stores
 * @see EntityStore EntityStore - same as ChunkStore but for entities
 * @see ChunkStore ChunkStore  - same as EntityStore but for blocks/chunks etc
 *
 * To clear up that recursive definition, a "store" stores stuff.
 * Ok, i hear you, that doesn't clear it up.
 * - An entity store contains "entities"
 * - A chunk store contains "blocks", "chunks", and "block entities"
 *
 * basically, if it moves or CAN move -> its probably an entity and in EntityStore
 * if it can't move and it's in a grid-like-fashion -> its probably a block/block-entity ChunkStore
 *
 * each world has these stores. So a WORLD has
 * - EntityStore: contains all the entities in the world (or a way to access
 *   them at the very least. obviously not ALL entities are stored in memory
 *   simultaneously. have you seen how large minecraft world sizes get? Hytale's
 *   BIGGER than minecraft (iirc) and 2b2t is 80 terrabytes. Nobody has that much ram)
 * - ChunkStore: contains all the chunks/blocks in the world
 *
 * Great, now we're officialy as clear as mud. I'll come back to this later. Unfortunately
 * the definition of "a store stores stuff" and that "its scoped to the world" and
 * "the kind of stuff it stores is defiend by either it being an `EntityStore` or `ChunkStore`
 * inside" is kind of the best definition i've got right now
 */
public abstract class TwunkLib {

    @Nonnull
    public static final EntityRegisterProvider ENTITY_REGISTRY = new EntityRegisterProvider();

    @Nonnull
    public static final ChunkRegisterProvider CHUNK_REGISTRY = new ChunkRegisterProvider();

    private static boolean hasRegisteredLibComponents = false;

    // Called automatically when you load a mod plugin
    public static void init(final @Nonnull ModPlugin plugin) {
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
        final @Nonnull Class<T> componentClass
    ) {
        return CHUNK_REGISTRY.getComponentType(componentClass);
    }

    /**
     * WARNING: Only ever call this AFTER your plugin's setup function. Component
     *          Types are stored on registration, thus, they're not there before
     *          registration...
     */
    @Nullable
    public static ComponentType<ChunkStore, ? extends Component<ChunkStore>> getChunkComponentType(
        final @Nonnull String componentId
    ) {
        return CHUNK_REGISTRY.getComponentType(componentId);
    }

    /**
     * WARNING: Only ever call this AFTER your plugin's setup function. Component
     *          Types are stored on registration, thus, they're not there before
     *          registration...
     */
    @Nonnull
    public static <T extends Component<EntityStore>> ComponentType<EntityStore, T> getEntityComponentType(
        final @Nonnull Class<T> componentClass
    ) {
        return ENTITY_REGISTRY.getComponentType(componentClass);
    }

    /**
     * WARNING: Only ever call this AFTER your plugin's setup function. Component
     *          Types are stored on registration, thus, they're not there before
     *          registration...
     */
    @Nullable
    public static ComponentType<EntityStore, ? extends Component<EntityStore>> getEntityComponentType(
        final @Nonnull String componentId
    ) {
        return ENTITY_REGISTRY.getComponentType(componentId);
    }

    /**
     * Register component type to both its Class, and to its ID
     */
    public static <T extends Component<ChunkStore>> void registerChunkComponentType(
        final @Nonnull ComponentType<ChunkStore, T> componentType,
        final @Nonnull Class<T> myClass,
        final @Nonnull String id
    ) {
        CHUNK_REGISTRY.registerComponentType(componentType, myClass, id);
    }

    public static <T extends Component<EntityStore>> void registerEntityComponentType(
        final @Nonnull ComponentType<EntityStore, T> componentType,
        final @Nonnull Class<T> myClass,
        final @Nonnull String id
    ) {
        ENTITY_REGISTRY.registerComponentType(componentType, myClass, id);
    }
}
