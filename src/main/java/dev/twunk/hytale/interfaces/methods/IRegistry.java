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
import dev.twunk.hytale.event.OnUniverseTick;
import dev.twunk.hytale.event.composite.OnScheduledTick;
import dev.twunk.hytale.interfaces.event.IOnAddRemove;
import dev.twunk.hytale.interfaces.event.IOnScheduledTick;
import dev.twunk.hytale.interfaces.event.IOnTick;
import dev.twunk.hytale.interfaces.event.IOnUniverseTick;
import dev.twunk.lib.codec.AutoSerializeParser;
import java.util.function.Function;
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

    public static <T> BuilderCodec<T> getBuilderCodec(Class<T> clazz) {
        final BuilderCodec<T> codec = AutoSerializeParser.tryGetBuilderCodec(clazz);
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
        return getOrRegisterComponent(plugin, clazz, getBuilderCodec(clazz));
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
        return this.registerComponent(plugin, clazz, getBuilderCodec(clazz), clazz.getName());
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
            getBuilderCodec(componentClass)::getDefaultValue,
            getOrRegisterComponent(plugin, componentClass)
        );
    }

    @SuppressWarnings("null")
    public default <T> void registerEventListeners(JavaPlugin plugin, T listener) {
        this.registerEventListeners(plugin, listener, listener.getClass().getName());
    }

    @SuppressWarnings({ "unchecked" })
    public default <T> void registerEventListeners(JavaPlugin plugin, T listener, String id) {
        if (listener instanceof IQuery q) {
            if (listener instanceof IOnAddRemove) {
                OnAddRemove.newDriverFor(
                    this,
                    q.getQuery(IOnAddRemove.class),
                    (IQuery<ECS_TYPE> & IOnAddRemove<ECS_TYPE>) listener
                ).onRegister(plugin);
            }

            if (listener instanceof IOnTick) {
                OnTick.newDriverFor(
                    this,
                    q.getQuery(IOnTick.class),
                    (IQuery<ECS_TYPE> & IOnTick<ECS_TYPE>) listener
                ).onRegister(plugin);
            }

            if (listener instanceof IOnScheduledTick) {
                OnScheduledTick.newDriverFor(
                    this,
                    q.getQuery(IOnScheduledTick.class),
                    (IQuery<ECS_TYPE> & IOnScheduledTick<ECS_TYPE>) listener,
                    id
                ).onRegister(plugin);
            }
        }

        if (listener instanceof IOnUniverseTick) {
            OnUniverseTick.newDriverFor(this, (IOnUniverseTick<ECS_TYPE>) listener).onRegister(plugin);
        }

        this.bindRegistrySpecificEventListeners(plugin, listener);
    }

    @SuppressWarnings("null")
    public default <T extends Component<ECS_TYPE>> void registerEventListeners(
        JavaPlugin plugin,
        Class<T> componentClass,
        Supplier<T> globalInstance, // probably just use codec::getDefaultValue for this, cause, i know that should work for components
        ComponentType<ECS_TYPE, T> componentType
    ) {
        this.registerEventListeners(plugin, componentClass, globalInstance, componentType, componentClass.getName());
    }

    public default <T extends Component<ECS_TYPE>> void registerEventListeners(
        JavaPlugin plugin,
        Class<T> componentClass,
        Supplier<T> supplier, // probably just use codec::getDefaultValue for this, cause, i know that should work for components
        ComponentType<ECS_TYPE, T> componentType,
        String id
    ) {
        var globalInstance = supplier.get();
        Function<Class<?>, Query<ECS_TYPE>> querySupplier = clazz -> Query.and(componentType);
        if (globalInstance instanceof IQuery) {
            @SuppressWarnings("unchecked")
            var asQueryable = ((IQuery<ECS_TYPE>) globalInstance);
            querySupplier = asQueryable::getQuery;
        }

        if (IOnAddRemove.class.isAssignableFrom(componentClass)) {
            OnAddRemove.newDriverFor(this, querySupplier.apply(IOnAddRemove.class), componentType).onRegister(plugin);
        }

        if (IOnTick.class.isAssignableFrom(componentClass)) {
            OnTick.newDriverFor(this, querySupplier.apply(IOnTick.class), componentType).onRegister(plugin);
        }

        if (IOnScheduledTick.class.isAssignableFrom(componentClass)) {
            OnScheduledTick.newDriverFor(
                this,
                querySupplier.apply(IOnScheduledTick.class),
                componentType,
                id
            ).onRegister(plugin);
        }

        if (IOnUniverseTick.class.isAssignableFrom(componentClass)) {
            @SuppressWarnings({ "unchecked" })
            var instance = (IOnUniverseTick<ECS_TYPE>) globalInstance;

            OnUniverseTick.newDriverFor(this, instance).onRegister(plugin);
        }

        this.bindRegistrySpecificEventListeners(plugin, componentClass, querySupplier, componentType);
    }

    // only need to override this if you've got specific events for the given registry you want to init
    // e.g. onBlockTick
    public default <T extends Component<ECS_TYPE>> void bindRegistrySpecificEventListeners(
        JavaPlugin plugin,
        Class<T> componentClass,
        Function<Class<?>, Query<ECS_TYPE>> supplier, // probably just use codec::getDefaultValue for this, cause, i know that should work for components
        ComponentType<ECS_TYPE, T> componentType
    ) {}

    // only need to override this if you've got specific events for the given registry you want to init
    // e.g. onBlockTick
    public default <T> void bindRegistrySpecificEventListeners(JavaPlugin plugin, T listener) {}
}
