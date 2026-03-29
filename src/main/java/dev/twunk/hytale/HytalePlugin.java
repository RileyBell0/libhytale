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
import dev.twunk.hytale.utils.AutoCodecGenerator;
import dev.twunk.interfaces.component.IBlockTickComponent;
import dev.twunk.interfaces.component.ILifetimeComponent;
import dev.twunk.interfaces.component.ITickComponent;
import dev.twunk.lib.system.AutoBlockLifetimeSystem;
import dev.twunk.lib.system.AutoBlockTickSystem;
import javax.annotation.Nonnull;

// Simple wrapper around JavaPlugin to make behaviour less annoying...
public abstract class HytalePlugin extends JavaPlugin {

    private static final HytaleLogger.Api console = HytaleLogger.forEnclosingClass().atInfo();

    public HytalePlugin(final @Nonnull JavaPluginInit init) {
        super(init);
        console.log("Initializing plugin " + this.getName());
        LibHytale.init(this);
    }

    // Just forcing
    @Override
    protected final void setup0() {
        console.log("Setting up plugin " + this.getName());
        super.setup0();
    }

    /**
     * Register the given system to the plugin
     * @param system
     */
    public final void register(final @Nonnull ISystem<ChunkStore> system) {
        this.getChunkStoreRegistry().registerSystem(system);
    }

    /**
     * Register the given system to the plugin
     * @param system
     */
    public final void registerSystem(final @Nonnull ISystem<ChunkStore> system) {
        this.getChunkStoreRegistry().registerSystem(system);
    }

    /**
     * Register the specified component via codec. Does NOT setup
     * system/initialiser.
     * Useful especially for non-ticking components
     *
     * If you want that to be auto-registered, call `registerTickingComponent`
     * instead
     */
    @Nonnull
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public <T extends Component<ChunkStore>> ComponentType<ChunkStore, T> registerChunkComponent(
        final @Nonnull BuilderCodec<T> codec
    ) {
        final Class<T> myClass = codec.getInnerClass();
        final var defaultId = myClass.getName();

        console.log("Adding component " + defaultId + " -- from class " + myClass);
        if (defaultId == null) {
            throw new RuntimeException("Failed to get classname while registering component with codec " + codec);
        }

        final ComponentType<ChunkStore, T> component = this.getChunkStoreRegistry().registerComponent(
            myClass,
            defaultId,
            codec
        );

        // Store our component in the global register
        LibHytale.registerChunkComponentType(component, myClass, defaultId);

        if (myClass.isAnnotationPresent(dev.twunk.annotations.RegisteredComponent.class)) {
            if (IBlockTickComponent.class.isAssignableFrom(myClass)) {
                new AutoBlockTickSystem(component).registerTo(this);
            }

            if (ITickComponent.class.isAssignableFrom(myClass)) {
                // TODO
            }

            if (ILifetimeComponent.class.isAssignableFrom(myClass)) {
                new AutoBlockLifetimeSystem(component).registerTo(this);
            }
        }

        return component;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private final <ECS_STORE extends WorldProvider, T extends Component<ECS_STORE>> void initCommonSystemsFor(
        @Nonnull Class<T> clazz,
        @Nonnull ComponentType<ECS_STORE, T> componentType
    ) {
        if (!clazz.isAnnotationPresent(dev.twunk.annotations.RegisteredComponent.class)) {
            return;
        }

        if (ITickComponent.class.isAssignableFrom(clazz)) {
            // TODO
        }

        if (ILifetimeComponent.class.isAssignableFrom(clazz)) {
            new AutoBlockLifetimeSystem(componentType).registerTo(this);
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
    public <T extends Component> void registerCommonComponent(final @Nonnull Class<T> clazz) {
        final var defaultId = clazz.getName();
        Object rawCodec;
        rawCodec = AutoCodecGenerator.tryGetCodec(clazz);
        if (!BuilderCodec.class.isAssignableFrom(rawCodec.getClass())) {
            throw new RuntimeException("Failed to get codec for class " + clazz);
        }

        final BuilderCodec codec = (BuilderCodec) rawCodec;

        console.log("Adding component " + defaultId + " -- from class " + clazz);
        if (defaultId == null) {
            throw new RuntimeException("Failed to get classname while registering component with codec " + codec);
        }

        // Store our component in the global register
        var chunkComponent = this.getChunkStoreRegistry().registerComponent(clazz, defaultId, codec);
        LibHytale.registerChunkComponentType(chunkComponent, clazz, defaultId);

        var entityComponent = this.getEntityStoreRegistry().registerComponent(clazz, defaultId, codec);
        LibHytale.registerEntityComponentType(entityComponent, clazz, defaultId);

        this.initCommonSystemsFor(clazz, chunkComponent);
        this.initCommonSystemsFor(clazz, entityComponent);
    }

    @Nonnull
    public <T extends Component<EntityStore>> ComponentType<EntityStore, T> registerEntityComponent(
        final @Nonnull BuilderCodec<T> codec
    ) {
        final Class<T> myClass = codec.getInnerClass();
        final var defaultId = myClass.getName();
        console.log("Adding component " + defaultId + " -- from class " + myClass);
        if (defaultId == null) {
            throw new RuntimeException("Failed to get classname while registering component with codec " + codec);
        }

        final ComponentType<EntityStore, T> component = this.getEntityStoreRegistry().registerComponent(
            myClass,
            defaultId,
            codec
        );

        // Store our component in the global register
        LibHytale.registerEntityComponentType(component, myClass, defaultId);

        return component;
    }

    @Nonnull
    public <T extends Interaction> Assets<Interaction, ?> registerInteraction(final @Nonnull BuilderCodec<T> codec) {
        final Class<T> myClass = codec.getInnerClass();
        final var defaultId = myClass.getName();
        if (defaultId == null) {
            throw new RuntimeException("Failed to get classname while registering interaction with codec " + codec);
        }

        return registerInteraction(codec, defaultId);
    }

    @Nonnull
    public <T extends Interaction> Assets<Interaction, ?> registerInteraction(
        final @Nonnull BuilderCodec<T> codec,
        final @Nonnull String id
    ) {
        final Class<T> myClass = codec.getInnerClass();

        console.log("Adding Interaction " + id + " -- from class " + myClass);

        return this.getCodecRegistry(Interaction.CODEC).register(id, myClass, codec);
    }
}
