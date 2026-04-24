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

public final class ChunkRegisterProvider extends ComponentRegistryHelper<ChunkStore> implements IRegistry<ChunkStore> {

    @Override
    public ComponentRegistryProxy<ChunkStore> getStoreRegistry(JavaPlugin plugin) {
        return plugin.getChunkStoreRegistry();
    }

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
