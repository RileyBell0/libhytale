package dev.twunk.utils.world;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.modules.block.BlockModule.BlockStateInfo;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.WorldProvider;
import com.hypixel.hytale.server.core.universe.world.accessor.BlockAccessor;
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
public final class BlockUtils {

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

    // DONE
    public static final class Entity {

        // #region getRef
        // ====================================================================
        // /\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\
        // ====================================================================
        //
        // Purpose:   Getting a Ref (`Ref<ChunkStore>`) for a given block
        // Requires:  A method of accessing blocks in a world, AND coordinates of the block (or its index and the chunks index etc)

        // #region WorldProvider
        // ====================================================================
        // WorldProvider  =>  ChunkStore
        // + GlobalCoords
        // ====================================================================

        @Nullable
        public static final Ref<ChunkStore> getRef(
            @Nonnull final WorldProvider worldProvider,
            @Nonnull final Vector3i coords
        ) {
            final var chunkIndex = ChunkUtil.indexChunkFromBlock(coords.x, coords.z);
            final var blockIndex = ChunkUtil.indexBlock(coords.x, coords.y, coords.z);

            // => (ChunkStore, chunkIndex, blockIndex)
            return Entity.getRef(worldProvider.getWorld().getChunkStore(), chunkIndex, blockIndex);
        }

        @Nullable
        public static final Ref<ChunkStore> getRef(
            @Nonnull final WorldProvider worldProvider,
            final int x,
            final int y,
            final int z
        ) {
            final var chunkIndex = ChunkUtil.indexChunkFromBlock(x, z);
            final var blockIndex = ChunkUtil.indexBlock(x, y, z);

            // => (ChunkStore, chunkIndex, blockIndex)
            return getRef(worldProvider.getWorld().getChunkStore(), chunkIndex, blockIndex);
        }

        @Nullable
        public static final Ref<ChunkStore> getRef(
            @Nonnull final WorldProvider worldProvider,
            final long chunkIndex,
            final int blockIndex
        ) {
            // => (ChunkStore, chunkIndex, blockIndex)
            return getRef(worldProvider.getWorld().getChunkStore(), chunkIndex, blockIndex);
        }

        // #endregion WorldProvider
        // #region World
        // ====================================================================
        // World  =>  ChunkStore
        // + Global coords
        // ====================================================================

        @Nullable
        public static final Ref<ChunkStore> getRef(@Nonnull final World world, @Nonnull final Vector3i coords) {
            final var chunkIndex = ChunkUtil.indexChunkFromBlock(coords.x, coords.z);
            final var blockIndex = ChunkUtil.indexBlock(coords.x, coords.y, coords.z);

            // => (ChunkStore, chunkIndex, blockIndex)
            return getRef(world.getChunkStore(), chunkIndex, blockIndex);
        }

        @Nullable
        public static final Ref<ChunkStore> getRef(@Nonnull final World world, final int x, final int y, final int z) {
            final var chunkIndex = ChunkUtil.indexChunkFromBlock(x, z);
            final var blockIndex = ChunkUtil.indexBlock(x, y, z);

            // => (ChunkStore, chunkIndex, blockIndex)
            return getRef(world.getChunkStore(), chunkIndex, blockIndex);
        }

        @Nullable
        public static final Ref<ChunkStore> getRef(
            @Nonnull final World world,
            final long chunkIndex,
            final int blockIndex
        ) {
            // => (ChunkStore, chunkIndex, blockIndex)
            return getRef(world.getChunkStore(), chunkIndex, blockIndex);
        }

        // #endregion World
        // #region CommandBuffer
        // ====================================================================
        // CommandBuffer  =>  ChunkStore
        // + Global coords
        // ====================================================================

        @Nullable
        public static final Ref<ChunkStore> getRef(
            @Nonnull final CommandBuffer<ChunkStore> commandBuffer,
            @Nonnull final Vector3i coords
        ) {
            final var chunkIndex = ChunkUtil.indexChunkFromBlock(coords.x, coords.z);
            final var blockIndex = ChunkUtil.indexBlock(coords.x, coords.y, coords.z);

            // => (ChunkStore, chunkIndex, blockIndex)
            return getRef(commandBuffer.getExternalData(), chunkIndex, blockIndex);
        }

        @Nullable
        public static final Ref<ChunkStore> getRef(
            @Nonnull final CommandBuffer<ChunkStore> commandBuffer,
            final int x,
            final int y,
            final int z
        ) {
            final var chunkIndex = ChunkUtil.indexChunkFromBlock(x, z);
            final var blockIndex = ChunkUtil.indexBlock(x, y, z);

            // => (ChunkStore, chunkIndex, blockIndex)
            return getRef(commandBuffer.getExternalData(), chunkIndex, blockIndex);
        }

        @Nullable
        public static final Ref<ChunkStore> getRef(
            @Nonnull final CommandBuffer<ChunkStore> commandBuffer,
            final long chunkIndex,
            final int blockIndex
        ) {
            // => (ChunkStore, chunkIndex, blockIndex)
            return getRef(commandBuffer.getExternalData(), chunkIndex, blockIndex);
        }

        // #endregion CommandBuffer
        // #region Store<ChunkStore>
        // ====================================================================
        // Store<ChunkStore>  =>  ChunkStore
        // + Global coords
        // ====================================================================

        @Nullable
        public static final Ref<ChunkStore> getRef(
            @Nonnull final Store<ChunkStore> chunkStore,
            @Nonnull final Vector3i coords
        ) {
            final var chunkIndex = ChunkUtil.indexChunkFromBlock(coords.x, coords.z);
            final var blockIndex = ChunkUtil.indexBlock(coords.x, coords.y, coords.z);

            // => (ChunkStore, chunkIndex, blockIndex)
            return getRef(chunkStore.getExternalData(), chunkIndex, blockIndex);
        }

        @Nullable
        public static final Ref<ChunkStore> getRef(
            @Nonnull final Store<ChunkStore> chunkStore,
            final int x,
            final int y,
            final int z
        ) {
            final var chunkIndex = ChunkUtil.indexChunkFromBlock(x, z);
            final var blockIndex = ChunkUtil.indexBlock(x, y, z);

            // => (ChunkStore, chunkIndex, blockIndex)
            return getRef(chunkStore.getExternalData(), chunkIndex, blockIndex);
        }

        @Nullable
        public static final Ref<ChunkStore> getRef(
            @Nonnull final Store<ChunkStore> chunkStore,
            final long chunkIndex,
            final int blockIndex
        ) {
            // => (ChunkStore, chunkIndex, blockIndex)
            return getRef(chunkStore.getExternalData(), chunkIndex, blockIndex);
        }

        // #endregion Store<ChunkStore>
        // #region ChunkStore
        // ====================================================================
        // ChunkStore  =>  ChunkRef
        // + Global coords OR Chunk Coords
        // ====================================================================

        @Nullable
        public static final Ref<ChunkStore> getRef(
            @Nonnull final ChunkStore chunkStore,
            @Nonnull final Vector3i coords
        ) {
            final var chunkRef = Chunk.getChunkRef(chunkStore, ChunkUtil.indexChunkFromBlock(coords.x, coords.z));
            if (chunkRef == null) {
                return null;
            }

            return getRef(chunkRef, ChunkUtil.indexBlock(coords.x, coords.y, coords.z));
        }

        @Nullable
        public static final Ref<ChunkStore> getRef(
            @Nonnull final ChunkStore chunkStore,
            final int x,
            final int y,
            final int z
        ) {
            final var chunkRef = Chunk.getChunkRef(chunkStore, ChunkUtil.indexChunkFromBlock(x, z));
            if (chunkRef == null) {
                return null;
            }

            return getRef(chunkRef, ChunkUtil.indexBlock(x, y, z));
        }

        @Nullable
        public static final Ref<ChunkStore> getRef(
            @Nonnull final ChunkStore chunkStore,
            final long chunkIndex,
            final int blockIndex
        ) {
            final var chunkRef = Chunk.getChunkRef(chunkStore, chunkIndex);
            if (chunkRef == null) {
                return null;
            }

            return getRef(chunkRef, blockIndex);
        }

        // #endregion ChunkStore
        // #region Ref<ChunkStore>
        // ====================================================================
        // Ref<ChunkStore> (ChunkRef)  =>  BlockComponentChunk
        // + Global OR Local coords
        // ====================================================================

        @Nullable
        public static final Ref<ChunkStore> getRef(
            @Nonnull final Ref<ChunkStore> chunkRef,
            @Nonnull final Vector3i coords
        ) {
            final var blockComponentChunk = BlockComponent.getBlockComponentChunk(chunkRef);
            if (blockComponentChunk == null) {
                return null;
            }

            return getRef(blockComponentChunk, ChunkUtil.indexBlockInColumn(coords.x, coords.y, coords.z));
        }

        @Nullable
        public static final Ref<ChunkStore> getRef(
            @Nonnull final Ref<ChunkStore> chunkRef,
            final int x,
            final int y,
            final int z
        ) {
            final var blockComponentChunk = BlockComponent.getBlockComponentChunk(chunkRef);
            if (blockComponentChunk == null) {
                return null;
            }

            return getRef(blockComponentChunk, ChunkUtil.indexBlockInColumn(x, y, z));
        }

        @Nullable
        public static final Ref<ChunkStore> getRef(@Nonnull final Ref<ChunkStore> chunkRef, final int blockIndex) {
            final var blockComponentChunk = BlockComponent.getBlockComponentChunk(chunkRef);
            if (blockComponentChunk == null) {
                return null;
            }

            return getRef(blockComponentChunk, blockIndex);
        }

        // #endregion Ref<ChunkStore>
        // #region BlockComponentChunk
        // ====================================================================
        // BlockComponentChunk!
        // + Global OR Local coords
        // ====================================================================

        @Nullable
        public static final Ref<ChunkStore> getRef(
            @Nonnull final BlockComponentChunk blockComponentChunk,
            @Nonnull final Vector3i coords
        ) {
            return getRef(blockComponentChunk, ChunkUtil.indexBlockInColumn(coords.x, coords.y, coords.z));
        }

        @Nullable
        public static final Ref<ChunkStore> getRef(
            @Nonnull final BlockComponentChunk blockComponentChunk,
            final int x,
            final int y,
            final int z
        ) {
            return getRef(blockComponentChunk, ChunkUtil.indexBlockInColumn(x, y, z));
        }

        @Nullable
        public static final Ref<ChunkStore> getRef(
            @Nonnull final BlockComponentChunk blockComponentChunk,
            final int blockIndex
        ) {
            final var blockRef = blockComponentChunk.getEntityReference(blockIndex);
            if (blockRef == null || !blockRef.isValid()) {
                return null;
            }

            return blockRef;
        }

        // #endregion BlockComponentChunk
    }

    // DONE
    public static final class Info {

        @Nonnull
        @SuppressWarnings("null")
        private static final ComponentType<ChunkStore, BlockStateInfo> BLOCK_STATE_INFO_COMPONENT =
            BlockStateInfo.getComponentType();

        // #region getInfo
        // ====================================================================
        // /\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\
        // ====================================================================
        //
        // Purpose:   Getting the `BlockStateInfo` component for a block specified block
        // Requires:  A ref to the block (or a way to get a ref to the block)

        // #region WorldProvider
        // ====================================================================
        // WorldProvider
        // + Global coords
        // ====================================================================

        @Nullable
        public static final BlockStateInfo getInfo(
            @Nonnull final WorldProvider worldProvider,
            @Nonnull final Vector3i pos
        ) {
            // => BlockRef
            return getInfo(Entity.getRef(worldProvider, pos));
        }

        @Nullable
        public static final BlockStateInfo getInfo(
            @Nonnull final WorldProvider worldProvider,
            final int x,
            final int y,
            final int z
        ) {
            // => BlockRef
            return getInfo(Entity.getRef(worldProvider, x, y, z));
        }

        @Nullable
        public static final BlockStateInfo getInfo(
            @Nonnull final WorldProvider worldProvider,
            final long chunkIndex,
            final int blockIndex
        ) {
            // => BlockRef
            return getInfo(Entity.getRef(worldProvider, chunkIndex, blockIndex));
        }

        // #endregion WorldProvider
        // #region World
        // ====================================================================
        // World
        // + Global coords
        // ====================================================================

        @Nullable
        public static final BlockStateInfo getInfo(@Nonnull final World world, @Nonnull final Vector3i pos) {
            // => BlockRef
            return getInfo(Entity.getRef(world, pos));
        }

        @Nullable
        public static final BlockStateInfo getInfo(@Nonnull final World world, final int x, final int y, final int z) {
            // => BlockRef
            return getInfo(Entity.getRef(world, x, y, z));
        }

        @Nullable
        public static final BlockStateInfo getInfo(
            @Nonnull final World world,
            final long chunkIndex,
            final int blockIndex
        ) {
            // => BlockRef
            return getInfo(Entity.getRef(world, chunkIndex, blockIndex));
        }

        // #endregion World
        // #region CommandBuffer
        // ====================================================================
        // CommandBuffer  =>  ChunkStore
        // + Global coords
        // ====================================================================

        @Nullable
        public static final BlockStateInfo getInfo(
            @Nonnull final CommandBuffer<ChunkStore> commandBuffer,
            @Nonnull final Vector3i pos
        ) {
            // => BlockRef
            return getInfo(Entity.getRef(commandBuffer, pos));
        }

        @Nullable
        public static final BlockStateInfo getInfo(
            @Nonnull final CommandBuffer<ChunkStore> commandBuffer,
            final int x,
            final int y,
            final int z
        ) {
            // => BlockRef
            return getInfo(Entity.getRef(commandBuffer, x, y, z));
        }

        @Nullable
        public static final BlockStateInfo getInfo(
            @Nonnull final CommandBuffer<ChunkStore> commandBuffer,
            final long chunkIndex,
            final int blockIndex
        ) {
            // => BlockRef
            return getInfo(Entity.getRef(commandBuffer, chunkIndex, blockIndex));
        }

        // #endregion CommandBuffer
        // #region Store<ChunkStore>
        // ====================================================================
        // Store<ChunkStore>
        // + Global coords
        // ====================================================================

        @Nullable
        public static final BlockStateInfo getInfo(
            @Nonnull final Store<ChunkStore> chunkStore,
            @Nonnull final Vector3i pos
        ) {
            // => BlockRef
            return getInfo(Entity.getRef(chunkStore, pos));
        }

        @Nullable
        public static final BlockStateInfo getInfo(
            @Nonnull final Store<ChunkStore> chunkStore,
            final int x,
            final int y,
            final int z
        ) {
            // => BlockRef
            return getInfo(Entity.getRef(chunkStore, x, y, z));
        }

        @Nullable
        public static final BlockStateInfo getInfo(
            @Nonnull final Store<ChunkStore> chunkStore,
            final long chunkIndex,
            final int blockIndex
        ) {
            // => BlockRef
            return getInfo(Entity.getRef(chunkStore, chunkIndex, blockIndex));
        }

        // #endregion Store<ChunkStore>
        // #region ChunkStore
        // ====================================================================
        // ChunkStore
        // + Global coords
        // ====================================================================

        @Nullable
        public static final BlockStateInfo getInfo(@Nonnull final ChunkStore chunkStore, @Nonnull final Vector3i pos) {
            // => BlockRef
            return getInfo(Entity.getRef(chunkStore, pos));
        }

        @Nullable
        public static final BlockStateInfo getInfo(
            @Nonnull final ChunkStore chunkStore,
            final int x,
            final int y,
            final int z
        ) {
            // => BlockRef
            return getInfo(Entity.getRef(chunkStore, x, y, z));
        }

        @Nullable
        public static final BlockStateInfo getInfo(
            @Nonnull final ChunkStore chunkStore,
            final long chunkIndex,
            final int blockIndex
        ) {
            // => BlockRef
            return getInfo(Entity.getRef(chunkStore, chunkIndex, blockIndex));
        }

        // #endregion ChunkStore
        // #region Ref<ChunkStore> (ChunkRef)
        // ====================================================================
        // Ref<ChunkStore> (ChunkRef)  =>  BlockRef
        // + Global OR Local coords
        // ====================================================================

        @Nullable
        public static final BlockStateInfo getInfo(
            @Nonnull final Ref<ChunkStore> chunkRef,
            @Nonnull final Vector3i coords
        ) {
            return getInfo(Entity.getRef(chunkRef, coords));
        }

        @Nullable
        public static final BlockStateInfo getInfo(
            @Nonnull final Ref<ChunkStore> chunkRef,
            final int x,
            final int y,
            final int z
        ) {
            return getInfo(Entity.getRef(chunkRef, x, y, z));
        }

        @Nullable
        public static final BlockStateInfo getInfo(@Nonnull final Ref<ChunkStore> chunkRef, final int blockIndex) {
            return getInfo(Entity.getRef(chunkRef, blockIndex));
        }

        // #endregion Ref<ChunkStore>
        // #region BlockComponentChunk
        // ====================================================================
        // BlockComponentChunk  =>  BlockRef
        // + Local OR Global coords
        // ====================================================================

        @Nullable
        public static final BlockStateInfo getInfo(
            @Nonnull final BlockComponentChunk chunk,
            @Nonnull final Vector3i coords
        ) {
            // => BlockRef
            return getInfo(Entity.getRef(chunk, coords));
        }

        @Nullable
        public static final BlockStateInfo getInfo(
            @Nonnull final BlockComponentChunk chunk,
            final int x,
            final int y,
            final int z
        ) {
            // => BlockRef
            return getInfo(Entity.getRef(chunk, x, y, z));
        }

        @Nullable
        public static final BlockStateInfo getInfo(@Nonnull final BlockComponentChunk chunk, final int blockIndex) {
            // => BlockRef
            return getInfo(Entity.getRef(chunk, blockIndex));
        }

        // #endregion BlockComponentChunk
        // #region BlockRef
        // ====================================================================
        // BlockRef!
        // ====================================================================

        // BlockRef
        @Nullable
        public static final BlockStateInfo getInfo(@Nullable final Ref<ChunkStore> blockRef) {
            if (blockRef == null) {
                return null;
            }

            return BlockComponent.getComponent(blockRef, BLOCK_STATE_INFO_COMPONENT);
        }
        // #endregion BlockRef

        // #endregion getInfo
    }

    public static final class Coords {

        // #region getLocalCoords
        // ====================================================================
        // /\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\
        // ====================================================================
        //
        // Purpose:   Getting the local coordinates of the block (within its chunk)
        // Requires:  BlockStateInfo component of the relevant block (or a method of getting this -> see Info.getInfo() and thus -> Entity.getRef)
        // importantly, most methods of getInfo are pointless, as usually these require the coordinates of a block (and if you have that, you don't need this)

        @Nullable
        public static final Vector3i getLocalCoords(@Nonnull final Ref<ChunkStore> blockRef) {
            final var info = Info.getInfo(blockRef);
            if (info == null) {
                return null;
            }

            // => blockIndex
            return getLocalCoords(info.getIndex());
        }

        @Nonnull
        public static final Vector3i getLocalCoords(@Nonnull final BlockStateInfo info) {
            // => blockIndex
            return getLocalCoords(info.getIndex());
        }

        @Nonnull
        public static final Vector3i getLocalCoords(@Nonnull final Vector3i coords) {
            return getLocalCoords(coords.x, coords.y, coords.z);
        }

        // TODO riley come back and test this. ah yes, this is that moment we all dream
        // of, writing a "todo" or "fixme" that really may never be seen again. I mean, before
        // i release this (if i do) i'll do a quick check for TODOs i guess so eh nah
        // this'll be fine, i'll come back.
        @Nonnull
        public static final Vector3i getLocalCoords(final int x, final int y, final int z) {
            // => blockIndex
            // TODO not sure if this works, haven't tested
            var _index = ChunkUtil.indexBlock(x, y, z);
            var _coord = new Vector3i(
                ChunkUtil.xFromBlockInColumn(_index),
                ChunkUtil.yFromBlockInColumn(_index),
                ChunkUtil.zFromBlockInColumn(_index)
            );
            var coord = new Vector3i(x % 32, y % 32, z % 32);
            var index = ChunkUtil.indexBlock(coord.x, coord.y, coord.z);
            if (_index != index || !coord.equals(_coord)) {
                console.log("AAAH FUCK RILEY COME BACK AND FIX THIS");
                console.log("AAAH FUCK RILEY COME BACK AND FIX THIS");
                console.log("AAAH FUCK RILEY COME BACK AND FIX THIS");
                console.log("AAAH FUCK RILEY COME BACK AND FIX THIS");
                console.log("AAAH FUCK RILEY COME BACK AND FIX THIS");
                console.log("AAAH FUCK RILEY COME BACK AND FIX THIS");
            }
            assert _index == index;
            assert coord.equals(coord);
            return coord;
        }

        // ====================================================================
        // index -> this is how we actually get coords of a block throughout their system
        // ====================================================================

        // remember kids: multidimensional arrays are like birds - a lie
        @Nonnull
        public static final Vector3i getLocalCoords(final int blockIndex) {
            final int x = ChunkUtil.xFromBlockInColumn(blockIndex);
            final int y = ChunkUtil.yFromBlockInColumn(blockIndex);
            final int z = ChunkUtil.zFromBlockInColumn(blockIndex);

            return new Vector3i(x, y, z);
        }

        // #endregion getLocalCoords

        // #region getGlobalCoords
        // ====================================================================
        // /\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\
        // ====================================================================
        //
        // Purpose:   Getting the GLOBAL coordinates of the block in its world
        // Requires:  Your block AND the chunk it's in (local coords + chunk coords)

        @Nullable
        public static final Vector3i getGlobalCoords(@Nonnull final Ref<ChunkStore> blockRef) {
            final var info = Info.getInfo(blockRef);
            if (info == null) {
                return null;
            }

            return getGlobalCoords(info);
        }

        @Nullable
        public static final Vector3i getGlobalCoords(@Nonnull final BlockStateInfo info) {
            final var chunk = Chunk.getWorldChunk(info);
            if (chunk == null) {
                return null;
            }

            return getGlobalCoords(chunk, info);
        }

        @Nonnull
        public static final Vector3i getGlobalCoords(
            @Nonnull final BlockAccessor chunk,
            @Nonnull final BlockStateInfo info
        ) {
            final var localCoords = Coords.getLocalCoords(info);
            return toGlobalCoords(chunk, localCoords.x, localCoords.y, localCoords.z);
        }

        @Nonnull
        public static final Vector3i getGlobalCoords(@Nonnull final BlockAccessor chunk, final int blockIndex) {
            final var localCoords = Coords.getLocalCoords(blockIndex);
            return toGlobalCoords(chunk, localCoords.x, localCoords.y, localCoords.z);
        }

        @Nonnull
        public static final Vector3i getGlobalCoords(
            @Nonnull final BlockAccessor chunk,
            @Nonnull final Vector3i localCoords
        ) {
            return toGlobalCoords(chunk, localCoords.x, localCoords.y, localCoords.z);
        }

        // #region blockChunk
        // SPECIAL CASE: BlockChunk
        // For some reason "BlockChunk" isn't a block accessor, but World chunk is (and world chunk USES block chunk, so, ??)
        // so, the code is identical, but hey, fuck it, if it works, it works.

        @Nonnull
        public static final Vector3i getGlobalCoords(
            @Nonnull final BlockChunk chunk,
            @Nonnull final BlockStateInfo info
        ) {
            return getGlobalCoords(chunk, Coords.getLocalCoords(info));
        }

        @Nonnull
        public static final Vector3i getGlobalCoords(
            @Nonnull final BlockChunk chunk,
            @Nonnull final Vector3i localCoords
        ) {
            return toGlobalCoords(chunk, localCoords.x, localCoords.y, localCoords.z);
        }

        @Nonnull
        public static final Vector3i toGlobalCoords(
            @Nonnull final BlockChunk chunk,
            final int localX,
            final int localY,
            final int localZ
        ) {
            final int globalX = localX + (chunk.getX() * 32);
            final int globalZ = localZ + (chunk.getZ() * 32);

            return new Vector3i(globalX, localY, globalZ);
        }

        // #endregion blockChunk
        // #endregion getGlobalCoords

        @Nonnull
        public static final Vector3i toGlobalCoords(
            @Nonnull final BlockAccessor chunk,
            final int localX,
            final int localY,
            final int localZ
        ) {
            final int globalX = localX + (chunk.getX() * 32);
            final int globalZ = localZ + (chunk.getZ() * 32);

            return new Vector3i(globalX, localY, globalZ);
        }
    }

    public static final class Chunk {

        // #region getWorldChunk

        @Nullable
        public static final WorldChunk getWorldChunkFromChunkRef(@Nonnull final Ref<ChunkStore> chunkRef) {
            return BlockComponent.getComponent(chunkRef, WORLD_CHUNK_COMPONENT);
        }

        @Nullable
        public static final WorldChunk getWorldChunkFromBlockRef(@Nonnull final Ref<ChunkStore> blockRef) {
            final var info = Info.getInfo(blockRef);
            if (info == null) {
                return null;
            }

            return BlockComponent.getComponent(info.getChunkRef(), WORLD_CHUNK_COMPONENT);
        }

        // BlockStateInfo is your friend. All block entities have it.
        // - If you want to get the world a block is in
        // - If you want to get the CHUNK a block is in
        // - If you want to get OTHER blocks
        //
        // All of those ^^ are gotten THROUGH BlockStateInfo
        // effectively, BlockStateInfo + Ref to your block => your dream come true

        @Nullable
        public static final WorldChunk getWorldChunk(@Nonnull final Ref<ChunkStore> ref) {
            // Potential 1: The ref you passed me is a CHUNK ref. slay. thats the good shit.
            final var maybeChunk = BlockComponent.getComponent(ref, WORLD_CHUNK_COMPONENT);
            if (maybeChunk != null) {
                return maybeChunk;
            }

            // Potential 2: the ref you passed me is a BLOCK ref. GOOD.
            final var info = Info.getInfo(ref);
            if (info == null) {
                return null;
            }

            return BlockComponent.getComponent(info.getChunkRef(), WORLD_CHUNK_COMPONENT);
        }

        @Nullable
        public static final WorldChunk getWorldChunk(@Nonnull final BlockStateInfo info) {
            return BlockComponent.getComponent(info.getChunkRef(), WORLD_CHUNK_COMPONENT);
        }

        // ====================================================================
        // the below methods all assume
        // - you have a way to access components in a given world
        // - you also have some identifying about the chunk you want
        // ====================================================================

        /**
         * does NOT get the chunk that the info is in, it USES the info to get
         * the world.
         *
         * Then uses the world to get the WorldChunk at the coords provided
         */
        @Nullable
        public static final WorldChunk getWorldChunk(
            @Nonnull final BlockStateInfo info,
            final int chunkX,
            final int chunkZ
        ) {
            throw new RuntimeException("ERROR: Riley you need to TEST THIS FUNCTION. Remove me if intentional.");
            // return info
            //     .getChunkRef()
            //     .getStore()
            //     .getExternalData()
            //     .getChunkComponent(ChunkUtil.indexChunk(chunkX, chunkZ), WORLD_CHUNK_COMPONENT);
            // ALT VERSION: use the ref as above in the single ref param version of this function to figure out if we're a block ref or a chunk ref and get the right store based on that
        }

        /**
         * does NOT get the chunk that the info is in, it USES the info to get
         * the world.
         *
         * Then uses the world to get the WorldChunk at the coords provided
         */
        @Nullable
        public static final WorldChunk getWorldChunk(@Nonnull final BlockStateInfo info, final long chunkIndex) {
            throw new RuntimeException("ERROR: Riley you need to TEST THIS FUNCTION. Remove me if intentional.");
            // return info.getChunkRef().getStore().getExternalData().getChunkComponent(chunkIndex, WORLD_CHUNK_COMPONENT);
            // ALT VERSION: use the ref as above in the single ref param version of this function to figure out if we're a block ref or a chunk ref and get the right store based on that
        }

        /**
         * does NOT get the chunk that the info is in, it USES the ref to get
         * more global data.
         *
         * Then uses the world to get the WorldChunk at the coords provided
         */
        @Nullable
        public static final WorldChunk getWorldChunk(
            @Nonnull final Ref<ChunkStore> ref,
            final int chunkX,
            final int chunkZ
        ) {
            throw new RuntimeException("ERROR: Riley you need to TEST THIS FUNCTION. Remove me if intentional.");
            // return ref
            //     .getStore()
            //     .getExternalData()
            //     .getChunkComponent(ChunkUtil.indexChunk(chunkX, chunkZ), WORLD_CHUNK_COMPONENT);
            // ALT VERSION: use the ref as above in the single ref param version of this function to figure out if we're a block ref or a chunk ref and get the right store based on that
        }

        /**
         * does NOT get the chunk that the info is in, it USES the ref to get
         * more global data.
         *
         * Then uses the world to get the WorldChunk at the coords provided
         */
        @Nullable
        public static final WorldChunk getWorldChunk(@Nonnull final Ref<ChunkStore> ref, final long chunkIndex) {
            throw new RuntimeException("ERROR: Riley you need to TEST THIS FUNCTION. Remove me if intentional.");
            // return ref.getStore().getExternalData().getChunkComponent(chunkIndex, WORLD_CHUNK_COMPONENT);
            // ALT VERSION: use the ref as above in the single ref param version of this function to figure out if we're a block ref or a chunk ref and get the right store based on that
        }

        // UNTESTED (most methods are untested, or, kind of half tested. some i'm pretty sure work, some i've got no idea, at some point i'll go through and verify them all)
        @Nullable
        public static final WorldChunk getWorldChunk(
            @Nonnull final CommandBuffer<ChunkStore> commandBuffer, // << Doesn't need to get the world, it can access components directly
            @Nonnull final BlockChunk blockChunk
        ) {
            // but regardless we don't need the world to get the world chunk
            return getWorldChunk(commandBuffer.getExternalData(), blockChunk);
        }

        // UNTESTED
        @Nullable
        public static final WorldChunk getWorldChunk(
            @Nonnull final Store<ChunkStore> store,
            @Nonnull final BlockChunk blockChunk
        ) {
            return getWorldChunk(store.getExternalData(), blockChunk);
        }

        // UNTESTED
        // NOTE: ChunkStore IS a world provider, but, there's seemingly other easier ways to get the world chunk component? maybe? cause otherwise we'd have to do
        // `chunkStore.getWorld().getChunk(blockChunk.getIndex());`
        @Nullable
        public static final WorldChunk getWorldChunk(
            @Nonnull final ChunkStore chunkStore,
            @Nonnull final BlockChunk blockChunk
        ) {
            // ALT version: `chunkStore.getWorld().getChunk(blockChunk.getIndex());`
            return chunkStore.getChunkComponent(blockChunk.getIndex(), WORLD_CHUNK_COMPONENT);
        }

        // ====================================================================
        // the below methods all assume
        // - you have some way of accessing the world
        // - you also have some identifying about the chunk you want
        // ====================================================================

        // not sure if there's a way to get a ref from a block chunk directly, need
        // some way to tie it to a world.
        // UNTESTED
        @Nullable
        public static final WorldChunk getWorldChunk(
            @Nonnull final World world, // well, it is the world, sooo
            @Nonnull final BlockChunk blockChunk
        ) {
            return world.getChunk(blockChunk.getIndex());
        }

        // not sure if there's a way to get a ref from a block chunk directly, need
        // some way to tie it to a world.
        // UNTESTED
        @Nullable
        public static final WorldChunk getWorldChunk(
            @Nonnull final World world, // well, it is the world, sooo
            final long chunkIndex
        ) {
            return world.getChunk(chunkIndex);
        }

        // WorldProvider can be
        // - World
        // - ChunkStore
        // - idk, probably other stuff too
        // UNTESTED
        @Nullable
        public static final WorldChunk getWorldChunk(
            @Nonnull final WorldProvider worldProvider,
            @Nonnull final BlockChunk blockChunk
        ) {
            return worldProvider.getWorld().getChunk(blockChunk.getIndex());
        }

        // UNTESTED
        @Nullable
        public static final WorldChunk getWorldChunk(
            @Nonnull final WorldProvider worldProvider,
            final long chunkIndex
        ) {
            return worldProvider.getWorld().getChunk(chunkIndex);
        }

        // #endregion getWorldChunk

        // #region getChunkRef
        // lets say you don't have a chunk ref, but you KNOW you have the information required to find one.
        // well, good news. that's the entire point of this section. if you wanna see how i get a chunk
        // ref out of your components follow the path through from your inputs and shit should start making
        // more sense over time

        // With position vector -> redirects to the chunk index version
        @Nullable
        public static final Ref<ChunkStore> getChunkRef(
            @Nonnull final CommandBuffer<ChunkStore> commandBuffer,
            final Vector3i coords
        ) {
            return getChunkRef(commandBuffer.getExternalData(), ChunkUtil.indexChunkFromBlock(coords.x, coords.z));
        }

        @Nullable
        public static final Ref<ChunkStore> getChunkRef(@Nonnull final World world, final Vector3i coords) {
            return getChunkRef(world, ChunkUtil.indexChunkFromBlock(coords.x, coords.z));
        }

        @Nullable
        public static final Ref<ChunkStore> getChunkRef(
            @Nonnull final ChunkStore chunkStore,
            @Nonnull final Vector3i coords
        ) {
            return getChunkRef(chunkStore, ChunkUtil.indexChunkFromBlock(coords.x, coords.z));
        }

        // please don't use this one.. just here for completeness so you know you CAN get the chunk ref out of info - in fact, that's (in my understanding) the preferred way
        @Nullable
        public static final Ref<ChunkStore> getChunkRef(@Nonnull final BlockStateInfo info) {
            return info.getChunkRef();
        }

        @Nullable
        public static final Ref<ChunkStore> getChunkRef(@Nonnull final Ref<ChunkStore> blockRef) {
            final var info = Info.getInfo(blockRef);
            if (info == null) {
                return null;
            }
            return info.getChunkRef();
        }

        // With coords (x, z) -> redirects to the chunk index version
        @Nullable
        public static final Ref<ChunkStore> getChunkRef(
            @Nonnull final CommandBuffer<ChunkStore> commandBuffer,
            final int x,
            final int z
        ) {
            return getChunkRef(commandBuffer.getExternalData(), ChunkUtil.indexChunkFromBlock(x, z));
        }

        @Nullable
        public static final Ref<ChunkStore> getChunkRef(
            @Nonnull final ChunkStore chunkStore,
            final int x,
            final int z
        ) {
            return getChunkRef(chunkStore, ChunkUtil.indexChunkFromBlock(x, z));
        }

        @Nullable
        public static final Ref<ChunkStore> getChunkRef(@Nonnull final World world, final int x, final int z) {
            return getChunkRef(world, ChunkUtil.indexChunkFromBlock(x, z));
        }

        // With chunk index
        @Nullable
        public static final Ref<ChunkStore> getChunkRef(
            @Nonnull final CommandBuffer<ChunkStore> commandBuffer,
            final long chunkIndex
        ) {
            return getChunkRef(commandBuffer.getExternalData(), chunkIndex);
        }

        @Nullable
        public static final Ref<ChunkStore> getChunkRef(@Nonnull final World world, final long chunkIndex) {
            return getChunkRef(world.getChunkStore(), chunkIndex);
        }

        @Nullable
        public static final Ref<ChunkStore> getChunkRef(@Nonnull final ChunkStore chunkStore, final long chunkIndex) {
            return chunkStore.getChunkReference(chunkIndex);
        }

        // #endregion getChunkRef
    }

    public static final class BlockId {

        // ====================================================================
        // Option 1: Getting the ID of a block by its coords
        // - there's a certain coordinate at which exists a block. You want
        //   to know what type of block it is (its ID)
        // ====================================================================
        @Nullable
        public static final Integer getBlockId(
            @Nonnull final CommandBuffer<ChunkStore> commandBuffer,
            @Nonnull final Vector3i coords
        ) {
            final var chunkRef = Chunk.getChunkRef(commandBuffer, coords.x, coords.z);
            if (chunkRef == null) {
                return null;
            }

            final var worldChunk = Chunk.getWorldChunk(chunkRef);
            if (worldChunk == null) {
                return null;
            }

            return worldChunk.getBlock(coords.x, coords.y, coords.z);
        }

        @Nullable
        public static final Integer getBlockId(
            @Nonnull final CommandBuffer<ChunkStore> commandBuffer,
            final int x,
            final int y,
            final int z
        ) {
            final var chunkRef = Chunk.getChunkRef(commandBuffer, x, z);
            if (chunkRef == null) {
                return null;
            }

            final var worldChunk = BlockComponent.getComponent(chunkRef, WORLD_CHUNK_COMPONENT);
            if (worldChunk == null) {
                return null;
            }

            return worldChunk.getBlock(x, y, z);
        }

        // ====================================================================
        // Option 2: Getting the ID of a block by its type
        //  - if you know the string ID of the block (you decided
        //    this when naming your block) then you can get it by that string
        // ====================================================================

        /**
         * Get the integer ID for a block by its string ID
         *
         * @param blockId
         * @return
         */
        public static final int getBlockId(@Nonnull final String blockId) {
            return BlockType.getAssetMap().getIndex(blockId);
        }
    }

    public static final class TickProcedure {

        public static final boolean setTicking(@Nonnull final Ref<ChunkStore> ref) {
            return setTicking(ref, true);
        }

        public static final boolean setTicking(@Nonnull final Ref<ChunkStore> ref, final boolean ticking) {
            final var info = Info.getInfo(ref);
            if (info == null) {
                console.log("Info was null");
                return false;
            }

            return setTicking(info, ticking);
        }

        public static final boolean setTicking(@Nonnull final BlockStateInfo info) {
            return setTicking(info, true);
        }

        public static final boolean setTicking(@Nonnull final BlockStateInfo info, final boolean ticking) {
            final var worldChunk = Chunk.getWorldChunk(info);
            if (worldChunk == null) {
                console.log("World chunk was null");
                return false;
            }

            final var coords = Coords.getLocalCoords(info);
            return setTicking(worldChunk, coords, ticking);
        }

        public static final boolean setTicking(@Nonnull final WorldChunk worldChunk, @Nonnull final Vector3i coords) {
            return setTicking(worldChunk, coords, true);
        }

        public static final boolean setTicking(
            @Nonnull final WorldChunk worldChunk,
            @Nonnull final Vector3i coords,
            final boolean ticking
        ) {
            return worldChunk.setTicking(coords.x, coords.y, coords.z, ticking);
        }

        public static final boolean setTicking(
            @Nonnull final BlockChunk chunk,
            @Nonnull final BlockStateInfo info,
            final boolean ticking
        ) {
            final var coords = Coords.getLocalCoords(info);
            return chunk.setTicking(coords.x, coords.y, coords.z, ticking);
        }

        public static final boolean setTicking(@Nonnull final BlockChunk chunk, @Nonnull final Vector3i coords) {
            return chunk.setTicking(coords.x, coords.y, coords.z, true);
        }

        public static final boolean setTicking(
            @Nonnull final BlockChunk chunk,
            @Nonnull final Vector3i coords,
            final boolean ticking
        ) {
            return chunk.setTicking(coords.x, coords.y, coords.z, ticking);
        }
    }

    public static final class BlockComponent {

        @Nullable
        public static final <T extends Component<ChunkStore>> T getComponent(
            @Nonnull final ComponentType<ChunkStore, T> componentType,
            @Nonnull final World world,
            final int x,
            final int y,
            final int z
        ) {
            final var chunkRef = world.getChunkStore().getChunkReference(ChunkUtil.indexChunkFromBlock(x, z));
            if (chunkRef == null) {
                return null;
            }

            final var chunkStore = world.getChunkStore().getStore();
            final var blockComponentChunk = BlockComponent.getBlockComponentChunk(chunkRef);
            if (blockComponentChunk == null) {
                return null;
            }

            final var blockRef = blockComponentChunk.getEntityReference(ChunkUtil.indexBlockInColumn(x, y, z));
            if (blockRef == null || !blockRef.isValid()) {
                return null;
            }

            return chunkStore.getComponent(blockRef, componentType);
        }

        @Nullable
        public static final BlockComponentChunk getBlockComponentChunk(@Nonnull final Ref<ChunkStore> chunkRef) {
            return chunkRef.getStore().getComponent(chunkRef, BLOCK_COMPONENT_CHUNK);
        }

        public static final <T extends Component<ChunkStore>> T getComponent(
            @Nonnull final Supplier<ComponentType<ChunkStore, T>> getComponentType,
            @Nonnull final BlockComponentChunk chunk,
            final int localX,
            final int localY,
            final int localZ
        ) {
            final var ref = Entity.getRef(chunk, localX, localY, localZ);
            if (ref == null) {
                return null;
            }
            final var componentType = getComponentType.get();
            if (componentType == null) {
                return null;
            }

            return ref.getStore().getComponent(ref, componentType);
        }

        public static final <T extends Component<ChunkStore>> T getComponent(
            @Nonnull final Ref<ChunkStore> ref,
            @Nonnull final Supplier<ComponentType<ChunkStore, T>> getComponentType
        ) {
            final var componentType = getComponentType.get();
            if (componentType == null) {
                return null;
            }
            return ref.getStore().getComponent(ref, componentType);
        }

        public static final <T extends Component<ChunkStore>> T getComponent(
            @Nonnull final Ref<ChunkStore> ref,
            @Nonnull final ComponentType<ChunkStore, T> componentType
        ) {
            return ref.getStore().getComponent(ref, componentType);
        }

        @Nullable
        public static final <T extends Component<ChunkStore>> T getComponent(
            @Nonnull final ComponentType<ChunkStore, T> componentType,
            @Nonnull final BlockComponentChunk chunk,
            final int localX,
            final int localY,
            final int localZ
        ) {
            final var ref = Entity.getRef(chunk, localX, localY, localZ);
            if (ref == null) {
                return null;
            }
            return ref.getStore().getComponent(ref, componentType);
        }

        public static final <T extends Component<ChunkStore>> boolean hasComponent(
            @Nonnull final Supplier<ComponentType<ChunkStore, T>> getComponentType,
            @Nonnull final Ref<ChunkStore> ref
        ) {
            final var componentType = getComponentType.get();
            if (componentType == null) {
                return false;
            }

            return hasComponent(componentType, ref);
        }

        public static final <T extends Component<ChunkStore>> boolean hasComponent(
            @Nonnull final ComponentType<ChunkStore, T> componentType,
            @Nonnull final Ref<ChunkStore> ref
        ) {
            return (T) ref.getStore().getComponent(ref, componentType) != null;
        }
    }

    public static final class Type {

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
        public static final BlockType getBlockType(@Nonnull final String blockId) {
            return BlockType.getAssetMap().getAsset(blockId);
        }
    }
}
