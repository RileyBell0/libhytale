package dev.twunk.utils;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.util.FastRandom;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.event.events.ecs.DropItemEvent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.entity.item.ItemComponent;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class ItemUtils extends com.hypixel.hytale.server.core.entity.ItemUtils {

    // // This is a "stack" of items, i believe all items should be considered a stack, really
    // // hoping that a single item is just a stack of "1" items
    // private static final Class<?> Item = ItemStack.class;

    // // This seems to contain all the stuff i already wanted working
    // //
    // // Notably, hytale seems to have really focussed in on entities and general
    // // interactions like THIS and not the backend logic of blocks/interactions in
    // // that regard. This entity code seems way more polished than literally anything
    // // related to chunks and blocks and the world itself
    // private static final Class<?> InvHelper = InventoryHelper.class;

    @SuppressWarnings("unused")
    private static final HytaleLogger.Api console = HytaleLogger.forEnclosingClass().atInfo();

    private static final float DEFAULT_VELOCITY = 1.5F;

    @Nonnull
    @SuppressWarnings("null")
    private static final Vector3f ZERO_VEC = Vector3f.ZERO;

    public static final void test(
        @Nonnull final Ref<ChunkStore> blockRef,
        @Nonnull final WorldChunk worldChunk,
        @Nonnull final CommandBuffer<ChunkStore> commandBuffer,
        @Nonnull final Vector3i providedCoords
    ) {
        // final var blockX = providedCoords.x;
        // final var blockY = providedCoords.y;
        // final var blockZ = providedCoords.z;
        // final var index = Coords.Index.get(blockX, blockY, blockZ);
        // final var localCoords = Coords.Local.get(index);
        // final var blockCoords = new Vector3i(blockX, blockY, blockZ);
        // final var test = new TestUtil(commandBuffer, blockCoords);
        // var local = Coords.Local.get(test.blockRef);
        // if (local == null || !local.equals(localCoords)) {
        //     throw new RuntimeException("Failed to convert blockRef to localCoords");
        // }
    }

    @Nullable
    public static Ref<EntityStore> spawn(
        // need someone to OWN an event
        @Nonnull final Ref<EntityStore> eventOwnerRef,
        @Nonnull final ComponentAccessor<EntityStore> componentAccessor,
        @Nonnull final Vector3i throwPosition,
        @Nonnull final ItemStack itemsToSpawn
    ) {
        // Dispatch a drop event
        DropItemEvent.Drop event = new DropItemEvent.Drop(itemsToSpawn, DEFAULT_VELOCITY);
        componentAccessor.invoke(eventOwnerRef, event);
        final var items = event.getItemStack();
        if (event.isCancelled()) {
            return null;
        }

        // Calc the random dir to throw the item
        FastRandom rand = new FastRandom();
        Vector3d throwDirection = new Vector3d(
            rand.nextFloat() * 2 - 1,
            // forces y to be positive, so random number is in range [0.3, 1.0]
            rand.nextFloat() * 0.5 + 1f,
            // YAW (can be any amount in any direction)
            rand.nextFloat() * 2 - 1
        );

        return ItemUtils.spawn(
            eventOwnerRef,
            componentAccessor,
            items,
            throwDirection,
            throwPosition.toVector3d().add(0.5d, 0.5d, 0.5d), // center spawn in block position
            event.getThrowSpeed()
        );
    }

    @Nullable
    public static Ref<EntityStore> spawn(
        @Nonnull Ref<EntityStore> ref,
        @Nonnull ComponentAccessor<EntityStore> store,
        @Nonnull ItemStack itemStack,
        @Nonnull Vector3d throwDirection,
        @Nonnull Vector3d throwPosition,
        float throwSpeed
    ) {
        if (!ref.isValid()) {
            LOGGER.atWarning().log("Attempted to throw item %s by invalid entity %s", itemStack, ref.getIndex());
            return null;
        }

        // Make default
        Holder<EntityStore> itemEntityHolder = ItemComponent.generateItemDrop(
            store,
            itemStack,
            throwPosition,
            ZERO_VEC,
            (float) throwDirection.x * throwSpeed,
            (float) throwDirection.y * throwSpeed,
            (float) throwDirection.z * throwSpeed
        );
        if (itemEntityHolder == null) {
            return null;
        }

        // Delay pickup
        ItemComponent itemComponent = (ItemComponent) itemEntityHolder.getComponent(ItemComponent.getComponentType());
        if (itemComponent != null) {
            itemComponent.setPickupDelay(1.5F);
        }

        return store.addEntity(itemEntityHolder, AddReason.SPAWN);
    }

    @Nullable
    public static Ref<EntityStore> throwItemFromEntity(
        @Nonnull Ref<EntityStore> ref,
        @Nonnull ItemStack itemStack,
        @Nonnull ComponentAccessor<EntityStore> componentAccessor
    ) {
        return throwItem(ref, itemStack, DEFAULT_VELOCITY, componentAccessor);
    }
}
