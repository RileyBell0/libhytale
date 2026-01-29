package com.example.plugin.interfaces;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.modules.block.BlockModule;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import java.util.HashMap;
import java.util.function.Supplier;
import javax.annotation.Nonnull;

// Simple wrapper around JavaPlugin to make behaviour less annoying...
public abstract class ModPlugin extends JavaPlugin {

    protected ModPlugin instance;

    public ModPlugin(@Nonnull JavaPluginInit init) {
        super(init);
        instance = this;
        console.log("Initializing plugin " + this.getName());
    }

    public ModPlugin get() {
        return this.instance;
    }

    protected HashMap<String, ComponentType<ChunkStore, ? extends Component<ChunkStore>>> registeredBlocks =
        new HashMap<String, ComponentType<ChunkStore, ? extends Component<ChunkStore>>>();
    private static final HytaleLogger.Api console = HytaleLogger.forEnclosingClass().atInfo();

    // Just forcing
    @Override
    protected final void setup0() {
        instance = this;
        console.log("Setting up plugin " + this.getName());
        super.setup0();
    }

    protected void setup() {}

    public <T extends Component<ChunkStore>> void addToRegister(
        @Nonnull String id,
        @Nonnull ComponentType<ChunkStore, T> block
    ) {
        this.registeredBlocks.put(id, block);
    }

    public <T extends TickingBlockComponent> void addToRegisterWithNewSystems(
        @Nonnull String id,
        @Nonnull ComponentType<ChunkStore, T> block
    ) {
        this.registeredBlocks.put(id, block);
        this.registerTickingBlock(block);
    }

    protected <T extends TickingBlockComponent> ComponentType<ChunkStore, T> registerTickingBlock(
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

    protected <T extends TickingBlockComponent> void registerTickingBlock(
        @Nonnull Supplier<ComponentType<ChunkStore, T>> supplier
    ) {
        var initialiser = new TickingBlockComponent_Initialiser(
            Query.and(BlockModule.BlockStateInfo.getComponentType(), supplier.get())
        );
        var system = new TickingBlockComponent_System<T>(supplier);
        this.getChunkStoreRegistry().registerSystem(initialiser);
        this.getChunkStoreRegistry().registerSystem(system);
    }

    /**
     * Registers your component to the given plugin.
     *
     * Call `NameOfThisClass.register(this);` in your plugin's setup method
     */
    public <T extends TickingBlockComponent> ComponentType<ChunkStore, T> easyRegisterComponent(
        Class<T> myClass,
        BuilderCodec<T> codec
    ) {
        var defaultId = myClass.getSimpleName();
        var component = this.getChunkStoreRegistry().registerComponent(myClass, defaultId, codec);

        // Store our component in the global register
        TickingBlockComponent.registerComponentType(myClass, component);

        return component;
    }

    /**
     * Registers your component to the given plugin.
     *
     * Call `NameOfThisClass.register(this);` in your plugin's setup method
     */
    public <T extends TickingBlockComponent> ComponentType<ChunkStore, T> easyRegisterTickingComponent(
        Class<T> myClass,
        BuilderCodec<T> codec
    ) {
        return this.registerTickingBlock(this.easyRegisterComponent(myClass, codec));
    }
}
