package dev.twunk.interfaces;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.WorldProvider;
import dev.twunk.hytale.HytalePlugin;
import javax.annotation.Nonnull;

/**
 * Hytale seperates the components and systems i can register out by two types
 * - ChunkStore
 * - EntityStore
 *
 * Notably, that makes it quite annoying for defining common utilities like any
 * subsystem for entities (onEntityAdded, onEntityRemove, onTick).
 *
 * Given this, i'd love to not have to define mutliple copies of the exact same thing.
 *
 * So, since my subsystems simply run a parent class instance that it receives,
 * something that implements IRegistry can be used for EITHER (in an entity store case
 * it'll return the `EntityStore` versions of a plugin's register methods, in a chunk store
 * case it'll do the same but for chunks etc)
 */
public interface IRegistry<ECS_STORE extends WorldProvider> {
    public <T extends Component<ECS_STORE>> ComponentType<ECS_STORE, T> getComponentType(
        final @Nonnull Class<T> componentClass
    );

    public ComponentType<ECS_STORE, ? extends Component<ECS_STORE>> getComponentType(
        final @Nonnull String componentClass
    );

    public <T extends Component<ECS_STORE>> void registerComponentType(
        final @Nonnull ComponentType<ECS_STORE, T> componentType,
        final @Nonnull Class<T> myClass,
        final @Nonnull String id
    );

    public void registerSystem(final @Nonnull HytalePlugin plugin, final @Nonnull ISubSystem<ECS_STORE> system);
}
