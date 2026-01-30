package dev.twunk.examples;

import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import dev.twunk.interfaces.ModPlugin;
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
