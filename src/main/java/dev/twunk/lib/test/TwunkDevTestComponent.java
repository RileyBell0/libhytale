package dev.twunk.lib.test;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import dev.twunk.hytale.annotations.Serializable;
import dev.twunk.hytale.annotations.Serialize;

@Serializable
public class TwunkDevTestComponent implements Component<ChunkStore> {

    @Serialize
    private int value;

    public final TwunkDevTestComponent setValue(final int val) {
        this.value = val;
        return this;
    }

    public final int getValue() {
        return this.value;
    }

    public TwunkDevTestComponent clone() {
        return new TwunkDevTestComponent();
    }
}
