package dev.twunk.interfaces;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.asset.type.blocktick.config.TickProcedure;
import com.hypixel.hytale.server.core.modules.block.BlockModule;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import dev.twunk.ticking.component.ITickingComponent;
import dev.twunk.ticking.component.system.TickingBlockComponent_Initialiser;
import dev.twunk.ticking.component.system.TickingBlockComponent_System;
import dev.twunk.utils.TwunkLib;
import java.util.function.Supplier;
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
     * Register the specified component via codec. Does NOT setup
     * system/initialiser.
     * Useful especially for non-ticking components
     *
     * If you want that to be auto-registered, call `registerTickingComponent`
     * instead
     */
    @Nonnull
    public <T extends Component<ChunkStore>> ComponentType<ChunkStore, T> registerComponent(BuilderCodec<T> codec) {
        var myClass = codec.getInnerClass();
        var defaultId = myClass.getSimpleName();
        if (defaultId == null) {
            throw new RuntimeException("HECK, for some reason couldn't get the simple classname");
        }

        var component = this.getChunkStoreRegistry().registerComponent(myClass, defaultId, codec);

        // Store our component in the global register
        ITickingComponent.registerComponentType(myClass, component);

        return component;
    }

    /**
     * Register the specified component via codec. Does NOT setup
     * system/initialiser.
     * Useful especially for non-ticking components
     *
     * If you want that to be auto-registered, call `registerTickingComponent`
     * instead
     */
    public <T extends TickProcedure> void registerTickProcedure(BuilderCodec<T> codec) {
        var myClass = codec.getInnerClass();
        var defaultId = myClass.getSimpleName();
        if (defaultId == null) {
            throw new RuntimeException("HECK, for some reason couldn't get the simple classname");
        }

        TickProcedure.CODEC.register(defaultId, myClass, codec);
    }

    /**
     * Register component AND setup system/initialiser for the given component
     * (associated by codec)
     */
    public <T extends ITickingComponent> ComponentType<ChunkStore, T> registerTickingComponent(
            @Nonnull BuilderCodec<T> codec) {
        var component = registerComponent(codec);

        return this.registerTickingComponent(component);
    }

    /**
     * Setup system/initialiser for the given component type
     */
    @Nonnull
    public <T extends ITickingComponent> ComponentType<ChunkStore, T> registerTickingComponent(
            @Nonnull Supplier<ComponentType<ChunkStore, T>> supplier) {
        var val = supplier.get();
        if (val == null) {
            throw new RuntimeException("ERROR - supplier failed");
        }

        HytaleLogger.forEnclosingClass().atSevere().log("RILEY RILEY RILEY " + val.getClass());

        // TickProcedure.CODEC.register(val.getClass().getSimpleName(),
        // InherentTickProcedure.class,
        // InherentTickProcedure.CODEC);

        return this.registerTickingComponent(val);
    }

    /**
     * Setup system/initialiser for the given component type
     */
    @Nonnull
    public <T extends ITickingComponent> ComponentType<ChunkStore, T> registerTickingComponent(
            @Nonnull ComponentType<ChunkStore, T> componentType) {
        var initialiser = new TickingBlockComponent_Initialiser(
                Query.and(BlockModule.BlockStateInfo.getComponentType(), componentType));
        var system = new TickingBlockComponent_System<T>(componentType);

        this.getChunkStoreRegistry().registerSystem(initialiser);
        this.getChunkStoreRegistry().registerSystem(system);

        return componentType;
    }
}
