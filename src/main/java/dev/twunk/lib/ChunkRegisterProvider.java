package dev.twunk.lib;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.system.ISystem;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import dev.twunk.hytale.HytalePlugin;
import dev.twunk.hytale.system.OnAddRemove;
import dev.twunk.hytale.system.OnBlockTick;
import dev.twunk.hytale.system.OnTick;
import dev.twunk.hytale.system.composite.OnScheduledTick;
import dev.twunk.interfaces.events.IOnAddRemove;
import dev.twunk.interfaces.events.IOnBlockTick;
import dev.twunk.interfaces.events.IOnScheduledTick;
import dev.twunk.interfaces.events.IOnTick;
import dev.twunk.interfaces.events.IOnWorldTick;
import dev.twunk.interfaces.methods.IQuery;
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

    public final void registerSystem(final HytalePlugin plugin, final ISystem<ChunkStore> system) {
        plugin.getChunkStoreRegistry().registerSystem(system);
    }

    public <T extends IQuery<ChunkStore>> void bindEventListeners(HytalePlugin plugin, T unknown) {
        // find the interfaces it supports
        var clazz = unknown.getClass();
        if (IOnAddRemove.class.isAssignableFrom(clazz)) {
            OnAddRemove.ForListener.newUninitialised((T & IOnAddRemove<ChunkStore>) unknown, this);
        }
        if (IOnBlockTick.class.isAssignableFrom(clazz)) {
            OnBlockTick.ForListener.newUninitialised((T & IOnBlockTick) unknown);
        }
        if (IOnTick.class.isAssignableFrom(clazz)) {
            OnTick.ForListener.newUninitialised((T & IOnTick<ChunkStore>) unknown, this);
        }
        if (IOnScheduledTick.class.isAssignableFrom(clazz)) {
            OnScheduledTick.newUninitialised((T & IOnScheduledTick<ChunkStore>) unknown, this);
        }
        if (IOnWorldTick.class.isAssignableFrom(clazz)) {
        }
    }
}
