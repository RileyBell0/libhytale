package com.example.plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.inventory.container.SimpleItemContainer;
import com.hypixel.hytale.server.core.modules.entity.item.ItemComponent;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.meta.BlockStateModule;
import com.hypixel.hytale.server.core.universe.world.meta.state.ItemContainerState;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;

public class ExampleBlock extends SimpleItemContainer implements Component<ChunkStore> {
    private static final short INVENTORY_SLOTS = 5;

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private static final ComponentType<ChunkStore, ItemContainerState> ITEM_CONTAINER_TYPE = BlockStateModule.get()
            .getComponentType(ItemContainerState.class);
    public static final BuilderCodec<ExampleBlock> CODEC;
    private boolean isRunning = false;
    private ItemStack contents = null;
    private int tick = 0;
    private boolean initialized = false;

    private World world;
    private int x, y, z;

    private ArrayList<Ref<ChunkStore>> containers = new ArrayList<Ref<ChunkStore>>();

    public ExampleBlock() {
    }

    public static ComponentType<ChunkStore, ExampleBlock> getComponentType() {
        return ExamplePlugin.get().getExampleBlockComponentType();
    }

    @Override
    public short getCapacity() {
        return 2;
    }

    // I want this one to extract from a chest it touches
    public void tick(int x, int y, int z, ComponentAccessor<ChunkStore> cmd, World world) {
        if (!initialized) {
            LOGGER.atInfo().log("INITIALIZING...");

            this.initialized = true;
            this.world = world;
            this.x = x;
            this.y = y;
            this.z = z;
            this.initialize();
            return;
        }

        if (this.isRunning) {
            return;
        }

        // only run every 2 seconds at most
        if (this.tick < 60) {
            this.tick++;
            return;
        }

        this.tick = 0;

        world.execute(() -> {
            var maybeNextItem = this.takeItem();
            if (maybeNextItem.isEmpty()) {
                return;
            }
            var nextItem = maybeNextItem.get();
            var asList = new ArrayList<ItemStack>();
            asList.add(nextItem);

            // now spawn that item above the block
            var itemEntityHolders = ItemComponent.generateItemDrops(
                    world.getEntityStore().getStore(),
                    (List<ItemStack>) asList,
                    new Vector3d(x, y + 1, z),
                    Vector3f.ZERO);

            if (itemEntityHolders.length > 0) {
                world.execute(() -> world.getEntityStore().getStore().addEntities(itemEntityHolders, AddReason.SPAWN));
            }

            this.contents = null;
        });
    }

    private Optional<ItemStack> takeItem() {
        if (this.contents != null) {
            return Optional.of(this.contents);
        }

        var containers = this.getContainers();

        if (containers.size() == 0) {
            return Optional.empty();
        }

        for (var container : containers) {
            if (container.isEmpty()) {
                continue;
            }

            var items = container.removeAllItemStacks();

            var item = items.removeFirst();

            container.addItemStacks(items);

            this.contents = item;

            break;
        }

        if (this.contents == null) {
            return Optional.empty();
        }

        return Optional.of(this.contents);
    }

    private void initialize() {
        this.isRunning = true;
        var surrounding = Block.getTouching(this.world, this.x, this.y, this.z);

        surrounding.thenRun(() -> {
            for (var elem : surrounding.join()) {
                if (elem == null || !elem.isValid()) {
                    continue;
                }

                var containerComponent = elem.getStore().ensureAndGetComponent(elem, ITEM_CONTAINER_TYPE);
                if (containerComponent != null) {
                    this.containers.add(elem);
                }
            }
            this.isRunning = false;
            LOGGER.atInfo().log("initialized!!!");
        });
    }

    private ArrayList<ItemContainer> getContainers() {
        var toRemove = new ArrayList<Integer>();
        var containers = new ArrayList<ItemContainer>();

        for (int i = 0; i < this.containers.size(); i++) {
            var elem = this.containers.get(i);
            if (elem == null || !elem.isValid()) {
                toRemove.add(i);
                continue;
            }

            var containerComponent = elem.getStore().ensureAndGetComponent(elem, ITEM_CONTAINER_TYPE);
            if (containerComponent == null) {
                toRemove.add(i);
                continue;
            }

            containers.add(containerComponent.getItemContainer());
        }

        // cleanup containers we found to no longer be valid
        for (int i : toRemove.reversed()) {
            this.containers.remove(i);
        }

        return containers;
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