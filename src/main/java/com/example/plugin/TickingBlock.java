package com.example.plugin;

import javax.annotation.Nullable;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;

public class TickingBlock implements Component<ChunkStore> {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    public static final BuilderCodec<TickingBlock> CODEC;

    public TickingBlock() {

    }

    public static ComponentType<ChunkStore, TickingBlock> getComponentType() {
        return ExamplePlugin.get().getTickingBlockComponentType();
    }

    public void tick(int x, int y, int z, World world) {
        LOGGER.atInfo().log("Block %d %d %d - %s".formatted(x, y, z, world.toString()));
    }

    @Nullable
    public Component<ChunkStore> clone() {
        return new TickingBlock();
    }

    static {
        CODEC = BuilderCodec.builder(TickingBlock.class, TickingBlock::new).build();
    }
}

// package com.example.plugin;

// import javax.annotation.Nullable;

// import com.hypixel.hytale.codec.builder.BuilderCodec;
// import com.hypixel.hytale.component.Component;
// import com.hypixel.hytale.component.ComponentType;
// import com.hypixel.hytale.server.core.universe.world.World;
// import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;

// public class TickingBlock implements Component<ChunkStore> {

// public static final BuilderCodec<TickingBlock> CODEC;

// public TickingBlock() {

// }

// public static ComponentType<ChunkStore, TickingBlock> getComponentType() {
// return ExamplePlugin.get().getTickingBlockComponentType();
// }

// public void runBlockAction(int x, int y, int z, World world) {
// world.execute(() -> {
// world.setBlock(x + 1, y, z, "Rock_Ice");
// });
// }

// @Nullable
// public Component<ChunkStore> clone() {
// return new TickingBlock();
// }

// static {
// CODEC = BuilderCodec.builder(TickingBlock.class, TickingBlock::new).build();
// }
// }