package com.example.plugin;

import javax.annotation.Nonnull;

import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;

public class ExamplePlugin extends JavaPlugin {
    protected static ExamplePlugin instance;
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    private ComponentType<ChunkStore, ExampleBlock> exampleBlockComponentType;
    private ComponentType<ChunkStore, TickingBlock> tickingBlockComponentType;

    public static ExamplePlugin get() {
        return instance;
    }

    public ExamplePlugin(@Nonnull JavaPluginInit init) {
        super(init);
        LOGGER.atInfo().log("Hello from " + this.getName() + " version " + this.getManifest().getVersion().toString());
    }

    @Override
    protected void setup() {
        instance = this;
        LOGGER.atInfo().log("Setting up plugin " + this.getName());
        this.exampleBlockComponentType = this.getChunkStoreRegistry().registerComponent(ExampleBlock.class,
                "ExampleBlock", ExampleBlock.CODEC);
        this.tickingBlockComponentType = this.getChunkStoreRegistry().registerComponent(TickingBlock.class,
                "TickingBlock", TickingBlock.CODEC);
        this.getChunkStoreRegistry().registerSystem(new ExampleSystem());

    }

    public ComponentType<ChunkStore, ExampleBlock> getExampleBlockComponentType() {
        return this.exampleBlockComponentType;
    }

    public ComponentType<ChunkStore, TickingBlock> getTickingBlockComponentType() {
        return this.tickingBlockComponentType;
    }
}