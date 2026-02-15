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
import com.hypixel.hytale.server.core.universe.world.World;
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
    @Nonnull
    @SuppressWarnings({ "null", "removal" })
    public static final ComponentType<ChunkStore, ItemContainerState> ITEM_CONTAINER_TYPE =
        BlockStateModule.get().getComponentType(ItemContainerState.class);

    @Nonnull
    @SuppressWarnings("null")
    public static final ComponentType<ChunkStore, BlockComponentChunk> BLOCK_COMPONENT_CHUNK =
        BlockComponentChunk.getComponentType();

    // ==================================================
    // Grouped functions together
    // ==================================================

    public abstract static class Entity {

        // #region getRef
        // Function to get a reference to a block entity at given coordinates

        // ====================================================================
        // ====================================================================
        // GET REF: from seperate int coords
        // ====================================================================
        // ====================================================================

        /**
         * STEP: get chunk store
         * REMAINING STEPS: 3
         *
         * Gets the block entity at the given coords if one exists.
         *
         * NOTE: not all blocks are block entities. Thus, there can exist a block there without it
         * being a block entity.
         *
         * This does not for example check if there's a grass block. if there is, it will still return
         * null as grass is not a block entity.
         *
         * However, if there's a chest etc it WILL return a ref
         *
         * ok, so, turns out i wrote the same thing they've got in BlockModule by accident, so,
         * yup, good news, this works (it must, it was logically equivelant to theirs lmao)
         *
         * NEXT STEP: get chunk ref
         */
        @Nullable
        public static Ref<ChunkStore> getRef(@Nonnull World world, int x, int y, int z) {
            return getRef(world.getChunkStore(), x, y, z);
        }

        /**
         * STEP: get chunk store
         * REMAINING STEPS: 3
         *
         * Gets a ref to the block at the given coords within the world that the
         * provided command buffer resides in
         *
         * NEXT STEP: get chunk ref
         */
        @Nullable
        public static Ref<ChunkStore> getRef(@Nonnull CommandBuffer<ChunkStore> commandBuffer, int x, int y, int z) {
            return getRef(commandBuffer.getExternalData(), x, y, z);
        }

        /**
         * STEP: get chunk ref
         * REMAINING STEPS: 2
         *
         * Gets a ref to the block at the given coords within the world that the
         * provided ChunkStore is for
         *
         * NEXT STEP: get chunk component
         */
        @Nullable
        public static Ref<ChunkStore> getRef(@Nonnull ChunkStore chunkStore, int x, int y, int z) {
            var chunkRef = chunkStore.getChunkReference(ChunkUtil.indexChunkFromBlock(x, z));
            if (chunkRef == null) {
                return null;
            }

            return getRef(chunkRef, x, y, z);
        }

        /**
         * STEP: get chunk component
         * REMAINING STEPS: 1
         *
         * Get a ref to the a block at the coordinates WITHIN the provided chunk
         */
        @Nullable
        public static Ref<ChunkStore> getRef(@Nonnull Ref<ChunkStore> chunkRef, int x, int y, int z) {
            var blockComponentChunk = chunkRef.getStore().getComponent(chunkRef, BLOCK_COMPONENT_CHUNK);
            if (blockComponentChunk == null) {
                return null;
            }

            return getRef(blockComponentChunk, x, y, z);
        }

        /**
         * FINAL STEP
         *
         * Get a ref to the a block at the coordinates WITHIN the provided chunk
         * - any method to get a ref to a block component (that i'm aware of - i
         *   haven't looked that deeply tbh) comes through BlockComponentChunk
         * - thus, the goal of all prior methods is to get the relevant BlockComponentChunk
         *   for the block you're after at the coords you've got
         * - thus, if you DON'T have one, its fine, try the other methods, they'll get you here
         */
        @Nullable
        public static Ref<ChunkStore> getRef(@Nonnull BlockComponentChunk blockComponentChunk, int x, int y, int z) {
            Ref<ChunkStore> ref = blockComponentChunk.getEntityReference(ChunkUtil.indexBlockInColumn(x, y, z));

            return (ref == null || !ref.isValid()) ? null : ref;
        }

        // ====================================================================
        // ====================================================================
        // GET REF: from seperate vector coords
        // ====================================================================
        // ====================================================================

        /**
         * STEP: get chunk store
         * REMAINING STEPS: 3
         *
         * Gets the block entity at the given coords if one exists.
         *
         * NOTE: not all blocks are block entities. Thus, there can exist a block there without it
         * being a block entity.
         *
         * This does not for example check if there's a grass block. if there is, it will still return
         * null as grass is not a block entity.
         *
         * However, if there's a chest etc it WILL return a ref
         *
         * ok, so, turns out i wrote the same thing they've got in BlockModule by accident, so,
         * yup, good news, this works (it must, it was logically equivelant to theirs lmao)
         *
         * NEXT STEP: get chunk ref
         */
        @Nullable
        public static Ref<ChunkStore> getRef(@Nonnull World world, @Nonnull Vector3i coords) {
            return getRef(world, coords.x, coords.y, coords.z);
        }

        /**
         * STEP: get chunk store
         * REMAINING STEPS: 3
         *
         * Gets a ref to the block at the given coords within the world that the
         * provided command buffer resides in
         *
         * NEXT STEP: get chunk ref
         */
        @Nullable
        public static Ref<ChunkStore> getRef(
            @Nonnull CommandBuffer<ChunkStore> commandBuffer,
            @Nonnull Vector3i coords
        ) {
            return getRef(commandBuffer, coords.x, coords.y, coords.z);
        }

        /**
         * STEP: get chunk ref
         * REMAINING STEPS: 2
         *
         * Gets a ref to the block at the given coords within the world that the
         * provided ChunkStore is for
         *
         * NEXT STEP: get chunk component
         */
        @Nullable
        public static Ref<ChunkStore> getRef(@Nonnull ChunkStore chunkStore, @Nonnull Vector3i coords) {
            return getRef(chunkStore, coords.x, coords.y, coords.z);
        }

        /**
         * STEP: get chunk component
         * REMAINING STEPS: 1
         *
         * Get a ref to the a block at the coordinates WITHIN the provided chunk
         */
        @Nullable
        public static Ref<ChunkStore> getRef(@Nonnull Ref<ChunkStore> chunkRef, @Nonnull Vector3i coords) {
            return getRef(chunkRef, coords.x, coords.y, coords.z);
        }

        /**
         * FINAL STEP
         *
         * Get a ref to the a block at the coordinates WITHIN the provided chunk
         * - any method to get a ref to a block component (that i'm aware of - i
         *   haven't looked that deeply tbh) comes through BlockComponentChunk
         * - thus, the goal of all prior methods is to get the relevant BlockComponentChunk
         *   for the block you're after at the coords you've got
         * - thus, if you DON'T have one, its fine, try the other methods, they'll get you here
         */
        @Nullable
        public static Ref<ChunkStore> getRef(
            @Nonnull BlockComponentChunk blockComponentChunk,
            @Nonnull Vector3i coords
        ) {
            return getRef(blockComponentChunk, coords.x, coords.y, coords.z);
        }
        // #endregion getRef
    }

    public abstract static class Info {

        @Nonnull
        @SuppressWarnings("null")
        private static final ComponentType<ChunkStore, BlockStateInfo> BLOCK_STATE_INFO_COMPONENT =
            BlockModule.BlockStateInfo.getComponentType();

        // #region get
        // Function to get `BlockStateInfo` component for a block. You can provide
        // - a ref to the block
        // - the local coordinates of the block and the chunk it's in
        // - the global coordinates of the block #TODO

        @Nullable
        public static BlockModule.BlockStateInfo get(@Nonnull Ref<ChunkStore> ref) {
            // We want the "info" component from the block
            // -> this is how we find out the coords
            var info = (BlockModule.BlockStateInfo) ref.getStore().getComponent(ref, BLOCK_STATE_INFO_COMPONENT);
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
            return Info.get(ref);
        }

        // #endregion get
    }

    public abstract static class Coords {

        // #region getLocalCoords
        // Function: given some aspect of a block and i'll find a way to get the coords of it.
        // - BlockStateInfo => (coords inbuilt, but, i make it easier to access)
        // - Ref to the block itself

        // Get the local coords of the block in its chunk
        @Nonnull
        public static Vector3i getLocalCoords(@Nonnull BlockModule.BlockStateInfo info) {
            var indexInChunk = info.getIndex();
            int x = ChunkUtil.xFromBlockInColumn(indexInChunk);
            int y = ChunkUtil.yFromBlockInColumn(indexInChunk);
            int z = ChunkUtil.zFromBlockInColumn(indexInChunk);

            return new Vector3i(x, y, z);
        }

        @Nonnull
        public static Vector3i getLocalCoords(@Nonnull Ref<ChunkStore> ref) {
            var index = ref.getIndex();
            return new Vector3i(index & 31, (index >> 10) & 31, (index >> 5) & 31);
        }

        // #endregion getLocalCoords

        // #region getGlobalCoords
        // Function: From various information I can get you the global coordinates of a block

        @Nullable
        public static Vector3i getGlobalCoords(@Nonnull Ref<ChunkStore> ref) {
            var info = Info.get(ref);
            if (info == null) {
                return null;
            }

            return Coords.getGlobalCoords(info);
        }

        @Nullable
        public static Vector3i getGlobalCoords(@Nonnull BlockModule.BlockStateInfo info) {
            var chunk = Chunk.getWorldChunk(info);
            if (chunk == null) {
                return null;
            }

            return Coords.getGlobalCoords(chunk, info);
        }

        @Nonnull
        public static Vector3i getGlobalCoords(@Nonnull WorldChunk chunk, @Nonnull BlockModule.BlockStateInfo info) {
            var localCoords = Coords.getLocalCoords(info);

            return Coords.getGlobalCoords(chunk, localCoords);
        }

        @Nonnull
        public static Vector3i getGlobalCoords(@Nonnull WorldChunk chunk, @Nonnull Vector3i localCoords) {
            return Coords.getGlobalCoords(chunk, localCoords.x, localCoords.y, localCoords.z);
        }

        @Nonnull
        public static Vector3i getGlobalCoords(@Nonnull WorldChunk chunk, int localX, int localY, int localZ) {
            int globalX = localX + (chunk.getX() * 32);
            int globalZ = localZ + (chunk.getZ() * 32);

            return new Vector3i(globalX, localY, globalZ);
        }

        // #endregion getGlobalCoords
    }

    public abstract static class Chunk {

        // get the chunk for a given block
        @Nullable
        public static WorldChunk getWorldChunk(@Nonnull Ref<ChunkStore> ref) {
            var info = Info.get(ref);
            if (info == null) {
                return null;
            }

            return Chunk.getWorldChunk(info);
        }

        // get the chunk for a given block
        @Nullable
        public static WorldChunk getWorldChunk(@Nonnull BlockModule.BlockStateInfo info) {
            var chunkRef = info.getChunkRef();

            return chunkRef.getStore().getComponent(chunkRef, WORLD_CHUNK_COMPONENT);
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

        public static boolean setTicking(@Nonnull Ref<ChunkStore> ref) {
            return setTicking(ref, true);
        }

        public static boolean setTicking(@Nonnull Ref<ChunkStore> ref, boolean ticking) {
            var info = Info.get(ref);
            if (info == null) {
                console.log("Info was null");
                return false;
            }

            return setTicking(info, ticking);
        }

        public static boolean setTicking(@Nonnull BlockModule.BlockStateInfo info) {
            return setTicking(info, true);
        }

        public static boolean setTicking(@Nonnull BlockModule.BlockStateInfo info, boolean ticking) {
            var worldChunk = Chunk.getWorldChunk(info);
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

        @Nullable
        public static BlockComponentChunk getBlockComponentChunk(@Nonnull Ref<ChunkStore> chunkRef) {
            return chunkRef.getStore().getComponent(chunkRef, BLOCK_COMPONENT_CHUNK);
        }

        public static <T extends Component<ChunkStore>> T getComponent(
            @Nonnull Supplier<ComponentType<ChunkStore, T>> getComponentType,
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

            return ref.getStore().getComponent(ref, componentType);
        }

        public static <T extends Component<ChunkStore>> T getComponent(
            @Nonnull Supplier<ComponentType<ChunkStore, T>> getComponentType,
            @Nonnull Ref<ChunkStore> ref
        ) {
            var componentType = getComponentType.get();
            if (componentType == null) {
                return null;
            }
            return ref.getStore().getComponent(ref, componentType);
        }

        public static <T extends Component<ChunkStore>> T getComponent(
            @Nonnull ComponentType<ChunkStore, T> componentType,
            @Nonnull Ref<ChunkStore> ref
        ) {
            return ref.getStore().getComponent(ref, componentType);
        }

        @Nullable
        public static <T extends Component<ChunkStore>> T getComponent(
            @Nonnull ComponentType<ChunkStore, T> componentType,
            @Nonnull BlockComponentChunk chunk,
            int localX,
            int localY,
            int localZ
        ) {
            var ref = Entity.getRef(chunk, localX, localY, localZ);
            if (ref == null) {
                return null;
            }
            return ref.getStore().getComponent(ref, componentType);
        }

        public static <T extends Component<ChunkStore>> boolean hasComponent(
            @Nonnull Supplier<ComponentType<ChunkStore, T>> getComponentType,
            @Nonnull Ref<ChunkStore> ref
        ) {
            var componentType = getComponentType.get();
            if (componentType == null) {
                return false;
            }

            return hasComponent(componentType, ref);
        }

        public static <T extends Component<ChunkStore>> boolean hasComponent(
            @Nonnull ComponentType<ChunkStore, T> componentType,
            @Nonnull Ref<ChunkStore> ref
        ) {
            return (T) ref.getStore().getComponent(ref, componentType) != null;
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
