package dev.twunk.hytale.interfaces.methods;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.dependency.Order;
import com.hypixel.hytale.component.dependency.SystemDependency;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.universe.world.WorldProvider;
import dev.twunk.hytale.codec.SerializeParser;
import dev.twunk.hytale.event.OnAddRemove;
import dev.twunk.hytale.event.OnTick;
import dev.twunk.hytale.event.OnUniverseTick;
import dev.twunk.hytale.event.OnWorldTick;
import dev.twunk.hytale.event.composite.OnScheduledTick;
import dev.twunk.hytale.interfaces.IEventDriver;
import dev.twunk.hytale.interfaces.config.IQuery;
import dev.twunk.hytale.interfaces.event.*;
import dev.twunk.hytale.interfaces.methods.IRegistry.EventDriver;
import dev.twunk.lib.LibHytaleException;
import dev.twunk.lib.registry.EventOrderInferrer;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

/**
 * Hytale seperates the components and systems i can register out by two types
 * - ChunkStore
 * - EntityStore
 * <p>
 * Notably, that makes it quite annoying for defining common utilities like any
 * subsystem for entities (onEntityAdded, onEntityRemove, onTick).
 * <p>
 * Given this, i'd love to not have to define mutliple copies of the exact same thing.
 * <p>
 * So, since my subsystems simply run a parent class instance that it receives,
 * something that implements IRegistry can be used for EITHER (in an entity store case
 * it'll return the `EntityStore` versions of a plugin's register methods, in a chunk store
 * case it'll do the same but for chunks etc)
 */
public interface IRegistry<ECS_TYPE extends WorldProvider> {

    static <T> BuilderCodec<T> getBuilderCodec(Class<T> clazz) {
        final BuilderCodec<T> codec = SerializeParser.tryGetBuilderCodec(clazz);
        if (codec == null) {
            throw new LibHytaleException("Failed to get codec for class " + clazz);
        }

        return codec;
    }

    // ////////////////////////////////////////////////////////////////////////
    // Methods that require an implementation
    // ////////////////////////////////////////////////////////////////////////

    /**
     * Caches a component type for easy access via either its class or the ID associated with it
     * <p>
     * See its definition in ComponentRegistryHelper for the reason behind its existence
     */
    <T extends Component<ECS_TYPE>> void cacheComponentType(
            ComponentType<ECS_TYPE, T> componentType,
            Class<T> myClass,
            String id
    );

    <T extends Resource<ECS_TYPE>> void cacheResourceType(
            ResourceType<ECS_TYPE, T> resourceType,
            Class<T> myClass,
            String id
    );

    @Nullable
    <T extends Resource<ECS_TYPE>> ResourceType<ECS_TYPE, T> getResourceType(Class<T> resourceClass);

    @Nullable
    <T extends Resource<ECS_TYPE>> ResourceType<ECS_TYPE, T> getResourceType(String resourceId);

    @Nullable
    <T extends Component<ECS_TYPE>> ComponentType<ECS_TYPE, T> getComponentType(Class<T> componentClass);

    @Nullable
    <T extends Component<ECS_TYPE>> ComponentType<ECS_TYPE, T> getComponentType(String componentId);

    default Set<Class<?>> getKnownInterfaceClasses() {
        return EventOrderInferrer.EVENT_INTERFACES;
    }

    ComponentRegistryProxy<ECS_TYPE> getStoreRegistry(JavaPlugin plugin);

    // ////////////////////////////////////////////////////////////////////////
    // All of the below should be left as is, an implementor not need change them.
    // ////////////////////////////////////////////////////////////////////////

    // //////////////////////
    // get/register resource
    // //////////////////////

    default <T extends Resource<ECS_TYPE>> ResourceType<ECS_TYPE, T> getOrRegisterResource(
            JavaPlugin plugin,
            Class<T> clazz
    ) {
        return getOrRegisterResource(plugin, clazz, getBuilderCodec(clazz));
    }

    @SuppressWarnings("null") // suppressing null warning from clazz.getName()
    default <T extends Resource<ECS_TYPE>> ResourceType<ECS_TYPE, T> getOrRegisterResource(
            JavaPlugin plugin,
            Class<T> clazz,
            BuilderCodec<T> codec
    ) {
        final var resourceType = this.getResourceType(clazz);
        if (resourceType != null) {
            return resourceType;
        }

        return this.registerResource(plugin, clazz, codec, clazz.getName());
    }

    default <T extends Resource<ECS_TYPE>> ResourceType<ECS_TYPE, T> getOrRegisterResource(
            JavaPlugin plugin,
            Class<T> clazz,
            String id
    ) {
        final var resourceType = this.getResourceType(clazz);
        if (resourceType != null) {
            return resourceType;
        }

        return this.registerResource(plugin, clazz, getBuilderCodec(clazz), id);
    }

    default <T extends Resource<ECS_TYPE>> ResourceType<ECS_TYPE, T> getOrRegisterResource(
            JavaPlugin plugin,
            Class<T> clazz,
            BuilderCodec<T> codec,
            String id
    ) {
        final var resourceType = this.getResourceType(clazz);
        if (resourceType != null) {
            return resourceType;
        }

        return this.registerResource(plugin, clazz, codec, id);
    }

    // //////////////////
    // register resource
    // //////////////////

    default <T extends Resource<ECS_TYPE>> ResourceType<ECS_TYPE, T> registerResource(
            JavaPlugin plugin,
            Class<T> clazz,
            String id
    ) {
        return this.registerResource(plugin, clazz, getBuilderCodec(clazz), id);
    }

    @SuppressWarnings("null") // suppressing null warning from clazz.getName()
    default <T extends Resource<ECS_TYPE>> ResourceType<ECS_TYPE, T> registerResource(
            JavaPlugin plugin,
            Class<T> clazz
    ) {
        return this.registerResource(plugin, clazz, getBuilderCodec(clazz), clazz.getName());
    }

    @SuppressWarnings("null") // suppressing null warning from clazz.getName()
    default <T extends Resource<ECS_TYPE>> ResourceType<ECS_TYPE, T> registerResource(
            JavaPlugin plugin,
            BuilderCodec<T> codec
    ) {
        return this.registerResource(plugin, codec.getInnerClass(), codec, codec.getInnerClass().getName());
    }

    /// BASE method that actually registers the component
    default <T extends Resource<ECS_TYPE>> ResourceType<ECS_TYPE, T> registerResource(
            JavaPlugin plugin,
            Class<T> clazz,
            BuilderCodec<T> codec,
            String id
    ) {
        // register the component with hytale
        final var resourceType = this.getStoreRegistry(plugin).registerResource(clazz, id, codec);

        // store the component type for lookup via <class> or <id>
        this.cacheResourceType(resourceType, clazz, id);

        return resourceType;
    }

    // //////////////////////
    // get/register component
    // //////////////////////

    default <T extends Component<ECS_TYPE>> ComponentType<ECS_TYPE, T> getOrRegisterComponent(
            JavaPlugin plugin,
            Class<T> clazz
    ) {
        return getOrRegisterComponent(plugin, clazz, getBuilderCodec(clazz));
    }

    @SuppressWarnings("null") // suppressing null warning from clazz.getName()
    default <T extends Component<ECS_TYPE>> ComponentType<ECS_TYPE, T> getOrRegisterComponent(
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
    default <T extends Component<ECS_TYPE>> ComponentType<ECS_TYPE, T> registerComponent(
            JavaPlugin plugin,
            Class<T> clazz
    ) {
        return this.registerComponent(plugin, clazz, getBuilderCodec(clazz), clazz.getName());
    }

    @SuppressWarnings("null") // suppressing null warning from clazz.getName()
    default <T extends Component<ECS_TYPE>> ComponentType<ECS_TYPE, T> registerComponent(
            JavaPlugin plugin,
            BuilderCodec<T> codec
    ) {
        return this.registerComponent(plugin, codec.getInnerClass(), codec, codec.getInnerClass().getName());
    }

    /// BASE method that actually registers the component
    default <T extends Component<ECS_TYPE>> ComponentType<ECS_TYPE, T> registerComponent(
            JavaPlugin plugin,
            Class<T> clazz,
            BuilderCodec<T> codec,
            String id
    ) {
        // register the component with hytale
        final var componentType = this.getStoreRegistry(plugin).registerComponent(clazz, id, codec);

        // store the component type for lookup via <class> or <id>
        this.cacheComponentType(componentType, clazz, id);

        return componentType;
    }

    // ////////////////////
    // bind event listeners
    // ////////////////////

    @SuppressWarnings("null")
    default <T> void registerEventListeners(JavaPlugin plugin, T listener) {
        this.registerEventListeners(plugin, listener, listener.getClass().getName());
    }

    @SuppressWarnings({"unchecked"})
    default <T> void registerEventListeners(JavaPlugin plugin, T listener, String id) {
        List<EventDriver<ECS_TYPE>> drivers = new ArrayList<>();

        if (listener instanceof IQuery<?> q) {
            if (listener instanceof IOnAddRemove<?> l) {
                drivers.add(EventDriver.of(OnAddRemove.newDriverFor(this, (IQuery<ECS_TYPE>) q, (IOnAddRemove<ECS_TYPE>) l), IOnAddRemove.class));
            }

            if (listener instanceof IOnTick<?> l) {
                drivers.add(EventDriver.of(OnTick.newDriverFor(this, (IQuery<ECS_TYPE>) q, (IOnTick<ECS_TYPE>) l), IOnTick.class));
            }

            if (listener instanceof IOnScheduledTick<?> l) {
                drivers.add(EventDriver.of(OnScheduledTick.newDriverFor(this, (IQuery<ECS_TYPE>) q, (IOnScheduledTick<ECS_TYPE>) l, id), IOnScheduledTick.class));
            }

            if (listener instanceof IOnWorldTick<?> l) {
                drivers.add(EventDriver.of(OnWorldTick.newDriverFor(this, (IQuery<ECS_TYPE>) q, (IOnWorldTick<ECS_TYPE>) l), IOnWorldTick.class));
            }
        }

        if (listener instanceof IOnUniverseTick<?> l) {
            drivers.add(EventDriver.of(OnUniverseTick.newDriverFor(this, (IOnUniverseTick<ECS_TYPE>) l), IOnUniverseTick.class));
        }

        drivers.addAll(this.bindRegistrySpecificEventListeners(plugin, listener));

        Methods.registerDrivers(plugin, this, drivers, listener.getClass());
    }

    // only need to override this if you've got specific events for the given registry you want to init
    // e.g. onBlockTick
    default <T extends Component<ECS_TYPE>> List<EventDriver<ECS_TYPE>> bindRegistrySpecificEventListeners(
            JavaPlugin plugin,
            Class<T> componentClass,
            Function<Class<?>, Query<ECS_TYPE>> supplier, // probably just use codec::getDefaultValue for this, cause, i know that should work for components
            ComponentType<ECS_TYPE, T> componentType
    ) {
        return new ArrayList<>();
    }

    // only need to override this if you've got specific events for the given registry you want to init
    // e.g. onBlockTick
    default <T> List<EventDriver<ECS_TYPE>> bindRegistrySpecificEventListeners(JavaPlugin plugin, T listener) {
        return new ArrayList<>();
    }

    record EventDriver<ECS_TYPE extends WorldProvider>(IEventDriver<ECS_TYPE> driver, Class<?> interfaceClass) {

        public static <ECS_TYPE extends WorldProvider> EventDriver<ECS_TYPE> of(
                IEventDriver<ECS_TYPE> driver,
                Class<?> interfaceClass
        ) {
            return new EventDriver<>(driver, interfaceClass);
        }
    }
}

class Methods {

    @SuppressWarnings("unchecked")
    public static <ECS_TYPE extends WorldProvider> void registerDrivers(
            JavaPlugin plugin,
            IRegistry<ECS_TYPE> registry,
            List<EventDriver<ECS_TYPE>> drivers,
            Class<?> clazz
    ) {
        var order = new ArrayList<>(EventOrderInferrer.analyze(clazz, registry.getKnownInterfaceClasses()));
        drivers.sort(Comparator.comparingInt(a -> order.indexOf(a.interfaceClass())));

        // then i order the dependencies by the order they're defined
        IEventDriver<ECS_TYPE> lastDependency = null;
        for (var elem : drivers) {
            if (lastDependency != null) {
                elem.driver().addDependency(new SystemDependency<>(Order.AFTER, (Class<IEventDriver<ECS_TYPE>>) lastDependency.getClass()));
            }

            elem.driver().onRegister(plugin);

            lastDependency = elem.driver();
        }
    }
}
