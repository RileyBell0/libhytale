package dev.twunk.examples;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.asset.type.blocktick.BlockTickStrategy;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import dev.twunk.interfaces.TickingBlockComponent;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ExampleTickingBlockComponent implements TickingBlockComponent {

    // Your vars
    private int ticks = 0;

    // serializing/deserializing your vars
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

    public ExampleTickingBlockComponent() {}

    public ExampleTickingBlockComponent(int ticks) {
        this.ticks = ticks;
    }

    /**
     * Run actions every tick
     * TODO have the caller actually process and handle blocktickstrategy
     */
    @Nonnull
    public BlockTickStrategy onTick(
        @Nonnull World world,
        @Nonnull WorldChunk wc,
        int worldX,
        int worldY,
        int worldZ,
        int blockId
    ) {
        this.ticks++;
        HytaleLogger.forEnclosingClass()
            .atInfo()
            .log("Ticked block at (" + worldX + ", " + worldY + ", " + worldZ + " ) " + this.ticks + " times");

        return BlockTickStrategy.CONTINUE;
    }

    @Nullable
    public ExampleTickingBlockComponent clone() {
        return new ExampleTickingBlockComponent(ticks);
    }
}
