package dev.twunk.hytale.utils;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.packets.interface_.Page;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.windows.ContainerBlockWindow;
import com.hypixel.hytale.server.core.inventory.container.SimpleItemContainer;
import com.hypixel.hytale.server.core.universe.world.SoundUtil;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.twunk.hytale.interfaces.component.IContainerComponent;
import dev.twunk.hytale.interfaces.methods.IContainer;
import dev.twunk.hytale.interfaces.methods.IPersistentContainer;
import dev.twunk.hytale.ref.EntityRef;

import java.util.Map;
import java.util.UUID;

/// Currently just has a method for opening a GUI for a given container for a given player
///
/// My code
///
/// @see IContainer          - MY container interface. Defines the methods a class must
///                            satisfy to be able to be considered a container. Really base level.
///                            requires:
///                            - getCapacity of the container
///                            - getContainer - container itself (doesn't have to be the same one each time,
///                              e.g. for "trash" I just instantiate a new empty container each time
///                              someone tries to open it)
/// @see IContainerComponent - is both IContainer and a Component. The absolute minimum
///                            requirements for my ContainerUtils code functions.
///
///
/// Hytale's code
/// @see SimpleItemContainer - THE underlying BASE item container. Just stores items
///                            and has a capacity. Really. if you need to store items
///                            in any capacity, probably just use this
public abstract class ContainerUtils {

    /// Opens a UI window for the player (only works for players here) for the
    /// container within the container component type
    ///
    /// Most of this comes from the hytale src code
    public static void openContainerGUI(
            final ComponentType<ChunkStore, ? extends IContainerComponent<ChunkStore>> containerComponentType,
            final CommandBuffer<EntityStore> commandBuffer,
            final InteractionContext context,
            final Vector3i pos
    ) {
        // get ref to the block we're looking at
        final EntityRef ref = new EntityRef(context.getEntity());
        final var world = ref.getWorld();
        final var block = context.getTargetBlock();
        if (block == null) {
            return;
        }
        final var blockRef = BlockUtils.Refs.get(world, block.x, block.y, block.z);
        if (blockRef == null) {
            return;
        }

        // Ensure we can both VIEW and OPEN the container
        final var containerComponent = ComponentUtils.get(blockRef, containerComponentType);
        if (containerComponent == null || !containerComponent.canView() || !containerComponent.canOpen()) {
            return;
        }

        final var container = containerComponent.getContainer();
        final BlockType blockType = world.getBlockType(pos.x, pos.y, pos.z);
        if (blockType == null) {
            return;
        }

        final UUIDComponent uuidComponent = ref.getComponent(UUIDComponent.getComponentType());
        if (uuidComponent == null) {
            return;
        }

        final UUID uuid = uuidComponent.getUuid();
        final WorldChunk chunk = world.getChunk(ChunkUtil.indexChunkFromBlock(pos.x, pos.z));
        if (chunk == null) {
            return;
        }

        @SuppressWarnings("removal") final ContainerBlockWindow window = new ContainerBlockWindow(
                pos.x,
                pos.y,
                pos.z,
                chunk.getRotationIndex(pos.x, pos.y, pos.z),
                blockType,
                container
        );

        final Map<UUID, ContainerBlockWindow> windows = containerComponent.getWindows();
        if (windows.putIfAbsent(uuid, window) != null) {
            containerComponent.onOpen(ref);
            return;
        }

        final Player playerComponent = ref.getComponent(Player.getComponentType());
        if (playerComponent == null) {
            return;
        }

        if (!playerComponent.getPageManager().setPageWithWindows(ref, ref.getStore(), Page.Bench, true, window)) {
            containerComponent.onOpen(ref);
            windows.remove(uuid, window);
            return;
        }

        if (containerComponent instanceof IPersistentContainer persistentContainer) {
            persistentContainer.setChunk(chunk);
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

            final BlockType interactionState = currentBlockType.getBlockForState("CloseWindow");
            if (interactionState == null) {
                return;
            }
            final int soundEventIndex = interactionState.getInteractionSoundEventIndex();
            if (soundEventIndex == 0) {
                return;
            }

            @SuppressWarnings("removal") final int rotationIndex = chunk.getRotationIndex(pos.x, pos.y, pos.z);
            final Vector3d soundPos = new Vector3d();
            blockType.getBlockCenter(rotationIndex, soundPos);
            soundPos.add(pos);
            SoundUtil.playSoundEvent3d(ref, soundEventIndex, soundPos, commandBuffer);
        });

        if (windows.size() == 1) {
            world.setBlockInteractionState(pos, blockType, "OpenWindow");
        }

        final BlockType interactionState = blockType.getBlockForState("OpenWindow");
        if (interactionState == null) {
            return;
        }

        final int soundEventIndex = interactionState.getInteractionSoundEventIndex();
        if (soundEventIndex == 0) {
            return;
        }

        @SuppressWarnings("removal") final int rotationIndex = chunk.getRotationIndex(pos.x, pos.y, pos.z);
        final Vector3d soundPos = new Vector3d();
        blockType.getBlockCenter(rotationIndex, soundPos);
        soundPos.add(pos);
        SoundUtil.playSoundEvent3d(ref, soundEventIndex, soundPos, commandBuffer);
        windows.remove(uuid, window);

        containerComponent.onOpen(ref);
    }
}
