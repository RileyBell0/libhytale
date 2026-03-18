package dev.twunk.utils.world;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Vector2i;
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
import dev.twunk.test.TestUtil;
import java.util.ArrayList;
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

    @Nonnull
    @SuppressWarnings("null")
    public static final ComponentType<ChunkStore, BlockStateInfo> BLOCK_STATE_INFO_COMPONENT_TYPE =
        BlockStateInfo.getComponentType();

    // ==================================================
    // Grouped functions together
    // ==================================================

    // TESTS ADDED AND VERIFIED
    public static final class Block {

        public static final ArrayList<Boolean> testRefDetection(@Nonnull final Ref<ChunkStore> blockRef) {
            // functions to test
            final ArrayList<Boolean> refs = new ArrayList<>();

            final var info = Block.Info.getInfo(blockRef);
            if (info == null) {
                throw new RuntimeException("Error: failed to get info for test in asfiuogrt71t7o83");
            }

            final var chunkRef = info.getChunkRef();

            // BLOCK ref passed to isBlockRef (expect TRUE)
            refs.add(isBlockRef(blockRef));
            // BLOCK ref passed to isChunkRef (expect FALSE)
            refs.add(isChunkRef(blockRef));

            // CHUNK ref passed to isChunkRef (expect TRUE)
            refs.add(isChunkRef(chunkRef));
            // CHUNK ref passed to isBlockRef (expect FALSE)
            refs.add(isBlockRef(chunkRef));

            return refs;
        }

        // TESTS ADDED AND VERIFIED
        public static final boolean isBlockRef(@Nonnull final Ref<ChunkStore> ref) {
            return Utils.Component_.has(BLOCK_STATE_INFO_COMPONENT_TYPE, ref);
        }

        // TESTS ADDED AND VERIFIED
        public static final boolean isChunkRef(@Nonnull final Ref<ChunkStore> ref) {
            return Utils.Component_.has(WORLD_CHUNK_COMPONENT, ref);
        }

        /// -> get Ref<ChunkStore>  (BlockRef)
        /// -> get BlockStateInfo
        /// -> get ID               (int)
        /// -> get BlockType

        // TESTS ADDED AND VERIFIED
        public static final class Ref_ {

            /**
             * figured out how to
             * - break blocks (depends on if they're block entities or not on how you can do
             *     it, both can be done the same way if you do world.execute(()->world.breakBlock)
             *     BUT, if it's NOT a block entity you can just call breakBlock
             * - get block
             * - get block type
             * - get chunk (in various methods)
             */
            public static final void testWorldMethods(@Nonnull final World world, @Nonnull final Vector3i coords) {
                try {
                    /**
                     * world.breakBlock -> not allowed to call on blocks of your own store, well, rather you're
                     * not allowed to call it on BLOCK ENTITIES, so no chests etc
                     *
                     * or, seems like they only call it on non-ticking chunks?? nah just not allowed to kill a block entity that might run
                     */
                    // this works
                    // var ref = Ref_.getRef(cmd, coords.x, coords.y - 1, coords.z);

                    /** We're allowed to call break block if it's not also a block entity, otherwise it says the store
                     * is currently running some code and tells us NO BAD #illegal
                     *
                     * so, generally, just "execute" break block through world
                     */
                    // if (ref == null) {
                    //     world.breakBlock(coords.x, coords.y - 1, coords.z, 0);
                    // } else {
                    //     world.execute(() -> world.breakBlock(coords.x, coords.y - 1, coords.z, 0));
                    // }
                    world.execute(() -> world.breakBlock(coords.x, coords.y - 1, coords.z, 0));

                    /**
                     * These five work as is
                     */
                    world.getBlock(coords.x, coords.y, coords.z);
                    world.getBlockType(coords);
                    world.getChunkIfInMemory(ChunkCoords.Index.getChunkIndex(coords));
                    world.getChunkIfLoaded(ChunkCoords.Index.getChunkIndex(coords));
                    world.getChunk(ChunkCoords.Index.getChunkIndex(coords));

                    /**
                     * get chunk if non ticking seems to be of no value
                     * idk how to get it to not-fail, always fails for me
                     */
                    // var c = world.getChunkIfNonTicking(ChunkCoords.Index.getChunkIndex(coords));
                    // if (c == null) {
                    //     console.log("getChunkIfNonTicking failed");
                    // }
                } catch (Exception e) {
                    console.log("ERROR! " + e);
                }
            }

            /**
             * Tests all methods i've defined for getRef, good news - they work :)
             */
            @Nonnull
            public static final ArrayList<Ref<ChunkStore>> test(
                @Nonnull final Ref<ChunkStore> blockRef,
                @Nonnull final WorldChunk worldChunk,
                @Nonnull final CommandBuffer<ChunkStore> commandBuffer,
                @Nonnull final Vector3i providedCoords
            ) {
                /**
                 * worldChunk.getWorld() is allowed, that works
                 *
                 * and the following 4 also work
                 *
                 * so basically the world is working fine now
                 */
                final var testWorld = worldChunk.getWorld();
                if (testWorld == null) {
                    throw new RuntimeException("ERROR: world was null in test func 12351h9fovasbidlv");
                }
                testWorldMethods(testWorld, providedCoords);
                testWorldMethods(commandBuffer.getExternalData().getWorld(), providedCoords);
                testWorldMethods(commandBuffer.getStore().getExternalData().getWorld(), providedCoords);
                testWorldMethods(blockRef.getStore().getExternalData().getWorld(), providedCoords);

                final var blockX = providedCoords.x;
                final var blockY = providedCoords.y;
                final var blockZ = providedCoords.z;
                final var index = BlockCoords.Index.getLocalIndex(blockX, blockY, blockZ);
                final var localCoords = BlockCoords.Local.getLocalCoords(index);
                final var blockCoords = new Vector3i(blockX, blockY, blockZ);
                final var chunkIndex = ChunkCoords.Index.getChunkIndex(blockCoords);

                final var test = new TestUtil(commandBuffer, blockCoords);
                // functions to test
                final ArrayList<Ref<ChunkStore>> refs = new ArrayList<>();

                // WorldProvider
                refs.add(Block.Ref_.getRef(test.worldProvider, blockCoords));
                refs.add(Block.Ref_.getRef(test.worldProvider, blockX, blockY, blockZ));
                refs.add(Block.Ref_.getRef(test.worldProvider, chunkIndex, index));

                // World
                refs.add(Block.Ref_.getRef(test.world, blockCoords));
                refs.add(Block.Ref_.getRef(test.world, blockX, blockY, blockZ));
                refs.add(Block.Ref_.getRef(test.world, chunkIndex, index));

                // CommandBuffer
                refs.add(Block.Ref_.getRef(test.commandBuffer, blockCoords));
                refs.add(Block.Ref_.getRef(test.commandBuffer, blockX, blockY, blockZ));
                refs.add(Block.Ref_.getRef(test.commandBuffer, chunkIndex, index));

                // BlockStateInfo
                refs.add(Block.Ref_.getRef(test.info, blockCoords));
                refs.add(Block.Ref_.getRef(test.info, blockX, blockY, blockZ));
                refs.add(Block.Ref_.getRef(test.info, chunkIndex, index));

                // ChunkRef (global)
                refs.add(Block.Ref_.getRef_chunkRef(test.chunkRef, blockCoords));
                refs.add(Block.Ref_.getRef_chunkRef(test.chunkRef, blockX, blockY, blockZ));
                refs.add(Block.Ref_.getRef_chunkRef(test.chunkRef, chunkIndex, index));
                // ChunkRef (local)
                refs.add(Block.Ref_.getLocalRef(test.chunkRef, blockCoords));
                refs.add(Block.Ref_.getLocalRef(test.chunkRef, localCoords));
                refs.add(Block.Ref_.getLocalRef(test.chunkRef, blockX, blockY, blockZ));
                refs.add(Block.Ref_.getLocalRef(test.chunkRef, localCoords.x, localCoords.y, localCoords.z));
                refs.add(Block.Ref_.getLocalRef(test.chunkRef, index));

                // Store
                refs.add(Block.Ref_.getRef(test.store, blockCoords));
                refs.add(Block.Ref_.getRef(test.store, blockX, blockY, blockZ));
                refs.add(Block.Ref_.getRef(test.store, chunkIndex, index));

                // ChunkStore
                refs.add(Block.Ref_.getRef(test.chunkStore, blockCoords));
                refs.add(Block.Ref_.getRef(test.chunkStore, blockX, blockY, blockZ));
                refs.add(Block.Ref_.getRef(test.chunkStore, chunkIndex, index));

                // BlockRef
                refs.add(Block.Ref_.getRef_blockRef(test.blockRef, blockCoords));
                refs.add(Block.Ref_.getRef_blockRef(test.blockRef, blockX, blockY, blockZ));
                refs.add(Block.Ref_.getRef_blockRef(test.blockRef, chunkIndex, index));

                // WorldChunk
                refs.add(Block.Ref_.getRef(test.worldChunk, blockCoords));
                refs.add(Block.Ref_.getRef(test.worldChunk, blockX, blockY, blockZ));
                refs.add(Block.Ref_.getRef(test.worldChunk, chunkIndex, index));
                refs.add(Block.Ref_.getLocalRef(test.worldChunk, blockCoords));
                refs.add(Block.Ref_.getLocalRef(test.worldChunk, blockX, blockY, blockZ));
                refs.add(Block.Ref_.getLocalRef(test.worldChunk, index));

                // BlockComponentChunk
                refs.add(Block.Ref_.getRef(test.blockComponentChunk, index));
                refs.add(Block.Ref_.getLocalRef(test.blockComponentChunk, blockCoords));
                refs.add(Block.Ref_.getLocalRef(test.blockComponentChunk, blockX, blockY, blockZ));
                refs.add(Block.Ref_.getLocalRef(test.blockComponentChunk, index));

                // var i = 0;
                // for (var e : refs) {
                //     if (e == null) {
                //         console.log("" + i + ") null");
                //     } else {
                //         console.log("" + i + ") " + e);
                //     }
                //     i++;
                // }

                return refs;
            }

            // #region getRef
            // ====================================================================
            // /\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\
            // ====================================================================
            //
            // Purpose:   Getting a Ref (`Ref<ChunkStore>`) for a given block
            // Requires:  A method of accessing blocks in a world, AND coordinates of the block (or its index and the chunks index etc)
            //
            // NOTE: I don't support every combination. If you believe your
            //       refs/etc are enough to identify a chunk, look at methods
            //       within here that use similar data and just, copy+paste

            //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//
            // TODO Blocks I believe operate under a different chunk store to
            // the world they're in? not sure. should test...
            //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//

            // #region BlockRef (Ref<ChunkStore>)

            // => (ChunkStore, chunkIndex, blockIndex)
            @Nullable
            public static final Ref<ChunkStore> getRef_blockRef(
                @Nonnull final Ref<ChunkStore> blockRef,
                @Nonnull final Vector3i blockCoords
            ) {
                // "other" because it's not necessarily THIS chunk that i'm
                // using to get a block ref for the coords `blockCoords`
                final var otherChunkRef = Chunk.Ref_.getChunkRef(blockRef);
                if (otherChunkRef == null) {
                    return null;
                }

                return getRef2(
                    otherChunkRef.getStore().getExternalData(),
                    ChunkCoords.Index.getChunkIndex(blockCoords),
                    BlockCoords.Index.getLocalIndex(blockCoords)
                );
            }

            // => (ChunkStore, chunkIndex, blockIndex)
            @Nullable
            public static final Ref<ChunkStore> getRef_blockRef(
                @Nonnull final Ref<ChunkStore> blockRef,
                final int blockX,
                final int blockY,
                final int blockZ
            ) {
                // "other" because it's not necessarily THIS chunk that i'm
                // using to get a block ref for the coords `blockCoords`
                final var otherChunkRef = Chunk.Ref_.getChunkRef(blockRef);
                if (otherChunkRef == null) {
                    return null;
                }

                return getRef2(
                    otherChunkRef.getStore().getExternalData(),
                    ChunkCoords.Index.getChunkIndex(blockX, blockZ),
                    BlockCoords.Index.getLocalIndex(blockX, blockY, blockZ)
                );
            }

            // => (ChunkStore, chunkIndex, blockIndex)
            @Nullable
            public static final Ref<ChunkStore> getRef_blockRef(
                @Nonnull final Ref<ChunkStore> blockRef,
                final long chunkIndex,
                final int blockIndex
            ) {
                final var otherChunkRef = Chunk.Ref_.getChunkRef(blockRef);
                if (otherChunkRef == null) {
                    return null;
                }

                return getRef2(otherChunkRef.getStore().getExternalData(), chunkIndex, blockIndex);
            }

            // #endregion BlockRef (Ref<ChunkStore>)

            //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//
            // These almost directly have the chunk store to your world
            // -> they can easily get the store that has chunk your block is in
            //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//
            //
            // All require global coords for your block (either global block coords
            // or chunk identification and then local block coords/index)
            //

            // #region WorldProvider

            // => (ChunkStore, chunkIndex, blockIndex)
            @Nullable
            public static final Ref<ChunkStore> getRef(
                @Nonnull final WorldProvider worldProvider,
                @Nonnull final Vector3i coords
            ) {
                // return worldProvider
                //     .getWorld()
                //     .getChunk(ChunkCoords.Index.getChunkIndex(coords))
                //     .getBlockComponentEntity(coords.x, coords.y, coords.z);
                return getRef2(
                    worldProvider.getWorld().getChunkStore(),
                    ChunkCoords.Index.getChunkIndex(coords),
                    BlockCoords.Index.getLocalIndex(coords)
                );
            }

            // => (ChunkStore, chunkIndex, blockIndex)
            @Nullable
            public static final Ref<ChunkStore> getRef(
                @Nonnull final WorldProvider worldProvider,
                final int blockX,
                final int blockY,
                final int blockZ
            ) {
                return getRef2(
                    worldProvider.getWorld().getChunkStore(),
                    ChunkCoords.Index.getChunkIndex(blockX, blockZ),
                    BlockCoords.Index.getLocalIndex(blockX, blockY, blockZ)
                );
            }

            // => (ChunkStore, chunkIndex, blockIndex)
            @Nullable
            public static final Ref<ChunkStore> getRef(
                @Nonnull final WorldProvider worldProvider,
                final long chunkIndex,
                final int blockIndex
            ) {
                // final var localCoords = BlockCoords.Local.getLocalCoords(blockIndex);
                // return worldProvider
                //     .getWorld()
                //     .getChunk(chunkIndex)
                //     .getBlockComponentEntity(localCoords.x, localCoords.y, localCoords.z);
                return getRef2(worldProvider.getWorld().getChunkStore(), chunkIndex, blockIndex);
            }

            // #endregion WorldProvider
            // #region World

            // => (ChunkStore, chunkIndex, blockIndex)
            @Nullable
            public static final Ref<ChunkStore> getRef(@Nonnull final World world, @Nonnull final Vector3i coords) {
                // return world
                //     .getChunk(ChunkCoords.Index.getChunkIndex(coords))
                //     .getBlockComponentEntity(coords.x, coords.y, coords.z);
                return getRef2(
                    world.getChunkStore(),
                    ChunkCoords.Index.getChunkIndex(coords),
                    BlockCoords.Index.getLocalIndex(coords)
                );
            }

            // => (ChunkStore, chunkIndex, blockIndex)
            @Nullable
            public static final Ref<ChunkStore> getRef(
                @Nonnull final World world,
                final int blockX,
                final int blockY,
                final int blockZ
            ) {
                // return world
                //     .getChunk(ChunkCoords.Index.getChunkIndex(blockX, blockZ))
                //     .getBlockComponentEntity(blockX, blockY, blockZ);
                return getRef2(
                    world.getChunkStore(),
                    ChunkCoords.Index.getChunkIndex(blockX, blockZ),
                    BlockCoords.Index.getLocalIndex(blockX, blockY, blockZ)
                );
            }

            // => (ChunkStore, chunkIndex, blockIndex)
            @Nullable
            public static final Ref<ChunkStore> getRef(
                @Nonnull final World world,
                final long chunkIndex,
                final int blockIndex
            ) {
                // final var localCoords = BlockCoords.Local.getLocalCoords(blockIndex);
                // return world.getChunk(chunkIndex).getBlockComponentEntity(localCoords.x, localCoords.y, localCoords.z);
                return getRef2(world.getChunkStore(), chunkIndex, blockIndex);
            }

            // #endregion World
            // #region CommandBuffer

            // => (ChunkStore, chunkIndex, blockIndex)
            @Nullable
            public static final Ref<ChunkStore> getRef(
                @Nonnull final CommandBuffer<ChunkStore> commandBuffer,
                @Nonnull final Vector3i coords
            ) {
                return getRef2(
                    commandBuffer.getExternalData(),
                    ChunkCoords.Index.getChunkIndex(coords),
                    BlockCoords.Index.getLocalIndex(coords)
                );
            }

            // => (ChunkStore, chunkIndex, blockIndex)
            @Nullable
            public static final Ref<ChunkStore> getRef(
                @Nonnull final CommandBuffer<ChunkStore> commandBuffer,
                final int blockX,
                final int blockY,
                final int blockZ
            ) {
                return getRef2(
                    commandBuffer.getExternalData(),
                    ChunkCoords.Index.getChunkIndex(blockX, blockZ),
                    BlockCoords.Index.getLocalIndex(blockX, blockY, blockZ)
                );
            }

            // => (ChunkStore, chunkIndex, blockIndex)
            @Nullable
            public static final Ref<ChunkStore> getRef(
                @Nonnull final CommandBuffer<ChunkStore> commandBuffer,
                final long chunkIndex,
                final int blockIndex
            ) {
                return getRef2(commandBuffer.getExternalData(), chunkIndex, blockIndex);
            }

            // #endregion CommandBuffer
            // #region BlockStateInfo

            @Nullable
            public static final Ref<ChunkStore> getRef(
                @Nonnull final BlockStateInfo info,
                @Nonnull final Vector3i blockCoords
            ) {
                return getRef2(
                    info.getChunkRef().getStore().getExternalData(),
                    ChunkCoords.Index.getChunkIndex(blockCoords),
                    BlockCoords.Index.getLocalIndex(blockCoords)
                );
            }

            @Nullable
            public static final Ref<ChunkStore> getRef(
                @Nonnull final BlockStateInfo info,
                final int blockX,
                final int blockY,
                final int blockZ
            ) {
                return getRef2(
                    info.getChunkRef().getStore().getExternalData(),
                    ChunkCoords.Index.getChunkIndex(blockX, blockZ),
                    BlockCoords.Index.getLocalIndex(blockX, blockY, blockZ)
                );
            }

            @Nullable
            public static final Ref<ChunkStore> getRef(
                @Nonnull final BlockStateInfo info,
                final long chunkIndex,
                final int blockIndex
            ) {
                return getRef2(info.getChunkRef().getStore().getExternalData(), chunkIndex, blockIndex);
            }

            // #endregion BlockStateInfo
            // #region ChunkRef (Ref<ChunkStore>)

            /**
             * Get a ref USING another chunk ref to simply access the world
             * @param otherChunkRef
             * @param blockCoords
             * @return
             */
            // some ChunkRef => store of all chunks in the world => our chunk => component ON the chunk itself => block ref in the chunk
            @Nullable
            public static final Ref<ChunkStore> getRef_chunkRef(
                @Nonnull final Ref<ChunkStore> otherChunkRef,
                @Nonnull final Vector3i blockCoords
            ) {
                return getRef2(
                    otherChunkRef.getStore().getExternalData(),
                    ChunkCoords.Index.getChunkIndex(blockCoords),
                    BlockCoords.Index.getLocalIndex(blockCoords)
                );
            }

            // some ChunkRef => store of all chunks in the world => our chunk => component ON the chunk itself => block ref in the chunk
            @Nullable
            public static final Ref<ChunkStore> getRef_chunkRef(
                @Nonnull final Ref<ChunkStore> otherChunkRef,
                final int blockX,
                final int blockY,
                final int blockZ
            ) {
                return getRef2(
                    otherChunkRef.getStore().getExternalData(),
                    ChunkCoords.Index.getChunkIndex(blockX, blockZ),
                    BlockCoords.Index.getLocalIndex(blockX, blockY, blockZ)
                );
            }

            // some ChunkRef => store of all chunks in the world => our chunk => component ON the chunk itself => block ref in the chunk
            @Nullable
            public static final Ref<ChunkStore> getRef_chunkRef(
                @Nonnull final Ref<ChunkStore> otherChunkRef,
                final long chunkIndex,
                final int blockIndex
            ) {
                return getRef2(otherChunkRef.getStore().getExternalData(), chunkIndex, blockIndex);
            }

            // #endregion ChunkRef (Ref<ChunkStore>)

            //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//
            // These ARE the chunk store to your world
            // -> they can easily get the chunk your block is in
            //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//
            //
            // All require global coords for your block (either global block coords
            // or chunk identification and then local block coords/index)
            //

            // #region Store<ChunkStore>

            // => (ChunkStore, chunkIndex, blockIndex)
            @Nullable
            public static final Ref<ChunkStore> getRef(
                @Nonnull final Store<ChunkStore> store,
                @Nonnull final Vector3i blockCoords
            ) {
                final var chunkRef = store
                    .getExternalData()
                    .getChunkReference(ChunkCoords.Index.getChunkIndex(blockCoords));
                if (chunkRef == null) {
                    return null;
                }

                return getLocalRef1_chunkRef(chunkRef, BlockCoords.Index.getLocalIndex(blockCoords));
            }

            // => (ChunkStore, chunkIndex, blockIndex)
            @Nullable
            public static final Ref<ChunkStore> getRef(
                @Nonnull final Store<ChunkStore> store,
                final int blockX,
                final int blockY,
                final int blockZ
            ) {
                final var chunkRef = store
                    .getExternalData()
                    .getChunkReference(ChunkCoords.Index.getChunkIndex(blockX, blockZ));
                if (chunkRef == null) {
                    return null;
                }

                return getLocalRef1_chunkRef(chunkRef, BlockCoords.Index.getLocalIndex(blockX, blockY, blockZ));
            }

            // => (ChunkStore, chunkIndex, blockIndex)
            @Nullable
            public static final Ref<ChunkStore> getRef(
                @Nonnull final Store<ChunkStore> store,
                final long chunkIndex,
                final int blockIndex
            ) {
                final var chunkRef = store.getExternalData().getChunkReference(chunkIndex);
                if (chunkRef == null) {
                    return null;
                }

                return getLocalRef1_chunkRef(chunkRef, blockIndex);
            }

            // #endregion Store<ChunkStore>
            // #region ChunkStore

            // => (ChunkStore, chunkIndex, blockIndex)
            @Nullable
            public static final Ref<ChunkStore> getRef(
                @Nonnull final ChunkStore chunkStore,
                @Nonnull final Vector3i blockCoords
            ) {
                final var chunkRef = chunkStore.getChunkReference(ChunkCoords.Index.getChunkIndex(blockCoords));
                if (chunkRef == null) {
                    return null;
                }

                return getLocalRef1_chunkRef(chunkRef, BlockCoords.Index.getLocalIndex(blockCoords));
            }

            // => (ChunkStore, chunkIndex, blockIndex)
            @Nullable
            public static final Ref<ChunkStore> getRef(
                @Nonnull final ChunkStore chunkStore,
                final int blockX,
                final int blockY,
                final int blockZ
            ) {
                final var chunkRef = chunkStore.getChunkReference(ChunkCoords.Index.getChunkIndex(blockX, blockZ));
                if (chunkRef == null) {
                    return null;
                }

                return getLocalRef1_chunkRef(chunkRef, BlockCoords.Index.getLocalIndex(blockX, blockY, blockZ));
            }

            @Nullable
            public static final Ref<ChunkStore> getRef(
                @Nonnull final ChunkStore chunkStore,
                final long chunkIndex,
                final int blockIndex
            ) {
                final var chunkRef = chunkStore.getChunkReference(chunkIndex);
                if (chunkRef == null) {
                    return null;
                }

                return getLocalRef1_chunkRef(chunkRef, blockIndex);
            }

            @Nullable
            private static final Ref<ChunkStore> getRef2(
                @Nonnull final ChunkStore chunkStore,
                final long chunkIndex,
                final int blockIndex
            ) {
                final var chunkRef = chunkStore.getChunkReference(chunkIndex);
                if (chunkRef == null) {
                    return null;
                }

                return getLocalRef1_chunkRef(chunkRef, blockIndex);
            }

            // #endregion ChunkStore

            //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//
            // The below methods allow you to access a block ref WITHIN
            // THE SAME CHUNK. They do NOT allow global access
            // equally, they're the endpoint for all fetching of block refs
            // - well, rather, BlockComponentChunk is
            //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//
            //
            // All require global coords for your block (if you're using coordinates)
            // OR the block index. I really didn't want to dupe methods for both
            // global coords (x,y,z) and local coords (x,y,z) since, that seems
            // more confusing than useful
            //

            // #region anyLocalRef
            @Nullable
            public static final Ref<ChunkStore> getLocalRef(
                @Nonnull final Ref<ChunkStore> ref,
                @Nonnull final Vector3i blockCoords
            ) {
                if (isBlockRef(ref)) {
                    return getLocalRef_blockRef(ref, blockCoords);
                } else {
                    return getLocalRef_chunkRef(ref, blockCoords);
                }
            }

            @Nullable
            public static final Ref<ChunkStore> getLocalRef(
                @Nonnull final Ref<ChunkStore> ref,
                final int blockX,
                final int blockY,
                final int blockZ
            ) {
                if (isBlockRef(ref)) {
                    return getLocalRef_blockRef(ref, blockX, blockY, blockZ);
                } else {
                    return getLocalRef_chunkRef(ref, blockX, blockY, blockZ);
                }
            }

            @Nullable
            public static final Ref<ChunkStore> getLocalRef(@Nonnull final Ref<ChunkStore> ref, final int blockIndex) {
                if (isBlockRef(ref)) {
                    return getLocalRef_blockRef(ref, blockIndex);
                } else {
                    return getLocalRef_chunkRef(ref, blockIndex);
                }
            }

            // #endregion anyLocalRef
            // #region localBlockRef
            @Nullable
            public static final Ref<ChunkStore> getLocalRef_blockRef(
                @Nonnull final Ref<ChunkStore> blockRef,
                @Nonnull final Vector3i blockCoords
            ) {
                var info = Utils.Component_.get(blockRef, BLOCK_STATE_INFO_COMPONENT_TYPE);
                if (info == null) {
                    return null;
                }

                var blockIndex = BlockCoords.Index.getLocalIndex(blockCoords);

                return getLocalRef1_chunkRef(info.getChunkRef(), blockIndex);
            }

            @Nullable
            public static final Ref<ChunkStore> getLocalRef_blockRef(
                @Nonnull final Ref<ChunkStore> blockRef,
                final int blockX,
                final int blockY,
                final int blockZ
            ) {
                var info = Utils.Component_.get(blockRef, BLOCK_STATE_INFO_COMPONENT_TYPE);
                if (info == null) {
                    return null;
                }

                var blockIndex = BlockCoords.Index.getLocalIndex(blockX, blockY, blockZ);

                return getLocalRef1_chunkRef(info.getChunkRef(), blockIndex);
            }

            @Nullable
            public static final Ref<ChunkStore> getLocalRef_blockRef(
                @Nonnull final Ref<ChunkStore> blockRef,
                final int blockIndex
            ) {
                var info = Utils.Component_.get(blockRef, BLOCK_STATE_INFO_COMPONENT_TYPE);
                if (info == null) {
                    return null;
                }

                return getLocalRef1_chunkRef(info.getChunkRef(), blockIndex);
            }

            // #endregion localBlockRef
            // #region localChunkRef

            // ChunkRef => component ON the chunk itself => block ref in the chunk
            @Nullable
            public static final Ref<ChunkStore> getLocalRef_chunkRef(
                @Nonnull final Ref<ChunkStore> chunkRef,
                @Nonnull final Vector3i blockCoords
            ) {
                final var blockComponentChunk = Component_.getBlockComponentChunk(chunkRef);
                if (blockComponentChunk == null) {
                    return null;
                }

                return getLocalRef0(blockComponentChunk, BlockCoords.Index.getLocalIndex(blockCoords));
            }

            // ChunkRef => component ON the chunk itself => block ref in the chunk
            @Nullable
            public static final Ref<ChunkStore> getLocalRef_chunkRef(
                @Nonnull final Ref<ChunkStore> chunkRef,
                final int blockX,
                final int blockY,
                final int blockZ
            ) {
                final var blockComponentChunk = Component_.getBlockComponentChunk(chunkRef);
                if (blockComponentChunk == null) {
                    return null;
                }

                return getLocalRef0(blockComponentChunk, BlockCoords.Index.getLocalIndex(blockX, blockY, blockZ));
            }

            // ChunkRef => component ON the chunk itself => block ref in the chunk
            @Nullable
            public static final Ref<ChunkStore> getLocalRef_chunkRef(
                @Nonnull final Ref<ChunkStore> chunkRef,
                final int blockIndex
            ) {
                return getLocalRef1_chunkRef(chunkRef, blockIndex);
            }

            @Nullable
            private static final Ref<ChunkStore> getLocalRef1_chunkRef(
                @Nonnull final Ref<ChunkStore> chunkRef,
                final int blockIndex
            ) {
                final var blockComponentChunk = Component_.getBlockComponentChunk(chunkRef);
                if (blockComponentChunk == null) {
                    return null;
                }

                return getLocalRef0(blockComponentChunk, blockIndex);
            }

            // #endregion localChunkRef
            // #region localBlockStateInfo

            /**
             * Returns another block based on its local coords WITHIN THE SAME CHUNK
             */
            // BlockInfo => ChunkRef => component ON the chunk itself => block ref in the chunk
            @Nullable
            public static final Ref<ChunkStore> getLocalRef(
                @Nonnull final BlockStateInfo info,
                @Nonnull final Vector3i blockCoords
            ) {
                final var blockComponentChunk = Component_.getBlockComponentChunk(info.getChunkRef());
                if (blockComponentChunk == null) {
                    return null;
                }

                return getLocalRef0(blockComponentChunk, BlockCoords.Index.getLocalIndex(blockCoords));
            }

            /**
             * Returns another block based on its local coords WITHIN THE SAME CHUNK
             */
            // BlockInfo => ChunkRef => component ON the chunk itself => block ref in the chunk
            @Nullable
            public static final Ref<ChunkStore> getLocalRef(
                @Nonnull final BlockStateInfo info,
                final int blockX,
                final int blockY,
                final int blockZ
            ) {
                final var blockComponentChunk = Component_.getBlockComponentChunk(info.getChunkRef());
                if (blockComponentChunk == null) {
                    return null;
                }

                return getLocalRef0(blockComponentChunk, BlockCoords.Index.getLocalIndex(blockX, blockY, blockZ));
            }

            /**
             * Returns another block based on its local coords WITHIN THE SAME CHUNK
             */
            // BlockInfo => ChunkRef => component ON the chunk itself => block ref in the chunk
            @Nullable
            public static final Ref<ChunkStore> getLocalRef(@Nonnull final BlockStateInfo info, final int blockIndex) {
                final var blockComponentChunk = Component_.getBlockComponentChunk(info.getChunkRef());
                if (blockComponentChunk == null) {
                    return null;
                }

                return getLocalRef0(blockComponentChunk, blockIndex);
            }

            // #endregion localBlockStateInfo
            // #region WorldChunk

            @Nullable
            public static final Ref<ChunkStore> getRef(
                @Nonnull final WorldChunk worldChunk,
                @Nonnull final Vector3i blockCoords
            ) {
                return getRef2(
                    worldChunk.getReference().getStore().getExternalData(),
                    ChunkCoords.Index.getChunkIndex(blockCoords),
                    BlockCoords.Index.getLocalIndex(blockCoords)
                );
            }

            /** SHOULD REALLY BE CALLED GET LOCAL REF */
            // component ON the chunk itself => block ref in the chunk
            @Nullable
            public static final Ref<ChunkStore> getRef(
                @Nonnull final WorldChunk worldChunk,
                final int blockX,
                final int blockY,
                final int blockZ
            ) {
                return getRef2(
                    worldChunk.getReference().getStore().getExternalData(),
                    ChunkCoords.Index.getChunkIndex(blockX, blockZ),
                    BlockCoords.Index.getLocalIndex(blockX, blockY, blockZ)
                );
            }

            @Nullable
            public static final Ref<ChunkStore> getRef(
                @Nonnull final WorldChunk worldChunk,
                final long chunkIndex,
                final int blockIndex
            ) {
                return getRef2(worldChunk.getReference().getStore().getExternalData(), chunkIndex, blockIndex);
            }

            @Nullable
            public static final Ref<ChunkStore> getLocalRef(
                @Nonnull final WorldChunk worldChunk,
                @Nonnull final Vector3i blockCoords
            ) {
                return worldChunk.getBlockComponentEntity(blockCoords.x, blockCoords.y, blockCoords.z);
            }

            /** SHOULD REALLY BE CALLED GET LOCAL REF */
            // component ON the chunk itself => block ref in the chunk
            @Nullable
            public static final Ref<ChunkStore> getLocalRef(
                @Nonnull final WorldChunk worldChunk,
                final int blockX,
                final int blockY,
                final int blockZ
            ) {
                return worldChunk.getBlockComponentEntity(blockX, blockY, blockZ);
            }

            @Nullable
            public static final Ref<ChunkStore> getLocalRef(
                @Nonnull final WorldChunk worldChunk,
                final int blockIndex
            ) {
                final var localCoords = BlockCoords.Local.getLocalCoords(blockIndex);
                return worldChunk.getBlockComponentEntity(localCoords.x, localCoords.y, localCoords.z);
            }

            // #endregion WorldChunk
            // #region BlockComponentChunk
            @Nullable
            public static final Ref<ChunkStore> getRef(
                @Nonnull final BlockComponentChunk blockComponentChunk,
                final int blockIndex
            ) {
                return getLocalRef0(blockComponentChunk, blockIndex);
            }

            @Nullable
            public static final Ref<ChunkStore> getLocalRef(
                @Nonnull final BlockComponentChunk blockComponentChunk,
                @Nonnull final Vector3i blockCoords
            ) {
                return getLocalRef0(blockComponentChunk, BlockCoords.Index.getLocalIndex(blockCoords));
            }

            @Nullable
            public static final Ref<ChunkStore> getLocalRef(
                @Nonnull final BlockComponentChunk blockComponentChunk,
                final int blockX,
                final int blockY,
                final int blockZ
            ) {
                return getLocalRef0(blockComponentChunk, BlockCoords.Index.getLocalIndex(blockX, blockY, blockZ));
            }

            @Nullable
            public static final Ref<ChunkStore> getLocalRef(
                @Nonnull final BlockComponentChunk blockComponentChunk,
                final int blockIndex
            ) {
                return getLocalRef0(blockComponentChunk, blockIndex);
            }

            @Nullable
            private static final Ref<ChunkStore> getLocalRef0(
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

        // TESTS ADDED AND VERIFIED
        public static final class Info {

            /**
             * Tests all methods i've defined for getInfo
             */
            @Nonnull
            public static final ArrayList<BlockStateInfo> test(
                @Nonnull final Ref<ChunkStore> blockRef,
                @Nonnull final WorldChunk worldChunk,
                @Nonnull final CommandBuffer<ChunkStore> commandBuffer,
                @Nonnull final Vector3i providedCoords
            ) {
                final var blockX = providedCoords.x;
                final var blockY = providedCoords.y;
                final var blockZ = providedCoords.z;
                final var localIndex = BlockCoords.Index.getLocalIndex(blockX, blockY, blockZ);
                final var localCoords = BlockCoords.Local.getLocalCoords(localIndex);
                final var blockCoords = new Vector3i(blockX, blockY, blockZ);
                final var chunkIndex = ChunkCoords.Index.getChunkIndex(blockCoords);

                final var test = new TestUtil(commandBuffer, blockCoords);
                // functions to test
                final ArrayList<BlockStateInfo> refs = new ArrayList<>();

                // WorldProvider
                refs.add(Block.Info.getInfo(test.worldProvider, blockCoords));
                refs.add(Block.Info.getInfo(test.worldProvider, blockX, blockY, blockZ));
                refs.add(Block.Info.getInfo(test.worldProvider, chunkIndex, localIndex));

                // World
                refs.add(Block.Info.getInfo(test.world, blockCoords));
                refs.add(Block.Info.getInfo(test.world, blockX, blockY, blockZ));
                refs.add(Block.Info.getInfo(test.world, chunkIndex, localIndex));

                // CommandBuffer
                refs.add(Block.Info.getInfo(test.commandBuffer, blockCoords));
                refs.add(Block.Info.getInfo(test.commandBuffer, blockX, blockY, blockZ));
                refs.add(Block.Info.getInfo(test.commandBuffer, chunkIndex, localIndex));

                // BlockStateInfo
                refs.add(Block.Info.getInfo(test.info, blockCoords));
                refs.add(Block.Info.getInfo(test.info, blockX, blockY, blockZ));
                refs.add(Block.Info.getInfo(test.info, chunkIndex, localIndex));

                // ChunkRef (global)
                // - without specifying that it's a chunk ref we provided, letting the code figure that out
                refs.add(Block.Info.getInfo(test.chunkRef, blockCoords));
                refs.add(Block.Info.getInfo(test.chunkRef, blockX, blockY, blockZ));
                refs.add(Block.Info.getInfo(test.chunkRef, chunkIndex, localIndex));
                // - specifying it's a chunk ref
                refs.add(Block.Info.getInfo_chunkRef(test.chunkRef, blockCoords));
                refs.add(Block.Info.getInfo_chunkRef(test.chunkRef, blockX, blockY, blockZ));
                refs.add(Block.Info.getInfo_chunkRef(test.chunkRef, chunkIndex, localIndex));

                // ChunkRef (local)
                // - without specifying that it's a chunk ref we provided, letting the code figure that out
                refs.add(Block.Info.getLocalInfo(test.chunkRef, localCoords));
                refs.add(Block.Info.getLocalInfo(test.chunkRef, localCoords.x, localCoords.y, localCoords.z));
                refs.add(Block.Info.getLocalInfo(test.chunkRef, localIndex));
                // - specifying it's a chunk ref
                refs.add(Block.Info.getLocalInfo_chunkRef(test.chunkRef, localCoords));
                refs.add(Block.Info.getLocalInfo_chunkRef(test.chunkRef, localCoords.x, localCoords.y, localCoords.z));
                refs.add(Block.Info.getLocalInfo_chunkRef(test.chunkRef, localIndex));

                // Store
                refs.add(Block.Info.getInfo(test.store, blockCoords));
                refs.add(Block.Info.getInfo(test.store, blockX, blockY, blockZ));
                refs.add(Block.Info.getInfo(test.store, chunkIndex, localIndex));

                // ChunkStore
                refs.add(Block.Info.getInfo(test.chunkStore, blockCoords));
                refs.add(Block.Info.getInfo(test.chunkStore, blockX, blockY, blockZ));
                refs.add(Block.Info.getInfo(test.chunkStore, chunkIndex, localIndex));

                // BlockRef
                refs.add(Block.Info.getInfo_blockRef(test.blockRef, blockCoords));
                refs.add(Block.Info.getInfo_blockRef(test.blockRef, blockX, blockY, blockZ));
                refs.add(Block.Info.getInfo_chunkRef(test.blockRef, chunkIndex, localIndex));

                // WorldChunk
                refs.add(Block.Info.getInfo(test.worldChunk, blockCoords));
                refs.add(Block.Info.getInfo(test.worldChunk, blockX, blockY, blockZ));
                refs.add(Block.Info.getInfo(test.worldChunk, chunkIndex, localIndex));
                refs.add(Block.Info.getLocalInfo(test.worldChunk, blockCoords));
                refs.add(Block.Info.getLocalInfo(test.worldChunk, blockX, blockY, blockZ));
                refs.add(Block.Info.getLocalInfo(test.worldChunk, localIndex));

                // BlockComponentChunk
                refs.add(Block.Info.getInfo(test.blockComponentChunk, localIndex));
                refs.add(Block.Info.getLocalInfo(test.blockComponentChunk, blockCoords));
                refs.add(Block.Info.getLocalInfo(test.blockComponentChunk, blockX, blockY, blockZ));
                refs.add(Block.Info.getLocalInfo(test.blockComponentChunk, localIndex));

                // var i = 0;
                // for (var e : refs) {
                //     if (e == null) {
                //         console.log("" + i + ") null");
                //     } else {
                //         console.log("" + i + ") " + e);
                //     }
                //     i++;
                // }

                return refs;
            }

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

            /**
             * Getting BlockStateInfo (for dummies)
             *
             * WHAT YOU'LL NEED
             * - A BlockRef (Ref<ChunkStore)
             *
             * HOW TO GET IT
             * - See `Ref_` section
             *
             * So, basically, this entire family of functions just boils down to
             * "get me a block ref"
             *
             * Which, to do that, all you need is an implementation of any and
             * all methods in `Ref_`
             */

            //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//
            // TODO Blocks I believe operate under a different chunk store to
            // the world they're in? not sure. should test...
            //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//

            // #region Ref<ChunkStore> (BlockRef)

            @Nullable
            public static final BlockStateInfo getInfo_blockRef(
                @Nonnull final Ref<ChunkStore> blockRef,
                @Nonnull final Vector3i coords
            ) {
                return getInfo0(Block.Ref_.getRef_blockRef(blockRef, coords));
            }

            @Nullable
            public static final BlockStateInfo getInfo_blockRef(
                @Nonnull final Ref<ChunkStore> blockRef,
                final int x,
                final int y,
                final int z
            ) {
                return getInfo0(Block.Ref_.getRef_blockRef(blockRef, x, y, z));
            }

            @Nullable
            public static final BlockStateInfo getInfo_blockRef(
                @Nonnull final Ref<ChunkStore> blockRef,
                final long chunkIndex,
                final int blockIndex
            ) {
                return getInfo0(Block.Ref_.getRef_blockRef(blockRef, chunkIndex, blockIndex));
            }

            @Nullable
            public static final BlockStateInfo getInfo_blockRef(
                @Nonnull final Ref<ChunkStore> blockRef,
                final int blockIndex
            ) {
                return getInfo0(Block.Ref_.getLocalRef(blockRef, blockIndex));
            }

            // #endregion Ref<ChunkStore> (BlockRef)

            //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//
            // These almost directly have the chunk store to your world
            // -> they can easily get the store that has chunk your block is in
            //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//
            //
            // All require global coords for your block (either global block coords
            // or chunk identification and then local block coords/index)
            //

            // #region WorldProvider

            @Nullable
            public static final BlockStateInfo getInfo(
                @Nonnull final WorldProvider worldProvider,
                @Nonnull final Vector3i pos
            ) {
                // => BlockRef
                return getInfo0(Block.Ref_.getRef(worldProvider, pos));
            }

            @Nullable
            public static final BlockStateInfo getInfo(
                @Nonnull final WorldProvider worldProvider,
                final int x,
                final int y,
                final int z
            ) {
                // => BlockRef
                return getInfo0(Block.Ref_.getRef(worldProvider, x, y, z));
            }

            @Nullable
            public static final BlockStateInfo getInfo(
                @Nonnull final WorldProvider worldProvider,
                final long chunkIndex,
                final int blockIndex
            ) {
                // => BlockRef
                return getInfo0(Block.Ref_.getRef(worldProvider, chunkIndex, blockIndex));
            }

            // #endregion WorldProvider
            // #region World

            @Nullable
            public static final BlockStateInfo getInfo(@Nonnull final World world, @Nonnull final Vector3i pos) {
                // => BlockRef
                return getInfo0(Block.Ref_.getRef(world, pos));
            }

            @Nullable
            public static final BlockStateInfo getInfo(
                @Nonnull final World world,
                final int x,
                final int y,
                final int z
            ) {
                // => BlockRef
                return getInfo0(Block.Ref_.getRef(world, x, y, z));
            }

            @Nullable
            public static final BlockStateInfo getInfo(
                @Nonnull final World world,
                final long chunkIndex,
                final int blockIndex
            ) {
                // => BlockRef
                return getInfo0(Block.Ref_.getRef(world, chunkIndex, blockIndex));
            }

            // #endregion World
            // #region CommandBuffer

            @Nullable
            public static final BlockStateInfo getInfo(
                @Nonnull final CommandBuffer<ChunkStore> commandBuffer,
                @Nonnull final Vector3i pos
            ) {
                // => BlockRef
                return getInfo0(Block.Ref_.getRef(commandBuffer, pos));
            }

            @Nullable
            public static final BlockStateInfo getInfo(
                @Nonnull final CommandBuffer<ChunkStore> commandBuffer,
                final int x,
                final int y,
                final int z
            ) {
                // => BlockRef
                return getInfo0(Block.Ref_.getRef(commandBuffer, x, y, z));
            }

            @Nullable
            public static final BlockStateInfo getInfo(
                @Nonnull final CommandBuffer<ChunkStore> commandBuffer,
                final long chunkIndex,
                final int blockIndex
            ) {
                // => BlockRef
                return getInfo0(Block.Ref_.getRef(commandBuffer, chunkIndex, blockIndex));
            }

            // #endregion CommandBuffer
            // #region BlockStateInfo

            @Nullable
            public static final BlockStateInfo getInfo(
                @Nonnull final BlockStateInfo info,
                @Nonnull final Vector3i blockCoords
            ) {
                return getInfo0(Block.Ref_.getRef(info, blockCoords));
            }

            @Nullable
            public static final BlockStateInfo getInfo(
                @Nonnull final BlockStateInfo info,
                final int blockX,
                final int blockY,
                final int blockZ
            ) {
                return getInfo0(Block.Ref_.getRef(info, blockX, blockY, blockZ));
            }

            @Nullable
            public static final BlockStateInfo getInfo(
                @Nonnull final BlockStateInfo info,
                final long chunkIndex,
                final int blockIndex
            ) {
                return getInfo0(Block.Ref_.getRef(info, chunkIndex, blockIndex));
            }

            // #endregion BlockStateInfo
            // #region anyRef
            @Nullable
            public static final BlockStateInfo getInfo(
                @Nonnull final Ref<ChunkStore> ref,
                @Nonnull final Vector3i coords
            ) {
                // Potential: the ref you passed me is a BLOCK ref. slay. thats the good shit. that's what we're after
                final var info = Block.Info.getInfo(ref);
                if (info != null) {
                    return info;
                }

                // If there wasn't a block state info on your ref, we know its a chunk ref
                return getInfo_chunkRef(ref, coords);
            }

            @Nullable
            public static final BlockStateInfo getInfo(
                @Nonnull final Ref<ChunkStore> ref,
                final int blockX,
                final int blockY,
                final int blockZ
            ) {
                // Potential: the ref you passed me is a BLOCK ref. slay. thats the good shit. that's what we're after
                final var info = Block.Info.getInfo(ref);
                if (info != null) {
                    return info;
                }

                // If there wasn't a block state info on your ref, we know its a chunk ref
                return getInfo_chunkRef(ref, blockX, blockY, blockZ);
            }

            @Nullable
            public static final BlockStateInfo getInfo(
                @Nonnull final Ref<ChunkStore> ref,
                final long chunkIndex,
                final int blockIndex
            ) {
                // Potential: the ref you passed me is a BLOCK ref. slay. thats the good shit. that's what we're after
                final var info = Block.Info.getInfo(ref);
                if (info != null) {
                    return info;
                }

                // If there wasn't a block state info on your ref, we know its a chunk ref
                return getInfo_chunkRef(ref, chunkIndex, blockIndex);
            }

            // #endregion anyRef
            // #region Ref<ChunkStore> (ChunkRef)

            @Nullable
            public static final BlockStateInfo getInfo_chunkRef(
                @Nonnull final Ref<ChunkStore> chunkRef,
                @Nonnull final Vector3i coords
            ) {
                return getInfo0(Block.Ref_.getRef_chunkRef(chunkRef, coords));
            }

            @Nullable
            public static final BlockStateInfo getInfo_chunkRef(
                @Nonnull final Ref<ChunkStore> chunkRef,
                final int x,
                final int y,
                final int z
            ) {
                return getInfo0(Block.Ref_.getRef_chunkRef(chunkRef, x, y, z));
            }

            @Nullable
            public static final BlockStateInfo getInfo_chunkRef(
                @Nonnull final Ref<ChunkStore> chunkRef,
                final long chunkIndex,
                final int blockIndex
            ) {
                return getInfo0(Block.Ref_.getRef_chunkRef(chunkRef, chunkIndex, blockIndex));
            }

            // #endregion Ref<ChunkStore>
            // #region WorldChunk

            @Nullable
            public static final BlockStateInfo getInfo(
                @Nonnull final WorldChunk worldChunk,
                @Nonnull final Vector3i blockCoords
            ) {
                final var chunkRef = worldChunk.getReference();
                if (chunkRef == null) {
                    return null;
                }

                return getInfo0(Block.Ref_.getRef_chunkRef(chunkRef, blockCoords));
            }

            @Nullable
            public static final BlockStateInfo getInfo(
                @Nonnull final WorldChunk worldChunk,
                final int blockX,
                final int blockY,
                final int blockZ
            ) {
                final var chunkRef = worldChunk.getReference();
                if (chunkRef == null) {
                    return null;
                }
                return getInfo0(Block.Ref_.getRef_chunkRef(chunkRef, blockX, blockY, blockZ));
            }

            @Nullable
            public static final BlockStateInfo getInfo(
                @Nonnull final WorldChunk worldChunk,
                final long chunkIndex,
                final int blockIndex
            ) {
                final var chunkRef = worldChunk.getReference();
                if (chunkRef == null) {
                    return null;
                }
                return getInfo0(Block.Ref_.getRef_chunkRef(chunkRef, chunkIndex, blockIndex));
            }

            @Nullable
            public static final BlockStateInfo getLocalInfo(
                @Nonnull final WorldChunk worldChunk,
                @Nonnull final Vector3i blockCoords
            ) {
                return getInfo(worldChunk.getBlockComponentEntity(blockCoords.x, blockCoords.y, blockCoords.z));
            }

            @Nullable
            public static final BlockStateInfo getLocalInfo(
                @Nonnull final WorldChunk worldChunk,
                final int blockX,
                final int blockY,
                final int blockZ
            ) {
                return getInfo(worldChunk.getBlockComponentEntity(blockX, blockY, blockZ));
            }

            @Nullable
            public static final BlockStateInfo getLocalInfo(
                @Nonnull final WorldChunk worldChunk,
                final int blockIndex
            ) {
                final var localCoords = BlockCoords.Local.getLocalCoords(blockIndex);
                return getInfo(worldChunk.getBlockComponentEntity(localCoords.x, localCoords.y, localCoords.z));
            }

            // #endregion WorldChunk
            // #region BlockComponentChunk

            @Nullable
            public static final BlockStateInfo getInfo(
                @Nonnull final BlockComponentChunk blockComponentChunk,
                final int blockIndex
            ) {
                return blockComponentChunk.getComponent(blockIndex, BLOCK_STATE_INFO_COMPONENT);
            }

            @Nullable
            public static final BlockStateInfo getLocalInfo(
                @Nonnull final BlockComponentChunk blockComponentChunk,
                @Nonnull final Vector3i blockCoords
            ) {
                final int blockIndex = BlockCoords.Index.getLocalIndex(blockCoords);
                return blockComponentChunk.getComponent(blockIndex, BLOCK_STATE_INFO_COMPONENT);
            }

            @Nullable
            public static final BlockStateInfo getLocalInfo(
                @Nonnull final BlockComponentChunk blockComponentChunk,
                final int blockX,
                final int blockY,
                final int blockZ
            ) {
                final int blockIndex = BlockCoords.Index.getLocalIndex(blockX, blockY, blockZ);
                return blockComponentChunk.getComponent(blockIndex, BLOCK_STATE_INFO_COMPONENT);
            }

            @Nullable
            public static final BlockStateInfo getLocalInfo(
                @Nonnull final BlockComponentChunk blockComponentChunk,
                final int blockIndex
            ) {
                return blockComponentChunk.getComponent(blockIndex, BLOCK_STATE_INFO_COMPONENT);
            }

            // #endregion BlockComponentChunk

            //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//
            // These ARE the chunk store to your world
            // -> they can easily get the chunk your block is in
            //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//
            //
            // All require global coords for your block (either global block coords
            // or chunk identification and then local block coords/index)
            //

            // #region Store<ChunkStore>

            @Nullable
            public static final BlockStateInfo getInfo(
                @Nonnull final Store<ChunkStore> store,
                @Nonnull final Vector3i pos
            ) {
                // => BlockRef
                return getInfo0(Block.Ref_.getRef(store, pos));
            }

            @Nullable
            public static final BlockStateInfo getInfo(
                @Nonnull final Store<ChunkStore> store,
                final int x,
                final int y,
                final int z
            ) {
                // => BlockRef
                return getInfo0(Block.Ref_.getRef(store, x, y, z));
            }

            @Nullable
            public static final BlockStateInfo getInfo(
                @Nonnull final Store<ChunkStore> store,
                final long chunkIndex,
                final int blockIndex
            ) {
                // => BlockRef
                return getInfo0(Block.Ref_.getRef(store, chunkIndex, blockIndex));
            }

            // #endregion Store<ChunkStore>
            // #region ChunkStore

            @Nullable
            public static final BlockStateInfo getInfo(
                @Nonnull final ChunkStore chunkStore,
                @Nonnull final Vector3i pos
            ) {
                // => BlockRef
                return getInfo0(Block.Ref_.getRef(chunkStore, pos));
            }

            @Nullable
            public static final BlockStateInfo getInfo(
                @Nonnull final ChunkStore chunkStore,
                final int x,
                final int y,
                final int z
            ) {
                // => BlockRef
                return getInfo0(Block.Ref_.getRef(chunkStore, x, y, z));
            }

            @Nullable
            public static final BlockStateInfo getInfo(
                @Nonnull final ChunkStore chunkStore,
                final long chunkIndex,
                final int blockIndex
            ) {
                // => BlockRef
                return getInfo0(Block.Ref_.getRef(chunkStore, chunkIndex, blockIndex));
            }

            // #endregion ChunkStore

            //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//
            // The below methods allow you to access a block ref WITHIN
            // THE SAME CHUNK (we have your chunk by now).
            //
            // They do NOT allow global access to blocks in any other chunk.
            // Equally, they're basically the endpoint for all fetching components
            //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//
            //
            // All require global coords for your block (if you're using coordinates)
            // OR the block index. I really didn't want to dupe methods for both
            // global coords (x,y,z) and local coords (x,y,z) since, that seems
            // more confusing than useful
            //

            // #region localChunkRef

            // ChunkRef => component ON the chunk itself => block ref in the chunk
            @Nullable
            public static final BlockStateInfo getLocalInfo_chunkRef(
                @Nonnull final Ref<ChunkStore> chunkRef,
                @Nonnull final Vector3i blockCoords
            ) {
                return getInfo0(Block.Ref_.getLocalRef_chunkRef(chunkRef, blockCoords));
            }

            // ChunkRef => component ON the chunk itself => block ref in the chunk
            @Nullable
            public static final BlockStateInfo getLocalInfo_chunkRef(
                @Nonnull final Ref<ChunkStore> chunkRef,
                final int blockX,
                final int blockY,
                final int blockZ
            ) {
                return getInfo0(Block.Ref_.getLocalRef_chunkRef(chunkRef, blockX, blockY, blockZ));
            }

            // ChunkRef => component ON the chunk itself => block ref in the chunk
            @Nullable
            public static final BlockStateInfo getLocalInfo_chunkRef(
                @Nonnull final Ref<ChunkStore> chunkRef,
                final int blockIndex
            ) {
                return getInfo0(Block.Ref_.getLocalRef_chunkRef(chunkRef, blockIndex));
            }

            // #endregion localChunkRef
            // #region localBlockRef

            @Nullable
            public static final BlockStateInfo getLocalInfo_blockRef(
                @Nonnull final Ref<ChunkStore> blockRef,
                @Nonnull final Vector3i blockCoords
            ) {
                return getInfo0(Block.Ref_.getLocalRef_blockRef(blockRef, blockCoords));
            }

            // ChunkRef => component ON the chunk itself => block ref in the chunk
            @Nullable
            public static final BlockStateInfo getLocalInfo_blockRef(
                @Nonnull final Ref<ChunkStore> blockRef,
                final int blockX,
                final int blockY,
                final int blockZ
            ) {
                return getInfo0(Block.Ref_.getLocalRef_blockRef(blockRef, blockX, blockY, blockZ));
            }

            // ChunkRef => component ON the chunk itself => block ref in the chunk
            @Nullable
            public static final BlockStateInfo getLocalInfo_blockRef(
                @Nonnull final Ref<ChunkStore> blockRef,
                final int blockIndex
            ) {
                return getInfo0(Block.Ref_.getLocalRef_blockRef(blockRef, blockIndex));
            }

            // #endregion localBlockRef
            // #region anyLocalRef

            @Nullable
            public static final BlockStateInfo getLocalInfo(
                @Nonnull final Ref<ChunkStore> ref,
                @Nonnull final Vector3i blockCoords
            ) {
                if (isBlockRef(ref)) {
                    return getLocalInfo_blockRef(ref, blockCoords);
                } else {
                    return getLocalInfo_chunkRef(ref, blockCoords);
                }
            }

            // ChunkRef => component ON the chunk itself => block ref in the chunk
            @Nullable
            public static final BlockStateInfo getLocalInfo(
                @Nonnull final Ref<ChunkStore> ref,
                final int blockX,
                final int blockY,
                final int blockZ
            ) {
                if (isBlockRef(ref)) {
                    return getLocalInfo_blockRef(ref, blockX, blockY, blockZ);
                } else {
                    return getLocalInfo_chunkRef(ref, blockX, blockY, blockZ);
                }
            }

            // ChunkRef => component ON the chunk itself => block ref in the chunk
            @Nullable
            public static final BlockStateInfo getLocalInfo(@Nonnull final Ref<ChunkStore> ref, final int blockIndex) {
                if (isBlockRef(ref)) {
                    return getLocalInfo_blockRef(ref, blockIndex);
                } else {
                    return getLocalInfo_chunkRef(ref, blockIndex);
                }
            }

            // #endregion anyLocalRef
            // #region localBlockStateInfo

            /**
             * Returns another block based on its local coords WITHIN THE SAME CHUNK
             */
            // BlockInfo => ChunkRef => component ON the chunk itself => block ref in the chunk
            @Nullable
            public static final BlockStateInfo getLocalInfo(
                @Nonnull final BlockStateInfo info,
                @Nonnull final Vector3i blockCoords
            ) {
                return getInfo0(Block.Ref_.getLocalRef(info, blockCoords));
            }

            /**
             * Returns another block based on its local coords WITHIN THE SAME CHUNK
             */
            // BlockInfo => ChunkRef => component ON the chunk itself => block ref in the chunk
            @Nullable
            public static final BlockStateInfo getLocalInfo(
                @Nonnull final BlockStateInfo info,
                final int blockX,
                final int blockY,
                final int blockZ
            ) {
                return getInfo0(Block.Ref_.getLocalRef(info, blockX, blockY, blockZ));
            }

            /**
             * Returns another block based on its local coords WITHIN THE SAME CHUNK
             */
            // BlockInfo => ChunkRef => component ON the chunk itself => block ref in the chunk
            @Nullable
            public static final BlockStateInfo getLocalInfo(@Nonnull final BlockStateInfo info, final int blockIndex) {
                return getInfo0(Block.Ref_.getLocalRef(info, blockIndex));
            }

            // #endregion localBlockStateInfo

            //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//
            // ACTUAL INFO GETTING CODE ARRIVES BELOW HERE
            //
            // yup, all that above is literally just a wrapper around "ok but
            // get me the block ref"
            //
            // because yup, you guessed it, we need a block ref and a block ref
            // alone to get components on a block ref...
            //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//
            //
            // Couldn't be easier
            // Remember: you can get other components on your block entity
            //           simply through the ref
            // And:      BlockStateInfo is a component (if you didn't know).
            //           I'm not certain, but pretty sure it's always like ALWAYS
            //           inherently on all block entities
            //

            // #region BlockRef

            // BlockRef
            @Nullable
            public static final BlockStateInfo getInfo(@Nullable final Ref<ChunkStore> blockRef) {
                return getInfo0(blockRef);
            }

            // BlockRef
            @Nullable
            private static final BlockStateInfo getInfo0(@Nullable final Ref<ChunkStore> blockRef) {
                if (blockRef == null) {
                    return null;
                }

                return Component_.get(blockRef, BLOCK_STATE_INFO_COMPONENT);
            }
            // #endregion BlockRef

            // TODO multiple endpoints:
            // - blockRef + store => store gets OTHER component on entity with ref blockRef
            // - blockChunk -> gets the componnet on a block (doesn't require a ref!!, just the local coords of the block)

            // #endregion getInfo
        }

        // TESTS ADDED AND VERIFIED
        public static final class Id {

            /**
             * Tests all methods i've defined for getId
             */
            @Nonnull
            public static final ArrayList<Integer> test(
                @Nonnull final Ref<ChunkStore> blockRef,
                @Nonnull final WorldChunk worldChunk,
                @Nonnull final CommandBuffer<ChunkStore> commandBuffer,
                @Nonnull final Vector3i providedCoords
            ) {
                final var blockX = providedCoords.x;
                final var blockY = providedCoords.y;
                final var blockZ = providedCoords.z;
                final var localIndex = BlockCoords.Index.getLocalIndex(blockX, blockY, blockZ);
                final var blockIndex = localIndex;
                final var blockCoords = new Vector3i(blockX, blockY, blockZ);
                final var chunkIndex = ChunkCoords.Index.getChunkIndex(blockCoords);

                final var test = new TestUtil(commandBuffer, blockCoords);

                // functions to test
                final ArrayList<Integer> refs = new ArrayList<>();

                // WorldProvider
                refs.add(Block.Id.getId(test.worldProvider, blockCoords));
                refs.add(Block.Id.getId(test.worldProvider, blockCoords.x, blockCoords.y, blockCoords.z));
                refs.add(Block.Id.getId(test.worldProvider, chunkIndex, blockIndex));

                //  World
                refs.add(Block.Id.getId(test.world, blockCoords));
                refs.add(Block.Id.getId(test.world, blockCoords.x, blockCoords.y, blockCoords.z));
                refs.add(Block.Id.getId(test.world, chunkIndex, blockIndex));

                //  CommandBuffer
                refs.add(Block.Id.getId(test.commandBuffer, blockCoords));
                refs.add(Block.Id.getId(test.commandBuffer, blockCoords.x, blockCoords.y, blockCoords.z)); // test 7 failed
                refs.add(Block.Id.getId(test.commandBuffer, chunkIndex, blockIndex));

                //  Store<ChunkStore>
                refs.add(Block.Id.getId(test.store, blockCoords));
                refs.add(Block.Id.getId(test.store, blockCoords.x, blockCoords.y, blockCoords.z)); // test 10 failed
                refs.add(Block.Id.getId(test.store, chunkIndex, blockIndex));

                //  ChunkStore
                refs.add(Block.Id.getId(test.chunkStore, blockCoords));
                refs.add(Block.Id.getId(test.chunkStore, blockCoords.x, blockCoords.y, blockCoords.z)); // test 13 failed
                refs.add(Block.Id.getId(test.chunkStore, chunkIndex, blockIndex));

                //  Ref<ChunkStore>
                refs.add(Block.Id.getId(test.chunkRef, blockCoords));
                refs.add(Block.Id.getId(test.chunkRef, blockCoords.x, blockCoords.y, blockCoords.z)); // test 16 failed
                refs.add(Block.Id.getId(test.chunkRef, chunkIndex, blockIndex));
                refs.add(Block.Id.getId("TEST_BlockId"));

                return refs;
            }

            // ====================================================================
            // Option 1: Getting the ID of a block by its coords
            // - there's a certain coordinate at which exists a block. You want
            //   to know what type of block it is (its ID)
            // ====================================================================

            // #region WorldProvider
            // ====================================================================
            // WorldProvider  =>  ChunkStore
            // + GlobalCoords
            // ====================================================================

            @Nullable
            public static final Integer getId(
                @Nonnull final WorldProvider worldProvider,
                @Nonnull final Vector3i blockCoords
            ) {
                final var worldChunk = worldProvider.getWorld().getChunk(ChunkCoords.Index.getChunkIndex(blockCoords));
                if (worldChunk == null) {
                    return null;
                }

                return worldChunk.getBlock(blockCoords);
            }

            @Nullable
            public static final Integer getId(
                @Nonnull final WorldProvider worldProvider,
                final int blockX,
                final int blockY,
                final int blockZ
            ) {
                final var worldChunk = worldProvider
                    .getWorld()
                    .getChunk(ChunkCoords.Index.getChunkIndex(blockX, blockZ));
                if (worldChunk == null) {
                    return null;
                }

                return worldChunk.getBlock(blockX, blockY, blockZ);
            }

            @Nullable
            public static final Integer getId(
                @Nonnull final WorldProvider worldProvider,
                final long chunkIndex,
                final int blockIndex
            ) {
                final var worldChunk = worldProvider.getWorld().getChunk(chunkIndex);
                if (worldChunk == null) {
                    return null;
                }

                return worldChunk.getBlock(BlockCoords.Local.getLocalCoords(blockIndex));
            }

            // #endregion WorldProvider
            // #region World
            // ====================================================================
            // World  =>  ChunkStore
            // + Global coords
            // ====================================================================

            @Nullable
            public static final Integer getId(@Nonnull final World world, @Nonnull final Vector3i blockCoords) {
                final var worldChunk = world.getChunk(ChunkCoords.Index.getChunkIndex(blockCoords));
                if (worldChunk == null) {
                    return null;
                }

                return worldChunk.getBlock(blockCoords);
            }

            @Nullable
            public static final Integer getId(
                @Nonnull final World world,
                final int blockX,
                final int blockY,
                final int blockZ
            ) {
                final var worldChunk = world.getChunk(ChunkCoords.Index.getChunkIndex(blockX, blockZ));
                if (worldChunk == null) {
                    return null;
                }

                return worldChunk.getBlock(blockX, blockY, blockZ);
            }

            @Nullable
            public static final Integer getId(@Nonnull final World world, final long chunkIndex, final int blockIndex) {
                final var worldChunk = world.getChunk(chunkIndex);
                if (worldChunk == null) {
                    return null;
                }

                return worldChunk.getBlock(BlockCoords.Local.getLocalCoords(blockIndex));
            }

            // #endregion World
            // #region CommandBuffer
            // ====================================================================
            // CommandBuffer  =>  ChunkStore
            // + Global coords
            // ====================================================================

            @Nullable
            public static final Integer getId(
                @Nonnull final CommandBuffer<ChunkStore> commandBuffer,
                @Nonnull final Vector3i blockCoords
            ) {
                final var worldChunk = Chunk.WorldChunk_.getWorldChunk(commandBuffer, blockCoords);
                if (worldChunk == null) {
                    return null;
                }

                return worldChunk.getBlock(blockCoords);
            }

            @Nullable
            public static final Integer getId(
                @Nonnull final CommandBuffer<ChunkStore> commandBuffer,
                final int blockX,
                final int blockY,
                final int blockZ
            ) {
                final var worldChunk = Chunk.WorldChunk_.getWorldChunk(commandBuffer, blockX, blockZ);
                if (worldChunk == null) {
                    return null;
                }

                return worldChunk.getBlock(blockX, blockY, blockZ);
            }

            @Nullable
            public static final Integer getId(
                @Nonnull final CommandBuffer<ChunkStore> commandBuffer,
                final long chunkIndex,
                final int blockIndex
            ) {
                final var worldChunk = Chunk.WorldChunk_.getWorldChunk(commandBuffer, chunkIndex);
                if (worldChunk == null) {
                    return null;
                }

                return worldChunk.getBlock(BlockCoords.Local.getLocalCoords(blockIndex));
            }

            // #endregion CommandBuffer
            // #region Store<ChunkStore>
            // ====================================================================
            // Store<ChunkStore>  =>  ChunkStore
            // + Global coords
            // ====================================================================

            @Nullable
            public static final Integer getId(
                @Nonnull final Store<ChunkStore> store,
                @Nonnull final Vector3i blockCoords
            ) {
                final var worldChunk = Chunk.WorldChunk_.getWorldChunk(store, blockCoords);
                if (worldChunk == null) {
                    return null;
                }

                return worldChunk.getBlock(blockCoords);
            }

            @Nullable
            public static final Integer getId(
                @Nonnull final Store<ChunkStore> store,
                final int blockX,
                final int blockY,
                final int blockZ
            ) {
                final var worldChunk = Chunk.WorldChunk_.getWorldChunk(store, blockX, blockZ);
                if (worldChunk == null) {
                    return null;
                }

                return worldChunk.getBlock(blockX, blockY, blockZ);
            }

            @Nullable
            public static final Integer getId(
                @Nonnull final Store<ChunkStore> store,
                final long chunkIndex,
                final int blockIndex
            ) {
                final var worldChunk = Chunk.WorldChunk_.getWorldChunk(store, chunkIndex);
                if (worldChunk == null) {
                    return null;
                }

                return worldChunk.getBlock(BlockCoords.Local.getLocalCoords(blockIndex));
            }

            // #endregion Store<ChunkStore>
            // #region ChunkStore
            // ====================================================================
            // ChunkStore  =>  ChunkRef
            // + Global coords OR Chunk Coords
            // ====================================================================

            @Nullable
            public static final Integer getId(
                @Nonnull final ChunkStore chunkStore,
                @Nonnull final Vector3i blockCoords
            ) {
                final var worldChunk = Chunk.WorldChunk_.getWorldChunk(chunkStore, blockCoords);
                if (worldChunk == null) {
                    return null;
                }

                return worldChunk.getBlock(blockCoords);
            }

            @Nullable
            public static final Integer getId(
                @Nonnull final ChunkStore chunkStore,
                final int blockX,
                final int blockY,
                final int blockZ
            ) {
                final var worldChunk = Chunk.WorldChunk_.getWorldChunk(chunkStore, blockX, blockZ);
                if (worldChunk == null) {
                    return null;
                }

                return worldChunk.getBlock(blockX, blockY, blockZ);
            }

            @Nullable
            public static final Integer getId(
                @Nonnull final ChunkStore chunkStore,
                final long chunkIndex,
                final int blockIndex
            ) {
                final var worldChunk = Chunk.WorldChunk_.getWorldChunk(chunkStore, chunkIndex);
                if (worldChunk == null) {
                    return null;
                }

                return worldChunk.getBlock(BlockCoords.Local.getLocalCoords(blockIndex));
            }

            // #endregion ChunkStore
            // #region Ref<ChunkStore>
            // ====================================================================
            // Ref<ChunkStore> (ChunkRef)  =>  BlockComponentChunk
            // + Global OR Local coords
            // ====================================================================

            @Nullable
            public static final Integer getId(
                @Nonnull final Ref<ChunkStore> chunkRef,
                @Nonnull final Vector3i blockCoords
            ) {
                final var worldChunk = Chunk.WorldChunk_.getWorldChunk(chunkRef, blockCoords);
                if (worldChunk == null) {
                    return null;
                }

                return worldChunk.getBlock(blockCoords);
            }

            @Nullable
            public static final Integer getId(
                @Nonnull final Ref<ChunkStore> chunkRef,
                final int blockX,
                final int blockY,
                final int blockZ
            ) {
                final var worldChunk = Chunk.WorldChunk_.getWorldChunk(chunkRef, blockX, blockZ);
                if (worldChunk == null) {
                    return null;
                }

                return worldChunk.getBlock(blockX, blockY, blockZ);
            }

            @Nullable
            public static final Integer getId(
                @Nonnull final Ref<ChunkStore> chunkRef,
                final long chunkIndex,
                final int blockIndex
            ) {
                final var worldChunk = Chunk.WorldChunk_.getWorldChunk(chunkRef, chunkIndex);
                if (worldChunk == null) {
                    return null;
                }

                return worldChunk.getBlock(BlockCoords.Local.getLocalCoords(blockIndex));
            }

            // #endregion Ref<ChunkStore>

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

        // TESTS ADDED AND VERIFIED
        // Not even sure what BlockType is or what i was using it for
        public static final class Type {

            /**
             * Tests all methods i've defined for getType
             */
            @Nonnull
            public static final ArrayList<BlockType> test(
                @Nonnull final Ref<ChunkStore> blockRef,
                @Nonnull final WorldChunk worldChunk,
                @Nonnull final CommandBuffer<ChunkStore> commandBuffer,
                @Nonnull final Vector3i providedCoords
            ) {
                final ArrayList<BlockType> refs = new ArrayList<>();

                refs.add(Block.Type.getType("TEST_BlockType"));

                return refs;
            }

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

        // TESTS ADDED AND VERIFIED
        public static final boolean set(final @Nonnull World world, final @Nonnull Vector3i blockCoords, int blockId) {
            var chunk = Chunk.WorldChunk_.getWorldChunk(world, blockCoords);
            if (chunk == null) {
                return false;
            }

            return set0(chunk, blockCoords, blockId);
        }

        // TESTS ADDED AND VERIFIED
        public static final boolean set0(
            final @Nonnull WorldChunk chunk,
            final @Nonnull Vector3i blockCoords,
            int blockId
        ) {
            return chunk.setBlock(blockCoords.x, blockCoords.y, blockCoords.z, blockId);
        }

        // TESTS ADDED AND VERIFIED
        public static final int get(final @Nonnull World world, final @Nonnull Vector3i blockCoords) {
            var chunk = Chunk.WorldChunk_.getWorldChunk(world, blockCoords);
            if (chunk == null) {
                return -1;
            }

            return get0(chunk, blockCoords);
        }

        // TESTS ADDED AND VERIFIED
        public static final int get0(final @Nonnull WorldChunk chunk, final @Nonnull Vector3i blockCoords) {
            return chunk.getBlock(blockCoords);
        }
    }

    // TESTS ADDED AND VERIFIED
    public static final class Chunk {

        // TESTS ADDED AND VERIFIED
        /// -> get WorldChunk
        public static final class WorldChunk_ {

            /**
             * Tests all methods i've defined for getWorldChunk
             */
            @Nonnull
            public static final ArrayList<WorldChunk> test(
                @Nonnull final Ref<ChunkStore> blockRef,
                @Nonnull final WorldChunk worldChunk,
                @Nonnull final CommandBuffer<ChunkStore> commandBuffer,
                @Nonnull final Vector3i providedCoords
            ) {
                final var blockX = providedCoords.x;
                final var blockY = providedCoords.y;
                final var blockZ = providedCoords.z;
                // final var localIndex = BlockCoords.Index.getLocalIndex(blockX, blockY, blockZ);
                // final var localCoords = BlockCoords.Local.getLocalCoords(localIndex);
                final var blockCoords = new Vector3i(blockX, blockY, blockZ);
                final var chunkIndex = ChunkCoords.Index.getChunkIndex(blockCoords);
                final var chunkCoords = ChunkCoords.Global.getChunkCoords(blockCoords);
                final var chunkX = chunkCoords.x;
                final var chunkZ = chunkCoords.z;

                final var test = new TestUtil(commandBuffer, blockCoords);
                // functions to test
                final ArrayList<WorldChunk> refs = new ArrayList<>();

                // getWorldChunk of a given item directly (super easy minimal version)
                refs.add(Chunk.WorldChunk_.getWorldChunk_chunkRef(test.chunkRef));
                refs.add(Chunk.WorldChunk_.getWorldChunk_blockRef(test.blockRef));
                // - with: block ref (auto)
                refs.add(Chunk.WorldChunk_.getWorldChunk(blockRef));
                // - with: chunk ref (auto)
                refs.add(Chunk.WorldChunk_.getWorldChunk(test.chunkRef));
                refs.add(Chunk.WorldChunk_.getWorldChunk(test.info));

                // WorldProvider
                refs.add(Chunk.WorldChunk_.getWorldChunk(test.worldProvider, test.blockChunk));
                refs.add(Chunk.WorldChunk_.getWorldChunk(test.worldProvider, blockX, blockZ));
                refs.add(Chunk.WorldChunk_.getWorldChunk(test.worldProvider, chunkIndex));
                refs.add(Chunk.WorldChunk_.getWorldChunk(test.worldProvider, blockCoords));
                refs.add(Chunk.WorldChunk_.getWorldChunk_chunkCoords(test.worldProvider, chunkX, chunkZ));

                // World
                refs.add(Chunk.WorldChunk_.getWorldChunk(test.world, test.blockChunk));
                refs.add(Chunk.WorldChunk_.getWorldChunk(test.world, blockX, blockZ));
                refs.add(Chunk.WorldChunk_.getWorldChunk(test.world, chunkIndex));
                refs.add(Chunk.WorldChunk_.getWorldChunk(test.world, blockCoords));
                refs.add(Chunk.WorldChunk_.getWorldChunk_chunkCoords(test.world, chunkX, chunkZ));

                // BlockRef
                refs.add(Chunk.WorldChunk_.getWorldChunk(blockRef, test.blockChunk));
                refs.add(Chunk.WorldChunk_.getWorldChunk(blockRef, blockX, blockZ));
                refs.add(Chunk.WorldChunk_.getWorldChunk(blockRef, chunkIndex));
                refs.add(Chunk.WorldChunk_.getWorldChunk(blockRef, blockCoords));
                refs.add(Chunk.WorldChunk_.getWorldChunk_chunkCoords(blockRef, chunkX, chunkZ));

                // Info
                refs.add(Chunk.WorldChunk_.getWorldChunk(test.info, test.blockChunk));
                refs.add(Chunk.WorldChunk_.getWorldChunk(test.info, blockX, blockZ));
                refs.add(Chunk.WorldChunk_.getWorldChunk(test.info, chunkIndex));
                refs.add(Chunk.WorldChunk_.getWorldChunk(test.info, blockCoords));
                refs.add(Chunk.WorldChunk_.getWorldChunk_chunkCoords(test.info, chunkX, chunkZ));

                // CommandBuffer
                refs.add(Chunk.WorldChunk_.getWorldChunk(test.commandBuffer, test.blockChunk));
                refs.add(Chunk.WorldChunk_.getWorldChunk(test.commandBuffer, blockX, blockZ));
                refs.add(Chunk.WorldChunk_.getWorldChunk(test.commandBuffer, chunkIndex));
                refs.add(Chunk.WorldChunk_.getWorldChunk(test.commandBuffer, blockCoords));
                refs.add(Chunk.WorldChunk_.getWorldChunk_chunkCoords(test.commandBuffer, chunkX, chunkZ));

                // Store
                refs.add(Chunk.WorldChunk_.getWorldChunk(test.store, test.blockChunk));
                refs.add(Chunk.WorldChunk_.getWorldChunk(test.store, blockX, blockZ));
                refs.add(Chunk.WorldChunk_.getWorldChunk(test.store, chunkIndex));
                refs.add(Chunk.WorldChunk_.getWorldChunk(test.store, blockCoords));
                refs.add(Chunk.WorldChunk_.getWorldChunk_chunkCoords(test.store, chunkX, chunkZ));

                // ChunKStore
                refs.add(Chunk.WorldChunk_.getWorldChunk(test.chunkStore, test.blockChunk));
                refs.add(Chunk.WorldChunk_.getWorldChunk(test.chunkStore, blockX, blockZ));
                refs.add(Chunk.WorldChunk_.getWorldChunk(test.chunkStore, chunkIndex));
                refs.add(Chunk.WorldChunk_.getWorldChunk(test.chunkStore, blockCoords));
                refs.add(Chunk.WorldChunk_.getWorldChunk_chunkCoords(test.chunkStore, chunkX, chunkZ));

                // final test -> does it matter really if we pass a chunk ref or a block ref?
                // update: NO HECKING WAY, it doesn't. wild.
                refs.add(Chunk.WorldChunk_.getWorldChunk(blockRef, chunkIndex));
                refs.add(Chunk.WorldChunk_.getWorldChunk(test.chunkRef, chunkIndex));

                return refs;
            }

            // #region getWorldChunk

            @Nullable
            public static final WorldChunk getWorldChunk_chunkRef(@Nonnull final Ref<ChunkStore> chunkRef) {
                return Component_.get(chunkRef, WORLD_CHUNK_COMPONENT);
            }

            @Nullable
            public static final WorldChunk getWorldChunk_blockRef(@Nonnull final Ref<ChunkStore> blockRef) {
                final var info = Block.Info.getInfo(blockRef);
                if (info == null) {
                    return null;
                }

                return Component_.get(info.getChunkRef(), WORLD_CHUNK_COMPONENT);
            }

            @Nullable
            public static final WorldChunk getWorldChunk(@Nonnull final Ref<ChunkStore> ref) {
                // Potential 1: The ref you passed me is a CHUNK ref. slay. thats the good shit. that's what we're after
                var worldChunk = Component_.get(ref, WORLD_CHUNK_COMPONENT);
                if (worldChunk != null) {
                    return worldChunk;
                }

                // Potential 2: it's a block, otherwise i've got no clue what's going on
                return getWorldChunk_blockRef(ref);
            }

            @Nullable
            public static final WorldChunk getWorldChunk(@Nonnull final BlockStateInfo info) {
                return Component_.get(info.getChunkRef(), WORLD_CHUNK_COMPONENT);
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
                return Chunk.WorldChunk_.getWorldChunk(worldProvider, blockChunk.getIndex());
            }

            @Nullable
            public static final WorldChunk getWorldChunk(
                @Nonnull final WorldProvider worldProvider,
                final int blockX,
                final int blockZ
            ) {
                return Chunk.WorldChunk_.getWorldChunk(worldProvider, ChunkCoords.Index.getChunkIndex(blockX, blockZ));
            }

            @Nullable
            public static final WorldChunk getWorldChunk(
                @Nonnull final WorldProvider worldProvider,
                @Nonnull final Vector3i blockCoords
            ) {
                return Chunk.WorldChunk_.getWorldChunk(worldProvider, ChunkCoords.Index.getChunkIndex(blockCoords));
            }

            @Nullable
            public static final WorldChunk getWorldChunk_chunkCoords(
                @Nonnull final WorldProvider worldProvider,
                final int chunkX,
                final int chunkZ
            ) {
                return Chunk.WorldChunk_.getWorldChunk(
                    worldProvider,
                    ChunkCoords.Index.getChunkIndex_chunkCoords(chunkX, chunkZ)
                );
            }

            @Nullable
            public static final WorldChunk getWorldChunk(
                @Nonnull final WorldProvider worldProvider,
                final long chunkIndex
            ) {
                return worldProvider.getWorld().getChunk(chunkIndex);
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
                return Chunk.WorldChunk_.getWorldChunk(world, blockChunk.getIndex());
            }

            @Nullable
            public static final WorldChunk getWorldChunk(
                @Nonnull final World world,
                final int blockX,
                final int blockZ
            ) {
                return Chunk.WorldChunk_.getWorldChunk(world, ChunkCoords.Index.getChunkIndex(blockX, blockZ));
            }

            @Nullable
            public static final WorldChunk getWorldChunk(
                @Nonnull final World world,
                @Nonnull final Vector3i blockCoords
            ) {
                return Chunk.WorldChunk_.getWorldChunk(world, ChunkCoords.Index.getChunkIndex(blockCoords));
            }

            @Nullable
            public static final WorldChunk getWorldChunk_chunkCoords(
                @Nonnull final World world,
                final int chunkX,
                final int chunkZ
            ) {
                return Chunk.WorldChunk_.getWorldChunk(
                    world,
                    ChunkCoords.Index.getChunkIndex_chunkCoords(chunkX, chunkZ)
                );
            }

            @Nullable
            public static final WorldChunk getWorldChunk(@Nonnull final World world, final long chunkIndex) {
                return world.getChunk(chunkIndex);
            }

            //   #endregion World
            //   #region Ref<ChunkStore>
            //   ==================================================================
            //   Ref<ChunkStore>      -> WorldChunk
            //   ==================================================================

            @Nullable
            public static final WorldChunk getWorldChunk(
                @Nonnull final Ref<ChunkStore> anyRef,
                @Nonnull final BlockChunk blockChunk
            ) {
                return Chunk.WorldChunk_.getWorldChunk(anyRef, blockChunk.getIndex());
            }

            @Nullable
            public static final WorldChunk getWorldChunk(
                @Nonnull final Ref<ChunkStore> anyRef,
                final int blockX,
                final int blockZ
            ) {
                return Chunk.WorldChunk_.getWorldChunk(anyRef, ChunkCoords.Index.getChunkIndex(blockX, blockZ));
            }

            @Nullable
            public static final WorldChunk getWorldChunk(
                @Nonnull final Ref<ChunkStore> anyRef,
                @Nonnull final Vector3i blockCoords
            ) {
                return Chunk.WorldChunk_.getWorldChunk(anyRef, ChunkCoords.Index.getChunkIndex(blockCoords));
            }

            @Nullable
            public static final WorldChunk getWorldChunk_chunkCoords(
                @Nonnull final Ref<ChunkStore> anyRef,
                final int chunkX,
                final int chunkZ
            ) {
                return Chunk.WorldChunk_.getWorldChunk(
                    anyRef,
                    ChunkCoords.Index.getChunkIndex_chunkCoords(chunkX, chunkZ)
                );
            }

            @Nullable
            public static final WorldChunk getWorldChunk(@Nonnull final Ref<ChunkStore> anyRef, final long chunkIndex) {
                // TODO use this EVERYWHERE (oh my GOD this seems useful)
                return anyRef.getStore().getExternalData().getChunkComponent(chunkIndex, WORLD_CHUNK_COMPONENT);
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
                return Chunk.WorldChunk_.getWorldChunk(info, blockChunk.getIndex());
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
                final int blockX,
                final int blockZ
            ) {
                return Chunk.WorldChunk_.getWorldChunk(info, ChunkCoords.Index.getChunkIndex(blockX, blockZ));
            }

            @Nullable
            public static final WorldChunk getWorldChunk(
                @Nonnull final BlockStateInfo info,
                @Nonnull final Vector3i blockCoords
            ) {
                return Chunk.WorldChunk_.getWorldChunk(info, ChunkCoords.Index.getChunkIndex(blockCoords));
            }

            @Nullable
            public static final WorldChunk getWorldChunk_chunkCoords(
                @Nonnull final BlockStateInfo info,
                final int chunkX,
                final int chunkZ
            ) {
                return Chunk.WorldChunk_.getWorldChunk(
                    info,
                    ChunkCoords.Index.getChunkIndex_chunkCoords(chunkX, chunkZ)
                );
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
                return Chunk.WorldChunk_.getWorldChunk(commandBuffer, blockChunk.getIndex());
            }

            @Nullable
            public static final WorldChunk getWorldChunk(
                @Nonnull final CommandBuffer<ChunkStore> commandBuffer, // << Doesn't need to get the world, it can access components directly
                final int blockX,
                final int blockZ
            ) {
                // but regardless we don't need the world to get the world chunk
                return Chunk.WorldChunk_.getWorldChunk(commandBuffer, ChunkCoords.Index.getChunkIndex(blockX, blockZ));
            }

            @Nullable
            public static final WorldChunk getWorldChunk(
                @Nonnull final CommandBuffer<ChunkStore> commandBuffer,
                @Nonnull final Vector3i blockCoords
            ) {
                return Chunk.WorldChunk_.getWorldChunk(commandBuffer, ChunkCoords.Index.getChunkIndex(blockCoords));
            }

            @Nullable
            public static final WorldChunk getWorldChunk_chunkCoords(
                @Nonnull final CommandBuffer<ChunkStore> commandBuffer,
                final int chunkX,
                final int chunkZ
            ) {
                return Chunk.WorldChunk_.getWorldChunk(
                    commandBuffer,
                    ChunkCoords.Index.getChunkIndex_chunkCoords(chunkX, chunkZ)
                );
            }

            @Nullable
            public static final WorldChunk getWorldChunk(
                @Nonnull final CommandBuffer<ChunkStore> commandBuffer, // << Doesn't need to get the world, it can access components directly
                final long chunkIndex
            ) {
                // but regardless we don't need the world to get the world chunk
                return commandBuffer.getExternalData().getChunkComponent(chunkIndex, WORLD_CHUNK_COMPONENT);
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
                return Chunk.WorldChunk_.getWorldChunk(store, blockChunk.getIndex());
            }

            @Nullable
            public static final WorldChunk getWorldChunk(
                @Nonnull final Store<ChunkStore> store,
                final int blockX,
                final int blockZ
            ) {
                return Chunk.WorldChunk_.getWorldChunk(store, ChunkCoords.Index.getChunkIndex(blockX, blockZ));
            }

            @Nullable
            public static final WorldChunk getWorldChunk(
                @Nonnull final Store<ChunkStore> store,
                @Nonnull final Vector3i blockCoords
            ) {
                return Chunk.WorldChunk_.getWorldChunk(store, ChunkCoords.Index.getChunkIndex(blockCoords));
            }

            @Nullable
            public static final WorldChunk getWorldChunk_chunkCoords(
                @Nonnull final Store<ChunkStore> store,
                final int chunkX,
                final int chunkZ
            ) {
                return Chunk.WorldChunk_.getWorldChunk(
                    store,
                    ChunkCoords.Index.getChunkIndex_chunkCoords(chunkX, chunkZ)
                );
            }

            @Nullable
            public static final WorldChunk getWorldChunk(
                @Nonnull final Store<ChunkStore> store,
                final long chunkIndex
            ) {
                return store.getExternalData().getChunkComponent(chunkIndex, WORLD_CHUNK_COMPONENT);
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
                return Chunk.WorldChunk_.getWorldChunk(chunkStore, blockChunk.getIndex());
            }

            @Nullable
            public static final WorldChunk getWorldChunk(
                @Nonnull final ChunkStore chunkStore,
                final int blockX,
                final int blockZ
            ) {
                // ALT version: `chunkStore.getWorld().getChunk(blockChunk.getIndex());`
                return Chunk.WorldChunk_.getWorldChunk(chunkStore, ChunkCoords.Index.getChunkIndex(blockX, blockZ));
            }

            @Nullable
            public static final WorldChunk getWorldChunk(
                @Nonnull final ChunkStore chunkStore,
                @Nonnull final Vector3i blockCoords
            ) {
                return Chunk.WorldChunk_.getWorldChunk(chunkStore, ChunkCoords.Index.getChunkIndex(blockCoords));
            }

            @Nullable
            public static final WorldChunk getWorldChunk_chunkCoords(
                @Nonnull final ChunkStore chunkStore,
                final int chunkX,
                final int chunkZ
            ) {
                return Chunk.WorldChunk_.getWorldChunk(
                    chunkStore,
                    ChunkCoords.Index.getChunkIndex_chunkCoords(chunkX, chunkZ)
                );
            }

            @Nullable
            public static final WorldChunk getWorldChunk(@Nonnull final ChunkStore chunkStore, final long chunkIndex) {
                // ALT version: `chunkStore.getWorld().getChunk(blockChunk.getIndex());`
                return chunkStore.getChunkComponent(chunkIndex, WORLD_CHUNK_COMPONENT);
            }

            //   #endregion ChunkStore
            // #endregion getWorldChunk
        }

        // TESTS ADDED AND VERIFIED
        /// -> get Ref<ChunkStore>  (ChunkRef)
        public static final class Ref_ {

            /**
             * Tests all methods i've defined for getWorldChunk
             */
            @Nonnull
            public static final ArrayList<Ref<ChunkStore>> test(
                @Nonnull final Ref<ChunkStore> blockRef,
                @Nonnull final WorldChunk worldChunk,
                @Nonnull final CommandBuffer<ChunkStore> commandBuffer,
                @Nonnull final Vector3i providedCoords
            ) {
                final var blockX = providedCoords.x;
                final var blockY = providedCoords.y;
                final var blockZ = providedCoords.z;
                // final var localIndex = BlockCoords.Index.getLocalIndex(blockX, blockY, blockZ);
                // final var localCoords = BlockCoords.Local.getLocalCoords(localIndex);
                final var blockCoords = new Vector3i(blockX, blockY, blockZ);
                final var chunkIndex = ChunkCoords.Index.getChunkIndex(blockCoords);
                final var chunkCoords = ChunkCoords.Global.getChunkCoords(blockCoords);
                final var chunkX = chunkCoords.x;
                final var chunkZ = chunkCoords.z;

                final var test = new TestUtil(commandBuffer, blockCoords);
                // functions to test
                final ArrayList<Ref<ChunkStore>> refs = new ArrayList<>();

                // World
                refs.add(Chunk.Ref_.getChunkRef(test.world, blockCoords));
                refs.add(Chunk.Ref_.getChunkRef(test.world, blockX, blockZ));
                refs.add(Chunk.Ref_.getChunkRef(test.world, chunkIndex));
                refs.add(Chunk.Ref_.getChunkRef_chunkCoords(test.world, chunkX, chunkZ));

                refs.add(Chunk.Ref_.getChunkRef(test.commandBuffer, blockCoords));
                refs.add(Chunk.Ref_.getChunkRef(test.commandBuffer, blockX, blockZ));
                refs.add(Chunk.Ref_.getChunkRef(test.commandBuffer, chunkIndex));
                refs.add(Chunk.Ref_.getChunkRef_chunkCoords(test.commandBuffer, chunkX, chunkZ));

                refs.add(Chunk.Ref_.getChunkRef(test.store, blockCoords));
                refs.add(Chunk.Ref_.getChunkRef(test.store, blockX, blockZ));
                refs.add(Chunk.Ref_.getChunkRef(test.store, chunkIndex));
                refs.add(Chunk.Ref_.getChunkRef_chunkCoords(test.store, chunkX, chunkZ));

                refs.add(Chunk.Ref_.getChunkRef(test.chunkStore, blockCoords));
                refs.add(Chunk.Ref_.getChunkRef(test.chunkStore, blockX, blockZ));
                refs.add(Chunk.Ref_.getChunkRef(test.chunkStore, chunkIndex));
                refs.add(Chunk.Ref_.getChunkRef_chunkCoords(test.chunkStore, chunkX, chunkZ));

                refs.add(Chunk.Ref_.getChunkRef(test.chunkRef, blockCoords));
                refs.add(Chunk.Ref_.getChunkRef(test.chunkRef, blockX, blockZ));
                refs.add(Chunk.Ref_.getChunkRef(test.chunkRef, chunkIndex));
                refs.add(Chunk.Ref_.getChunkRef_chunkCoords(test.chunkRef, chunkX, chunkZ));

                refs.add(Chunk.Ref_.getChunkRef(test.info, blockCoords));
                refs.add(Chunk.Ref_.getChunkRef(test.info, blockX, blockZ));
                refs.add(Chunk.Ref_.getChunkRef(test.info, chunkIndex));
                refs.add(Chunk.Ref_.getChunkRef_chunkCoords(test.info, chunkX, chunkZ));

                refs.add(Chunk.Ref_.getChunkRef(test.blockRef));
                refs.add(Chunk.Ref_.getChunkRef(test.info));

                return refs;
            }

            /**
             * What do we need to get a chunk ref?
             * - Really, this is just stuff that can uniquely identify a chunk, MEANING we need both
             *   - The world the chunk is in
             *   - Coordinates (e.g. x+z, chunk index, etc)
             *
             * ## The world the chunk is in
             * Notably, the world can be found in various ways,
             * - A ref to a block in the same world you want a chunk for, a command buffer,
             *   the world, etc
             * - If you want the chunk that a block is in, this is WAY easy, since
             *   a block contains not only its coordinates, but the world it's in too
             *   THUS, going from a block to the chunk it's in, is actually pretty easy
             * - Of course, you don't have to use the block to get the chunk it's in, you
             *   can absolutely get any chunk from the WORLD it's in
             *
             * ChunkRef -> Other ChunkRef
             * - of course, a chunk knows what world it's in, so you can use the
             *   chunk to get other chunks in the same world pretty easy
             *
             * ## Coordinates
             * Easy enough. Chunks can be identified by
             * - Coordinates of a block (easy to convert -> we just divide the x and z of the block by 32 ish)
             * - Chunk coordinates (x and z of a chunk itsef)
             *
             * NOTE
             * - a chunk is from the base of the world to the top, and it's 32x32 wide
             * - there ARE older ideas in hytales codebase for chunks -> i forget what they're
             *   called but they're like ChunkSections or something, please don't use them,
             *   for my sake. I think they're used in the farming systems (that are deprecated i believe,
             *   oh and the fluid systems too i think??)
             */

            // what i need to get a chunk ref
            // - anything that could get a block ref
            // - anything that could get a chunk
            // - anything that could get

            // #region getChunkRef
            // lets say you don't have a chunk ref, but you KNOW you have the information required to find one.
            // well, good news. that's the entire point of this section. if you wanna see how i get a chunk
            // ref out of your components follow the path through from your inputs and shit should start making
            // more sense over time

            // #region WorldProvider
            // #endregion WorldProvider
            // #region World
            // ====================================================================
            // World
            // ====================================================================

            @Nullable
            public static final Ref<ChunkStore> getChunkRef(
                @Nonnull final World world,
                @Nonnull final Vector3i blockCoords
            ) {
                return Chunk.Ref_.getChunkRef(world, ChunkCoords.Index.getChunkIndex(blockCoords));
            }

            @Nullable
            public static final Ref<ChunkStore> getChunkRef(
                @Nonnull final World world,
                final int blockX,
                final int blockZ
            ) {
                return Chunk.Ref_.getChunkRef(world, ChunkCoords.Index.getChunkIndex(blockX, blockZ));
            }

            @Nullable
            public static final Ref<ChunkStore> getChunkRef_chunkCoords(
                @Nonnull final World world,
                final int chunkX,
                final int chunkZ
            ) {
                return Chunk.Ref_.getChunkRef(world, ChunkCoords.Index.getChunkIndex_chunkCoords(chunkX, chunkZ));
            }

            @Nullable
            public static final Ref<ChunkStore> getChunkRef(@Nonnull final World world, final long chunkIndex) {
                return world.getChunkStore().getChunkReference(chunkIndex);
            }

            // #endregion World
            // #region CommandBuffer
            // ====================================================================
            // CommandBuffer
            // ====================================================================

            @Nullable
            public static final Ref<ChunkStore> getChunkRef(
                @Nonnull final CommandBuffer<ChunkStore> commandBuffer,
                @Nonnull final Vector3i blockCoords
            ) {
                return Chunk.Ref_.getChunkRef(commandBuffer, ChunkCoords.Index.getChunkIndex(blockCoords));
            }

            @Nullable
            public static final Ref<ChunkStore> getChunkRef(
                @Nonnull final CommandBuffer<ChunkStore> commandBuffer,
                final int blockX,
                final int blockZ
            ) {
                return Chunk.Ref_.getChunkRef(commandBuffer, ChunkCoords.Index.getChunkIndex(blockX, blockZ));
            }

            @Nullable
            public static final Ref<ChunkStore> getChunkRef_chunkCoords(
                @Nonnull final CommandBuffer<ChunkStore> commandBuffer,
                final int chunkX,
                final int chunkZ
            ) {
                return Chunk.Ref_.getChunkRef(
                    commandBuffer,
                    ChunkCoords.Index.getChunkIndex_chunkCoords(chunkX, chunkZ)
                );
            }

            @Nullable
            public static final Ref<ChunkStore> getChunkRef(
                @Nonnull final CommandBuffer<ChunkStore> commandBuffer,
                final long chunkIndex
            ) {
                return commandBuffer.getExternalData().getChunkReference(chunkIndex);
            }

            // #endregion CommandBuffer
            // #region Store<ChunkStore>
            // ====================================================================
            // Store<ChunkStore>
            // ====================================================================

            @Nullable
            public static final Ref<ChunkStore> getChunkRef(
                @Nonnull final Store<ChunkStore> store,
                @Nonnull final Vector3i blockCoords
            ) {
                return Chunk.Ref_.getChunkRef(store, ChunkCoords.Index.getChunkIndex(blockCoords));
            }

            @Nullable
            public static final Ref<ChunkStore> getChunkRef(
                @Nonnull final Store<ChunkStore> store,
                final int blockX,
                final int blockZ
            ) {
                return Chunk.Ref_.getChunkRef(store, ChunkCoords.Index.getChunkIndex(blockX, blockZ));
            }

            @Nullable
            public static final Ref<ChunkStore> getChunkRef_chunkCoords(
                @Nonnull final Store<ChunkStore> store,
                final int chunkX,
                final int chunkZ
            ) {
                return Chunk.Ref_.getChunkRef(store, ChunkCoords.Index.getChunkIndex_chunkCoords(chunkX, chunkZ));
            }

            @Nullable
            public static final Ref<ChunkStore> getChunkRef(
                @Nonnull final Store<ChunkStore> store,
                final long chunkIndex
            ) {
                return store.getExternalData().getChunkReference(chunkIndex);
            }

            // #endregion Store<ChunkStore>
            // #region ChunkStore
            // ====================================================================
            // ChunkStore
            // ====================================================================

            @Nullable
            public static final Ref<ChunkStore> getChunkRef(
                @Nonnull final ChunkStore chunkStore,
                @Nonnull final Vector3i blockCoords
            ) {
                return Chunk.Ref_.getChunkRef(chunkStore, ChunkCoords.Index.getChunkIndex(blockCoords));
            }

            @Nullable
            public static final Ref<ChunkStore> getChunkRef(
                @Nonnull final ChunkStore chunkStore,
                final int blockX,
                final int blockZ
            ) {
                return Chunk.Ref_.getChunkRef(chunkStore, ChunkCoords.Index.getChunkIndex(blockX, blockZ));
            }

            @Nullable
            public static final Ref<ChunkStore> getChunkRef_chunkCoords(
                @Nonnull final ChunkStore chunkStore,
                final int chunkX,
                final int chunkZ
            ) {
                return Chunk.Ref_.getChunkRef(chunkStore, ChunkCoords.Index.getChunkIndex_chunkCoords(chunkX, chunkZ));
            }

            @Nullable
            public static final Ref<ChunkStore> getChunkRef(
                @Nonnull final ChunkStore chunkStore,
                final long chunkIndex
            ) {
                return chunkStore.getChunkReference(chunkIndex);
            }

            // #endregion ChunkStore
            // #region Ref<ChunkStore>

            @Nullable
            public static final Ref<ChunkStore> getChunkRef(
                @Nonnull final Ref<ChunkStore> chunkRef,
                @Nonnull final Vector3i blockCoords
            ) {
                return Chunk.Ref_.getChunkRef(chunkRef, ChunkCoords.Index.getChunkIndex(blockCoords));
            }

            @Nullable
            public static final Ref<ChunkStore> getChunkRef(
                @Nonnull final Ref<ChunkStore> chunkRef,
                final int blockX,
                final int blockZ
            ) {
                return Chunk.Ref_.getChunkRef(chunkRef, ChunkCoords.Index.getChunkIndex(blockX, blockZ));
            }

            @Nullable
            public static final Ref<ChunkStore> getChunkRef_chunkCoords(
                @Nonnull final Ref<ChunkStore> chunkRef,
                final int chunkX,
                final int chunkZ
            ) {
                return Chunk.Ref_.getChunkRef(chunkRef, ChunkCoords.Index.getChunkIndex_chunkCoords(chunkX, chunkZ));
            }

            @Nullable
            public static final Ref<ChunkStore> getChunkRef(
                @Nonnull final Ref<ChunkStore> chunkRef,
                final long chunkIndex
            ) {
                return chunkRef.getStore().getExternalData().getChunkReference(chunkIndex);
            }

            // #endregion Ref<ChunkStore>
            // #region BlockStateInfo
            // ====================================================================
            // BlockStateInfo
            // ====================================================================

            @Nullable
            public static final Ref<ChunkStore> getChunkRef(
                @Nonnull final BlockStateInfo info,
                @Nonnull final Vector3i blockCoords
            ) {
                return Chunk.Ref_.getChunkRef(info, ChunkCoords.Index.getChunkIndex(blockCoords));
            }

            @Nullable
            public static final Ref<ChunkStore> getChunkRef(
                @Nonnull final BlockStateInfo info,
                final int blockX,
                final int blockZ
            ) {
                return Chunk.Ref_.getChunkRef(info, ChunkCoords.Index.getChunkIndex(blockX, blockZ));
            }

            @Nullable
            public static final Ref<ChunkStore> getChunkRef_chunkCoords(
                @Nonnull final BlockStateInfo info,
                final int chunkX,
                final int chunkZ
            ) {
                return Chunk.Ref_.getChunkRef(info, ChunkCoords.Index.getChunkIndex_chunkCoords(chunkX, chunkZ));
            }

            @Nullable
            public static final Ref<ChunkStore> getChunkRef(@Nonnull final BlockStateInfo info, final long chunkIndex) {
                return info.getChunkRef().getStore().getExternalData().getChunkReference(chunkIndex);
            }

            // #endregion BlockStateInfo
            // #region BlockRef
            // ====================================================================
            // BlockRef
            // ====================================================================

            @Nullable
            public static final Ref<ChunkStore> getChunkRef(@Nonnull final Ref<ChunkStore> blockRef) {
                final var info = Block.Info.getInfo(blockRef);
                if (info == null) {
                    return null;
                }
                return info.getChunkRef();
            }

            // please don't use this one for getting it from info... just here for completeness so you know you CAN get the chunk ref out of info - in fact, that's (in my understanding) the preferred way
            @Nullable
            public static final Ref<ChunkStore> getChunkRef(@Nonnull final BlockStateInfo info) {
                return info.getChunkRef();
            }

            // #endregion BlockRef
            // #endregion getChunkRef
        }
    }

    // TODO
    public static final class Component_ {

        /**
         * Tests all methods i've defined for getWorldChunk
         */
        @Nonnull
        public static final ArrayList<? extends Component<ChunkStore>> test(
            @Nonnull final Ref<ChunkStore> blockRef,
            @Nonnull final WorldChunk worldChunk,
            @Nonnull final CommandBuffer<ChunkStore> commandBuffer,
            @Nonnull final Vector3i providedCoords
        ) {
            // final var blockX = providedCoords.x;
            // final var blockY = providedCoords.y;
            // final var blockZ = providedCoords.z;
            // // final var localIndex = BlockCoords.Index.getLocalIndex(blockX, blockY, blockZ);
            // // final var localCoords = BlockCoords.Local.getLocalCoords(localIndex);
            // final var blockCoords = new Vector3i(blockX, blockY, blockZ);
            // final var chunkIndex = ChunkCoords.Index.getChunkIndex(blockCoords);
            // final var chunkCoords = ChunkCoords.Global.getChunkCoords(blockCoords);
            // final var chunkX = chunkCoords.x;
            // final var chunkZ = chunkCoords.z;

            // final var test = new TestUtil(commandBuffer, blockCoords);
            // functions to test
            final ArrayList<? extends Component<ChunkStore>> refs = new ArrayList<>();

            // World

            return refs;
        }

        public static final <T extends Component<ChunkStore>> boolean has(
            @Nonnull final Supplier<ComponentType<ChunkStore, T>> getComponentType,
            @Nonnull final Ref<ChunkStore> ref
        ) {
            final var componentType = getComponentType.get();
            if (componentType == null) {
                return false;
            }

            return has(componentType, ref);
        }

        public static final <T extends Component<ChunkStore>> boolean has(
            @Nonnull final ComponentType<ChunkStore, T> componentType,
            @Nonnull final Ref<ChunkStore> ref
        ) {
            return (T) ref.getStore().getComponent(ref, componentType) != null;
        }

        // ====================================================================
        // Get another component that's ON the same ref you passed in
        // ====================================================================

        public static final <T extends Component<ChunkStore>> T get(
            @Nonnull final Ref<ChunkStore> anyRef,
            @Nonnull final Supplier<ComponentType<ChunkStore, T>> getComponentType
        ) {
            final var componentType = getComponentType.get();
            if (componentType == null) {
                return null;
            }

            return anyRef.getStore().getComponent(anyRef, componentType);
        }

        public static final <T extends Component<ChunkStore>> T get(
            @Nonnull final Ref<ChunkStore> anyRef,
            @Nonnull final ComponentType<ChunkStore, T> componentType
        ) {
            return anyRef.getStore().getComponent(anyRef, componentType);
        }

        @Nullable
        public static final BlockComponentChunk getBlockComponentChunk(@Nonnull final Ref<ChunkStore> anyRef) {
            if (Block.isChunkRef(anyRef)) {
                return anyRef.getStore().getComponent(anyRef, BLOCK_COMPONENT_CHUNK);
            } else if (Block.isBlockRef(anyRef)) {
                // get the chunkRef first
                final var chunkRef = Chunk.Ref_.getChunkRef(anyRef);
                if (chunkRef == null) {
                    return null;
                }

                // now we get the BlockComponentChunk
                return chunkRef.getStore().getComponent(chunkRef, BLOCK_COMPONENT_CHUNK);
            }

            return null;
        }

        // ====================================================================
        // Get a component from a block at the given coords (global)
        // ====================================================================

        @Nullable
        public static final <T extends Component<ChunkStore>> T get_blockCoords(
            @Nonnull final ComponentType<ChunkStore, T> componentType,
            @Nonnull final World world,
            final int blockX,
            final int blockY,
            final int blockZ
        ) {
            final var chunkRef = world.getChunkStore().getChunkReference(ChunkUtil.indexChunkFromBlock(blockX, blockZ));
            if (chunkRef == null) {
                return null;
            }

            final var chunkStore = world.getChunkStore().getStore();
            final var blockComponentChunk = Component_.getBlockComponentChunk(chunkRef);
            if (blockComponentChunk == null) {
                return null;
            }

            final var blockRef = blockComponentChunk.getEntityReference(
                ChunkUtil.indexBlockInColumn(blockX, blockY, blockZ)
            );
            if (blockRef == null || !blockRef.isValid()) {
                return null;
            }

            return chunkStore.getComponent(blockRef, componentType);
        }

        // ====================================================================
        // Get a component from a block at the given LOCAL coords
        // ====================================================================

        public static final <T extends Component<ChunkStore>> T get_localCoords(
            @Nonnull final Supplier<ComponentType<ChunkStore, T>> getComponentType,
            @Nonnull final BlockComponentChunk chunk,
            final int localX,
            final int localY,
            final int localZ
        ) {
            final var ref = Block.Ref_.getLocalRef(chunk, localX, localY, localZ);
            if (ref == null) {
                return null;
            }
            final var componentType = getComponentType.get();
            if (componentType == null) {
                return null;
            }

            return ref.getStore().getComponent(ref, componentType);
        }

        @Nullable
        public static final <T extends Component<ChunkStore>> T get_localCoords(
            @Nonnull final ComponentType<ChunkStore, T> componentType,
            @Nonnull final BlockComponentChunk chunk,
            final int localX,
            final int localY,
            final int localZ
        ) {
            final var ref = Block.Ref_.getLocalRef(chunk, localX, localY, localZ);
            if (ref == null) {
                return null;
            }
            return ref.getStore().getComponent(ref, componentType);
        }
    }

    public static final class BlockCoords {

        /// -> get LOCAL coordinates within chunk    (Vector3i)
        public static final class Local {

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
        }

        /// -> get LOCAL index                       (int | Integer) (returns Integer so it can be nullable on method that can fail)
        public static final class Index {

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

            // #endregion getLocalIndex}
        }

        /// -> get GLOBAL coordinates                (Vector3i)
        public static final class Global {

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
                final var worldChunk = Chunk.WorldChunk_.getWorldChunk_chunkRef(chunkRef);
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
                final var worldChunk = Chunk.WorldChunk_.getWorldChunk_chunkRef(chunkRef);
                if (worldChunk == null) {
                    return null;
                }

                return getGlobalCoords(worldChunk.getX(), worldChunk.getZ(), x, y, z);
            }

            @Nullable
            public static final Vector3i getGlobalCoords(
                @Nonnull final Ref<ChunkStore> chunkRef,
                final int blockIndex
            ) {
                final var worldChunk = Chunk.WorldChunk_.getWorldChunk_chunkRef(chunkRef);
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
                final var worldChunk = Chunk.WorldChunk_.getWorldChunk_chunkRef(chunkRef);
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
            public static final Vector3i getGlobalCoords(
                @Nonnull final WorldChunk chunk,
                @Nonnull final Vector3i coords
            ) {
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
                return getGlobalCoords(chunk.getX(), chunk.getZ(), blockIndex);
            }

            @Nonnull
            public static final Vector3i getGlobalCoords(
                @Nonnull final WorldChunk chunk,
                @Nonnull final BlockStateInfo info
            ) {
                return getGlobalCoords(chunk.getX(), chunk.getZ(), info.getIndex());
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
                return getGlobalCoords(chunk.getX(), chunk.getZ(), blockIndex);
            }

            @Nonnull
            public static final Vector3i getGlobalCoords(
                @Nonnull final BlockAccessor chunk,
                @Nonnull final BlockStateInfo info
            ) {
                return getGlobalCoords(chunk.getX(), chunk.getZ(), info.getIndex());
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
    }

    public static final class ChunkCoords {

        /// -> get CHUNK coordinates                 (long | Vector2i | ChunkCoordinates)
        public static final class Global {

            // #region getChunkCoords
            // ====================================================================
            // /\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\
            // ====================================================================
            //
            // Purpose:   Getting chunk coordinates within a world
            // Requires:  BlockStateInfo component of the relevant block (or a method of getting this -> see Info.getInfo() and thus -> Entity.getRef)
            // importantly, most methods of getInfo are pointless, as usually these require the coordinates of a block (and if you have that, you don't need this)

            // ====================================================================
            // Chunk Coordinates FROM block coordinates
            // ====================================================================

            // With GLOBAL block coords
            @Nonnull
            public static final ChunkCoordinates getChunkCoords(final int blockX, final int blockZ) {
                // see ChunkUtil.chunkCoordinate
                return new ChunkCoordinates(blockX >> 5, blockZ >> 5);
            }

            // With GLOBAL block coords
            @Nonnull
            public static final ChunkCoordinates getChunkCoords(@Nonnull final Vector3i blockCoords) {
                // see ChunkUtil.chunkCoordinate
                return new ChunkCoordinates(blockCoords.x >> 5, blockCoords.z >> 5);
            }

            /**
             * With GLOBAL block coords
             *
             * returned "y" in vector is actually the chunk's "z" coordinate
             */
            @Nonnull
            public static final Vector2i getChunkCoordsVec(final int blockX, final int blockZ) {
                // see ChunkUtil.chunkCoordinate
                return new Vector2i(blockX >> 5, blockZ >> 5);
            }

            /**
             * With GLOBAL block coords
             *
             * returned "y" in vector is actually the chunk's "z" coordinate
             */
            @Nonnull
            public static final Vector2i getChunkCoordsVec(@Nonnull final Vector3i blockCoords) {
                // see ChunkUtil.chunkCoordinate
                return new Vector2i(blockCoords.x >> 5, blockCoords.z >> 5);
            }
            // #endregion getChunkCoords
        }

        // ====================================================================
        // Chunk Index (index is interchangable with coordinates) FROM block coordinates
        // ====================================================================

        public static final class Index {

            // #region getChunkIndex

            /**
             * a `long` is just a bit-packed version of chunk coords.
             * int: 32 bits
             * long: 64 bits
             *
             * hence, a long is basically 2 ints back to back
             */
            public static final long getChunkIndex(final int blockX, final int blockZ) {
                // see ChunkUtil.indexChunkFromBlock
                return ((long) (blockX >> 5) << 32) | ((long) (blockZ >> 5) & 4294967295L);
            }

            public static final long getChunkIndex(@Nonnull final Vector3i blockCoords) {
                // see ChunkUtil.indexChunkFromBlock
                return ((long) (blockCoords.x >> 5) << 32) | ((long) (blockCoords.z >> 5) & 4294967295L);
            }

            public static final long getChunkIndex_chunkCoords(final int chunkX, final int chunkZ) {
                // see ChunkUtil.indexChunkFromBlock
                return ((long) chunkX << 32) | ((long) chunkZ & 4294967295L);
            }

            public static final long getChunkIndex_chunkCoords(@Nonnull final ChunkCoordinates coords) {
                // see ChunkUtil.indexChunkFromBlock
                return ((long) coords.x << 32) | ((long) coords.z & 4294967295L);
            }

            /**
             * WARNING: the `y` value MUST be the `z` value for the chunk
             */
            public static final long getChunkIndex_chunkCoords(@Nonnull final Vector2i chunkCoords) {
                // see ChunkUtil.indexChunkFromBlock
                return ((long) chunkCoords.x << 32) | ((long) chunkCoords.y & 4294967295L);
            }
            // #endregion getChunkIndex
        }
    }

    /**
     * @deprecated I don't plan to use tick procedure stuff, so I'm not maintaining a billion
     * methods rn. mainly, i dont want to go through and make all the millions of methods.
     *
     * SO, this stuff is fine to use, but, yeah...
     */
    public static final class TickProcedure {

        public static final boolean setTicking(@Nonnull final Ref<ChunkStore> ref) {
            return TickProcedure.setTicking(ref, true);
        }

        public static final boolean setTicking(@Nonnull final Ref<ChunkStore> ref, final boolean ticking) {
            final var info = Block.Info.getInfo(ref);
            if (info == null) {
                console.log("Info was null");
                return false;
            }

            return TickProcedure.setTicking(info, ticking);
        }

        public static final boolean setTicking(@Nonnull final BlockStateInfo info) {
            return TickProcedure.setTicking(info, true);
        }

        public static final boolean setTicking(@Nonnull final BlockStateInfo info, final boolean ticking) {
            final var worldChunk = Chunk.WorldChunk_.getWorldChunk(info);
            if (worldChunk == null) {
                console.log("World chunk was null");
                return false;
            }

            final var coords = BlockCoords.Local.getLocalCoords(info);
            return TickProcedure.setTicking(worldChunk, coords, ticking);
        }

        public static final boolean setTicking(@Nonnull final WorldChunk worldChunk, @Nonnull final Vector3i coords) {
            return TickProcedure.setTicking(worldChunk, coords, true);
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
            final var coords = BlockCoords.Local.getLocalCoords(info);
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
