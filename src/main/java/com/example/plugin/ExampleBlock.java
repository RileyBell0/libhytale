package com.example.plugin;

import javax.annotation.Nullable;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;

public class ExampleBlock implements Component<ChunkStore> {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    public static final BuilderCodec<ExampleBlock> CODEC;

    public ExampleBlock() {

    }

    public static ComponentType<ChunkStore, ExampleBlock> getComponentType() {
        return ExamplePlugin.get().getExampleBlockComponentType();
    }

    public void tick(int x, int y, int z, World world) {
        LOGGER.atInfo().log("Block %d %d %d - %s".formatted(x, y, z, world.toString()));
    }

    @Nullable
    public Component<ChunkStore> clone() {
        return new ExampleBlock();
    }

    static {
        CODEC = BuilderCodec.builder(ExampleBlock.class, ExampleBlock::new).build();
    }
}

// package com.example.plugin;

// import javax.annotation.Nullable;

// import com.hypixel.hytale.codec.builder.BuilderCodec;
// import com.hypixel.hytale.component.Component;
// import com.hypixel.hytale.component.ComponentType;
// import com.hypixel.hytale.server.core.universe.world.World;
// import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;

// public class ExampleBlock implements Component<ChunkStore> {

// public static final BuilderCodec<ExampleBlock> CODEC;

// public ExampleBlock() {

// }

// public static ComponentType<ChunkStore, ExampleBlock> getComponentType() {
// return ExamplePlugin.get().getExampleBlockComponentType();
// }

// public void runBlockAction(int x, int y, int z, World world) {
// world.execute(() -> {
// world.setBlock(x + 1, y, z, "Rock_Ice");
// });
// }

// @Nullable
// public Component<ChunkStore> clone() {
// return new ExampleBlock();
// }

// static {
// CODEC = BuilderCodec.builder(ExampleBlock.class, ExampleBlock::new).build();
// }
// }