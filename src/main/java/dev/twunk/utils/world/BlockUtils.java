package dev.twunk.utils.world;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.modules.block.BlockModule;
import com.hypixel.hytale.server.core.modules.block.BlockModule.BlockStateInfo;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockComponentChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.meta.BlockStateModule;
import com.hypixel.hytale.server.core.universe.world.meta.state.ItemContainerState;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

// @Nullable
// public static Ref<ChunkStore> getBlockEntity(@Nonnull World world, int x, int y, int z) {
//     ChunkStore chunkStore = world.getChunkStore();
//     Ref<ChunkStore> chunkRef = chunkStore.getChunkReference(ChunkUtil.indexChunkFromBlock(x, z));
//     if (chunkRef == null) {
//         return null;
//     } else {
//         BlockComponentChunk blockComponentChunk = chunkStore
//             .getStore()
//             .getComponent(chunkRef, BlockComponentChunk.getComponentType());
//         if (blockComponentChunk == null) {
//             return null;
//         } else {
//             int blockIndex = ChunkUtil.indexBlockInColumn(x, y, z);
//             Ref<ChunkStore> blockRef = blockComponentChunk.getEntityReference(blockIndex);
//             return blockRef != null && blockRef.isValid() ? blockRef : null;
//         }
//     }
// }

// @Nullable
// public <T extends Component<ChunkStore>> T getComponent(
//     ComponentType<ChunkStore, T> componentType,
//     World world,
//     int x,
//     int y,
//     int z
// ) {
//     Store<ChunkStore> chunkStore = world.getChunkStore().getStore();
//     Ref<ChunkStore> chunkRef = world.getChunkStore().getChunkReference(ChunkUtil.indexChunkFromBlock(x, z));
//     BlockComponentChunk blockComponentChunk = chunkStore.getComponent(chunkRef, BlockComponentChunk.getComponentType());
//     if (blockComponentChunk == null) {
//         return null;
//     } else {
//         int blockIndex = ChunkUtil.indexBlockInColumn(x, y, z);
//         Ref<ChunkStore> blockRef = blockComponentChunk.getEntityReference(blockIndex);
//         return blockRef != null && blockRef.isValid() ? chunkStore.getComponent(blockRef, componentType) : null;
//     }
// }

// @Nullable
// public Ref<ChunkStore> getChunkSectionReference(int x, int y, int z) {
//     Ref<ChunkStore> ref = this.getChunkReference(ChunkUtil.indexChunk(x, z));
//     if (ref == null) {
//         return null;
//     } else {
//         ChunkColumn chunkColumnComponent = this.store.getComponent(ref, ChunkColumn.getComponentType());
//         return chunkColumnComponent == null ? null : chunkColumnComponent.getSection(y);
//     }
// }

// @Nullable
// public Ref<ChunkStore> getChunkSectionReference(
//     @Nonnull ComponentAccessor<ChunkStore> commandBuffer,
//     int x,
//     int y,
//     int z
// ) {
//     Ref<ChunkStore> ref = this.getChunkReference(ChunkUtil.indexChunk(x, z));
//     if (ref == null) {
//         return null;
//     } else {
//         ChunkColumn chunkColumnComponent = commandBuffer.getComponent(ref, ChunkColumn.getComponentType());
//         return chunkColumnComponent == null ? null : chunkColumnComponent.getSection(y);
//     }
// }

// Utils for blocks. Slowly figuring out what this should look like
// NOTE - its current state is broken
public class BlockUtils {

    private static final HytaleLogger.Api console = HytaleLogger.forEnclosingClass().atInfo();

    // ==================================================
    // Component types (trust me bro, i swear they're not null)
    // ==================================================

    @Nonnull
    @SuppressWarnings("null")
    private static final ComponentType<ChunkStore, WorldChunk> WORLD_CHUNK_COMPONENT = WorldChunk.getComponentType();

    // This is a constant i need for checking if something its touching is a
    // container
    @SuppressWarnings("removal")
    public static final ComponentType<ChunkStore, ItemContainerState> ITEM_CONTAINER_TYPE =
        BlockStateModule.get().getComponentType(ItemContainerState.class);

    // ==================================================
    // Grouped functions together
    // ==================================================

    public abstract static class Entity {

        public static Ref<ChunkStore> getRef(@Nonnull BlockComponentChunk chunk, int localX, int localY, int localZ) {
            return chunk.getEntityReference(ChunkUtil.indexBlockInColumn(localX, localY, localZ));
        }

        /**
         * Gets the block entity at the given coords if one exists.
         *
         * NOTE: not all blocks are block entities. Thus, there can exist a block there without it
         * being a block entity.
         *
         * This does not for example check if there's a grass block. if there is, it will still return
         * null as grass is not a block entity.
         *
         * However, if there's a chest etc it WILL return a ref
         */
        public static Ref<ChunkStore> getBlockEntityAt(CommandBuffer<ChunkStore> commandBuffer, int x, int y, int z) {
            var chunkRef = commandBuffer.getExternalData().getChunkReference(ChunkUtil.indexChunkFromBlock(x, z));
            if (chunkRef == null) {
                return null;
            }

            var blockComponentChunk = chunkRef
                .getStore()
                .getComponent(chunkRef, WORLD_CHUNK_COMPONENT)
                .getBlockComponentChunk();
            if (blockComponentChunk == null) {
                return null;
            }

            return blockComponentChunk.getEntityReference(ChunkUtil.indexBlockInColumn(x, y, z));
        }

        /**
         * Gets the block entity at the given coords if one exists.
         *
         * NOTE: not all blocks are block entities. Thus, there can exist a block there without it
         * being a block entity.
         *
         * This does not for example check if there's a grass block. if there is, it will still return
         * null as grass is not a block entity.
         *
         * However, if there's a chest etc it WILL return a ref
         */
        public static Ref<ChunkStore> getBlockEntityAt(
            CommandBuffer<ChunkStore> commandBuffer,
            @Nonnull Vector3i coords
        ) {
            var ref = commandBuffer
                .getExternalData()
                .getChunkReference(ChunkUtil.indexChunkFromBlock(coords.x, coords.z));
            if (ref == null) {
                return null;
            }

            var blockComponentChunk = commandBuffer.getComponent(ref, WORLD_CHUNK_COMPONENT).getBlockComponentChunk();
            if (blockComponentChunk == null) {
                return null;
            }

            return blockComponentChunk.getEntityReference(ChunkUtil.indexBlockInColumn(coords.x, coords.y, coords.z));
        }
    }

    public abstract static class Info {

        @Nonnull
        @SuppressWarnings("null")
        private static final ComponentType<ChunkStore, BlockStateInfo> BLOCK_STATE_INFO_COMPONENT =
            BlockModule.BlockStateInfo.getComponentType();

        @Nullable
        public static BlockModule.BlockStateInfo get(
            @Nonnull CommandBuffer<ChunkStore> commandBuffer,
            @Nonnull Ref<ChunkStore> ref
        ) {
            // We want the "info" component from the block
            // -> this is how we find out the coords
            var info = (BlockModule.BlockStateInfo) commandBuffer.getComponent(ref, BLOCK_STATE_INFO_COMPONENT);
            if (info == null) {
                return null;
            }

            return info;
        }

        public static BlockModule.BlockStateInfo get(
            @Nonnull CommandBuffer<ChunkStore> commandBuffer,
            @Nonnull BlockComponentChunk chunk,
            int localX,
            int localY,
            int localZ
        ) {
            var ref = Entity.getRef(chunk, localX, localY, localZ);
            if (ref == null) {
                return null;
            }
            return Info.get(commandBuffer, ref);
        }
    }

    public abstract static class Coords {

        // Get the local coords of the block in its chunk
        @Nonnull
        public static Vector3i getLocalCoords(@Nonnull BlockModule.BlockStateInfo info) {
            var indexInChunk = info.getIndex();
            int x = ChunkUtil.xFromBlockInColumn(indexInChunk);
            int y = ChunkUtil.yFromBlockInColumn(indexInChunk);
            int z = ChunkUtil.zFromBlockInColumn(indexInChunk);

            return new Vector3i(x, y, z);
        }

        // @Nullable
        // public static Vector3i getGlobalCoords(
        // @Nonnull CommandBuffer<ChunkStore> commandBuffer,
        // @Nonnull BlockModule.BlockStateInfo info) {
        // var chunk = getWorldChunk(commandBuffer, info);
        // if (chunk == null) {
        // return null;
        // }

        // var localCoords = getLocalCoords(info);
        // return toGlobalCoords(chunk, localCoords);
        // }

        @Nonnull
        public static Vector3i getGlobalCoords(@Nonnull WorldChunk chunk, @Nonnull BlockModule.BlockStateInfo info) {
            var localCoords = getLocalCoords(info);
            return toGlobalCoords(chunk, localCoords);
        }

        @Nonnull
        public static Vector3i toGlobalCoords(@Nonnull WorldChunk chunk, @Nonnull Vector3i localCoords) {
            return toGlobalCoords(chunk, localCoords.x, localCoords.y, localCoords.z);
        }

        @Nonnull
        public static Vector3i toGlobalCoords(@Nonnull WorldChunk chunk, int localX, int localY, int localZ) {
            int globalX = localX + (chunk.getX() * 32);
            int globalZ = localZ + (chunk.getZ() * 32);

            return new Vector3i(globalX, localY, globalZ);
        }

        @Nonnull
        public static Vector3i getLocalCoords(@Nonnull Ref<ChunkStore> ref) {
            var index = ref.getIndex();
            return new Vector3i(index & 31, (index >> 10) & 31, (index >> 5) & 31);
        }
    }

    public abstract static class Chunk {

        // get the chunk for a given block
        @Nullable
        public static WorldChunk getWorldChunk(
            @Nonnull CommandBuffer<ChunkStore> commandBuffer,
            @Nonnull Ref<ChunkStore> ref
        ) {
            var info = Info.get(commandBuffer, ref);
            if (info == null) {
                return null;
            }

            return commandBuffer.getComponent(info.getChunkRef(), WORLD_CHUNK_COMPONENT);
        }

        // get the chunk for a given block
        @Nullable
        public static WorldChunk getWorldChunk(
            @Nonnull CommandBuffer<ChunkStore> commandBuffer,
            @Nonnull BlockModule.BlockStateInfo info
        ) {
            return commandBuffer.getComponent(info.getChunkRef(), WORLD_CHUNK_COMPONENT);
        }
    }

    public abstract static class BlockId {

        @Nullable
        public static Integer getBlockIdAt(CommandBuffer<ChunkStore> commandBuffer, int x, int y, int z) {
            var ref = commandBuffer.getExternalData().getChunkReference(ChunkUtil.indexChunkFromBlock(x, z));
            if (ref == null) {
                return null;
            }

            var worldChunk = ref.getStore().getComponent(ref, WORLD_CHUNK_COMPONENT);
            if (worldChunk == null) {
                return null;
            }

            return worldChunk.getBlock(x, y, z);
        }

        /**
         * Get the integer ID for a block by its string ID
         *
         * @param blockId
         * @return
         */
        public static int getBlockId(@Nonnull String blockId) {
            return BlockType.getAssetMap().getIndex(blockId);
        }
    }

    public abstract static class TickProcedure {

        public static boolean setTicking(
            @Nonnull CommandBuffer<ChunkStore> commandBuffer,
            @Nonnull Ref<ChunkStore> ref
        ) {
            return setTicking(commandBuffer, ref, true);
        }

        public static boolean setTicking(
            @Nonnull CommandBuffer<ChunkStore> commandBuffer,
            @Nonnull Ref<ChunkStore> ref,
            boolean ticking
        ) {
            var info = Info.get(commandBuffer, ref);
            if (info == null) {
                console.log("Info was null");
                return false;
            }

            return setTicking(commandBuffer, info, ticking);
        }

        public static boolean setTicking(
            @Nonnull CommandBuffer<ChunkStore> commandBuffer,
            @Nonnull BlockModule.BlockStateInfo info
        ) {
            return setTicking(commandBuffer, info, true);
        }

        public static boolean setTicking(
            @Nonnull CommandBuffer<ChunkStore> commandBuffer,
            @Nonnull BlockModule.BlockStateInfo info,
            boolean ticking
        ) {
            var worldChunk = Chunk.getWorldChunk(commandBuffer, info);
            if (worldChunk == null) {
                console.log("World chunk was null");
                return false;
            }

            var coords = Coords.getLocalCoords(info);
            return setTicking(worldChunk, coords, ticking);
        }

        public static boolean setTicking(@Nonnull WorldChunk worldChunk, @Nonnull Vector3i coords) {
            return setTicking(worldChunk, coords, true);
        }

        public static boolean setTicking(@Nonnull WorldChunk worldChunk, @Nonnull Vector3i coords, boolean ticking) {
            return worldChunk.setTicking(coords.x, coords.y, coords.z, ticking);
        }

        public static boolean setTicking(
            @Nonnull BlockChunk chunk,
            @Nonnull BlockModule.BlockStateInfo info,
            boolean ticking
        ) {
            var coords = Coords.getLocalCoords(info);
            return chunk.setTicking(coords.x, coords.y, coords.z, ticking);
        }

        public static boolean setTicking(@Nonnull BlockChunk chunk, @Nonnull Vector3i coords) {
            return chunk.setTicking(coords.x, coords.y, coords.z, true);
        }

        public static boolean setTicking(@Nonnull BlockChunk chunk, @Nonnull Vector3i coords, boolean ticking) {
            return chunk.setTicking(coords.x, coords.y, coords.z, ticking);
        }
    }

    public abstract static class BlockComponent {

        public static <T extends Component<ChunkStore>> T getComponent(
            @Nonnull Supplier<ComponentType<ChunkStore, T>> getComponentType,
            @Nonnull CommandBuffer<ChunkStore> commandBuffer,
            @Nonnull BlockComponentChunk chunk,
            int localX,
            int localY,
            int localZ
        ) {
            var ref = Entity.getRef(chunk, localX, localY, localZ);
            if (ref == null) {
                return null;
            }
            var componentType = getComponentType.get();
            if (componentType == null) {
                return null;
            }

            return commandBuffer.getComponent(ref, componentType);
        }

        public static <T extends Component<ChunkStore>> T getComponent(
            @Nonnull Supplier<ComponentType<ChunkStore, T>> getComponentType,
            @Nonnull CommandBuffer<ChunkStore> commandBuffer,
            @Nonnull Ref<ChunkStore> ref
        ) {
            var componentType = getComponentType.get();
            if (componentType == null) {
                return null;
            }
            return commandBuffer.getComponent(ref, componentType);
        }

        public static <T extends Component<ChunkStore>> T getComponent(
            @Nonnull ComponentType<ChunkStore, T> componentType,
            @Nonnull CommandBuffer<ChunkStore> commandBuffer,
            @Nonnull Ref<ChunkStore> ref
        ) {
            return commandBuffer.getComponent(ref, componentType);
        }

        @Nullable
        public static <T extends Component<ChunkStore>> T getComponent(
            @Nonnull ComponentType<ChunkStore, T> componentType,
            @Nonnull CommandBuffer<ChunkStore> commandBuffer,
            @Nonnull BlockComponentChunk chunk,
            int localX,
            int localY,
            int localZ
        ) {
            var ref = Entity.getRef(chunk, localX, localY, localZ);
            if (ref == null) {
                return null;
            }
            return commandBuffer.getComponent(ref, componentType);
        }

        public static <T extends Component<ChunkStore>> boolean hasComponent(
            @Nonnull Supplier<ComponentType<ChunkStore, T>> getComponentType,
            @Nonnull CommandBuffer<ChunkStore> commandBuffer,
            @Nonnull Ref<ChunkStore> ref
        ) {
            var componentType = getComponentType.get();
            if (componentType == null) {
                return false;
            }

            return hasComponent(componentType, commandBuffer, ref);
        }

        public static <T extends Component<ChunkStore>> boolean hasComponent(
            @Nonnull ComponentType<ChunkStore, T> componentType,
            @Nonnull CommandBuffer<ChunkStore> commandBuffer,
            @Nonnull Ref<ChunkStore> ref
        ) {
            return (T) commandBuffer.getComponent(ref, componentType) != null;
        }
    }

    public abstract static class Type {

        /**
         * Gets the "BlockType" for a block with the given Id.
         *
         * Note that the required `blockId` is the ID of the block that YOU SET in your
         * `resources/Server/Item/Items/RileysBlock.json` in the `Id` at the base of
         * the json object
         *
         * @param blockId The stringy ID you chose for your BLOCK asset in the
         *                `resources/Server/Item/Items/<>.json` file
         */
        public static BlockType getBlockType(@Nonnull String blockId) {
            return BlockType.getAssetMap().getAsset(blockId);
        }
    }
}
