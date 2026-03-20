package dev.twunk.lib.component;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import dev.twunk.TwunkLib;
import javax.annotation.Nonnull;

public class TwunkDevTestComponent implements Component<ChunkStore> {

    @Nonnull
    @SuppressWarnings("null")
    public static ComponentType<ChunkStore, TwunkDevTestComponent> COMPONENT_TYPE = null;

    @Nonnull
    public static final BuilderCodec<TwunkDevTestComponent> CODEC = BuilderCodec.builder(
        TwunkDevTestComponent.class,
        TwunkDevTestComponent::new
    )
        .append(
            new KeyedCodec<>("Value", BuilderCodec.INTEGER),
            (self, savedVal) -> self.val = savedVal,
            self -> self.val
        )
        .add()
        .build();

    private int val;

    public TwunkDevTestComponent() {}

    @Nonnull
    public TwunkDevTestComponent setVal(int val) {
        this.val = val;
        return this;
    }

    public int getVal() {
        return this.val;
    }

    @Nonnull
    public TwunkDevTestComponent clone() {
        return new TwunkDevTestComponent();
    }

    @Nonnull
    public static ComponentType<ChunkStore, TwunkDevTestComponent> getComponentType() {
        return (ComponentType<ChunkStore, TwunkDevTestComponent>) TwunkLib.getComponentType(
            TwunkDevTestComponent.class
        );
    }
}
