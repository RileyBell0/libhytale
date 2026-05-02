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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import javax.annotation.Nullable;

public final class ChunkRegisterProvider extends ComponentRegistryHelper<ChunkStore> {

    @SuppressWarnings("null")
    public final Set<Class<?>> EVENT_INTERFACES = Set.of(IOnBlockTick.class);

    @Override
    public ComponentRegistryProxy<ChunkStore> getStoreRegistry(JavaPlugin plugin) {
        return plugin.getChunkStoreRegistry();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> List<EventDriver<ChunkStore>> bindRegistrySpecificEventListeners(JavaPlugin _unused, T listener) {
        List<EventDriver<ChunkStore>> drivers = new ArrayList<>();

        if (listener instanceof IQuery q && listener instanceof IOnBlockTick l) {
            drivers.add(EventDriver.of(OnBlockTick.newDriverFor(q, l), IOnBlockTick.class));
        }

        return drivers;
    }

    @Override
    public <T extends Component<ChunkStore>> List<EventDriver<ChunkStore>> bindRegistrySpecificEventListeners(
        JavaPlugin _unused,
        Class<T> componentClass,
        Function<Class<?>, Query<ChunkStore>> querySupplier,
        ComponentType<ChunkStore, T> componentType
    ) {
        List<EventDriver<ChunkStore>> drivers = new ArrayList<>();

        if (IOnBlockTick.class.isAssignableFrom(componentClass)) {
            drivers.add(EventDriver.of(OnBlockTick.newDriverFor(querySupplier, componentType), IOnBlockTick.class));
        }

        return drivers;
    }

    @Nullable
    private static Set<Class<?>> knownInterfaces = null;

    @Override
    public Set<Class<?>> getKnownInterfaceClasses() {
        if (ChunkRegisterProvider.knownInterfaces != null) {
            return ChunkRegisterProvider.knownInterfaces;
        }

        Set<Class<?>> interfaces = new HashSet<>();
        interfaces.addAll(super.getKnownInterfaceClasses());
        interfaces.addAll(EVENT_INTERFACES);

        ChunkRegisterProvider.knownInterfaces = interfaces;

        return interfaces;
    }
}
