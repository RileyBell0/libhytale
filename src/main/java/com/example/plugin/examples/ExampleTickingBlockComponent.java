package com.example.plugin.examples;

import com.example.plugin.interfaces.TickingBlockComponent;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ExampleTickingBlockComponent implements TickingBlockComponent {

    private int ticks = 0;

    public ExampleTickingBlockComponent() {}

    @Nonnull
    public static final BuilderCodec<ExampleTickingBlockComponent> CODEC = BuilderCodec.builder(
        ExampleTickingBlockComponent.class,
        ExampleTickingBlockComponent::new
    )
        .append(
            new KeyedCodec<Integer>("Ticks", Codec.INTEGER),
            (data, value) -> data.ticks = value,
            data -> data.ticks
        )
        .add()
        .build();

    @Nullable
    public ExampleTickingBlockComponent clone() {
        return new ExampleTickingBlockComponent();
    }
}
