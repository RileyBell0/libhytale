package dev.twunk.hytale.interfaces.methods;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentRegistryProxy;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.system.ISystem;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.universe.world.WorldProvider;
import dev.twunk.hytale.LibHytaleException;
import dev.twunk.hytale.event.OnAddRemove;
import dev.twunk.hytale.event.OnTick;
import dev.twunk.hytale.event.OnWorldTick;
import dev.twunk.hytale.event.composite.OnScheduledTick;
import dev.twunk.hytale.interfaces.event.IOnAddRemove;
import dev.twunk.hytale.interfaces.event.IOnScheduledTick;
import dev.twunk.hytale.interfaces.event.IOnTick;
import dev.twunk.hytale.interfaces.event.IOnWorldTick;
import dev.twunk.lib.codec.AutoSerializeParser;
import java.util.Map;
import javax.annotation.Nullable;

/**
 * Hytale seperates the components and systems i can register out by two types
 * - ChunkStore
 * - EntityStore
 *
 * Notably, that makes it quite annoying for defining common utilities like any
 * subsystem for entities (onEntityAdded, onEntityRemove, onTick).
 *
 * Given this, i'd love to not have to define mutliple copies of the exact same thing.
 *
 * So, since my subsystems simply run a parent class instance that it receives,
 * something that implements IRegistry can be used for EITHER (in an entity store case
 * it'll return the `EntityStore` versions of a plugin's register methods, in a chunk store
 * case it'll do the same but for chunks etc)
 */
public interface IRegistry<ECS_TYPE extends WorldProvider> {
    @SuppressWarnings("null")
    static final HytaleLogger.Api console = HytaleLogger.forEnclosingClass().atInfo();

    public abstract Map<
        Class<? extends Component<ECS_TYPE>>,
        ComponentType<ECS_TYPE, ? extends Component<ECS_TYPE>>
    > getComponentMap();

    @Nullable
    public default <T extends Component<ECS_TYPE>> ComponentType<ECS_TYPE, T> getComponentType(
        Class<T> componentClass
    ) {
        var componentType = this.getComponentMap().get(componentClass);
        if (componentType == null) {
            return null;
        }

        // casting is safe as long as i haven't stuffed something up
        @SuppressWarnings("unchecked")
        var res = (ComponentType<ECS_TYPE, T>) componentType;

        return res;
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public default <T extends Component<ECS_TYPE>> ComponentType<ECS_TYPE, T> getComponentType(String componentId) {
        return (ComponentType<ECS_TYPE, T>) this.getComponentByIdMap().get(componentId);
    }

    public default <T extends Component<ECS_TYPE>> ComponentType<ECS_TYPE, T> registerComponent(
        JavaPlugin plugin,
        BuilderCodec<T> codec
    ) {
        var clazz = codec.getInnerClass();
        if (clazz == null) {
            throw new LibHytaleException("Failed to get component class from codec in asdiofhaowiehpg34");
        }

        var componentType = this._registerComponent(plugin, codec);
        this.bindEventListeners(plugin, clazz, componentType);

        return componentType;
    }

    public default <T extends Component<ECS_TYPE>> ComponentType<ECS_TYPE, T> registerComponent(
        JavaPlugin plugin,
        Class<T> clazz
    ) {
        var componentType = this._registerComponent(plugin, clazz);
        this.bindEventListeners(plugin, clazz, componentType);

        return componentType;
    }

    /**
     * Registers the component type with the static map that stores
     * all that goodness for us. IRegisteredComponent gets to know
     * about EVERYTHING above it WOOOO
     */
    public default <T extends Component<ECS_TYPE>> void registerComponentType(
        ComponentType<ECS_TYPE, T> componentType,
        Class<T> myClass
    ) {
        final var defaultId = myClass.getName();
        if (defaultId == null) {
            throw new LibHytaleException("Failed to make ID (failed to call .getName()) for class " + myClass);
        }
        this.registerComponentType(componentType, myClass, defaultId);
    }

    /**
     * Registers the component type with the static map that stores
     * all that goodness for us. IRegisteredComponent gets to know
     * about EVERYTHING above it WOOOO
     */
    public default <T extends Component<ECS_TYPE>> void registerComponentType(
        ComponentType<ECS_TYPE, T> componentType,
        Class<T> myClass,
        String id
    ) {
        this.getComponentMap().put(myClass, componentType);
        this.getComponentByIdMap().put(id, componentType);
    }

    public abstract Map<String, ComponentType<ECS_TYPE, ? extends Component<ECS_TYPE>>> getComponentByIdMap();

    public abstract ComponentRegistryProxy<ECS_TYPE> getStoreRegistry(JavaPlugin plugin);

    public default <T extends Component<ECS_TYPE>> ComponentType<ECS_TYPE, T> _registerComponent(
        JavaPlugin plugin,
        BuilderCodec<T> codec
    ) {
        final Class<T> clazz = codec.getInnerClass();
        final var defaultId = clazz.getName();

        console.log("COMPONENT  " + clazz.getSimpleName());
        console.log(" --ID:     " + defaultId);
        if (defaultId == null) {
            throw new LibHytaleException("Failed to get classname while registering component with codec " + codec);
        }

        final ComponentType<ECS_TYPE, T> component = this.getStoreRegistry(plugin).registerComponent(
            clazz,
            defaultId,
            codec
        );

        // Store our component in the global register
        this.registerComponentType(component, clazz, defaultId);
        this.bindEventListeners(plugin, clazz, component);

        return component;
    }

    public default <T extends Component<ECS_TYPE>> ComponentType<ECS_TYPE, T> _registerComponent(
        JavaPlugin plugin,
        Class<T> clazz
    ) {
        final BuilderCodec<T> codec = AutoSerializeParser.tryGetCodec(clazz);
        if (codec == null || !BuilderCodec.class.isAssignableFrom(codec.getClass())) {
            throw new LibHytaleException("Failed to get codec for class " + clazz);
        }

        return this.registerComponent(plugin, codec);
    }

    public default void registerSystem(final JavaPlugin plugin, final ISystem<ECS_TYPE> system) {
        this.getStoreRegistry(plugin).registerSystem(system);
    }

    /**
     * this one is interesting, should be the same as the above method basically except calling the newUninitialised method instead
     * without the query as thats just gonna be `Query.and(componentType)`;
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public default <T extends Component> void bindEventListenersUnchecked(JavaPlugin plugin, Class<T> componentClass) {
        var componentType = this.getComponentType(componentClass);
        if (componentType == null) {
            componentType = this._registerComponent(plugin, componentClass);
        }

        this.bindEventListeners(plugin, componentClass, componentType);
    }

    public default <T extends Component<ECS_TYPE>> void bindEventListeners(JavaPlugin plugin, Class<T> componentClass) {
        ComponentType<ECS_TYPE, T> componentType = this.getComponentType(componentClass);
        if (componentType == null) {
            componentType = this._registerComponent(plugin, componentClass);
        }

        this.bindEventListeners(plugin, componentClass, componentType);
    }

    public default <T extends IQuery<ECS_TYPE>> void bindEventListeners(JavaPlugin plugin, T listener) {
        var clazz = listener.getClass();

        // find the interfaces it supports

        if (IOnAddRemove.class.isAssignableFrom(clazz)) {
            @SuppressWarnings("unchecked")
            var driver = OnAddRemove.newUninitialised((IOnAddRemove.IOnAddRemove__IQuery<ECS_TYPE>) listener, this);
            driver.onRegister(plugin);
        }
        if (IOnTick.class.isAssignableFrom(clazz)) {
            @SuppressWarnings("unchecked")
            var driver = OnTick.newUninitialised((IOnTick.IOnTick__IQuery<ECS_TYPE>) listener, this);
            driver.onRegister(plugin);
        }

        if (IOnScheduledTick.class.isAssignableFrom(clazz)) {
            // its a composite one so interestingly enough this one is recursive
            // as in onRegister it calls bindEventListeners :)
            @SuppressWarnings("unchecked")
            var driver = OnScheduledTick.newUninitialised(
                "",
                (IOnScheduledTick.IOnScheduledTick__IQuery<ECS_TYPE>) listener,
                this
            );
            driver.onRegister(plugin);
        }

        if (IOnWorldTick.class.isAssignableFrom(clazz)) {
            @SuppressWarnings("unchecked")
            var driver = OnWorldTick.newUninitialised((IOnWorldTick.IOnWorldTick__IQuery<ECS_TYPE>) listener, this);
            driver.onRegister(plugin);
        }

        this.bindRegistrySpecificEventListeners(plugin, listener);
    }

    public default <T extends Component<ECS_TYPE>> void bindEventListeners(
        JavaPlugin plugin,
        Class<T> componentClass,
        ComponentType<ECS_TYPE, T> componentType
    ) {
        if (IOnAddRemove.class.isAssignableFrom(componentClass)) {
            @SuppressWarnings({ "unchecked", "rawtypes" })
            var driver = OnAddRemove.newUninitialised((ComponentType) componentType, this);
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

        this.bindRegistrySpecificEventListeners(plugin, componentClass, componentType);
    }

    public default <T extends Component<ECS_TYPE>> void bindRegistrySpecificEventListeners(
        JavaPlugin plugin,
        Class<T> componentClass,
        ComponentType<ECS_TYPE, T> componentType
    ) {
        // only need to override this if you've got specific events for the given registry you want to init
        // e.g. onBlockTick
    }

    public default <T extends IQuery<ECS_TYPE>> void bindRegistrySpecificEventListeners(JavaPlugin plugin, T listener) {
        // only need to override this if you've got specific events for the given registry you want to init
        // e.g. onBlockTick
    }
}
