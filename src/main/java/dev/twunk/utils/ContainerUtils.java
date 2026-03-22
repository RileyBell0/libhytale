package dev.twunk.utils;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.packets.interface_.Page;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.windows.ContainerBlockWindow;
import com.hypixel.hytale.server.core.universe.world.SoundUtil;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.twunk.components.IContainerComponent;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nonnull;

public abstract class ContainerUtils {

    @SuppressWarnings({ "removal" })
    public static void open(
        @Nonnull CommandBuffer<EntityStore> commandBuffer,
        @Nonnull InteractionContext context,
        @Nonnull Vector3i pos,
        @Nonnull ComponentType<ChunkStore, ? extends IContainerComponent> containerComponentType
    ) {
        Ref<EntityStore> ref = context.getEntity();
        Store<EntityStore> store = ref.getStore();
        Player playerComponent = commandBuffer.getComponent(ref, Player.getComponentType());
        if (playerComponent == null) {
            return;
        }

        final var block = context.getTargetBlock();
        if (block == null) {
            return;
        }

        final var world = commandBuffer.getExternalData().getWorld();

        final var blockRef = BlockUtils.Ref_.get(world, block.x, block.y, block.z);
        if (blockRef == null) {
            return;
        }

        final var containerComponent = ComponentUtils.get(blockRef, containerComponentType);
        if (containerComponent == null) {
            return;
        }

        final var container = containerComponent.getContainer();
        BlockType blockType = world.getBlockType(pos.x, pos.y, pos.z);
        if (blockType == null) {
            return;
        }

        UUIDComponent uuidComponent = commandBuffer.getComponent(ref, UUIDComponent.getComponentType());
        assert uuidComponent != null;

        UUID uuid = uuidComponent.getUuid();
        WorldChunk chunk = world.getChunk(ChunkUtil.indexChunkFromBlock(pos.x, pos.z));
        ContainerBlockWindow window = new ContainerBlockWindow(
            pos.x,
            pos.y,
            pos.z,
            chunk.getRotationIndex(pos.x, pos.y, pos.z),
            blockType,
            container
        );

        Map<UUID, ContainerBlockWindow> windows = containerComponent.getWindows();
        if (windows.putIfAbsent(uuid, window) != null) {
            // itemContainerState.onOpen(ref, world, store);
            return;
        }
        if (!playerComponent.getPageManager().setPageWithWindows(ref, store, Page.Bench, true, window)) {
            // itemContainerState.onOpen(ref, world, store);
            windows.remove(uuid, window);
            return;
        }

        window.registerCloseEvent(event -> {
            windows.remove(uuid, window);
            BlockType currentBlockType = world.getBlockType(pos);
            if (currentBlockType == null) {
                return;
            }
            if (windows.isEmpty()) {
                world.setBlockInteractionState(pos, currentBlockType, "CloseWindow");
            }

            BlockType interactionStatex = currentBlockType.getBlockForState("CloseWindow");
            if (interactionStatex == null) {
                return;
            }
            int soundEventIndexx = interactionStatex.getInteractionSoundEventIndex();
            if (soundEventIndexx == 0) {
                return;
            }

            int rotationIndexx = chunk.getRotationIndex(pos.x, pos.y, pos.z);
            Vector3d soundPosx = new Vector3d();
            blockType.getBlockCenter(rotationIndexx, soundPosx);
            soundPosx.add(pos);
            SoundUtil.playSoundEvent3d(ref, soundEventIndexx, soundPosx, commandBuffer);
        });

        if (windows.size() == 1) {
            world.setBlockInteractionState(pos, blockType, "OpenWindow");
        }

        BlockType interactionState = blockType.getBlockForState("OpenWindow");
        if (interactionState == null) {
            return;
        }

        int soundEventIndex = interactionState.getInteractionSoundEventIndex();
        if (soundEventIndex == 0) {
            return;
        }

        int rotationIndex = chunk.getRotationIndex(pos.x, pos.y, pos.z);
        Vector3d soundPos = new Vector3d();
        blockType.getBlockCenter(rotationIndex, soundPos);
        soundPos.add(pos);
        SoundUtil.playSoundEvent3d(ref, soundEventIndex, soundPos, commandBuffer);
        windows.remove(uuid, window);

        // itemContainerState.onOpen(ref, world, store);}
    }
}
