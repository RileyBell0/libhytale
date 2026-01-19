package com.example.plugin;

import java.util.ArrayList;

import javax.annotation.Nullable;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.meta.BlockStateModule;
import com.hypixel.hytale.server.core.universe.world.meta.state.ItemContainerState;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;

public class ExampleBlock implements Component<ChunkStore> {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private static final ComponentType<ChunkStore, ItemContainerState> ITEM_CONTAINER_TYPE = BlockStateModule.get()
            .getComponentType(ItemContainerState.class);
    public static final BuilderCodec<ExampleBlock> CODEC;
    private boolean isRunning = false;

    public ExampleBlock() {

    }

    public static ComponentType<ChunkStore, ExampleBlock> getComponentType() {
        return ExamplePlugin.get().getExampleBlockComponentType();
    }

    // I want this one to extract from a chest it touches
    public void tick(int x, int y, int z, World world) {
        if (this.isRunning) {
            return;
        }
        var surrounding = Block.getTouching(world, x, y, z);
        this.isRunning = true;

        surrounding.thenRun(() -> {
            for (var elem : surrounding.join()) {
                if (elem == null || !elem.isValid()) {
                    continue;
                }
                LOGGER.atInfo().log("Ensuring...");
                var containerComponent = elem.getStore().ensureAndGetComponent(elem, ITEM_CONTAINER_TYPE);
                if (containerComponent == null) {
                    LOGGER.atInfo().log("NO ITEM CONTAINER FOUND");
                    continue;
                }
                var container = containerComponent.getItemContainer();

                var items = new ArrayList<ItemStack>();
                for (short i = 0; i < container.getCapacity(); i++) {
                    var stack = container.getItemStack(i);
                    items.add(stack);
                }

                container.dropAllItemStacks();
                LOGGER.atInfo().log(items.toString());
            }

            LOGGER.atInfo().log("Block %d %d %d - %s".formatted(x, y, z, world.toString()));

            this.isRunning = false;
        });

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