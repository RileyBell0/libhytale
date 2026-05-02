package dev.twunk.hytale;

import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.twunk.examples.component.TrashComponent;
import dev.twunk.examples.interaction.SpawnItemInteraction;
import dev.twunk.hytale.component.ContainerComponent;
import dev.twunk.hytale.component.UUIDComponent;
import dev.twunk.hytale.interaction.LogInteraction;
import dev.twunk.hytale.interaction.OpenContainerComponentInteraction;
import dev.twunk.lib.WorldTickResource;
import dev.twunk.lib.WorldTickRunner;
import dev.twunk.lib.component.ActivelyTickingComponent;
import dev.twunk.lib.component.TestComponent;
import dev.twunk.lib.component.TickScheduleComponent;
import dev.twunk.lib.registry.ChunkRegisterProvider;
import dev.twunk.lib.registry.EntityRegisterProvider;

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
public abstract class LibHytale {

    public static final EntityRegisterProvider ENTITY_REGISTRY = new EntityRegisterProvider();

    public static final ChunkRegisterProvider CHUNK_REGISTRY = new ChunkRegisterProvider();

    private static boolean initialized = false;

    // Called automatically when a plugin that extends "HytalePlugin" is setup
    public static void init(final JavaPlugin plugin) {
        if (initialized) {
            return;
        }
        initialized = true;

        HytalePlugin.register(plugin, TestComponent.class);
        HytalePlugin.register(plugin, TickScheduleComponent.class);
        HytalePlugin.register(plugin, ContainerComponent.class);
        HytalePlugin.register(plugin, TrashComponent.class);
        HytalePlugin.register(plugin, ActivelyTickingComponent.class);
        HytalePlugin.register(plugin, UUIDComponent.class);

        // Register resources
        HytalePlugin.register(plugin, WorldTickResource.class);

        // Register interactions
        HytalePlugin.register(plugin, LogInteraction.class, "Log");
        HytalePlugin.register(plugin, SpawnItemInteraction.class, "SpawnItem");
        HytalePlugin.register(plugin, OpenContainerComponentInteraction.class, "OpenMahContainerPls");

        // Register anything else (systems)
        LibHytale.CHUNK_REGISTRY.registerEventListeners(plugin, new WorldTickRunner());
    }
}
