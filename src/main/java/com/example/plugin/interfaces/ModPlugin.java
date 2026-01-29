package com.example.plugin.interfaces;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.modules.block.BlockModule;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import java.util.function.Supplier;
import javax.annotation.Nonnull;

// Simple wrapper around JavaPlugin to make behaviour less annoying...
public abstract class ModPlugin extends JavaPlugin {

    private static final HytaleLogger.Api console = HytaleLogger.forEnclosingClass().atInfo();

    public ModPlugin(@Nonnull JavaPluginInit init) {
        super(init);
        console.log("Initializing plugin " + this.getName());
    }

    // Just forcing
    @Override
    protected final void setup0() {
        console.log("Setting up plugin " + this.getName());
        super.setup0();
    }

    protected void setup() {}

    /**
     * Register the specified component via codec. Does NOT setup system/initialiser.
     * Useful especially for non-ticking components
     *
     * If you want that to be auto-registered, call `setupTickingComponent` instead
     */
    public <T extends TickingBlockComponent> ComponentType<ChunkStore, T> registerComponent(BuilderCodec<T> codec) {
        var myClass = codec.getInnerClass();
        var defaultId = myClass.getSimpleName();

        var component = this.getChunkStoreRegistry().registerComponent(myClass, defaultId, codec);

        // Store our component in the global register
        TickingBlockComponent.registerComponentType(myClass, component);

        return component;
    }

    /**
     * Register component AND setup system/initialiser for the given component (associated by codec)
     */
    public <T extends TickingBlockComponent> ComponentType<ChunkStore, T> setupTickingComponent(
        @Nonnull BuilderCodec<T> codec
    ) {
        var component = registerComponent(codec);
        return this.setupTickingComponent(component);
    }

    /**
     * Setup system/initialiser for the given component type
     */
    public <T extends TickingBlockComponent> ComponentType<ChunkStore, T> setupTickingComponent(
        @Nonnull Supplier<ComponentType<ChunkStore, T>> supplier
    ) {
        return this.setupTickingComponent(supplier.get());
    }

    /**
     * Setup system/initialiser for the given component type
     */
    public <T extends TickingBlockComponent> ComponentType<ChunkStore, T> setupTickingComponent(
        @Nonnull ComponentType<ChunkStore, T> componentType
    ) {
        var initialiser = new TickingBlockComponent_Initialiser(
            Query.and(BlockModule.BlockStateInfo.getComponentType(), componentType)
        );
        var system = new TickingBlockComponent_System<T>(componentType);

        this.getChunkStoreRegistry().registerSystem(initialiser);
        this.getChunkStoreRegistry().registerSystem(system);

        return componentType;
    }
}
