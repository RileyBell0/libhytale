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
import dev.twunk.hytale.codec.annotations.Serializable;
import dev.twunk.hytale.interfaces.events.IOnAddRemove;
import dev.twunk.hytale.interfaces.events.IOnTick;
import dev.twunk.lib.CodeAnalysis;
import dev.twunk.lib.codec.AutoSerializeParser;

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

    //

    /**
     * Register the given system to the plugin
     * @param system
     */
    public final void register(ISystem<ChunkStore> system) {
        HytalePlugin.register(this, system);
    }

    /**
     * Register event listeners for components of the given type. Note: this will
     * setup systems to call the methods defined ON your component of type T
     *
     * classOfYourComponentThatImplementsEventListenerMethodsThatICanCall
     */
    public final <T> void register(Class<T> clazz) {
        HytalePlugin.register(this, clazz);
    }

    /**
     * Register the given system to the plugin
     * @param system
     */
    public static final void register(JavaPlugin plugin, ISystem<ChunkStore> system) {
        plugin.getChunkStoreRegistry().registerSystem(system);
    }

    /**
     * Register event listeners for components of the given type. Note: this will
     * setup systems to call the methods defined ON your component of type T
     *
     * classOfYourComponentThatImplementsEventListenerMethodsThatICanCall
     */
    public static final <T> void register(JavaPlugin plugin, Class<T> clazz) {
        if (Interaction.class.isAssignableFrom(clazz)) {
            @SuppressWarnings("unchecked")
            var asInteraction = (Class<? extends Interaction>) clazz;

            HytalePlugin.registerInteraction(plugin, asInteraction);
        }

        if (Component.class.isAssignableFrom(clazz)) {
            @SuppressWarnings("unchecked")
            var asComponent = (Class<? extends Component<?>>) clazz;

            HytalePlugin.registerComponent(plugin, asComponent);
        }
    }

    //

    /**
     * Register the given system to the plugin
     * @param system
     */
    public final void registerSystem(ISystem<ChunkStore> system) {
        HytalePlugin.registerSystem(this, system);
    }

    public static final void registerSystem(JavaPlugin plugin, ISystem<ChunkStore> system) {
        plugin.getChunkStoreRegistry().registerSystem(system);
    }

    //

    public final <ECS_TYPE extends WorldProvider, T extends Component<?>> void registerComponent(Class<T> clazz) {
        HytalePlugin.registerComponent(this, clazz);
    }

    public static final <ECS_TYPE extends WorldProvider, T extends Component<?>> void registerComponent(
        JavaPlugin plugin,
        Class<T> clazz
    ) {
        // seriously i know ive got so many commits on this now but omg oh my GOD this works, fuck YES
        var inferred = CodeAnalysis.inferTypeReceivedByGenericInClassT(Component.class, clazz);

        if (inferred == null) {
            console.atWarning().log(" > [INFERRED] ECS type  <Common>");

            @SuppressWarnings("unchecked")
            var inferredAsEntity = (Class<? extends Component<EntityStore>>) clazz;
            LibHytale.ENTITY_REGISTRY.bindEventListeners(plugin, inferredAsEntity);

            @SuppressWarnings("unchecked")
            var inferredAsChunk = (Class<? extends Component<ChunkStore>>) clazz;
            LibHytale.CHUNK_REGISTRY.bindEventListeners(plugin, inferredAsChunk);
        } else if (ChunkStore.class.isAssignableFrom(inferred)) {
            console.atInfo().log(" > [INFERRED] ECS type  <" + inferred + ">");

            @SuppressWarnings("unchecked")
            var inferredClazz = (Class<? extends Component<ChunkStore>>) clazz;
            LibHytale.CHUNK_REGISTRY.bindEventListeners(plugin, inferredClazz);
        } else if (EntityStore.class.isAssignableFrom(inferred)) {
            console.atInfo().log(" > [INFERRED] ECS type  <" + inferred + ">");

            @SuppressWarnings("unchecked")
            var inferredClazz = (Class<? extends Component<EntityStore>>) clazz;
            LibHytale.ENTITY_REGISTRY.bindEventListeners(plugin, inferredClazz);
        } else if (WorldProvider.class.isAssignableFrom(inferred)) {
            console.atWarning().log(" > [INFERRED] ECS type  <Common>");

            @SuppressWarnings("unchecked")
            var inferredAsEntity = (Class<? extends Component<EntityStore>>) clazz;
            LibHytale.ENTITY_REGISTRY.bindEventListeners(plugin, inferredAsEntity);

            @SuppressWarnings("unchecked")
            var inferredAsChunk = (Class<? extends Component<ChunkStore>>) clazz;
            LibHytale.CHUNK_REGISTRY.bindEventListeners(plugin, inferredAsChunk);
        } else {
            console.atSevere().log(" > FAILED TO INFER ECS type of " + inferred);
            console.atSevere().log(" > COMPONENT WAS NOT ADDED TO ANY REGISTRY");
        }
    }

    //

    protected final <ECS_TYPE extends WorldProvider, T extends Component<ECS_TYPE>> void initCommonSystemsFor(
        Class<T> clazz,
        ComponentType<ECS_TYPE, T> componentType
    ) {
        HytalePlugin.initCommonSystemsFor(this, clazz, componentType);
    }

    public static final <ECS_TYPE extends WorldProvider, T extends Component<ECS_TYPE>> void initCommonSystemsFor(
        JavaPlugin plugin,
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

    //

    public <T extends Component<ChunkStore>> ComponentType<ChunkStore, T> registerChunkComponent(
        BuilderCodec<T> codec
    ) {
        return HytalePlugin.registerChunkComponent(this, codec);
    }

    public <T extends Component<ChunkStore>> ComponentType<ChunkStore, T> registerChunkComponent(Class<T> clazz) {
        return HytalePlugin.registerChunkComponent(this, clazz);
    }

    public static final <T extends Component<ChunkStore>> ComponentType<ChunkStore, T> registerChunkComponent(
        JavaPlugin plugin,
        Class<T> clazz
    ) {
        return LibHytale.CHUNK_REGISTRY.registerComponent(plugin, clazz);
    }

    public static final <T extends Component<ChunkStore>> ComponentType<ChunkStore, T> registerChunkComponent(
        JavaPlugin plugin,
        BuilderCodec<T> codec
    ) {
        return LibHytale.CHUNK_REGISTRY.registerComponent(plugin, codec);
    }

    //

    public <T extends Component<EntityStore>> ComponentType<EntityStore, T> registerEntityComponent(
        BuilderCodec<T> codec
    ) {
        return HytalePlugin.registerEntityComponent(this, codec);
    }

    public <T extends Component<EntityStore>> ComponentType<EntityStore, T> registerEntityComponent(Class<T> clazz) {
        return HytalePlugin.registerEntityComponent(this, clazz);
    }

    public static final <T extends Component<EntityStore>> ComponentType<EntityStore, T> registerEntityComponent(
        JavaPlugin plugin,
        BuilderCodec<T> codec
    ) {
        return LibHytale.ENTITY_REGISTRY.registerComponent(plugin, codec);
    }

    public static final <T extends Component<EntityStore>> ComponentType<EntityStore, T> registerEntityComponent(
        JavaPlugin plugin,
        Class<T> clazz
    ) {
        return LibHytale.ENTITY_REGISTRY.registerComponent(plugin, clazz);
    }

    //

    /**
     * Register the specified component via codec. Does NOT setup
     * system/initialiser.
     * Useful especially for non-ticking components
     *
     * If you want that to be auto-registered, call `registerTickingComponent`
     * instead
     */
    @SuppressWarnings({ "rawtypes" })
    public <T extends Component> void registerCommonComponent(Class<T> clazz) {
        HytalePlugin.registerCommonComponent(this, clazz);
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
    public static final <T extends Component> void registerCommonComponent(JavaPlugin plugin, Class<T> clazz) {
        final var defaultId = clazz.getName();
        final BuilderCodec<T> codec = AutoSerializeParser.tryGetCodec(clazz);
        if (codec == null || !BuilderCodec.class.isAssignableFrom(codec.getClass())) {
            throw new RuntimeException("Failed to get codec for class " + clazz);
        }

        console.atInfo().log("COMPONENT  " + clazz.getSimpleName());
        console.atInfo().log(" --ID:     " + defaultId);
        if (defaultId == null) {
            throw new RuntimeException("Failed to get classname while registering component with codec " + codec);
        }

        // Store our component in the global register
        final var chunkComponent = plugin.getChunkStoreRegistry().registerComponent(clazz, defaultId, codec);
        LibHytale.registerChunkComponentType(chunkComponent, clazz, defaultId);

        final var entityComponent = plugin.getEntityStoreRegistry().registerComponent(clazz, defaultId, codec);
        LibHytale.registerEntityComponentType(entityComponent, clazz, defaultId);

        HytalePlugin.initCommonSystemsFor(plugin, clazz, chunkComponent);
        HytalePlugin.initCommonSystemsFor(plugin, clazz, entityComponent);
    }

    //

    public <T extends Interaction> Assets<Interaction, ?> registerInteraction(Class<T> clazz) {
        return HytalePlugin.registerInteraction(this, clazz);
    }

    public <T extends Interaction> Assets<Interaction, ?> registerInteraction(Class<T> clazz, String id) {
        return HytalePlugin.registerInteraction(this, clazz, id);
    }

    public <T extends Interaction> Assets<Interaction, ?> registerInteraction(BuilderCodec<T> codec) {
        return HytalePlugin.registerInteraction(this, codec);
    }

    public <T extends Interaction> Assets<Interaction, ?> registerInteraction(BuilderCodec<T> codec, String id) {
        return HytalePlugin.registerInteraction(this, codec, id);
    }

    public static final <T extends Interaction> Assets<Interaction, ?> registerInteraction(
        JavaPlugin plugin,
        Class<T> clazz
    ) {
        final var defaultId = clazz.getName();
        if (defaultId == null) {
            throw new RuntimeException("Failed to get classname while registering interaction with class " + clazz);
        }

        return registerInteraction(plugin, clazz, defaultId);
    }

    public static final <T extends Interaction> Assets<Interaction, ?> registerInteraction(
        JavaPlugin plugin,
        Class<T> clazz,
        String id
    ) {
        final BuilderCodec<T> codec = AutoSerializeParser.tryGetCodec(clazz);
        if (codec == null || !BuilderCodec.class.isAssignableFrom(codec.getClass())) {
            throw new RuntimeException("Failed to get codec for class " + clazz);
        }

        return registerInteraction(plugin, codec, id);
    }

    public static final <T extends Interaction> Assets<Interaction, ?> registerInteraction(
        JavaPlugin plugin,
        BuilderCodec<T> codec
    ) {
        final Class<T> myClass = codec.getInnerClass();
        final var defaultId = myClass.getName();
        if (defaultId == null) {
            throw new RuntimeException("Failed to get classname while registering interaction with codec " + codec);
        }

        return registerInteraction(plugin, codec, defaultId);
    }

    public static final <T extends Interaction> Assets<Interaction, ?> registerInteraction(
        JavaPlugin plugin,
        BuilderCodec<T> codec,
        String id
    ) {
        final Class<T> myClass = codec.getInnerClass();

        console.atInfo().log("INTERACTION \"" + myClass.getName() + "\"");
        console.atInfo().log(" -- Class: \"" + myClass.getSimpleName() + "\"");
        console.atInfo().log(" -- ID:    \"" + id + "\"");

        return plugin.getCodecRegistry(Interaction.CODEC).register(id, myClass, codec);
    }
}
