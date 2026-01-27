package com.example.plugin.interfaces;

import java.util.HashMap;

import javax.annotation.Nonnull;

import com.example.plugin.TickingSystem;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;

// Simple wrapper around JavaPlugin to make behaviour less annoying...
public abstract class ModdedServerPlugin extends JavaPlugin {
    private static final HytaleLogger.Api console = HytaleLogger.forEnclosingClass().atInfo();
    protected HashMap<String, ComponentType<ChunkStore, ? extends Component<ChunkStore>>> registeredBlocks = new HashMap<String, ComponentType<ChunkStore, ? extends Component<ChunkStore>>>();

    public ModdedServerPlugin(@Nonnull JavaPluginInit init) {
        super(init);
    }

    public <T extends Component<ChunkStore>> void addToRegister(String id, ComponentType<ChunkStore, T> block) {
        this.registeredBlocks.put(id, block);
    }

    protected void setup() {
        console.log("Setting up plugin " + this.getName());
    }

    protected void start() {
        this.getChunkStoreRegistry().registerSystem(new TickingSystem());
    }
}