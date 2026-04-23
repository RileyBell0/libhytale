package dev.twunk.lib;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.system.ISystem;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import dev.twunk.hytale.HytalePlugin;
import dev.twunk.hytale.LibHytale;
import dev.twunk.hytale.system.OnAddRemove;
import dev.twunk.hytale.system.OnBlockTick;
import dev.twunk.hytale.system.OnTick;
import dev.twunk.hytale.system.OnWorldTick;
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

    @SuppressWarnings("null")
    private static final HytaleLogger.Api console = HytaleLogger.forEnclosingClass().atInfo();

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

    @Nullable
    public final <T extends Component<ChunkStore>> ComponentType<ChunkStore, T> getComponentType(
        final Class<T> componentClass
    ) {
        var componentType = registeredChunkComponents.get(componentClass);
        if (componentType == null) {
            return null;
        }

        // casting is safe as long as i haven't stuffed something up
        @SuppressWarnings("unchecked")
        var res = (ComponentType<ChunkStore, T>) componentType;

        return res;
    }

    @Nullable
    public final ComponentType<ChunkStore, ? extends Component<ChunkStore>> getComponentType(String componentId) {
        return registeredChunkComponentsById.get(componentId);
    }

    public <T extends Component<ChunkStore>> ComponentType<ChunkStore, T> registerComponent(
        JavaPlugin plugin,
        BuilderCodec<T> codec
    ) {
        var clazz = codec.getInnerClass();
        if (clazz == null) {
            throw new RuntimeException("Failed to get component class from codec in asdiofhaowiehpg34");
        }

        var componentType = this._registerComponent(plugin, codec);
        this.bindEventListeners(plugin, clazz, componentType);

        return componentType;
    }

    public <T extends Component<ChunkStore>> ComponentType<ChunkStore, T> registerComponent(
        JavaPlugin plugin,
        Class<T> clazz
    ) {
        var componentType = this._registerComponent(plugin, clazz);
        this.bindEventListeners(plugin, clazz, componentType);

        return componentType;
    }

    /**
     * Register the specified component via codec. Does NOT setup
     * system/initialiser.
     * Useful especially for non-ticking components
     *
     * If you want that to be auto-registered, call `registerTickingComponent`
     * instead
     */
    public <T extends Component<ChunkStore>> ComponentType<ChunkStore, T> _registerComponent(
        JavaPlugin plugin,
        BuilderCodec<T> codec
    ) {
        final Class<T> clazz = codec.getInnerClass();
        final var defaultId = clazz.getName();

        console.log("COMPONENT  " + clazz.getSimpleName());
        console.log(" --ID:     " + defaultId);
        if (defaultId == null) {
            throw new RuntimeException("Failed to get classname while registering component with codec " + codec);
        }

        final ComponentType<ChunkStore, T> component = plugin
            .getChunkStoreRegistry()
            .registerComponent(clazz, defaultId, codec);

        // Store our component in the global register
        LibHytale.registerChunkComponentType(component, clazz, defaultId);
        HytalePlugin.initCommonSystemsFor(plugin, clazz, component);

        if (clazz.isAnnotationPresent(dev.twunk.annotations.Serializable.class)) {
            if (Component.class.isAssignableFrom(clazz)) {
                if (IOnBlockTick.class.isAssignableFrom(clazz)) {
                    // new AutoBlockTickSystem(component).registerTo(this);
                }
            }
        }

        return component;
    }

    public <T extends Component<ChunkStore>> ComponentType<ChunkStore, T> _registerComponent(
        JavaPlugin plugin,
        Class<T> clazz
    ) {
        final BuilderCodec<T> codec = AutoBuilderCodec.tryGetCodec(clazz);
        if (codec == null || !BuilderCodec.class.isAssignableFrom(codec.getClass())) {
            throw new RuntimeException("Failed to get codec for class " + clazz);
        }

        return this.registerComponent(plugin, codec);
    }

    /**
     * Registers the component type with the static map that stores
     * all that goodness for us. IRegisteredComponent gets to know
     * about EVERYTHING above it WOOOO
     */
    public final <T extends Component<ChunkStore>> void registerComponentType(
        final ComponentType<ChunkStore, T> componentType,
        final Class<T> myClass
    ) {
        final var defaultId = myClass.getName();
        if (defaultId == null) {
            throw new RuntimeException("Failed to make ID (failed to call .getName()) for class " + myClass);
        }
        this.registerComponentType(componentType, myClass, defaultId);
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

    public final void registerSystem(final JavaPlugin plugin, final ISystem<ChunkStore> system) {
        plugin.getChunkStoreRegistry().registerSystem(system);
    }

    // this one is interesting, should be the same as the above method basically except calling the newUninitialised method instead
    // without the query as thats just gonna be Query.and(componentType);
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
    public <T extends Component<ChunkStore>> void bindEventListeners(JavaPlugin plugin, Class<T> componentClass) {
        ComponentType<ChunkStore, T> componentType = this.getComponentType(componentClass);
        if (componentType == null) {
            componentType = this._registerComponent(plugin, componentClass);
        }

        this.bindEventListeners(plugin, componentClass, componentType);
    }

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
}
