package dev.twunk;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import dev.twunk.plugin.ModPlugin;
import dev.twunk.subsystem.ISubSystem;
import dev.twunk.subsystem.composite.interfaces.IRegistry;
import java.util.HashMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class ChunkRegisterProvider implements IRegistry<ChunkStore> {

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
    public final <T extends Component<ChunkStore>> ComponentType<ChunkStore, T> getComponentType(
        final @Nonnull Class<T> componentClass
    ) {
        return TwunkLib.getChunkComponentType(componentClass);
    }

    @Nullable
    public final ComponentType<ChunkStore, ? extends Component<ChunkStore>> getComponentType(
        final @Nonnull String componentId
    ) {
        return TwunkLib.getChunkComponentType(componentId);
    }

    /**
     * Registers the component type with the static map that stores
     * all that goodness for us. IRegisteredComponent gets to know
     * about EVERYTHING above it WOOOO
     */
    public final <T extends Component<ChunkStore>> void registerComponentType(
        final @Nonnull ComponentType<ChunkStore, T> componentType,
        final @Nonnull Class<T> myClass,
        final @Nonnull String id
    ) {
        TwunkLib.registerChunkComponentType(componentType, myClass, id);
    }

    public final void registerSystem(final @Nonnull ModPlugin plugin, final @Nonnull ISubSystem<ChunkStore> system) {
        plugin.getChunkStoreRegistry().registerSystem(system);
    }
}
