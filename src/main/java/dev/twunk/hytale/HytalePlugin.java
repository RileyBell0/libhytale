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
    //    (system)
    // ///////////////////////////////////////////

    public final void register(ISystem<ChunkStore> system) {
        HytalePlugin.register(this, system);
    }

    public static final void register(JavaPlugin plugin, ISystem<ChunkStore> system) {
        plugin.getChunkStoreRegistry().registerSystem(system);
    }

    // /////////////////////////////////////////////
    //    (component/interaction)
    // /////////////////////////////////////////////
    // Register event listeners for components of the given type. Note: this will
    // setup systems to call the methods defined ON your component of type T
    //
    //  T should extend Interaction or Component

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
    public static final <T> void register(JavaPlugin plugin, Class<T> clazz) {
        HytalePlugin.register0(plugin, clazz, IRegistry.getBuilderCodec(clazz), clazz.getName());
    }

    public static final <T> void register(JavaPlugin plugin, Class<T> clazz, String id) {
        HytalePlugin.register0(plugin, clazz, IRegistry.getBuilderCodec(clazz), id);
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
        // TODO some day add some type inferrer logic that means we can search for Component.class's generic
        // for a type that implements it, e.g. if i implement onTick<ChunkStore> and onWorldTick<EntityStore> theoretically
        // that should be legal, no reason to block it, so i should lean into it and MAKE it super legal so you can
        // define both on the same one and i'll just figure out if i should have both entity and chunk systems for it or just entity or just chunk etc
        // and then if ive got that the code would do it for each one, e.g. whenever i find X is assignable from <your class> i can just
        // follow X down until i find the actual class that defines IOnAddRemove or whatever and just check that path for one that satisfies both.
        //
        // might be as easy as replacing Component.class with (Class other) -> Component.class.isAssignableFrom(other) && IOnAddRemove.isAssignableFrom(other)
        // except, notably, i need to have two "modes" i think, yeah one that finds IOnAdddRemove and THEN one that finds component
        // so more like an array for me to go down, use the first until you actually find the exact defintion of it, then use the second etc. if i wrote it recursively
        // that would be really easy, meaning i dont do a lambda, i do [IOnAddRemove.class, Component.class] and yeah you find IOnAddRemove then keep going down JUST into that type via reflection
        final var inferredStore = TypeInferrer.inferTypeReceivedByGenericInClassT(Component.class, componentClass);
        if (ChunkStore.class.isAssignableFrom(inferredStore)) {
            console.atInfo().log(" > [INFERRED] ECS type  <Chunk>    " + componentClass);
        } else if (EntityStore.class.isAssignableFrom(inferredStore)) {
            console.atInfo().log(" > [INFERRED] ECS type  <Entity>   " + componentClass);
        } else {
            console.atWarning().log(" > [INFERRED] ECS type  <Common>   " + componentClass);
        }

        @SuppressWarnings("rawtypes")
        final Class rawClass = componentClass;

        @SuppressWarnings("rawtypes")
        final BuilderCodec rawCodec = codec;

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
