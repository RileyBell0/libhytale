package dev.twunk.hytale;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.system.ISystem;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.plugin.registry.CodecMapRegistry.Assets;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.twunk.hytale.interfaces.methods.IRegistry;
import dev.twunk.lib.registry.TypeInferrer;

// Simple wrapper around JavaPlugin to make behaviour less annoying...
public abstract class HytalePlugin extends JavaPlugin {

    @SuppressWarnings("null")
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
    // register events to an instance of an object   (system)
    // ///////////////////////////////////////////

    public final void register(ISystem<ChunkStore> system) {
        HytalePlugin.register(this, system);
    }

    public static final void register(JavaPlugin plugin, ISystem<ChunkStore> system) {
        plugin.getChunkStoreRegistry().registerSystem(system);
    }

    // /////////////////////////////////////////////
    // register events to a component or interaction   (component/interaction)
    // /////////////////////////////////////////////

    /**
     * Register event listeners for components of the given type. Note: this will
     * setup systems to call the methods defined ON your component of type T
     *
     *  T should extend Interaction or Component
     */
    @SuppressWarnings("null")
    public final <T> void register(Class<T> clazz) {
        HytalePlugin.register0(this, clazz, IRegistry.getCodec(clazz), clazz.getName());
    }

    public final <T> void register(Class<T> clazz, String id) {
        HytalePlugin.register0(this, clazz, IRegistry.getCodec(clazz), id);
    }

    public final <T> void register(Class<T> clazz, BuilderCodec<T> codec, String id) {
        HytalePlugin.register0(this, clazz, codec, id);
    }

    @SuppressWarnings("null")
    public static final <T> void register(JavaPlugin plugin, Class<T> clazz) {
        HytalePlugin.register0(plugin, clazz, IRegistry.getCodec(clazz), clazz.getName());
    }

    public static final <T> void register(JavaPlugin plugin, Class<T> clazz, String id) {
        HytalePlugin.register0(plugin, clazz, IRegistry.getCodec(clazz), id);
    }

    public static final <T> void register(JavaPlugin plugin, Class<T> clazz, BuilderCodec<T> codec, String id) {
        HytalePlugin.register0(plugin, clazz, codec, id);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    protected static final void register0(JavaPlugin plugin, Class clazz, BuilderCodec codec, String id) {
        if (Interaction.class.isAssignableFrom(clazz)) {
            HytalePlugin.registerInteraction0(plugin, clazz, codec, id);
        }

        if (Component.class.isAssignableFrom(clazz)) {
            HytalePlugin.registerComponent0(plugin, clazz, codec, id);
        }
    }

    @SuppressWarnings("unchecked")
    protected static final <T extends Component<?>> void registerComponent0(
        JavaPlugin plugin,
        Class<T> componentClass,
        BuilderCodec<T> codec,
        String id
    ) {
        var inferredStore = TypeInferrer.inferTypeReceivedByGenericInClassT(Component.class, componentClass);
        if (ChunkStore.class.isAssignableFrom(inferredStore)) {
            console.atInfo().log(" > [INFERRED] ECS type  <Chunk>    " + componentClass);
        } else if (EntityStore.class.isAssignableFrom(inferredStore)) {
            console.atInfo().log(" > [INFERRED] ECS type  <Entity>   " + componentClass);
        } else {
            console.atWarning().log(" > [INFERRED] ECS type  <Common>   " + componentClass);
        }

        @SuppressWarnings("rawtypes")
        Class rawClass = componentClass;

        @SuppressWarnings("rawtypes")
        BuilderCodec rawCodec = codec;

        if (ChunkStore.class.isAssignableFrom(inferredStore)) {
            LibHytale.CHUNK_REGISTRY.registerComponent(plugin, rawClass, rawCodec, id);
        } else if (EntityStore.class.isAssignableFrom(inferredStore)) {
            LibHytale.ENTITY_REGISTRY.registerComponent(plugin, rawClass, rawCodec, id);
        } else {
            LibHytale.CHUNK_REGISTRY.registerComponent(plugin, rawClass, rawCodec, id);
            LibHytale.ENTITY_REGISTRY.registerComponent(plugin, rawClass, rawCodec, id);
        }
    }

    protected static final <T extends Interaction> Assets<
        Interaction,
        ? extends Codec<? extends Interaction>
    > registerInteraction0(JavaPlugin plugin, Class<T> clazz, BuilderCodec<T> codec, String id) {
        return plugin.getCodecRegistry(Interaction.CODEC).register(id, clazz, codec);
    }
}
