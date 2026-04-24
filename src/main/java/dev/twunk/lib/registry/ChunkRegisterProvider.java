package dev.twunk.lib.registry;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentRegistryProxy;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import dev.twunk.hytale.event.OnAddRemove;
import dev.twunk.hytale.event.OnBlockTick;
import dev.twunk.hytale.event.OnTick;
import dev.twunk.hytale.event.OnWorldTick;
import dev.twunk.hytale.event.composite.OnScheduledTick;
import dev.twunk.hytale.interfaces.event.IOnAddRemove;
import dev.twunk.hytale.interfaces.event.IOnBlockTick;
import dev.twunk.hytale.interfaces.event.IOnScheduledTick;
import dev.twunk.hytale.interfaces.event.IOnTick;
import dev.twunk.hytale.interfaces.event.IOnWorldTick;
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

    /**
     * this one is interesting, should be the same as the above method basically except calling the newUninitialised method instead
     * without the query as thats just gonna be Query.and(componentType);
     */
    @Override
    public <T extends IQuery<ChunkStore>> void bindEventListeners(JavaPlugin plugin, T listener) {
        var clazz = listener.getClass();

        // find the interfaces it supports

        if (IOnAddRemove.class.isAssignableFrom(clazz)) {
            @SuppressWarnings("unchecked")
            var driver = OnAddRemove.newUninitialised((IOnAddRemove.IOnAddRemove__IQuery<ChunkStore>) listener, this);
            driver.onRegister(plugin);
        }

        if (IOnBlockTick.class.isAssignableFrom(clazz)) {
            var driver = OnBlockTick.newUninitialised((IOnBlockTick.IOnBlockTick__IQuery) listener);
            driver.onRegister(plugin);
        }

        if (IOnTick.class.isAssignableFrom(clazz)) {
            @SuppressWarnings("unchecked")
            var driver = OnTick.newUninitialised((IOnTick.IOnTick__IQuery<ChunkStore>) listener, this);
            driver.onRegister(plugin);
        }

        if (IOnScheduledTick.class.isAssignableFrom(clazz)) {
            // its a composite one so interestingly enough this one is recursive
            // as in onRegister it calls bindEventListeners :)
            @SuppressWarnings("unchecked")
            var driver = OnScheduledTick.newUninitialised(
                "",
                (IOnScheduledTick.IOnScheduledTick__IQuery<ChunkStore>) listener,
                this
            );
            driver.onRegister(plugin);
        }

        if (IOnWorldTick.class.isAssignableFrom(clazz)) {
            @SuppressWarnings("unchecked")
            var driver = OnWorldTick.newUninitialised((IOnWorldTick.IOnWorldTick__IQuery<ChunkStore>) listener, this);
            driver.onRegister(plugin);
        }
    }

    @Override
    public <T extends Component<ChunkStore>> void bindEventListeners(
        JavaPlugin plugin,
        Class<T> componentClass,
        ComponentType<ChunkStore, T> componentType
    ) {
        if (IOnAddRemove.class.isAssignableFrom(componentClass)) {
            @SuppressWarnings({ "unchecked", "rawtypes" })
            var driver = OnAddRemove.newUninitialised((ComponentType) componentType, this);
            driver.onRegister(plugin);
        }

        if (IOnBlockTick.class.isAssignableFrom(componentClass)) {
            @SuppressWarnings({ "unchecked", "rawtypes" })
            var driver = OnBlockTick.newUninitialised((ComponentType) componentType);
            driver.onRegister(plugin);
        }

        if (IOnTick.class.isAssignableFrom(componentClass)) {
            @SuppressWarnings({ "unchecked", "rawtypes" })
            var driver = OnTick.newUninitialised((ComponentType) componentType, this);
            driver.onRegister(plugin);
        }

        if (IOnScheduledTick.class.isAssignableFrom(componentClass)) {
            // its a composite one so interestingly enough this one is recursive
            // as in onRegister it calls bindEventListeners :)
            @SuppressWarnings({ "unchecked", "rawtypes" })
            var driver = OnScheduledTick.newUninitialised("", (ComponentType) componentType, this);
            driver.onRegister(plugin);
        }

        // TODO - allow only annotated static methods to be used here
        // if (IOnWorldTick.class.isAssignableFrom(componentClass)) {
        //     @SuppressWarnings("unchecked") // var driver = OnWorldTick.newUninitialised(, this);
        //     driver.onRegister(plugin);
        // }
    }

    @Override
    public ComponentRegistryProxy<ChunkStore> getStoreRegistry(JavaPlugin plugin) {
        return plugin.getChunkStoreRegistry();
    }
}
