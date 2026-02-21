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

// BlockStateInfo is your friend. All block entities have it.
// - If you want to get the world a block is in
// - If you want to get the CHUNK a block is in
// - If you want to get OTHER blocks
//
// All of those ^^ are gotten THROUGH BlockStateInfo
// effectively, BlockStateInfo + Ref to your block => your dream come true

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
public final class Utils {

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

    public static final class Block {

        /// -> get Ref<ChunkStore>  (BlockRef)
        /// -> get BlockStateInfo
        /// -> get ID               (int)
        /// -> get BlockType

        // DONE
        public static final class Ref_ {

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
                return Ref_.getRef(worldProvider.getWorld().getChunkStore(), chunkIndex, blockIndex);
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
            public static final Ref<ChunkStore> getRef(
                @Nonnull final World world,
                final int x,
                final int y,
                final int z
            ) {
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
                final var chunkRef = Chunk.Ref_.getChunkRef(
                    chunkStore,
                    ChunkUtil.indexChunkFromBlock(coords.x, coords.z)
                );
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
                final var chunkRef = Chunk.Ref_.getChunkRef(chunkStore, ChunkUtil.indexChunkFromBlock(x, z));
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
                final var chunkRef = Chunk.Ref_.getChunkRef(chunkStore, chunkIndex);
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
                final var blockComponentChunk = Component_.getBlockComponentChunk(chunkRef);
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
                final var blockComponentChunk = Component_.getBlockComponentChunk(chunkRef);
                if (blockComponentChunk == null) {
                    return null;
                }

                return getRef(blockComponentChunk, ChunkUtil.indexBlockInColumn(x, y, z));
            }

            @Nullable
            public static final Ref<ChunkStore> getRef(@Nonnull final Ref<ChunkStore> chunkRef, final int blockIndex) {
                final var blockComponentChunk = Component_.getBlockComponentChunk(chunkRef);
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
            // #endregion getRef
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
                return getInfo(Ref_.getRef(worldProvider, pos));
            }

            @Nullable
            public static final BlockStateInfo getInfo(
                @Nonnull final WorldProvider worldProvider,
                final int x,
                final int y,
                final int z
            ) {
                // => BlockRef
                return getInfo(Ref_.getRef(worldProvider, x, y, z));
            }

            @Nullable
            public static final BlockStateInfo getInfo(
                @Nonnull final WorldProvider worldProvider,
                final long chunkIndex,
                final int blockIndex
            ) {
                // => BlockRef
                return getInfo(Ref_.getRef(worldProvider, chunkIndex, blockIndex));
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
                return getInfo(Ref_.getRef(world, pos));
            }

            @Nullable
            public static final BlockStateInfo getInfo(
                @Nonnull final World world,
                final int x,
                final int y,
                final int z
            ) {
                // => BlockRef
                return getInfo(Ref_.getRef(world, x, y, z));
            }

            @Nullable
            public static final BlockStateInfo getInfo(
                @Nonnull final World world,
                final long chunkIndex,
                final int blockIndex
            ) {
                // => BlockRef
                return getInfo(Ref_.getRef(world, chunkIndex, blockIndex));
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
                return getInfo(Ref_.getRef(commandBuffer, pos));
            }

            @Nullable
            public static final BlockStateInfo getInfo(
                @Nonnull final CommandBuffer<ChunkStore> commandBuffer,
                final int x,
                final int y,
                final int z
            ) {
                // => BlockRef
                return getInfo(Ref_.getRef(commandBuffer, x, y, z));
            }

            @Nullable
            public static final BlockStateInfo getInfo(
                @Nonnull final CommandBuffer<ChunkStore> commandBuffer,
                final long chunkIndex,
                final int blockIndex
            ) {
                // => BlockRef
                return getInfo(Ref_.getRef(commandBuffer, chunkIndex, blockIndex));
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
                return getInfo(Ref_.getRef(chunkStore, pos));
            }

            @Nullable
            public static final BlockStateInfo getInfo(
                @Nonnull final Store<ChunkStore> chunkStore,
                final int x,
                final int y,
                final int z
            ) {
                // => BlockRef
                return getInfo(Ref_.getRef(chunkStore, x, y, z));
            }

            @Nullable
            public static final BlockStateInfo getInfo(
                @Nonnull final Store<ChunkStore> chunkStore,
                final long chunkIndex,
                final int blockIndex
            ) {
                // => BlockRef
                return getInfo(Ref_.getRef(chunkStore, chunkIndex, blockIndex));
            }

            // #endregion Store<ChunkStore>
            // #region ChunkStore
            // ====================================================================
            // ChunkStore
            // + Global coords
            // ====================================================================

            @Nullable
            public static final BlockStateInfo getInfo(
                @Nonnull final ChunkStore chunkStore,
                @Nonnull final Vector3i pos
            ) {
                // => BlockRef
                return getInfo(Ref_.getRef(chunkStore, pos));
            }

            @Nullable
            public static final BlockStateInfo getInfo(
                @Nonnull final ChunkStore chunkStore,
                final int x,
                final int y,
                final int z
            ) {
                // => BlockRef
                return getInfo(Ref_.getRef(chunkStore, x, y, z));
            }

            @Nullable
            public static final BlockStateInfo getInfo(
                @Nonnull final ChunkStore chunkStore,
                final long chunkIndex,
                final int blockIndex
            ) {
                // => BlockRef
                return getInfo(Ref_.getRef(chunkStore, chunkIndex, blockIndex));
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
                return getInfo(Ref_.getRef(chunkRef, coords));
            }

            @Nullable
            public static final BlockStateInfo getInfo(
                @Nonnull final Ref<ChunkStore> chunkRef,
                final int x,
                final int y,
                final int z
            ) {
                return getInfo(Ref_.getRef(chunkRef, x, y, z));
            }

            @Nullable
            public static final BlockStateInfo getInfo(@Nonnull final Ref<ChunkStore> chunkRef, final int blockIndex) {
                return getInfo(Ref_.getRef(chunkRef, blockIndex));
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
                return getInfo(Ref_.getRef(chunk, coords));
            }

            @Nullable
            public static final BlockStateInfo getInfo(
                @Nonnull final BlockComponentChunk chunk,
                final int x,
                final int y,
                final int z
            ) {
                // => BlockRef
                return getInfo(Ref_.getRef(chunk, x, y, z));
            }

            @Nullable
            public static final BlockStateInfo getInfo(@Nonnull final BlockComponentChunk chunk, final int blockIndex) {
                // => BlockRef
                return getInfo(Ref_.getRef(chunk, blockIndex));
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

                return Component_.getComponent(blockRef, BLOCK_STATE_INFO_COMPONENT);
            }
            // #endregion BlockRef

            // #endregion getInfo
        }

        public static final class Id {

            // ====================================================================
            // Option 1: Getting the ID of a block by its coords
            // - there's a certain coordinate at which exists a block. You want
            //   to know what type of block it is (its ID)
            // ====================================================================
            @Nullable
            public static final Integer getId(
                @Nonnull final CommandBuffer<ChunkStore> commandBuffer,
                @Nonnull final Vector3i coords
            ) {
                final var chunkRef = Chunk.Ref_.getChunkRef(commandBuffer, coords.x, coords.z);
                if (chunkRef == null) {
                    return null;
                }

                final var worldChunk = Chunk.WorldChunk_.getWorldChunk(chunkRef);
                if (worldChunk == null) {
                    return null;
                }

                return worldChunk.getBlock(coords.x, coords.y, coords.z);
            }

            @Nullable
            public static final Integer getId(
                @Nonnull final CommandBuffer<ChunkStore> commandBuffer,
                final int x,
                final int y,
                final int z
            ) {
                final var chunkRef = Chunk.Ref_.getChunkRef(commandBuffer, x, z);
                if (chunkRef == null) {
                    return null;
                }

                final var worldChunk = Component_.getComponent(chunkRef, WORLD_CHUNK_COMPONENT);
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
            public static final int getId(@Nonnull final String blockId) {
                return BlockType.getAssetMap().getIndex(blockId);
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
            public static final BlockType getType(@Nonnull final String blockId) {
                return BlockType.getAssetMap().getAsset(blockId);
            }
        }
    }

    public static final class Chunk {

        /// -> get WorldChunk
        /// -> get Ref<ChunkStore>  (ChunkRef)

        // DONE
        public static final class WorldChunk_ {

            // #region getWorldChunk

            @Nullable
            public static final WorldChunk getWorldChunkFromChunk(@Nonnull final Ref<ChunkStore> chunkRef) {
                return Component_.getComponent(chunkRef, WORLD_CHUNK_COMPONENT);
            }

            @Nullable
            public static final WorldChunk getWorldChunkFromBlock(@Nonnull final Ref<ChunkStore> blockRef) {
                final var info = Block.Info.getInfo(blockRef);
                if (info == null) {
                    return null;
                }

                return Component_.getComponent(info.getChunkRef(), WORLD_CHUNK_COMPONENT);
            }

            @Nullable
            public static final WorldChunk getWorldChunk(@Nonnull final Ref<ChunkStore> ref) {
                // Potential 1: The ref you passed me is a CHUNK ref. slay. thats the good shit. that's what we're after
                final var maybeChunk = Component_.getComponent(ref, WORLD_CHUNK_COMPONENT);
                if (maybeChunk != null) {
                    console.log("You gave me a chunk ref!");
                    return maybeChunk;
                }

                // Potential 2: the ref you passed me is a BLOCK ref. GOOD.
                final var info = Block.Info.getInfo(ref);
                if (info == null) {
                    return null;
                }
                console.log("You gave me a block ref!");

                return Component_.getComponent(info.getChunkRef(), WORLD_CHUNK_COMPONENT);
            }

            @Nullable
            public static final WorldChunk getWorldChunk(@Nonnull final BlockStateInfo info) {
                return Component_.getComponent(info.getChunkRef(), WORLD_CHUNK_COMPONENT);
            }

            // || Get an unrelated chunk in a world USING something that can get us that world
            // || - you have a way to access components in a given world
            // || - you also have some identifying about the chunk you want
            // ||====================================================================

            //   #region WorldProvider
            //   ==================================================================
            //   WorldProvider        -> WorldChunk
            //   ==================================================================
            //   WorldProvider can be
            //   - World
            //   - ChunkStore
            //   - idk, probably other stuff too

            @Nullable
            public static final WorldChunk getWorldChunk(
                @Nonnull final WorldProvider worldProvider,
                @Nonnull final BlockChunk blockChunk
            ) {
                return worldProvider.getWorld().getChunk(blockChunk.getIndex());
            }

            @Nullable
            public static final WorldChunk getWorldChunk(
                @Nonnull final WorldProvider worldProvider,
                final int chunkX,
                final int chunkZ
            ) {
                return worldProvider.getWorld().getChunk(ChunkUtil.indexChunk(chunkX, chunkZ));
            }

            @Nullable
            public static final WorldChunk getWorldChunk(
                @Nonnull final WorldProvider worldProvider,
                final long chunkIndex
            ) {
                return worldProvider.getWorld().getChunk(chunkIndex);
            }

            @Nullable
            public static final WorldChunk getWorldChunkFromBlock(
                @Nonnull final WorldProvider worldProvider,
                final int globalX,
                final int globalY
            ) {
                return worldProvider.getWorld().getChunk(ChunkUtil.indexChunkFromBlock(globalX, globalY));
            }

            //   #endregion WorldProvider
            //   #region World
            //   ==================================================================
            //   World                -> WorldChunk
            //   ==================================================================

            @Nullable
            public static final WorldChunk getWorldChunk(
                @Nonnull final World world,
                @Nonnull final BlockChunk blockChunk
            ) {
                return world.getChunk(blockChunk.getIndex());
            }

            @Nullable
            public static final WorldChunk getWorldChunk(
                @Nonnull final World world,
                final int chunkX,
                final int chunkZ
            ) {
                return world.getChunk(ChunkUtil.indexChunk(chunkX, chunkZ));
            }

            @Nullable
            public static final WorldChunk getWorldChunk(@Nonnull final World world, final long chunkIndex) {
                return world.getChunk(chunkIndex);
            }

            @Nullable
            public static final WorldChunk getWorldChunkFromBlock(
                @Nonnull final World world,
                final int globalX,
                final int globalZ
            ) {
                return world.getChunk(ChunkUtil.indexChunkFromBlock(globalX, globalZ));
            }

            //   #endregion World
            //   #region Ref<ChunkStore>
            //   ==================================================================
            //   Ref<ChunkStore>      -> WorldChunk
            //   ==================================================================

            @Nullable
            public static final WorldChunk getWorldChunk(
                @Nonnull final Ref<ChunkStore> ref,
                @Nonnull final BlockChunk blockChunk
            ) {
                return ref.getStore().getExternalData().getChunkComponent(blockChunk.getIndex(), WORLD_CHUNK_COMPONENT);
            }

            @Nullable
            public static final WorldChunk getWorldChunk(
                @Nonnull final Ref<ChunkStore> ref,
                final int chunkX,
                final int chunkZ
            ) {
                return ref
                    .getStore()
                    .getExternalData()
                    .getChunkComponent(ChunkUtil.indexChunk(chunkX, chunkZ), WORLD_CHUNK_COMPONENT);
            }

            @Nullable
            public static final WorldChunk getWorldChunk(@Nonnull final Ref<ChunkStore> ref, final long chunkIndex) {
                return ref.getStore().getExternalData().getChunkComponent(chunkIndex, WORLD_CHUNK_COMPONENT);
            }

            @Nullable
            public static final WorldChunk getWorldChunkFromBlock(
                @Nonnull final Ref<ChunkStore> ref,
                final int globalX,
                final int globalZ
            ) {
                return ref
                    .getStore()
                    .getExternalData()
                    .getChunkComponent(ChunkUtil.indexChunkFromBlock(globalX, globalZ), WORLD_CHUNK_COMPONENT);
            }

            //   #endregion WorldChunk
            //   #region BlockStateInfo
            //   ==================================================================
            //   BlockStateInfo       -> WorldChunk
            //   ==================================================================

            @Nullable
            public static final WorldChunk getWorldChunk(
                @Nonnull final BlockStateInfo info,
                @Nonnull final BlockChunk blockChunk
            ) {
                return info
                    .getChunkRef()
                    .getStore()
                    .getExternalData()
                    .getChunkComponent(blockChunk.getIndex(), WORLD_CHUNK_COMPONENT);
            }

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
                return info
                    .getChunkRef()
                    .getStore()
                    .getExternalData()
                    .getChunkComponent(ChunkUtil.indexChunk(chunkX, chunkZ), WORLD_CHUNK_COMPONENT);
            }

            /**
             * does NOT get the chunk that the info is in, it USES the info to get
             * the world.
             *
             * Then uses the world to get the WorldChunk at the coords provided
             */
            @Nullable
            public static final WorldChunk getWorldChunk(@Nonnull final BlockStateInfo info, final long chunkIndex) {
                return info
                    .getChunkRef()
                    .getStore()
                    .getExternalData()
                    .getChunkComponent(chunkIndex, WORLD_CHUNK_COMPONENT);
            }

            @Nullable
            public static final WorldChunk getWorldChunkFromBlock(
                @Nonnull final BlockStateInfo info,
                final int globalX,
                final int globalZ
            ) {
                return info
                    .getChunkRef()
                    .getStore()
                    .getExternalData()
                    .getChunkComponent(ChunkUtil.indexChunkFromBlock(globalX, globalZ), WORLD_CHUNK_COMPONENT);
            }

            //   #endregion BlockStateInfo
            //   #region CommandBuffer
            //   ==================================================================
            //   CommandBuffer        -> WorldChunk
            //   ==================================================================

            @Nullable
            public static final WorldChunk getWorldChunk(
                @Nonnull final CommandBuffer<ChunkStore> commandBuffer, // << Doesn't need to get the world, it can access components directly
                @Nonnull final BlockChunk blockChunk
            ) {
                // but regardless we don't need the world to get the world chunk
                return commandBuffer.getExternalData().getChunkComponent(blockChunk.getIndex(), WORLD_CHUNK_COMPONENT);
            }

            @Nullable
            public static final WorldChunk getWorldChunk(
                @Nonnull final CommandBuffer<ChunkStore> commandBuffer, // << Doesn't need to get the world, it can access components directly
                final int chunkX,
                final int chunkZ
            ) {
                // but regardless we don't need the world to get the world chunk
                return commandBuffer
                    .getExternalData()
                    .getChunkComponent(ChunkUtil.indexChunk(chunkX, chunkZ), WORLD_CHUNK_COMPONENT);
            }

            @Nullable
            public static final WorldChunk getWorldChunk(
                @Nonnull final CommandBuffer<ChunkStore> commandBuffer, // << Doesn't need to get the world, it can access components directly
                final long chunkIndex
            ) {
                // but regardless we don't need the world to get the world chunk
                return commandBuffer.getExternalData().getChunkComponent(chunkIndex, WORLD_CHUNK_COMPONENT);
            }

            @Nullable
            public static final WorldChunk getWorldChunkFromBlock(
                @Nonnull final CommandBuffer<ChunkStore> commandBuffer,
                final int globalX,
                final int globalZ
            ) {
                return commandBuffer
                    .getExternalData()
                    .getChunkComponent(ChunkUtil.indexChunkFromBlock(globalX, globalZ), WORLD_CHUNK_COMPONENT);
            }

            //   #endregion CommandBuffer
            //   #region Store<ChunkStore>
            //   ==================================================================
            //   Store<ChunkStore>    -> WorldChunk
            //   ==================================================================

            @Nullable
            public static final WorldChunk getWorldChunk(
                @Nonnull final Store<ChunkStore> store,
                @Nonnull final BlockChunk blockChunk
            ) {
                return store.getExternalData().getChunkComponent(blockChunk.getIndex(), WORLD_CHUNK_COMPONENT);
            }

            @Nullable
            public static final WorldChunk getWorldChunk(
                @Nonnull final Store<ChunkStore> store,
                final int chunkX,
                final int chunkZ
            ) {
                return store
                    .getExternalData()
                    .getChunkComponent(ChunkUtil.indexChunk(chunkX, chunkZ), WORLD_CHUNK_COMPONENT);
            }

            @Nullable
            public static final WorldChunk getWorldChunk(
                @Nonnull final Store<ChunkStore> store,
                final long chunkIndex
            ) {
                return store.getExternalData().getChunkComponent(chunkIndex, WORLD_CHUNK_COMPONENT);
            }

            @Nullable
            public static final WorldChunk getWorldChunkFromBlock(
                @Nonnull final Store<ChunkStore> store,
                final int globalX,
                final int globalZ
            ) {
                return store
                    .getExternalData()
                    .getChunkComponent(ChunkUtil.indexChunkFromBlock(globalX, globalZ), WORLD_CHUNK_COMPONENT);
            }

            //   #endregion Store<ChunkStore>
            //   #region ChunkStore
            //   ==================================================================
            //   ChunkStore           -> WorldChunk
            //   ==================================================================
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

            @Nullable
            public static final WorldChunk getWorldChunk(
                @Nonnull final ChunkStore chunkStore,
                final int chunkX,
                final int chunkZ
            ) {
                // ALT version: `chunkStore.getWorld().getChunk(blockChunk.getIndex());`
                return chunkStore.getChunkComponent(ChunkUtil.indexChunk(chunkX, chunkZ), WORLD_CHUNK_COMPONENT);
            }

            @Nullable
            public static final WorldChunk getWorldChunk(@Nonnull final ChunkStore chunkStore, final long chunkIndex) {
                // ALT version: `chunkStore.getWorld().getChunk(blockChunk.getIndex());`
                return chunkStore.getChunkComponent(chunkIndex, WORLD_CHUNK_COMPONENT);
            }

            @Nullable
            public static final WorldChunk getWorldChunkFromBlock(
                @Nonnull final ChunkStore chunkStore,
                final int globalX,
                final int globalZ
            ) {
                return chunkStore.getChunkComponent(
                    ChunkUtil.indexChunkFromBlock(globalX, globalZ),
                    WORLD_CHUNK_COMPONENT
                );
            }

            //   #endregion ChunkStore
            // #endregion getWorldChunk
        }

        public static final class Ref_ {

            // what i need to get a chunk ref
            // - anything that could get a block ref
            // - anything that could get a chunk
            // - anything that could get

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
                final var info = Block.Info.getInfo(blockRef);
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
            public static final Ref<ChunkStore> getChunkRef(
                @Nonnull final ChunkStore chunkStore,
                final long chunkIndex
            ) {
                return chunkStore.getChunkReference(chunkIndex);
            }

            // #endregion getChunkRef
        }
    }

    public static final class Component_ {

        /// -> get ? extends Component<ChunkStore>    < works for blocks, chunks

        // ====================================================================
        // Get a component from a block at the given coords
        // ====================================================================

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
            final var blockComponentChunk = Component_.getBlockComponentChunk(chunkRef);
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
            final var ref = Block.Ref_.getRef(chunk, localX, localY, localZ);
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
            final var ref = Block.Ref_.getRef(chunk, localX, localY, localZ);
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

    // DONE
    public static final class Coords {

        /// -> get LOCAL coordinates within chunk    (Vector3i)
        /// -> get LOCAL index                       (int | Integer) (returns Integer so it can be nullable on method that can fail)
        /// -> get GLOBAL coordinates                (Vector3i)

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
            final var info = Block.Info.getInfo(blockRef);
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

        // ====================================================================
        // Global coords to local coords
        // ====================================================================

        @Nonnull
        public static final Vector3i getLocalCoords(@Nonnull final Vector3i coords) {
            return new Vector3i(coords.x & 31, coords.y, coords.z & 31);
        }

        // You're telling me i can get local coordinates from coordinates?
        // yeah, cause, well, if you're calling this its definitely with global coords
        //
        // thus, all we need to do is do is keep x % 32, y, z % 32
        //
        // notably, 32 = 2^5, thus we only need to keep the 5 lowest bits
        @Nonnull
        public static final Vector3i getLocalCoords(final int x, final int y, final int z) {
            return new Vector3i(x & 31, y, z & 31);
        }

        // ====================================================================
        // index -> this is how we actually get coords of a block throughout their system
        // ====================================================================

        @Nonnull
        public static final Vector3i getLocalCoords(final int blockIndex) {
            // bits are as follows (assumes 32bit integers)
            //  1  2  3  4  5
            //  x  x  x  x  x
            //
            //  6  7  8  9  10
            //  z  z  z  z  z
            //
            // 11 12 13 14 15 16 17 18 19
            //  y  y  y  y  y  y  y  y  y
            //
            // bits 20-32 are assumed to be unused at this stage
            //
            // meaning:
            //  x: [0, 32)   | x can represent 32 different positions
            //  y: [0, 512)  | y can represent 512 different positions
            //  z: [0, 32)   | z can represent 32 different positions
            // thus, from this we can conclude (but also we just kind of know) that
            // chunks in hytale are 32x, 512y, 32z -> a really tall rectangular prism

            // tidbit on block coordinates
            // - local coordinates are the position within the chunk for the block
            // - this can be represented within 19 bits
            // - thus, every blockIndex is unique ONLY to the chunk its within
            // - thus, with just the first 19 bits we can figure out the local coordinates
            //   of any block given its index within its chunk

            final int x = blockIndex & 31; // keep 5
            final int y = (blockIndex >> 10) & ChunkUtil.HEIGHT_MASK; // shift 10 then keep 9
            final int z = (blockIndex >> 5) & 31; // shift 5 then keep 5

            return new Vector3i(x, y, z);
        }

        // #endregion getLocalCoords
        // #region getLocalIndex

        // Integer cause, nullable
        @Nullable
        public static final Integer getLocalIndex(@Nonnull final Ref<ChunkStore> blockRef) {
            final var info = Block.Info.getInfo(blockRef);
            if (info == null) {
                return null;
            }

            // => blockIndex
            return info.getIndex();
        }

        /**
         * IMPORTANT: don't use this, just call `getIndex()` on the info component....
         *
         * i mean, fr.
         */
        public static final int getLocalIndex(@Nonnull final BlockStateInfo info) {
            return info.getIndex();
        }

        public static final int getLocalIndex(@Nonnull final Vector3i coords) {
            return ChunkUtil.indexBlockInColumn(coords.x, coords.y, coords.z);
        }

        public static final int getLocalIndex(final int x, final int y, final int z) {
            return ChunkUtil.indexBlockInColumn(x, y, z);
        }

        // #endregion getLocalIndex
        // #region getGlobalCoords
        // ====================================================================
        // /\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\
        // ====================================================================
        //
        // Purpose:   Getting the GLOBAL coordinates of the block in its world
        // Requires:  THE CHUNK your block is in AND the local coordinates of your block (or some way i can get its local coords)

        //   #region block
        //   ====================================================================
        //   idea:
        //   + you have a reference to your block
        //   ====================================================================

        @Nullable
        public static final Vector3i getGlobalCoords(@Nonnull final Ref<ChunkStore> blockRef) {
            final var info = Block.Info.getInfo(blockRef);
            if (info == null) {
                return null;
            }

            return getGlobalCoords(info.getChunkRef(), info.getIndex());
        }

        @Nullable
        public static final Vector3i getGlobalCoords(@Nonnull final BlockStateInfo info) {
            return getGlobalCoords(info.getChunkRef(), info.getIndex());
        }

        //   #endregion block
        //   #region chunkNLocal
        //   ====================================================================
        //   idea:
        //   + you have a reference to the chunk
        //   + you have the local coords of your block
        //   ====================================================================

        //     #region chunkRef
        //     |=================================================================
        //     | Ref<ChunkStore> (ChunkRef)
        //     |=================================================================

        @Nullable
        public static final Vector3i getGlobalCoords(
            @Nonnull final Ref<ChunkStore> chunkRef,
            @Nonnull final Vector3i coords
        ) {
            final var worldChunk = Chunk.WorldChunk_.getWorldChunkFromChunk(chunkRef);
            if (worldChunk == null) {
                return null;
            }

            return getGlobalCoords(worldChunk.getX(), worldChunk.getZ(), coords);
        }

        @Nullable
        public static final Vector3i getGlobalCoords(
            @Nonnull final Ref<ChunkStore> chunkRef,
            final int x,
            final int y,
            final int z
        ) {
            final var worldChunk = Chunk.WorldChunk_.getWorldChunkFromChunk(chunkRef);
            if (worldChunk == null) {
                return null;
            }

            return getGlobalCoords(worldChunk.getX(), worldChunk.getZ(), x, y, z);
        }

        @Nullable
        public static final Vector3i getGlobalCoords(@Nonnull final Ref<ChunkStore> chunkRef, final int blockIndex) {
            final var worldChunk = Chunk.WorldChunk_.getWorldChunkFromChunk(chunkRef);
            if (worldChunk == null) {
                return null;
            }

            return getGlobalCoords(worldChunk.getX(), worldChunk.getZ(), blockIndex);
        }

        @Nullable
        public static final Vector3i getGlobalCoords(
            @Nonnull final Ref<ChunkStore> chunkRef,
            @Nonnull final BlockStateInfo info
        ) {
            final var worldChunk = Chunk.WorldChunk_.getWorldChunkFromChunk(chunkRef);
            if (worldChunk == null) {
                return null;
            }

            return getGlobalCoords(worldChunk.getX(), worldChunk.getZ(), info.getIndex());
        }

        //     #endregion chunkRef
        //     #region BlockAccessor
        //     |=================================================================
        //     | BlockAccessor OR WorldChunk
        //     |=================================================================

        @Nonnull
        public static final Vector3i getGlobalCoords(@Nonnull final WorldChunk chunk, @Nonnull final Vector3i coords) {
            return getGlobalCoords(chunk.getX(), chunk.getZ(), coords);
        }

        @Nonnull
        public static final Vector3i getGlobalCoords(
            @Nonnull final WorldChunk chunk,
            final int x,
            final int y,
            final int z
        ) {
            return getGlobalCoords(chunk.getX(), chunk.getZ(), x, y, z);
        }

        @Nonnull
        public static final Vector3i getGlobalCoords(@Nonnull final WorldChunk chunk, final int blockIndex) {
            return new Vector3i(chunk.getX(), chunk.getZ(), blockIndex);
        }

        @Nonnull
        public static final Vector3i getGlobalCoords(
            @Nonnull final WorldChunk chunk,
            @Nonnull final BlockStateInfo info
        ) {
            return new Vector3i(chunk.getX(), chunk.getZ(), info.getIndex());
        }

        //     #endregion chunkRef
        //     #region BlockAccessor
        //     |=================================================================
        //     | BlockAccessor OR WorldChunk
        //     |=================================================================

        @Nonnull
        public static final Vector3i getGlobalCoords(
            @Nonnull final BlockAccessor chunk,
            @Nonnull final Vector3i coords
        ) {
            return getGlobalCoords(chunk.getX(), chunk.getZ(), coords);
        }

        @Nonnull
        public static final Vector3i getGlobalCoords(
            @Nonnull final BlockAccessor chunk,
            final int x,
            final int y,
            final int z
        ) {
            return getGlobalCoords(chunk.getX(), chunk.getZ(), x, y, z);
        }

        @Nonnull
        public static final Vector3i getGlobalCoords(@Nonnull final BlockAccessor chunk, final int blockIndex) {
            return new Vector3i(chunk.getX(), chunk.getZ(), blockIndex);
        }

        @Nonnull
        public static final Vector3i getGlobalCoords(
            @Nonnull final BlockAccessor chunk,
            @Nonnull final BlockStateInfo info
        ) {
            return new Vector3i(chunk.getX(), chunk.getZ(), info.getIndex());
        }

        //     #endregion BlockAccessor
        //     #region BlockChunk
        //     |=================================================================
        //     | BlockChunk
        //     |=================================================================

        @Nonnull
        public static final Vector3i getGlobalCoords(
            @Nonnull final BlockChunk chunk,
            @Nonnull final Vector3i localCoords
        ) {
            return getGlobalCoords(chunk.getX(), chunk.getZ(), localCoords);
        }

        @Nonnull
        public static final Vector3i getGlobalCoords(@Nonnull final BlockChunk chunk, final int blockIndex) {
            return getGlobalCoords(chunk.getX(), chunk.getZ(), blockIndex);
        }

        @Nonnull
        public static final Vector3i getGlobalCoords(
            @Nonnull final BlockChunk chunk,
            @Nonnull final BlockStateInfo info
        ) {
            return getGlobalCoords(chunk.getX(), chunk.getZ(), info.getIndex());
        }

        @Nonnull
        public static final Vector3i getGlobalCoords(
            @Nonnull final BlockChunk chunk,
            final int x,
            final int y,
            final int z
        ) {
            return getGlobalCoords(chunk.getX(), chunk.getZ(), x, y, z);
        }

        //     #endregion BlockChunk
        //     #region done
        //     |=================================================================
        //     | Done!
        //     |=================================================================

        // Chunk Coords AND Block Coords
        @Nonnull
        public static final Vector3i getGlobalCoords(
            final int chunkX,
            final int chunkZ,
            @Nonnull final Vector3i blockCoords
        ) {
            return new Vector3i(blockCoords.x + (chunkX << 5), blockCoords.y, blockCoords.z + (chunkZ << 5));
        }

        // Chunk Coords AND Block Coords
        @Nonnull
        public static final Vector3i getGlobalCoords(
            final int chunkX,
            final int chunkZ,
            final int x,
            final int y,
            final int z
        ) {
            return new Vector3i(x + (chunkX << 5), y, z + (chunkZ << 5));
        }

        // Chunk coords AND Block Index
        @Nonnull
        public static final Vector3i getGlobalCoords(final int chunkX, final int chunkZ, final int blockIndex) {
            // remember: 5 bits -> 32
            final int globalX = (blockIndex & 31) + (chunkX << 5); // globalX = local X + 32 * chunkX (chunk is 32x32 for x,z)
            final int globalY = (blockIndex >> 10) & ChunkUtil.HEIGHT_MASK; // same as local y, there is no distinction between the two
            final int globalZ = ((blockIndex >> 5) & 31) + (chunkZ << 5); // globalX = local z + 32 * chunkZ (chunk is 32x32 for x,z)

            return new Vector3i(globalX, globalY, globalZ);
        }

        // Chunk coords AND Block Index
        @Nonnull
        public static final Vector3i getGlobalCoords(
            final int chunkX,
            final int chunkZ,
            @Nonnull final BlockStateInfo info
        ) {
            final var blockIndex = info.getIndex();

            // remember: 5 bits -> 32
            final int globalX = (blockIndex & 31) + (chunkX << 5); // globalX = local X + 32 * chunkX (chunk is 32x32 for x,z)
            final int globalY = (blockIndex >> 10) & ChunkUtil.HEIGHT_MASK; // same as local y, there is no distinction between the two
            final int globalZ = ((blockIndex >> 5) & 31) + (chunkZ << 5); // globalX = local z + 32 * chunkZ (chunk is 32x32 for x,z)

            return new Vector3i(globalX, globalY, globalZ);
        }

        // Chunk Coords AND Block Coords
        @Nonnull
        public static final Vector3i getGlobalCoords(final long chunkIndex, @Nonnull final Vector3i blockCoords) {
            final int chunkX = (int) (chunkIndex >> 32);
            final int chunkZ = (int) (chunkIndex);

            return new Vector3i(blockCoords.x + (chunkX << 5), blockCoords.y, blockCoords.z + (chunkZ << 5));
        }

        // Chunk Coords AND Block Coords
        @Nonnull
        public static final Vector3i getGlobalCoords(final long chunkIndex, final int x, final int y, final int z) {
            final int chunkX = (int) (chunkIndex >> 32);
            final int chunkZ = (int) (chunkIndex);

            return new Vector3i(x + (chunkX << 5), y, z + (chunkZ << 5));
        }

        // Chunk coords AND Block Index
        @Nonnull
        public static final Vector3i getGlobalCoords(final long chunkIndex, final int blockIndex) {
            // our chunk X coord is in the 33-64 bits (inclusive) of chunkIndex. We want that part, multiplied by 32, thus we must drop the first 32 bits of chunkIndex, then later shift it back 5 bits to "multiply" it by 32
            final int chunkX = (int) (chunkIndex >> 32);
            // our chunk z coord is in the first 32 bits, so we'll cast it to an int (just keeps first 32 bits) then later shift it back 5 bits to "multiply" it by 32
            final int chunkZ = (int) (chunkIndex);

            final int globalX = (blockIndex & 31) + (chunkX << 5);
            final int globalY = (blockIndex >> 10) & ChunkUtil.HEIGHT_MASK;
            final int globalZ = ((blockIndex >> 5) & 31) + (chunkZ << 5);

            return new Vector3i(globalX, globalY, globalZ);
        }

        // Chunk coords AND Block Index
        @Nonnull
        public static final Vector3i getGlobalCoords(final long chunkIndex, @Nonnull final BlockStateInfo info) {
            final var blockIndex = info.getIndex();

            // our chunk X coord is in the 33-64 bits (inclusive) of chunkIndex. We want that part, multiplied by 32, thus we must drop the first 32 bits of chunkIndex, then later shift it back 5 bits to "multiply" it by 32
            final int chunkX = (int) (chunkIndex >> 32);
            // our chunk z coord is in the first 32 bits, so we'll cast it to an int (just keeps first 32 bits) then later shift it back 5 bits to "multiply" it by 32
            final int chunkZ = (int) (chunkIndex);

            final int globalX = (blockIndex & 31) + (chunkX << 5);
            final int globalY = (blockIndex >> 10) & ChunkUtil.HEIGHT_MASK;
            final int globalZ = ((blockIndex >> 5) & 31) + (chunkZ << 5);

            return new Vector3i(globalX, globalY, globalZ);
        }

        //     #endregion done
        //   #endregion chunkNLocal
        // #endregion getGlobalCoords
    }

    /**
     * @deprecated I don't plan to use tick procedure stuff, so I'm not maintaining a billion
     * methods rn. mainly, i dont want to go through and make all the millions of methods.
     *
     * SO, this stuff is fine to use, but, yeah...
     */
    public static final class TickProcedure {

        public static final boolean setTicking(@Nonnull final Ref<ChunkStore> ref) {
            return setTicking(ref, true);
        }

        public static final boolean setTicking(@Nonnull final Ref<ChunkStore> ref, final boolean ticking) {
            final var info = Block.Info.getInfo(ref);
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
            final var worldChunk = Chunk.WorldChunk_.getWorldChunk(info);
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
}
