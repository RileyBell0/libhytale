package dev.twunk.hytale;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.system.ISystem;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.plugin.registry.CodecMapRegistry.Assets;
import com.hypixel.hytale.server.core.universe.world.WorldProvider;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.twunk.annotations.EventRunners;
import dev.twunk.annotations.Serializable;
import dev.twunk.interfaces.methods.IOnAddRemove;
import dev.twunk.interfaces.methods.IOnBlockTick;
import dev.twunk.interfaces.methods.IOnTick;
import dev.twunk.interfaces.methods.IQuery;
import dev.twunk.lib.AutoBuilderCodec;
import dev.twunk.lib.system.AutoBlockLifetimeSystem;
import dev.twunk.lib.system.AutoBlockTickSystem;
import java.util.Arrays;
import java.util.HashSet;

// Simple wrapper around JavaPlugin to make behaviour less annoying...
public abstract class HytalePlugin extends JavaPlugin {

    @SuppressWarnings("null")
    private static final HytaleLogger.Api console = HytaleLogger.forEnclosingClass().atInfo();

    public HytalePlugin(final JavaPluginInit init) {
        super(init);
        console.log("Initializing plugin " + this.getName());
        LibHytale.init(this);
    }

    @Override
    protected final void setup0() {
        console.log("Setting up plugin " + this.getName());
        super.setup0();
    }

    /**
     * Register the given system to the plugin
     * @param system
     */
    public final void register(final ISystem<ChunkStore> system) {
        this.getChunkStoreRegistry().registerSystem(system);
    }

    /**
     * Register the given system to the plugin
     * @param system
     */
    public final void registerSystem(final ISystem<ChunkStore> system) {
        this.getChunkStoreRegistry().registerSystem(system);
    }

    /**
     * Register event listeners to the provided instance
     *
     * Requires a query on which to setup the event drivers themselves
     */
    public final void register(IQuery<?> objectThatImplementsEventListenerMethodsThatICanCallFromSubSystems) {}

    /**
     * Register event listeners for components of the given type. Note: this will
     * setup systems to call the methods defined ON your component of type T
     */
    public final <ECS_TYPE extends WorldProvider, T extends Component<ECS_TYPE>> void register(
        Class<T> classOfYourComponentThatImplementsEventListenerMethodsThatICanCall
    ) {
        // first: check if its a common component
        var isCommon = false;
        var isChunk = true;
        var isEntity = false;
        if (isCommon || isChunk) {
            addChunkEventListeners(classOfYourComponentThatImplementsEventListenerMethodsThatICanCall);
        }
        if (isCommon || isEntity) {
            addEntityEventListeners(classOfYourComponentThatImplementsEventListenerMethodsThatICanCall);
        }
    }

    private final <ECS_TYPE extends WorldProvider, T extends Component<ECS_TYPE>> void addChunkEventListeners(
        Class<T> clazz
    ) {
        // final ComponentType<ECS_TYPE, T> component = this.getChunkStoreRegistry().registerComponent(
        //     clazz,
        //     defaultId,
        //     codec
        // );

        // look for event annotations on it
        EventRunners.Chunk chunkEvents = clazz.getAnnotation(EventRunners.Chunk.class);
        if (chunkEvents == null) {
            return;
        }

        var listeners = new HashSet<>(Arrays.asList(chunkEvents.value()));
        for (@SuppressWarnings("unused")
        var eventListenerType : listeners) {}
    }

    private final <ECS_TYPE extends WorldProvider, T extends Component<ECS_TYPE>> void addEntityEventListeners(
        Class<T> clazz
    ) {
        EventRunners.Entity entityEvents = clazz.getAnnotation(EventRunners.Entity.class);
        if (entityEvents == null) {
            return;
        }

        var listeners = new HashSet<>(Arrays.asList(entityEvents.value()));
        for (@SuppressWarnings("unused")
        var eventListenerType : listeners) {}
    }

    /**
     * Register the specified component via codec. Does NOT setup
     * system/initialiser.
     * Useful especially for non-ticking components
     *
     * If you want that to be auto-registered, call `registerTickingComponent`
     * instead
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public <T extends Component<ChunkStore>> ComponentType<ChunkStore, T> registerChunkComponent(
        final BuilderCodec<T> codec
    ) {
        final Class<T> clazz = codec.getInnerClass();
        final var defaultId = clazz.getName();

        console.log("Adding component " + defaultId + " -- from class " + clazz);
        if (defaultId == null) {
            throw new RuntimeException("Failed to get classname while registering component with codec " + codec);
        }

        final ComponentType<ChunkStore, T> component = this.getChunkStoreRegistry().registerComponent(
            clazz,
            defaultId,
            codec
        );

        // Store our component in the global register
        LibHytale.registerChunkComponentType(component, clazz, defaultId);
        this.initCommonSystemsFor(clazz, component);

        if (clazz.isAnnotationPresent(dev.twunk.annotations.Serializable.class)) {
            if (Component.class.isAssignableFrom(clazz)) {
                if (IOnBlockTick.class.isAssignableFrom(clazz)) {
                    new AutoBlockTickSystem(component).registerTo(this);
                }
            }
        }

        return component;
    }

    public <T extends Component<ChunkStore>> ComponentType<ChunkStore, T> registerChunkComponent(final Class<T> clazz) {
        final BuilderCodec<T> codec = AutoBuilderCodec.tryGetCodec(clazz);
        if (codec == null || !BuilderCodec.class.isAssignableFrom(codec.getClass())) {
            throw new RuntimeException("Failed to get codec for class " + clazz);
        }

        return registerChunkComponent(codec);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private final <ECS_TYPE extends WorldProvider, T extends Component<ECS_TYPE>> void initCommonSystemsFor(
        Class<T> clazz,
        ComponentType<ECS_TYPE, T> componentType
    ) {
        if (!clazz.isAnnotationPresent(Serializable.class)) {
            return;
        }
        // need to make a hashmap for annotations

        if (Component.class.isAssignableFrom(clazz)) {
            if (IOnTick.class.isAssignableFrom(clazz)) {
                // var config = getSystemConfig(clazz, ITickComponent.class);
                new AutoBlockTickSystem(componentType).registerTo(this);
            }

            if (IOnAddRemove.class.isAssignableFrom(clazz)) {
                // var config = getSystemConfig(clazz, ILifetimeComponent.class);
                new AutoBlockLifetimeSystem(componentType).registerTo(this);
            }
        }
    }

    /**
     * Register the specified component via codec. Does NOT setup
     * system/initialiser.
     * Useful especially for non-ticking components
     *
     * If you want that to be auto-registered, call `registerTickingComponent`
     * instead
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public <T extends Component> void registerCommonComponent(final Class<T> clazz) {
        final var defaultId = clazz.getName();
        final BuilderCodec<T> codec = AutoBuilderCodec.tryGetCodec(clazz);
        if (codec == null || !BuilderCodec.class.isAssignableFrom(codec.getClass())) {
            throw new RuntimeException("Failed to get codec for class " + clazz);
        }

        console.log("Adding component " + defaultId + " -- from class " + clazz);
        if (defaultId == null) {
            throw new RuntimeException("Failed to get classname while registering component with codec " + codec);
        }

        // Store our component in the global register
        final var chunkComponent = this.getChunkStoreRegistry().registerComponent(clazz, defaultId, codec);
        LibHytale.registerChunkComponentType(chunkComponent, clazz, defaultId);

        final var entityComponent = this.getEntityStoreRegistry().registerComponent(clazz, defaultId, codec);
        LibHytale.registerEntityComponentType(entityComponent, clazz, defaultId);

        this.initCommonSystemsFor(clazz, chunkComponent);
        this.initCommonSystemsFor(clazz, entityComponent);
    }

    public <T extends Component<EntityStore>> ComponentType<EntityStore, T> registerEntityComponent(
        final Class<T> clazz
    ) {
        final BuilderCodec<T> codec = AutoBuilderCodec.tryGetCodec(clazz);
        if (codec == null || !BuilderCodec.class.isAssignableFrom(codec.getClass())) {
            throw new RuntimeException("Failed to get codec for class " + clazz);
        }

        return registerEntityComponent(codec);
    }

    public <T extends Component<EntityStore>> ComponentType<EntityStore, T> registerEntityComponent(
        final BuilderCodec<T> codec
    ) {
        final Class<T> clazz = codec.getInnerClass();
        final var defaultId = clazz.getName();
        console.log("Adding component " + defaultId + " -- from class " + clazz);
        if (defaultId == null) {
            throw new RuntimeException("Failed to get classname while registering component with codec " + codec);
        }

        final ComponentType<EntityStore, T> component = this.getEntityStoreRegistry().registerComponent(
            clazz,
            defaultId,
            codec
        );

        // Store our component in the global register
        LibHytale.registerEntityComponentType(component, clazz, defaultId);

        this.initCommonSystemsFor(clazz, component);

        return component;
    }

    public <T extends Interaction> Assets<Interaction, ?> registerInteraction(final Class<T> clazz) {
        final var defaultId = clazz.getName();
        if (defaultId == null) {
            throw new RuntimeException("Failed to get classname while registering interaction with class " + clazz);
        }

        return registerInteraction(clazz, defaultId);
    }

    public <T extends Interaction> Assets<Interaction, ?> registerInteraction(final Class<T> clazz, final String id) {
        final BuilderCodec<T> codec = AutoBuilderCodec.tryGetCodec(clazz);
        if (codec == null || !BuilderCodec.class.isAssignableFrom(codec.getClass())) {
            throw new RuntimeException("Failed to get codec for class " + clazz);
        }

        return registerInteraction(codec, id);
    }

    public <T extends Interaction> Assets<Interaction, ?> registerInteraction(final BuilderCodec<T> codec) {
        final Class<T> myClass = codec.getInnerClass();
        final var defaultId = myClass.getName();
        if (defaultId == null) {
            throw new RuntimeException("Failed to get classname while registering interaction with codec " + codec);
        }

        return registerInteraction(codec, defaultId);
    }

    public <T extends Interaction> Assets<Interaction, ?> registerInteraction(
        final BuilderCodec<T> codec,
        final String id
    ) {
        final Class<T> myClass = codec.getInnerClass();

        console.log("Adding Interaction " + id + " -- from class " + myClass);

        return this.getCodecRegistry(Interaction.CODEC).register(id, myClass, codec);
    }
}
