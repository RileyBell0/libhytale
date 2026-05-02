package dev.twunk.lib.registry;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentRegistryProxy;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import dev.twunk.hytale.event.OnBlockTick;
import dev.twunk.hytale.interfaces.config.IQuery;
import dev.twunk.hytale.interfaces.event.IOnBlockTick;
import java.util.function.Function;
import javax.annotation.Nonnull;

public final class ChunkRegisterProvider extends ComponentRegistryHelper<ChunkStore> {

    @Override
    public ComponentRegistryProxy<ChunkStore> getStoreRegistry(JavaPlugin plugin) {
        return plugin.getChunkStoreRegistry();
    }

    @Override
    public <T> void bindRegistrySpecificEventListeners(JavaPlugin plugin, T listener) {
        var clazz = listener.getClass();

        if (listener instanceof IQuery q && IOnBlockTick.class.isAssignableFrom(clazz)) {
            @SuppressWarnings("unchecked")
            Query<ChunkStore> query = q.getQuery(IOnBlockTick.class);
            var driver = OnBlockTick.newDriverFor(query, (IOnBlockTick) listener);
            driver.onRegister(plugin);
        }
    }

    @Override
    public <T extends Component<ChunkStore>> void bindRegistrySpecificEventListeners(
        JavaPlugin plugin,
        Class<T> componentClass,
        Function<Class<?>, Query<ChunkStore>> querySupplier,
        ComponentType<ChunkStore, T> componentType
    ) {
        @Nonnull
        @SuppressWarnings("null")
        Query<ChunkStore> query = querySupplier.apply(IOnBlockTick.class);

        if (IOnBlockTick.class.isAssignableFrom(componentClass)) {
            OnBlockTick.newDriverFor(query, componentType).onRegister(plugin);
        }
    }
}
