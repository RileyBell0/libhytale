package dev.twunk.hytale.utils;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.util.FastRandom;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.event.events.ecs.DropItemEvent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.entity.item.ItemComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nullable;

/**
 * Check out
 * - InventoryHelper     | com.hypixel.hytale.server.npc.util.InventoryHelper
 *
 */
public abstract class ItemUtils extends com.hypixel.hytale.server.core.entity.ItemUtils {

    private static final float DEFAULT_VELOCITY = 1.5F;

    @SuppressWarnings("null")
    private static final Vector3f ZERO_VEC = Vector3f.ZERO;

    @Nullable
    public static Ref<EntityStore> spawn(
        final Ref<EntityStore> eventOwnerRef,
        final ComponentAccessor<EntityStore> store,
        final Vector3d throwPosition,
        final ItemStack itemsToSpawn
    ) {
        // Dispatch a drop event (found in hytale code, not entirely sure what it does yet)
        final DropItemEvent.Drop event = new DropItemEvent.Drop(itemsToSpawn, DEFAULT_VELOCITY);
        store.invoke(eventOwnerRef, event);
        final var items = event.getItemStack();
        if (event.isCancelled()) {
            return null;
        }

        // Calc a small random dir to throw the item. Will all vaguely fall in
        // the same area, but at least with some variation now
        final FastRandom rand = new FastRandom();
        final Vector3d throwDirection = new Vector3d(
            rand.nextFloat() * 2 - 1,
            // forces y to be positive, so random number is in range [0.3, 1.0]
            rand.nextFloat() * 0.5 + 1f,
            // YAW (can be any amount in any direction)
            rand.nextFloat() * 2 - 1
        );

        // Finally: spawn the item stack
        return ItemUtils.spawn(
            eventOwnerRef,
            store,
            throwPosition.add(0.5d, 0.5d, 0.5d), // center spawn in block position
            throwDirection,
            event.getThrowSpeed(),
            items
        );
    }

    @Nullable
    public static Ref<EntityStore> spawn(
        final Ref<EntityStore> eventOwnerRef,
        final ComponentAccessor<EntityStore> store,
        final Vector3d throwPosition,
        final Vector3d throwDirection,
        final float throwSpeed,
        final ItemStack itemStack
    ) {
        if (!eventOwnerRef.isValid()) {
            LOGGER.atWarning().log(
                "Attempted to throw item %s by invalid entity %s",
                itemStack,
                eventOwnerRef.getIndex()
            );
            return null;
        }

        // Begin creating the entity for the ItemStack
        final Holder<EntityStore> itemEntityHolder = ItemComponent.generateItemDrop(
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

        // Delay players picking up the item by a little
        final ItemComponent itemComponent = (ItemComponent) itemEntityHolder.getComponent(
            ItemComponent.getComponentType()
        );
        if (itemComponent != null) {
            itemComponent.setPickupDelay(1.5F);
        }

        return store.addEntity(itemEntityHolder, AddReason.SPAWN);
    }

    @Nullable
    public static Ref<EntityStore> throwItemFromEntity(
        final Ref<EntityStore> ref,
        final ItemStack itemStack,
        final ComponentAccessor<EntityStore> componentAccessor
    ) {
        return com.hypixel.hytale.server.core.entity.ItemUtils.throwItem(
            ref,
            itemStack,
            DEFAULT_VELOCITY,
            componentAccessor
        );
    }
}
