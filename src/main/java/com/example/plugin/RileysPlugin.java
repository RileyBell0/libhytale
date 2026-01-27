package com.example.plugin;

import javax.annotation.Nonnull;

import com.example.plugin.interfaces.ModdedServerPlugin;
import com.example.plugin.structs.ExampleBlock;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;

public class RileysPlugin extends ModdedServerPlugin {
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
        super.setup();

        ExampleBlock.registerToPlugin(this);
    }

    protected void start() {
        this.getChunkStoreRegistry().registerSystem(new TickingInitializer());
        this.getChunkStoreRegistry().registerSystem(new TickingSystem());
    }
}