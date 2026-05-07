package dev.twunk.hytale;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.Resource;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.plugin.registry.CodecMapRegistry.Assets;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.twunk.hytale.interfaces.methods.IRegistry;
import dev.twunk.lib.registry.TypeInferrer;

// Simple wrapper around JavaPlugin to make behavior less annoying...
public abstract class HytalePlugin extends JavaPlugin {

    private static final HytaleLogger console = HytaleLogger.forEnclosingClass();

    protected HytalePlugin(final JavaPluginInit init) {
        super(init);
        console.atInfo().log("Initializing plugin " + this.getName());
        LibHytale.init(this);
    }

    @Override
    protected final void setup0() {
        console.atInfo().log("Setting up plugin " + this.getName());
        super.setup0();
    }

    // ///////////////////////////////////////////
    //    (system)
    // ///////////////////////////////////////////

    // /////////////////////////////////////////////
    //    (component/interaction)
    // /////////////////////////////////////////////
    // Register event listeners for components of the given type. Note: this will
    // set up systems to call the methods defined ON your component of type T
    //
    //   should extend Interaction or Component

    @SuppressWarnings("null")
    public final <T> void register(Class<T> clazz) {
        HytalePlugin.register0(this, clazz, IRegistry.getBuilderCodec(clazz), clazz.getName());
    }

    public final <T> void register(Class<T> clazz, String id) {
        HytalePlugin.register0(this, clazz, IRegistry.getBuilderCodec(clazz), id);
    }

    public final <T> void register(Class<T> clazz, BuilderCodec<T> codec, String id) {
        HytalePlugin.register0(this, clazz, codec, id);
    }

    @SuppressWarnings("null")
    public static <T> void register(JavaPlugin plugin, Class<T> clazz) {
        HytalePlugin.register0(plugin, clazz, IRegistry.getBuilderCodec(clazz), clazz.getName());
    }

    public static <T> void register(JavaPlugin plugin, Class<T> clazz, String id) {
        HytalePlugin.register0(plugin, clazz, IRegistry.getBuilderCodec(clazz), id);
    }

    public static <T> void register(JavaPlugin plugin, Class<T> clazz, BuilderCodec<T> codec, String id) {
        HytalePlugin.register0(plugin, clazz, codec, id);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    protected static void register0(JavaPlugin plugin, Class clazz, BuilderCodec codec, String id) {
        if (Interaction.class.isAssignableFrom(clazz)) {
            HytalePlugin.registerInteraction0(plugin, clazz, codec, id);
        }

        if (Component.class.isAssignableFrom(clazz)) {
            HytalePlugin.registerComponent0(plugin, clazz, codec, id);
        }

        if (Resource.class.isAssignableFrom(clazz)) {
            HytalePlugin.registerResource0(plugin, clazz, codec, id);
        }
    }

    @SuppressWarnings("unchecked")
    protected static <T extends Component<?>> void registerComponent0(
            JavaPlugin plugin,
            Class<T> componentClass,
            BuilderCodec<T> codec,
            String id
    ) {
        final var inferredStore = TypeInferrer.inferTypeReceivedByGenericInClassT(Component.class, componentClass);
        if (inferredStore == null) {
            console.atWarning().log(" > [INFERRED] ECS type  <Common>   " + componentClass);
        } else if (ChunkStore.class.isAssignableFrom(inferredStore)) {
            console.atInfo().log(" > [INFERRED] ECS type  <Chunk>    " + componentClass);
        } else if (EntityStore.class.isAssignableFrom(inferredStore)) {
            console.atInfo().log(" > [INFERRED] ECS type  <Entity>   " + componentClass);
        } else {
            console.atWarning().log(" > [INFERRED] ECS type  <Common>   " + componentClass);
        }

        @SuppressWarnings("rawtypes") final Class rawClass = componentClass;

        @SuppressWarnings("rawtypes") final BuilderCodec rawCodec = codec;

        if (inferredStore != null && ChunkStore.class.isAssignableFrom(inferredStore)) {
            final var chunkComponentType = LibHytale.CHUNK_REGISTRY.registerComponent(plugin, rawClass, rawCodec, id);

            InitComponentType.trySetAnnotatedComponentType(rawClass, ChunkStore.class, chunkComponentType);
        } else if (inferredStore != null && EntityStore.class.isAssignableFrom(inferredStore)) {
            final var entityComponentType = LibHytale.ENTITY_REGISTRY.registerComponent(plugin, rawClass, rawCodec, id);

            InitComponentType.trySetAnnotatedComponentType(rawClass, EntityStore.class, entityComponentType);
        } else {
            // same as `inferredStore == null` statement
            final var chunkComponentType = LibHytale.CHUNK_REGISTRY.registerComponent(plugin, rawClass, rawCodec, id);
            final var entityComponentType = LibHytale.ENTITY_REGISTRY.registerComponent(plugin, rawClass, rawCodec, id);

            InitComponentType.trySetAnnotatedComponentType(rawClass, ChunkStore.class, chunkComponentType);
            InitComponentType.trySetAnnotatedComponentType(rawClass, EntityStore.class, entityComponentType);
        }
    }

    @SuppressWarnings("unchecked")
    protected static <T extends Resource<?>> void registerResource0(
            JavaPlugin plugin,
            Class<T> resourceClass,
            BuilderCodec<T> codec,
            String id
    ) {
        final var inferredStore = TypeInferrer.inferTypeReceivedByGenericInClassT(Resource.class, resourceClass);
        if (inferredStore == null) {
            console.atWarning().log(" > [INFERRED] ECS type  <Common>   " + resourceClass);
        } else if (ChunkStore.class.isAssignableFrom(inferredStore)) {
            console.atInfo().log(" > [INFERRED] ECS type  <Chunk>    " + resourceClass);
        } else if (EntityStore.class.isAssignableFrom(inferredStore)) {
            console.atInfo().log(" > [INFERRED] ECS type  <Entity>   " + resourceClass);
        } else {
            console.atWarning().log(" > [INFERRED] ECS type  <Common>   " + resourceClass);
        }

        @SuppressWarnings("rawtypes") final Class rawClass = resourceClass;

        @SuppressWarnings("rawtypes") final BuilderCodec rawCodec = codec;

        if (inferredStore != null && ChunkStore.class.isAssignableFrom(inferredStore)) {
            LibHytale.CHUNK_REGISTRY.registerResource(plugin, rawClass, rawCodec, id);
        } else if (inferredStore != null && EntityStore.class.isAssignableFrom(inferredStore)) {
            LibHytale.ENTITY_REGISTRY.registerResource(plugin, rawClass, rawCodec, id);
        } else {
            LibHytale.CHUNK_REGISTRY.registerResource(plugin, rawClass, rawCodec, id);
            LibHytale.ENTITY_REGISTRY.registerResource(plugin, rawClass, rawCodec, id);
        }
    }

    @SuppressWarnings("UnusedReturnValue")
    protected static <T extends Interaction> Assets<
            Interaction,
            ? extends Codec<? extends Interaction>
            > registerInteraction0(JavaPlugin plugin, Class<T> clazz, BuilderCodec<T> codec, String id) {
        return plugin.getCodecRegistry(Interaction.CODEC).register(id, clazz, codec);
    }
}
