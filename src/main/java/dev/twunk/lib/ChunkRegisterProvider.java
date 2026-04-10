package dev.twunk.lib;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import dev.twunk.hytale.HytalePlugin;
import dev.twunk.interfaces.ISubSystem;
import dev.twunk.interfaces.methods.IRegistry;
import java.util.HashMap;
import javax.annotation.Nullable;

public final class ChunkRegisterProvider implements IRegistry<ChunkStore> {

    public static final HashMap<
        Class<? extends Component<ChunkStore>>,
        ComponentType<ChunkStore, ? extends Component<ChunkStore>>
    > registeredChunkComponents = new HashMap<>();

    public static final HashMap<
        String,
        ComponentType<ChunkStore, ? extends Component<ChunkStore>>
    > registeredChunkComponentsById = new HashMap<>();

    ///////////////////////////////////////////////////////////////////////////
    // \/======================\/-  Methods  -\/==========================\/ //
    ///////////////////////////////////////////////////////////////////////////

    public final <T extends Component<ChunkStore>> ComponentType<ChunkStore, T> getComponentType(
        final Class<T> componentClass
    ) {
        var componentType = registeredChunkComponents.get(componentClass);
        if (componentType == null) {
            throw new RuntimeException(
                "Called getComponentType on class " + componentClass + " before initialising said class"
            );
        }

        // casting is safe as long as i haven't stuffed something up
        @SuppressWarnings("unchecked")
        var res = (ComponentType<ChunkStore, T>) componentType;

        return res;
    }

    @Nullable
    public final ComponentType<ChunkStore, ? extends Component<ChunkStore>> getComponentType(final String componentId) {
        return registeredChunkComponentsById.get(componentId);
    }

    /**
     * Registers the component type with the static map that stores
     * all that goodness for us. IRegisteredComponent gets to know
     * about EVERYTHING above it WOOOO
     */
    public final <T extends Component<ChunkStore>> void registerComponentType(
        final ComponentType<ChunkStore, T> componentType,
        final Class<T> myClass,
        final String id
    ) {
        registeredChunkComponents.put(myClass, componentType);
        registeredChunkComponentsById.put(id, componentType);
    }

    public final void registerSystem(final HytalePlugin plugin, final ISubSystem<ChunkStore> system) {
        plugin.getChunkStoreRegistry().registerSystem(system);
    }
}
