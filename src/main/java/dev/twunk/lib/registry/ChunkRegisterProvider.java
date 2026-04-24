package dev.twunk.lib.registry;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentRegistryProxy;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import dev.twunk.hytale.event.OnBlockTick;
import dev.twunk.hytale.interfaces.event.IOnBlockTick;
import dev.twunk.hytale.interfaces.methods.IQuery;
import dev.twunk.hytale.interfaces.methods.IRegistry;
import java.util.HashMap;
import java.util.Map;

public final class ChunkRegisterProvider implements IRegistry<ChunkStore> {

    protected static final Map<
        Class<? extends Component<ChunkStore>>,
        ComponentType<ChunkStore, ? extends Component<ChunkStore>>
    > CHUNK_COMPONENTS = new HashMap<>();

    protected static final Map<
        String,
        ComponentType<ChunkStore, ? extends Component<ChunkStore>>
    > CHUNK_COMPONENTS_BY_ID = new HashMap<>();

    public final Map<
        Class<? extends Component<ChunkStore>>,
        ComponentType<ChunkStore, ? extends Component<ChunkStore>>
    > getComponentMap() {
        return CHUNK_COMPONENTS;
    }

    public final Map<String, ComponentType<ChunkStore, ? extends Component<ChunkStore>>> getComponentByIdMap() {
        return CHUNK_COMPONENTS_BY_ID;
    }

    // ////////////////////////////////////////////////////////////////////////
    // \/======================\/-  Methods  -\/==========================\/ //
    // ////////////////////////////////////////////////////////////////////////

    @Override
    public ComponentRegistryProxy<ChunkStore> getStoreRegistry(JavaPlugin plugin) {
        return plugin.getChunkStoreRegistry();
    }

    /**
     * this one is interesting, should be the same as the above method basically except calling the newUninitialised method instead
     * without the query as thats just gonna be Query.and(componentType);
     */
    @Override
    public <T extends IQuery<ChunkStore>> void bindRegistrySpecificEventListeners(JavaPlugin plugin, T listener) {
        var clazz = listener.getClass();

        if (IOnBlockTick.class.isAssignableFrom(clazz)) {
            var driver = OnBlockTick.newUninitialised((IOnBlockTick.IOnBlockTick__IQuery) listener);
            driver.onRegister(plugin);
        }
    }

    @Override
    public <T extends Component<ChunkStore>> void bindRegistrySpecificEventListeners(
        JavaPlugin plugin,
        Class<T> componentClass,
        ComponentType<ChunkStore, T> componentType
    ) {
        if (IOnBlockTick.class.isAssignableFrom(componentClass)) {
            @SuppressWarnings({ "unchecked", "rawtypes" })
            var driver = OnBlockTick.newUninitialised((ComponentType) componentType);
            driver.onRegister(plugin);
        }
    }
}
