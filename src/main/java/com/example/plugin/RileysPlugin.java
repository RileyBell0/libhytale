package com.example.plugin;

import com.example.plugin.interfaces.ModdedServerPlugin;
import com.example.plugin.structs.ExampleBlock;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import javax.annotation.Nonnull;

public class RileysPlugin extends ModdedServerPlugin {

    private static final HytaleLogger.Api console = HytaleLogger.forEnclosingClass().atInfo();

    protected static RileysPlugin instance;

    public static RileysPlugin get() {
        return instance;
    }

    public RileysPlugin(@Nonnull JavaPluginInit init) {
        super(init);
    }

    @Override
    protected void setup() {
        instance = this;

        console.log("Setting up plugin " + this.getName());
        ExampleBlock.registerToPlugin(this);
    }

    protected void start() {
        this.getChunkStoreRegistry().registerSystem(new Initialiser());
        this.getChunkStoreRegistry().registerSystem(new TickingSystem<ExampleBlock>(ExampleBlock::getComponentType));
    }
}
