package dev.twunk.hytale.interfaces.methods;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentRegistryProxy;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.ISystem;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.universe.world.WorldProvider;
import dev.twunk.hytale.LibHytaleException;
import dev.twunk.hytale.event.OnAddRemove;
import dev.twunk.hytale.event.OnTick;
import dev.twunk.hytale.event.OnWorldTick;
import dev.twunk.hytale.interfaces.event.IOnAddRemove;
import dev.twunk.hytale.interfaces.event.IOnTick;
import dev.twunk.hytale.interfaces.event.IOnWorldTick;
import dev.twunk.lib.codec.AutoSerializeParser;
import java.util.function.Supplier;
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

    public static <T> BuilderCodec<T> getCodec(Class<T> clazz) {
        final BuilderCodec<T> codec = AutoSerializeParser.tryGetCodec(clazz);
        if (codec == null || !BuilderCodec.class.isAssignableFrom(codec.getClass())) {
            throw new LibHytaleException("Failed to get codec for class " + clazz);
        }

        return codec;
    }

    // ////////////////////////////////////////////////////////////////////////
    // Methods that require an implementation
    // ////////////////////////////////////////////////////////////////////////

    /**
     * Caches a component type for easy access via either its class or the ID associated with it
     *
     * See its definition in ComponentRegistryHelper for the reason behind its existence
     */
    public <T extends Component<ECS_TYPE>> void cacheComponentType(
        ComponentType<ECS_TYPE, T> componentType,
        Class<T> myClass,
        String id
    );

    @Nullable
    public <T extends Component<ECS_TYPE>> ComponentType<ECS_TYPE, T> getComponentType(Class<T> componentClass);

    @Nullable
    public <T extends Component<ECS_TYPE>> ComponentType<ECS_TYPE, T> getComponentType(String componentId);

    public abstract ComponentRegistryProxy<ECS_TYPE> getStoreRegistry(JavaPlugin plugin);

    // ////////////////////////////////////////////////////////////////////////
    // All of the below should be left as is, an implementor not need change them.
    // ////////////////////////////////////////////////////////////////////////

    // //////////////////////
    // get/register component
    // //////////////////////

    public default <T extends Component<ECS_TYPE>> ComponentType<ECS_TYPE, T> getOrRegisterComponent(
        JavaPlugin plugin,
        Class<T> clazz
    ) {
        return getOrRegisterComponent(plugin, clazz, getCodec(clazz));
    }

    @SuppressWarnings("null") // suppressing null warning from clazz.getName()
    public default <T extends Component<ECS_TYPE>> ComponentType<ECS_TYPE, T> getOrRegisterComponent(
        JavaPlugin plugin,
        Class<T> clazz,
        BuilderCodec<T> codec
    ) {
        final var componentType = this.getComponentType(clazz);
        if (componentType != null) {
            return componentType;
        }

        return this.registerComponent(plugin, clazz, codec, clazz.getName());
    }

    // //////////////////
    // register component
    // //////////////////

    @SuppressWarnings("null") // suppressing null warning from clazz.getName()
    public default <T extends Component<ECS_TYPE>> ComponentType<ECS_TYPE, T> registerComponent(
        JavaPlugin plugin,
        Class<T> clazz
    ) {
        return this.registerComponent(plugin, clazz, getCodec(clazz), clazz.getName());
    }

    @SuppressWarnings("null") // suppressing null warning from clazz.getName()
    public default <T extends Component<ECS_TYPE>> ComponentType<ECS_TYPE, T> registerComponent(
        JavaPlugin plugin,
        BuilderCodec<T> codec
    ) {
        return this.registerComponent(plugin, codec.getInnerClass(), codec, codec.getInnerClass().getName());
    }

    /// BASE method that actually registers the component
    public default <T extends Component<ECS_TYPE>> ComponentType<ECS_TYPE, T> registerComponent(
        JavaPlugin plugin,
        Class<T> clazz,
        BuilderCodec<T> codec,
        String id
    ) {
        // register the component with hytale
        final var componentType = this.getStoreRegistry(plugin).registerComponent(clazz, id, codec);

        // store the component type for lookup via <class> or <id>
        this.cacheComponentType(componentType, clazz, id);

        // setup systems to run any event interfaces it implements
        this.registerEventListeners(plugin, clazz, codec::getDefaultValue, componentType);

        return componentType;
    }

    // ///////////////
    // register system
    // ///////////////

    /**
     * kinda low level as far as this library is concerned, as i've mostly tried to do away with the idea of systems
     */
    public default void registerSystem(JavaPlugin plugin, ISystem<ECS_TYPE> system) {
        this.getStoreRegistry(plugin).registerSystem(system);
    }

    // ////////////////////
    // bind event listeners
    // ////////////////////

    /**
     * registers event listeners
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public default <T extends Component> void unsafe_registerEventListeners(
        JavaPlugin plugin,
        Class<T> componentClass
    ) {
        this.registerEventListeners(
            plugin,
            componentClass,
            getCodec(componentClass)::getDefaultValue,
            getOrRegisterComponent(plugin, componentClass)
        );
    }

    @SuppressWarnings("null")
    public default <T extends IQuery<ECS_TYPE>> void registerEventListeners(JavaPlugin plugin, T listener) {
        this.registerEventListeners(plugin, listener, listener.getClass().getName());
    }

    @SuppressWarnings("unchecked")
    public default <T extends IQuery<ECS_TYPE>> void registerEventListeners(JavaPlugin plugin, T listener, String id) {
        final var clazz = listener.getClass();

        if (IOnAddRemove.class.isAssignableFrom(clazz)) {
            OnAddRemove.newDriverFor((IQuery<ECS_TYPE> & IOnAddRemove<ECS_TYPE>) listener, this).onRegister(plugin);
        }

        if (IOnTick.class.isAssignableFrom(clazz)) {
            OnTick.newDriverFor((IOnTick<ECS_TYPE> & IQuery<ECS_TYPE>) listener, this).onRegister(plugin);
        }

        // if (IOnScheduledTick.class.isAssignableFrom(clazz)) {
        //     OnScheduledTick.newDriverFor(id, (IOnScheduledTick<ECS_TYPE> & IQuery<ECS_TYPE>) listener, this).onRegister(
        //         plugin
        //     );
        // }

        if (IOnWorldTick.class.isAssignableFrom(clazz)) {
            OnWorldTick.newDriverFor((IOnWorldTick<ECS_TYPE> & IQuery<ECS_TYPE>) listener, this).onRegister(plugin);
        }

        this.bindRegistrySpecificEventListeners(plugin, listener);
    }

    @SuppressWarnings("null")
    public default <T extends Component<ECS_TYPE>> void registerEventListeners(
        JavaPlugin plugin,
        Class<T> componentClass,
        Supplier<T> instanceForStaticIshSystems, // probably just use codec::getDefaultValue for this, cause, i know that should work for components
        ComponentType<ECS_TYPE, T> componentType
    ) {
        this.registerEventListeners(
            plugin,
            componentClass,
            instanceForStaticIshSystems,
            componentType,
            componentClass.getName()
        );
    }

    public default <T extends Component<ECS_TYPE>> void registerEventListeners(
        JavaPlugin plugin,
        Class<T> componentClass,
        Supplier<T> instanceForStaticIshSystems, // probably just use codec::getDefaultValue for this, cause, i know that should work for components
        ComponentType<ECS_TYPE, T> componentType,
        String id
    ) {
        if (IOnAddRemove.class.isAssignableFrom(componentClass)) {
            OnAddRemove.newDriverFor(componentType, this).onRegister(plugin);
        }

        if (IOnTick.class.isAssignableFrom(componentClass)) {
            OnTick.newDriverFor(componentType, this).onRegister(plugin);
        }

        // if (IOnScheduledTick.class.isAssignableFrom(componentClass)) {
        //     OnScheduledTick.newDriverFor(id, componentType, this).onRegister(plugin);
        // }

        if (IOnWorldTick.class.isAssignableFrom(componentClass)) {
            @SuppressWarnings({ "unchecked" })
            var instanceThatListenes = (IOnWorldTick<ECS_TYPE>) instanceForStaticIshSystems.get();
            if (instanceThatListenes == null) {
                throw new LibHytaleException(
                    "error: failed to consume supplier for " + componentClass + " while registering event listeners"
                );
            }

            OnWorldTick.newDriverFor(instanceThatListenes, Query.and(componentType), this)
                // .setDependencies(Set.of(new SystemDependency<>(Order.AFTER, addRemoveClass)))
                .onRegister(plugin);
        }

        this.bindRegistrySpecificEventListeners(plugin, componentClass, componentType);
    }

    // only need to override this if you've got specific events for the given registry you want to init
    // e.g. onBlockTick
    public default <T extends Component<ECS_TYPE>> void bindRegistrySpecificEventListeners(
        JavaPlugin plugin,
        Class<T> componentClass,
        ComponentType<ECS_TYPE, T> componentType
    ) {}

    // only need to override this if you've got specific events for the given registry you want to init
    // e.g. onBlockTick
    public default <T extends IQuery<ECS_TYPE>> void bindRegistrySpecificEventListeners(
        JavaPlugin plugin,
        T listener
    ) {}
}
