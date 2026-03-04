package dev.twunk.plugin;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.system.ISystem;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import dev.twunk.component.IAutoTickingBlockComponent;
import dev.twunk.component.IRegisteredComponent;
import dev.twunk.system.TickableBlockComponentSystem;
import dev.twunk.utils.TwunkLib;
import javax.annotation.Nonnull;

// Simple wrapper around JavaPlugin to make behaviour less annoying...
public abstract class ModPlugin extends JavaPlugin {

    private static final HytaleLogger.Api console = HytaleLogger.forEnclosingClass().atInfo();

    public ModPlugin(@Nonnull JavaPluginInit init) {
        super(init);
        console.log("Initializing plugin " + this.getName());
        TwunkLib.init(this);
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
    public final void registerSystem(@Nonnull final ISystem<ChunkStore> system) {
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
    public <T extends Component<ChunkStore>> ComponentType<ChunkStore, T> registerComponent(BuilderCodec<T> codec) {
        Class<T> myClass = codec.getInnerClass();
        var defaultId = myClass.getName();
        console.log("Adding component " + defaultId + " -- from class " + myClass);
        if (defaultId == null) {
            throw new RuntimeException("Failed to get classname while registering component with codec " + codec);
        }

        ComponentType<ChunkStore, T> component = this.getChunkStoreRegistry().registerComponent(
            myClass,
            defaultId,
            codec
        );

        // Store our component in the global register
        IRegisteredComponent.registerComponentType(myClass, component);

        if (IAutoTickingBlockComponent.class.isAssignableFrom(myClass)) {
            // Not sure how to fix this type issue in java, know it should work so i'm really not that worried but yeah...
            new TickableBlockComponentSystem(component).registerTo(this);
        }

        return component;
    }
}
