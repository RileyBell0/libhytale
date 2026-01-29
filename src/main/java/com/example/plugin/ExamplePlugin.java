package com.example.plugin;

import com.example.plugin.examples.ExampleTickingBlockComponent;
import com.example.plugin.interfaces.ModPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import javax.annotation.Nonnull;

public class ExamplePlugin extends ModPlugin {

    public ExamplePlugin(@Nonnull JavaPluginInit init) {
        super(init);
    }

    @Override
    protected void setup() {
        this.setupTickingComponent(ExampleTickingBlockComponent.CODEC);
    }
}
