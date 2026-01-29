package com.example.plugin;

import com.example.plugin.examples.ExampleTickingBlockComponent;
import com.example.plugin.interfaces.ModPlugin;
// ===== Uncomment these to test alt option ======
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import javax.annotation.Nonnull;

public class ExamplePlugin extends ModPlugin {

    public ExamplePlugin(@Nonnull JavaPluginInit init) {
        super(init);
    }

    @Override
    protected void setup() {
        this.easyRegisterComponent(ExampleTickingBlockComponent.class, ExampleTickingBlockComponent.CODEC);
    }

    // If you haven't already setup your ticking blocks, you have two main reccomended approaches to chose from
    // protected void start() {
    //     // OPTION 1) Use my helper method (in either of these approaches)
    //     this.registerTickingBlock(ExampleTickingBlockComponent.getComponentType());
    //     this.registerTickingBlock(ExampleTickingBlockComponent::getComponentType);

    //     // OPTION 2) Define your own classes, override the bits i show in there (really not that much to worry about)
    //     // this.getChunkStoreRegistry().registerSystem(new ExampleTickingBlockComponent__Initialiser());
    //     // this.getChunkStoreRegistry().registerSystem(new ExampleTickingBlockComponent__System());
    // }
}
