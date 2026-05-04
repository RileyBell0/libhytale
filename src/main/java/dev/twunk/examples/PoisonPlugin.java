package dev.twunk.examples;

import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import dev.twunk.examples.poison.PoisonComponent;
import dev.twunk.examples.poison.PoisonSystem;
import dev.twunk.hytale.HytalePlugin;

public class PoisonPlugin extends HytalePlugin {

    public PoisonPlugin(JavaPluginInit init) {
        super(init);
    }

    @Override
    protected void setup() {
        this.register(PoisonComponent.class);
        this.register(PoisonSystem.class);
    }
}
