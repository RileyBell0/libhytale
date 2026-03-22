package dev.twunk;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import dev.twunk.plugin.ModPlugin;
import dev.twunk.subsystem.ISubSystem;
import dev.twunk.subsystem.composite.interfaces.IRegistry;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class ChunkRegisterProvider implements IRegistry<ChunkStore> {

    @Nonnull
    public final <T extends Component<ChunkStore>> ComponentType<ChunkStore, T> getComponentType(
        @Nonnull final Class<T> componentClass
    ) {
        return TwunkLib.getChunkComponentType(componentClass);
    }

    @Nullable
    public final ComponentType<ChunkStore, ? extends Component<ChunkStore>> getComponentType(
        @Nonnull final String componentId
    ) {
        return TwunkLib.getChunkComponentType(componentId);
    }

    /**
     * Registers the component type with the static map that stores
     * all that goodness for us. IRegisteredComponent gets to know
     * about EVERYTHING above it WOOOO
     */
    public final <T extends Component<ChunkStore>> void registerComponentType(
        @Nonnull final Class<T> myClass,
        @Nonnull final String id,
        @Nonnull final ComponentType<ChunkStore, T> componentType
    ) {
        TwunkLib.registerChunkComponentType(myClass, id, componentType);
    }

    public final void registerSystem(@Nonnull final ModPlugin plugin, @Nonnull final ISubSystem<ChunkStore> system) {
        plugin.getChunkStoreRegistry().registerSystem(system);
    }
}
