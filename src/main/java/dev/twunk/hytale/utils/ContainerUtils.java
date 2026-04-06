package dev.twunk.hytale.utils;

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
import com.hypixel.hytale.server.core.inventory.container.SimpleItemContainer;
import com.hypixel.hytale.server.core.universe.world.SoundUtil;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.twunk.hytale.refs.AnyRef;
import dev.twunk.interfaces.component.IContainerComponent;
import dev.twunk.interfaces.methods.IContainer;
import dev.twunk.interfaces.methods.IPersistentContainer;
import java.util.Map;
import java.util.UUID;

/**
 * Currently just has a method for opening a GUI for a given container for a given player
 *
 * My code
 * @see IContainer          - MY container interface. Defines the methods a class must
 *                            satisfy to be able to be considered a container. Really base level.
 *                            requires:
 *                            - getCapacity of the container
 *                            - getContainer - container itself (doesn't have to be the same one each time,
 *                              e.g. for "trash" i just instantiate a new empty container each time
 *                              someone tries to open it)
 * @see IContainerComponent - is both IContainer and a Component. The absolte minimum
 *                            requirements for my ContainerUtils code functions.
 *
 *
 * Hytale's code
 * @see SimpleItemContainer - THE underlying BASE item container. Just stores items
 *                            and has a capacity. Really. if you need to store items
 *                            in any capacity, probably just use this
 */
public abstract class ContainerUtils {

    /**
     * Opens a UI window for the player (only works for players here) for the
     * container within the container component type
     *
     * Most of this comes from the hytale src code
     */
    public static void openContainerGUI(
        final ComponentType<ChunkStore, ? extends IContainerComponent<ChunkStore>> containerComponentType,
        final CommandBuffer<EntityStore> commandBuffer,
        final InteractionContext context,
        final Vector3i pos
    ) {
        final Ref<EntityStore> ref = context.getEntity();
        final Store<EntityStore> store = ref.getStore();
        final Player playerComponent = commandBuffer.getComponent(ref, Player.getComponentType());
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

        final UUIDComponent uuidComponent = commandBuffer.getComponent(ref, UUIDComponent.getComponentType());
        assert uuidComponent != null;

        final UUID uuid = uuidComponent.getUuid();
        final WorldChunk chunk = world.getChunk(ChunkUtil.indexChunkFromBlock(pos.x, pos.z));
        final ContainerBlockWindow window = new ContainerBlockWindow(
            pos.x,
            pos.y,
            pos.z,
            chunk.getRotationIndex(pos.x, pos.y, pos.z),
            blockType,
            container
        );

        final Map<UUID, ContainerBlockWindow> windows = containerComponent.getWindows();
        if (windows.putIfAbsent(uuid, window) != null) {
            containerComponent.onOpen(new AnyRef<>(ref));
            return;
        }
        if (!playerComponent.getPageManager().setPageWithWindows(ref, store, Page.Bench, true, window)) {
            containerComponent.onOpen(new AnyRef<>(ref));
            windows.remove(uuid, window);
            return;
        }

        if (IPersistentContainer.class.isAssignableFrom(containerComponent.getClass())) {
            ((IPersistentContainer) containerComponent).setChunk(chunk);
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

            final BlockType interactionStatex = currentBlockType.getBlockForState("CloseWindow");
            if (interactionStatex == null) {
                return;
            }
            final int soundEventIndexx = interactionStatex.getInteractionSoundEventIndex();
            if (soundEventIndexx == 0) {
                return;
            }

            final int rotationIndexx = chunk.getRotationIndex(pos.x, pos.y, pos.z);
            final Vector3d soundPosx = new Vector3d();
            blockType.getBlockCenter(rotationIndexx, soundPosx);
            soundPosx.add(pos);
            SoundUtil.playSoundEvent3d(ref, soundEventIndexx, soundPosx, commandBuffer);
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

        final int rotationIndex = chunk.getRotationIndex(pos.x, pos.y, pos.z);
        final Vector3d soundPos = new Vector3d();
        blockType.getBlockCenter(rotationIndex, soundPos);
        soundPos.add(pos);
        SoundUtil.playSoundEvent3d(ref, soundEventIndex, soundPos, commandBuffer);
        windows.remove(uuid, window);

        containerComponent.onOpen(new AnyRef<>(ref));
    }
}
