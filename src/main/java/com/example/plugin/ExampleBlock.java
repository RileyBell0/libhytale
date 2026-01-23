package com.example.plugin;

import javax.annotation.Nonnull;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.universe.world.meta.BlockState;
import com.hypixel.hytale.server.core.universe.world.meta.state.ItemContainerState;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;

public class ExampleBlock extends ItemContainerState {
    private static final HytaleLogger.Api console = HytaleLogger.forEnclosingClass().atInfo();

    @SuppressWarnings("removal")
    public static final BuilderCodec<ExampleBlock> CODEC2 = BuilderCodec
            .builder(ExampleBlock.class, ExampleBlock::new, BlockState.BASE_CODEC)
            .build();

    public ExampleBlock() {
        super();

        /*
         * var a = BlockType.fromString("ExampleBlock");
         * var b = BlockType.getAssetMap().getAsset("ExampleBlock");
         * 
         * console.log("==============================");
         * console.log("==============================");
         * console.log("SPAGHETTI");
         * console.log("==============================");
         * console.log("==============================");
         * console.log(a == null ? "null" : a.toString());
         * console.log(b == null ? "null" : b.toString());
         * console.log("==============================");
         * if (a != null && b != null) {
         * console.log(a.equals(b) ? "true" : "false");
         * console.log(a == b ? "true" : "false");
         * }
         * if (b != null) {
         * // this.initialize(b);
         * } else if (a != null) {
         * // this.initialize(a);
         * } else {
         * // this.initialize(BlockType.UNKNOWN);
         * }
         * console.log("==============================\n\n\n\n");
         */
    }

    public static ComponentType<ChunkStore, ExampleBlock> getComponentType() {
        return ExamplePlugin.get().getExampleBlockComponentType();
    }

    @Nonnull
    public ExampleBlock clone() {
        return new ExampleBlock();
    }
}
