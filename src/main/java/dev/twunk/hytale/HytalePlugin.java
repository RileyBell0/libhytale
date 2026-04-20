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
import dev.twunk.annotations.Serializable;
import dev.twunk.interfaces.events.IOnAddRemove;
import dev.twunk.interfaces.events.IOnTick;
import dev.twunk.interfaces.methods.IQuery;
import dev.twunk.lib.AutoBuilderCodec;

// Simple wrapper around JavaPlugin to make behaviour less annoying...
public abstract class HytalePlugin extends JavaPlugin {

    @SuppressWarnings("null")
    private static final HytaleLogger console = HytaleLogger.forEnclosingClass();

    public HytalePlugin(final JavaPluginInit init) {
        super(init);
        console.atInfo().log("Initializing plugin " + this.getName());
        LibHytale.init(this);
    }

    @Override
    protected final void setup0() {
        console.atInfo().log("Setting up plugin " + this.getName());
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
     *
     * objectThatImplementsEventListenerMethodsThatICanCallFromSubSystems
     */
    public final void register(IQuery<?> instance) {}

    /**
     * Register event listeners for components of the given type. Note: this will
     * setup systems to call the methods defined ON your component of type T
     *
     * classOfYourComponentThatImplementsEventListenerMethodsThatICanCall
     */
    public final <ECS_TYPE extends WorldProvider, T> void register(Class<T> clazz) {
        if (Interaction.class.isAssignableFrom(clazz)) {
            @SuppressWarnings("unchecked")
            var asInteraction = (Class<? extends Interaction>) clazz;

            this.registerInteraction(asInteraction);
        }

        if (Component.class.isAssignableFrom(clazz)) {
            registerComponent((Class) clazz);
        }
    }

    public final <ECS_TYPE extends WorldProvider, T extends Component<?>> void registerComponent(Class<T> clazz) {
        var inferred = CodeAnalysis.inferTypeReceivedByGenericInClassT(Component.class, clazz);

        if (inferred == null) {
            console.atWarning().log(" > [INFERRED] ECS type  <Common>");
            LibHytale.ENTITY_REGISTRY.bindEventListeners(this, (Class) clazz);
            LibHytale.CHUNK_REGISTRY.bindEventListeners(this, (Class) clazz);
        } else if (ChunkStore.class.isAssignableFrom(inferred)) {
            console.atInfo().log(" > [INFERRED] ECS type  <" + inferred + ">");
            LibHytale.CHUNK_REGISTRY.bindEventListeners(this, (Class) clazz);
        } else if (EntityStore.class.isAssignableFrom(inferred)) {
            console.atInfo().log(" > [INFERRED] ECS type  <" + inferred + ">");
            LibHytale.ENTITY_REGISTRY.bindEventListeners(this, (Class) clazz);
        } else {
            console.atSevere().log(" > FAILED TO INFER ECS type of " + inferred);
            console.atSevere().log(" > COMPONENT WAS NOT ADDED TO ANY REGISTRY");
        }
    }

    protected final <ECS_TYPE extends WorldProvider, T extends Component<ECS_TYPE>> void initCommonSystemsFor(
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
                // new AutoBlockTickSystem(componentType).registerTo(this);
            }

            if (IOnAddRemove.class.isAssignableFrom(clazz)) {
                // var config = getSystemConfig(clazz, IOnAddRemoveComponent.class);
                // new AutoBlockLifetimeSystem(componentType).registerTo(this);
            }
        }
    }

    public <T extends Component<ChunkStore>> ComponentType<ChunkStore, T> registerChunkComponent(
        BuilderCodec<T> codec
    ) {
        return LibHytale.CHUNK_REGISTRY.registerComponent(this, codec);
    }

    public <T extends Component<ChunkStore>> ComponentType<ChunkStore, T> registerChunkComponent(Class<T> clazz) {
        return LibHytale.CHUNK_REGISTRY.registerComponent(this, clazz);
    }

    public <T extends Component<EntityStore>> ComponentType<EntityStore, T> registerEntityComponent(
        BuilderCodec<T> codec
    ) {
        return LibHytale.ENTITY_REGISTRY.registerComponent(this, codec);
    }

    public <T extends Component<EntityStore>> ComponentType<EntityStore, T> registerEntityComponent(Class<T> clazz) {
        return LibHytale.ENTITY_REGISTRY.registerComponent(this, clazz);
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

        console.atInfo().log("COMPONENT  " + clazz.getSimpleName());
        console.atInfo().log(" --ID:     " + defaultId);
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

        console.atInfo().log("INTERACTION \"" + myClass.getName() + "\"");
        console.atInfo().log(" -- Class: \"" + myClass.getSimpleName() + "\"");
        console.atInfo().log(" -- ID:    \"" + id + "\"");

        return this.getCodecRegistry(Interaction.CODEC).register(id, myClass, codec);
    }
}
