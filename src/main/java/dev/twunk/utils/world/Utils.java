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
import dev.twunk.component.TwunkDevTestComponent;
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

            final var info = Block.Info.get(blockRef);
            if (info == null) {
                throw new RuntimeException("Error: failed to get info for test in asfiuogrt71t7o83");
            }

            final var chunkRef = info.getChunkRef();

            // BLOCK ref passed to isBlockRef (expect TRUE)
            refs.add(Block.isBlockRef(blockRef));
            // BLOCK ref passed to isChunkRef (expect FALSE)
            refs.add(Block.isChunkRef(blockRef));

            // CHUNK ref passed to isChunkRef (expect TRUE)
            refs.add(Block.isChunkRef(chunkRef));
            // CHUNK ref passed to isBlockRef (expect FALSE)
            refs.add(Block.isBlockRef(chunkRef));

            return refs;
        }

        // TESTS ADDED AND VERIFIED
        public static final boolean isBlockRef(@Nonnull final Ref<ChunkStore> ref) {
            return Utils.Component_.has(ref, BLOCK_STATE_INFO_COMPONENT_TYPE);
        }

        // TESTS ADDED AND VERIFIED
        public static final boolean isChunkRef(@Nonnull final Ref<ChunkStore> ref) {
            return Utils.Component_.has(ref, WORLD_CHUNK_COMPONENT);
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
                    // var ref = Ref_.get(cmd, coords.x, coords.y - 1, coords.z);

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
                    world.getChunkIfInMemory(ChunkCoords.Index.get(coords));
                    world.getChunkIfLoaded(ChunkCoords.Index.get(coords));
                    world.getChunk(ChunkCoords.Index.get(coords));

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
                Block.Ref_.testWorldMethods(testWorld, providedCoords);
                Block.Ref_.testWorldMethods(commandBuffer.getExternalData().getWorld(), providedCoords);
                Block.Ref_.testWorldMethods(commandBuffer.getStore().getExternalData().getWorld(), providedCoords);
                Block.Ref_.testWorldMethods(blockRef.getStore().getExternalData().getWorld(), providedCoords);

                final var blockX = providedCoords.x;
                final var blockY = providedCoords.y;
                final var blockZ = providedCoords.z;
                final var index = BlockCoords.Index.get(blockX, blockY, blockZ);
                final var localCoords = BlockCoords.Local.get(index);
                final var blockCoords = new Vector3i(blockX, blockY, blockZ);
                final var chunkIndex = ChunkCoords.Index.get(blockCoords);

                final var test = new TestUtil(commandBuffer, blockCoords);
                // functions to test
                final ArrayList<Ref<ChunkStore>> refs = new ArrayList<>();

                // WorldProvider
                refs.add(Block.Ref_.get(test.worldProvider, blockCoords));
                refs.add(Block.Ref_.get(test.worldProvider, blockX, blockY, blockZ));
                refs.add(Block.Ref_.get(test.worldProvider, chunkIndex, index));

                // World
                refs.add(Block.Ref_.get(test.world, blockCoords));
                refs.add(Block.Ref_.get(test.world, blockX, blockY, blockZ));
                refs.add(Block.Ref_.get(test.world, chunkIndex, index));

                // CommandBuffer
                refs.add(Block.Ref_.get(test.commandBuffer, blockCoords));
                refs.add(Block.Ref_.get(test.commandBuffer, blockX, blockY, blockZ));
                refs.add(Block.Ref_.get(test.commandBuffer, chunkIndex, index));

                // BlockStateInfo
                refs.add(Block.Ref_.get(test.info, blockCoords));
                refs.add(Block.Ref_.get(test.info, blockX, blockY, blockZ));
                refs.add(Block.Ref_.get(test.info, chunkIndex, index));

                // ChunkRef (global)
                refs.add(Block.Ref_.get_chunkRef(test.chunkRef, blockCoords));
                refs.add(Block.Ref_.get_chunkRef(test.chunkRef, blockX, blockY, blockZ));
                refs.add(Block.Ref_.get_chunkRef(test.chunkRef, chunkIndex, index));
                // ChunkRef (local)
                refs.add(Block.Ref_.getLocal(test.chunkRef, blockCoords));
                refs.add(Block.Ref_.getLocal(test.chunkRef, localCoords));
                refs.add(Block.Ref_.getLocal(test.chunkRef, blockX, blockY, blockZ));
                refs.add(Block.Ref_.getLocal(test.chunkRef, localCoords.x, localCoords.y, localCoords.z));
                refs.add(Block.Ref_.getLocal(test.chunkRef, index));

                // Store
                refs.add(Block.Ref_.get(test.store, blockCoords));
                refs.add(Block.Ref_.get(test.store, blockX, blockY, blockZ));
                refs.add(Block.Ref_.get(test.store, chunkIndex, index));

                // ChunkStore
                refs.add(Block.Ref_.get(test.chunkStore, blockCoords));
                refs.add(Block.Ref_.get(test.chunkStore, blockX, blockY, blockZ));
                refs.add(Block.Ref_.get(test.chunkStore, chunkIndex, index));

                // BlockRef
                refs.add(Block.Ref_.get_blockRef(test.blockRef, blockCoords));
                refs.add(Block.Ref_.get_blockRef(test.blockRef, blockX, blockY, blockZ));
                refs.add(Block.Ref_.get_blockRef(test.blockRef, chunkIndex, index));

                // WorldChunk
                refs.add(Block.Ref_.get(test.worldChunk, blockCoords));
                refs.add(Block.Ref_.get(test.worldChunk, blockX, blockY, blockZ));
                refs.add(Block.Ref_.get(test.worldChunk, chunkIndex, index));
                refs.add(Block.Ref_.getLocal(test.worldChunk, blockCoords));
                refs.add(Block.Ref_.getLocal(test.worldChunk, blockX, blockY, blockZ));
                refs.add(Block.Ref_.getLocal(test.worldChunk, index));

                // BlockComponentChunk
                refs.add(Block.Ref_.get(test.blockComponentChunk, index));
                refs.add(Block.Ref_.getLocal(test.blockComponentChunk, blockCoords));
                refs.add(Block.Ref_.getLocal(test.blockComponentChunk, blockX, blockY, blockZ));
                refs.add(Block.Ref_.getLocal(test.blockComponentChunk, index));

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
            public static final Ref<ChunkStore> get_blockRef(
                @Nonnull final Ref<ChunkStore> blockRef,
                @Nonnull final Vector3i blockCoords
            ) {
                // "other" because it's not necessarily THIS chunk that i'm
                // using to get a block ref for the coords `blockCoords`
                final var otherChunkRef = Chunk.Ref_.get(blockRef);
                if (otherChunkRef == null) {
                    return null;
                }

                return Block.Ref_.get2(
                    otherChunkRef.getStore().getExternalData(),
                    ChunkCoords.Index.get(blockCoords),
                    BlockCoords.Index.get(blockCoords)
                );
            }

            // => (ChunkStore, chunkIndex, blockIndex)
            @Nullable
            public static final Ref<ChunkStore> get_blockRef(
                @Nonnull final Ref<ChunkStore> blockRef,
                final int blockX,
                final int blockY,
                final int blockZ
            ) {
                // "other" because it's not necessarily THIS chunk that i'm
                // using to get a block ref for the coords `blockCoords`
                final var otherChunkRef = Chunk.Ref_.get(blockRef);
                if (otherChunkRef == null) {
                    return null;
                }

                return Block.Ref_.get2(
                    otherChunkRef.getStore().getExternalData(),
                    ChunkCoords.Index.get(blockX, blockZ),
                    BlockCoords.Index.get(blockX, blockY, blockZ)
                );
            }

            // => (ChunkStore, chunkIndex, blockIndex)
            @Nullable
            public static final Ref<ChunkStore> get_blockRef(
                @Nonnull final Ref<ChunkStore> blockRef,
                final long chunkIndex,
                final int blockIndex
            ) {
                final var otherChunkRef = Chunk.Ref_.get(blockRef);
                if (otherChunkRef == null) {
                    return null;
                }

                return Block.Ref_.get2(otherChunkRef.getStore().getExternalData(), chunkIndex, blockIndex);
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
            public static final Ref<ChunkStore> get(
                @Nonnull final WorldProvider worldProvider,
                @Nonnull final Vector3i coords
            ) {
                // return worldProvider
                //     .getWorld()
                //     .getChunk(ChunkCoords.Index.getChunkIndex(coords))
                //     .getBlockComponentEntity(coords.x, coords.y, coords.z);
                return Block.Ref_.get2(
                    worldProvider.getWorld().getChunkStore(),
                    ChunkCoords.Index.get(coords),
                    BlockCoords.Index.get(coords)
                );
            }

            // => (ChunkStore, chunkIndex, blockIndex)
            @Nullable
            public static final Ref<ChunkStore> get(
                @Nonnull final WorldProvider worldProvider,
                final int blockX,
                final int blockY,
                final int blockZ
            ) {
                return Block.Ref_.get2(
                    worldProvider.getWorld().getChunkStore(),
                    ChunkCoords.Index.get(blockX, blockZ),
                    BlockCoords.Index.get(blockX, blockY, blockZ)
                );
            }

            // => (ChunkStore, chunkIndex, blockIndex)
            @Nullable
            public static final Ref<ChunkStore> get(
                @Nonnull final WorldProvider worldProvider,
                final long chunkIndex,
                final int blockIndex
            ) {
                // final var localCoords = BlockCoords.Local.get(blockIndex);
                // return worldProvider
                //     .getWorld()
                //     .getChunk(chunkIndex)
                //     .getBlockComponentEntity(localCoords.x, localCoords.y, localCoords.z);
                return Block.Ref_.get2(worldProvider.getWorld().getChunkStore(), chunkIndex, blockIndex);
            }

            // #endregion WorldProvider
            // #region World

            // => (ChunkStore, chunkIndex, blockIndex)
            @Nullable
            public static final Ref<ChunkStore> get(@Nonnull final World world, @Nonnull final Vector3i coords) {
                // return world
                //     .getChunk(ChunkCoords.Index.getChunkIndex(coords))
                //     .getBlockComponentEntity(coords.x, coords.y, coords.z);
                return Block.Ref_.get2(
                    world.getChunkStore(),
                    ChunkCoords.Index.get(coords),
                    BlockCoords.Index.get(coords)
                );
            }

            // => (ChunkStore, chunkIndex, blockIndex)
            @Nullable
            public static final Ref<ChunkStore> get(
                @Nonnull final World world,
                final int blockX,
                final int blockY,
                final int blockZ
            ) {
                // return world
                //     .getChunk(ChunkCoords.Index.getChunkIndex(blockX, blockZ))
                //     .getBlockComponentEntity(blockX, blockY, blockZ);
                return Block.Ref_.get2(
                    world.getChunkStore(),
                    ChunkCoords.Index.get(blockX, blockZ),
                    BlockCoords.Index.get(blockX, blockY, blockZ)
                );
            }

            // => (ChunkStore, chunkIndex, blockIndex)
            @Nullable
            public static final Ref<ChunkStore> get(
                @Nonnull final World world,
                final long chunkIndex,
                final int blockIndex
            ) {
                // final var localCoords = BlockCoords.Local.get(blockIndex);
                // return world.getChunk(chunkIndex).getBlockComponentEntity(localCoords.x, localCoords.y, localCoords.z);
                return Block.Ref_.get2(world.getChunkStore(), chunkIndex, blockIndex);
            }

            // #endregion World
            // #region CommandBuffer

            // => (ChunkStore, chunkIndex, blockIndex)
            @Nullable
            public static final Ref<ChunkStore> get(
                @Nonnull final CommandBuffer<ChunkStore> commandBuffer,
                @Nonnull final Vector3i coords
            ) {
                return Block.Ref_.get2(
                    commandBuffer.getExternalData(),
                    ChunkCoords.Index.get(coords),
                    BlockCoords.Index.get(coords)
                );
            }

            // => (ChunkStore, chunkIndex, blockIndex)
            @Nullable
            public static final Ref<ChunkStore> get(
                @Nonnull final CommandBuffer<ChunkStore> commandBuffer,
                final int blockX,
                final int blockY,
                final int blockZ
            ) {
                return Block.Ref_.get2(
                    commandBuffer.getExternalData(),
                    ChunkCoords.Index.get(blockX, blockZ),
                    BlockCoords.Index.get(blockX, blockY, blockZ)
                );
            }

            // => (ChunkStore, chunkIndex, blockIndex)
            @Nullable
            public static final Ref<ChunkStore> get(
                @Nonnull final CommandBuffer<ChunkStore> commandBuffer,
                final long chunkIndex,
                final int blockIndex
            ) {
                return Block.Ref_.get2(commandBuffer.getExternalData(), chunkIndex, blockIndex);
            }

            // #endregion CommandBuffer
            // #region BlockStateInfo

            @Nullable
            public static final Ref<ChunkStore> get(
                @Nonnull final BlockStateInfo info,
                @Nonnull final Vector3i blockCoords
            ) {
                return Block.Ref_.get2(
                    info.getChunkRef().getStore().getExternalData(),
                    ChunkCoords.Index.get(blockCoords),
                    BlockCoords.Index.get(blockCoords)
                );
            }

            @Nullable
            public static final Ref<ChunkStore> get(
                @Nonnull final BlockStateInfo info,
                final int blockX,
                final int blockY,
                final int blockZ
            ) {
                return Block.Ref_.get2(
                    info.getChunkRef().getStore().getExternalData(),
                    ChunkCoords.Index.get(blockX, blockZ),
                    BlockCoords.Index.get(blockX, blockY, blockZ)
                );
            }

            @Nullable
            public static final Ref<ChunkStore> get(
                @Nonnull final BlockStateInfo info,
                final long chunkIndex,
                final int blockIndex
            ) {
                return Block.Ref_.get2(info.getChunkRef().getStore().getExternalData(), chunkIndex, blockIndex);
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
            public static final Ref<ChunkStore> get_chunkRef(
                @Nonnull final Ref<ChunkStore> otherChunkRef,
                @Nonnull final Vector3i blockCoords
            ) {
                return Block.Ref_.get2(
                    otherChunkRef.getStore().getExternalData(),
                    ChunkCoords.Index.get(blockCoords),
                    BlockCoords.Index.get(blockCoords)
                );
            }

            // some ChunkRef => store of all chunks in the world => our chunk => component ON the chunk itself => block ref in the chunk
            @Nullable
            public static final Ref<ChunkStore> get_chunkRef(
                @Nonnull final Ref<ChunkStore> otherChunkRef,
                final int blockX,
                final int blockY,
                final int blockZ
            ) {
                return Block.Ref_.get2(
                    otherChunkRef.getStore().getExternalData(),
                    ChunkCoords.Index.get(blockX, blockZ),
                    BlockCoords.Index.get(blockX, blockY, blockZ)
                );
            }

            // some ChunkRef => store of all chunks in the world => our chunk => component ON the chunk itself => block ref in the chunk
            @Nullable
            public static final Ref<ChunkStore> get_chunkRef(
                @Nonnull final Ref<ChunkStore> otherChunkRef,
                final long chunkIndex,
                final int blockIndex
            ) {
                return Block.Ref_.get2(otherChunkRef.getStore().getExternalData(), chunkIndex, blockIndex);
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
            public static final Ref<ChunkStore> get(
                @Nonnull final Store<ChunkStore> store,
                @Nonnull final Vector3i blockCoords
            ) {
                final var chunkRef = store.getExternalData().getChunkReference(ChunkCoords.Index.get(blockCoords));
                if (chunkRef == null) {
                    return null;
                }

                return Block.Ref_.getLocal1_chunkRef(chunkRef, BlockCoords.Index.get(blockCoords));
            }

            // => (ChunkStore, chunkIndex, blockIndex)
            @Nullable
            public static final Ref<ChunkStore> get(
                @Nonnull final Store<ChunkStore> store,
                final int blockX,
                final int blockY,
                final int blockZ
            ) {
                final var chunkRef = store.getExternalData().getChunkReference(ChunkCoords.Index.get(blockX, blockZ));
                if (chunkRef == null) {
                    return null;
                }

                return Block.Ref_.getLocal1_chunkRef(chunkRef, BlockCoords.Index.get(blockX, blockY, blockZ));
            }

            // => (ChunkStore, chunkIndex, blockIndex)
            @Nullable
            public static final Ref<ChunkStore> get(
                @Nonnull final Store<ChunkStore> store,
                final long chunkIndex,
                final int blockIndex
            ) {
                final var chunkRef = store.getExternalData().getChunkReference(chunkIndex);
                if (chunkRef == null) {
                    return null;
                }

                return Block.Ref_.getLocal1_chunkRef(chunkRef, blockIndex);
            }

            // #endregion Store<ChunkStore>
            // #region ChunkStore

            // => (ChunkStore, chunkIndex, blockIndex)
            @Nullable
            public static final Ref<ChunkStore> get(
                @Nonnull final ChunkStore chunkStore,
                @Nonnull final Vector3i blockCoords
            ) {
                final var chunkRef = chunkStore.getChunkReference(ChunkCoords.Index.get(blockCoords));
                if (chunkRef == null) {
                    return null;
                }

                return Block.Ref_.getLocal1_chunkRef(chunkRef, BlockCoords.Index.get(blockCoords));
            }

            // => (ChunkStore, chunkIndex, blockIndex)
            @Nullable
            public static final Ref<ChunkStore> get(
                @Nonnull final ChunkStore chunkStore,
                final int blockX,
                final int blockY,
                final int blockZ
            ) {
                final var chunkRef = chunkStore.getChunkReference(ChunkCoords.Index.get(blockX, blockZ));
                if (chunkRef == null) {
                    return null;
                }

                return Block.Ref_.getLocal1_chunkRef(chunkRef, BlockCoords.Index.get(blockX, blockY, blockZ));
            }

            @Nullable
            public static final Ref<ChunkStore> get(
                @Nonnull final ChunkStore chunkStore,
                final long chunkIndex,
                final int blockIndex
            ) {
                final var chunkRef = chunkStore.getChunkReference(chunkIndex);
                if (chunkRef == null) {
                    return null;
                }

                return Block.Ref_.getLocal1_chunkRef(chunkRef, blockIndex);
            }

            @Nullable
            private static final Ref<ChunkStore> get2(
                @Nonnull final ChunkStore chunkStore,
                final long chunkIndex,
                final int blockIndex
            ) {
                final var chunkRef = chunkStore.getChunkReference(chunkIndex);
                if (chunkRef == null) {
                    return null;
                }

                return Block.Ref_.getLocal1_chunkRef(chunkRef, blockIndex);
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
            public static final Ref<ChunkStore> getLocal(
                @Nonnull final Ref<ChunkStore> ref,
                @Nonnull final Vector3i blockCoords
            ) {
                if (Block.isBlockRef(ref)) {
                    return Block.Ref_.getLocal_blockRef(ref, blockCoords);
                } else {
                    return Block.Ref_.getLocal_chunkRef(ref, blockCoords);
                }
            }

            @Nullable
            public static final Ref<ChunkStore> getLocal(
                @Nonnull final Ref<ChunkStore> ref,
                final int blockX,
                final int blockY,
                final int blockZ
            ) {
                if (Block.isBlockRef(ref)) {
                    return Block.Ref_.getLocal_blockRef(ref, blockX, blockY, blockZ);
                } else {
                    return Block.Ref_.getLocal_chunkRef(ref, blockX, blockY, blockZ);
                }
            }

            @Nullable
            public static final Ref<ChunkStore> getLocal(@Nonnull final Ref<ChunkStore> ref, final int blockIndex) {
                if (Block.isBlockRef(ref)) {
                    return Block.Ref_.getLocal_blockRef(ref, blockIndex);
                } else {
                    return Block.Ref_.getLocal_chunkRef(ref, blockIndex);
                }
            }

            // #endregion anyLocalRef
            // #region localBlockRef
            @Nullable
            public static final Ref<ChunkStore> getLocal_blockRef(
                @Nonnull final Ref<ChunkStore> blockRef,
                @Nonnull final Vector3i blockCoords
            ) {
                var info = Utils.Component_.get(blockRef, BLOCK_STATE_INFO_COMPONENT_TYPE);
                if (info == null) {
                    return null;
                }

                var blockIndex = BlockCoords.Index.get(blockCoords);

                return Block.Ref_.getLocal1_chunkRef(info.getChunkRef(), blockIndex);
            }

            @Nullable
            public static final Ref<ChunkStore> getLocal_blockRef(
                @Nonnull final Ref<ChunkStore> blockRef,
                final int blockX,
                final int blockY,
                final int blockZ
            ) {
                var info = Utils.Component_.get(blockRef, BLOCK_STATE_INFO_COMPONENT_TYPE);
                if (info == null) {
                    return null;
                }

                var blockIndex = BlockCoords.Index.get(blockX, blockY, blockZ);

                return Block.Ref_.getLocal1_chunkRef(info.getChunkRef(), blockIndex);
            }

            @Nullable
            public static final Ref<ChunkStore> getLocal_blockRef(
                @Nonnull final Ref<ChunkStore> blockRef,
                final int blockIndex
            ) {
                var info = Utils.Component_.get(blockRef, BLOCK_STATE_INFO_COMPONENT_TYPE);
                if (info == null) {
                    return null;
                }

                return Block.Ref_.getLocal1_chunkRef(info.getChunkRef(), blockIndex);
            }

            // #endregion localBlockRef
            // #region localChunkRef

            // ChunkRef => component ON the chunk itself => block ref in the chunk
            @Nullable
            public static final Ref<ChunkStore> getLocal_chunkRef(
                @Nonnull final Ref<ChunkStore> chunkRef,
                @Nonnull final Vector3i blockCoords
            ) {
                final var blockComponentChunk = Component_.getBlockComponentChunk(chunkRef);
                if (blockComponentChunk == null) {
                    return null;
                }

                return Block.Ref_.getLocal0(blockComponentChunk, BlockCoords.Index.get(blockCoords));
            }

            // ChunkRef => component ON the chunk itself => block ref in the chunk
            @Nullable
            public static final Ref<ChunkStore> getLocal_chunkRef(
                @Nonnull final Ref<ChunkStore> chunkRef,
                final int blockX,
                final int blockY,
                final int blockZ
            ) {
                final var blockComponentChunk = Component_.getBlockComponentChunk(chunkRef);
                if (blockComponentChunk == null) {
                    return null;
                }

                return Block.Ref_.getLocal0(blockComponentChunk, BlockCoords.Index.get(blockX, blockY, blockZ));
            }

            // ChunkRef => component ON the chunk itself => block ref in the chunk
            @Nullable
            public static final Ref<ChunkStore> getLocal_chunkRef(
                @Nonnull final Ref<ChunkStore> chunkRef,
                final int blockIndex
            ) {
                return Block.Ref_.getLocal1_chunkRef(chunkRef, blockIndex);
            }

            @Nullable
            private static final Ref<ChunkStore> getLocal1_chunkRef(
                @Nonnull final Ref<ChunkStore> chunkRef,
                final int blockIndex
            ) {
                final var blockComponentChunk = Component_.getBlockComponentChunk(chunkRef);
                if (blockComponentChunk == null) {
                    return null;
                }

                return Block.Ref_.getLocal0(blockComponentChunk, blockIndex);
            }

            // #endregion localChunkRef
            // #region localBlockStateInfo

            /**
             * Returns another block based on its local coords WITHIN THE SAME CHUNK
             */
            // BlockInfo => ChunkRef => component ON the chunk itself => block ref in the chunk
            @Nullable
            public static final Ref<ChunkStore> getLocal(
                @Nonnull final BlockStateInfo info,
                @Nonnull final Vector3i blockCoords
            ) {
                final var blockComponentChunk = Component_.getBlockComponentChunk(info.getChunkRef());
                if (blockComponentChunk == null) {
                    return null;
                }

                return Block.Ref_.getLocal0(blockComponentChunk, BlockCoords.Index.get(blockCoords));
            }

            /**
             * Returns another block based on its local coords WITHIN THE SAME CHUNK
             */
            // BlockInfo => ChunkRef => component ON the chunk itself => block ref in the chunk
            @Nullable
            public static final Ref<ChunkStore> getLocal(
                @Nonnull final BlockStateInfo info,
                final int blockX,
                final int blockY,
                final int blockZ
            ) {
                final var blockComponentChunk = Component_.getBlockComponentChunk(info.getChunkRef());
                if (blockComponentChunk == null) {
                    return null;
                }

                return Block.Ref_.getLocal0(blockComponentChunk, BlockCoords.Index.get(blockX, blockY, blockZ));
            }

            /**
             * Returns another block based on its local coords WITHIN THE SAME CHUNK
             */
            // BlockInfo => ChunkRef => component ON the chunk itself => block ref in the chunk
            @Nullable
            public static final Ref<ChunkStore> getLocal(@Nonnull final BlockStateInfo info, final int blockIndex) {
                final var blockComponentChunk = Component_.getBlockComponentChunk(info.getChunkRef());
                if (blockComponentChunk == null) {
                    return null;
                }

                return Block.Ref_.getLocal0(blockComponentChunk, blockIndex);
            }

            // #endregion localBlockStateInfo
            // #region WorldChunk

            @Nullable
            public static final Ref<ChunkStore> get(
                @Nonnull final WorldChunk worldChunk,
                @Nonnull final Vector3i blockCoords
            ) {
                return Block.Ref_.get2(
                    worldChunk.getReference().getStore().getExternalData(),
                    ChunkCoords.Index.get(blockCoords),
                    BlockCoords.Index.get(blockCoords)
                );
            }

            /** SHOULD REALLY BE CALLED GET LOCAL REF */
            // component ON the chunk itself => block ref in the chunk
            @Nullable
            public static final Ref<ChunkStore> get(
                @Nonnull final WorldChunk worldChunk,
                final int blockX,
                final int blockY,
                final int blockZ
            ) {
                return Block.Ref_.get2(
                    worldChunk.getReference().getStore().getExternalData(),
                    ChunkCoords.Index.get(blockX, blockZ),
                    BlockCoords.Index.get(blockX, blockY, blockZ)
                );
            }

            @Nullable
            public static final Ref<ChunkStore> get(
                @Nonnull final WorldChunk worldChunk,
                final long chunkIndex,
                final int blockIndex
            ) {
                return Block.Ref_.get2(worldChunk.getReference().getStore().getExternalData(), chunkIndex, blockIndex);
            }

            @Nullable
            public static final Ref<ChunkStore> getLocal(
                @Nonnull final WorldChunk worldChunk,
                @Nonnull final Vector3i blockCoords
            ) {
                return worldChunk.getBlockComponentEntity(blockCoords.x, blockCoords.y, blockCoords.z);
            }

            /** SHOULD REALLY BE CALLED GET LOCAL REF */
            // component ON the chunk itself => block ref in the chunk
            @Nullable
            public static final Ref<ChunkStore> getLocal(
                @Nonnull final WorldChunk worldChunk,
                final int blockX,
                final int blockY,
                final int blockZ
            ) {
                return worldChunk.getBlockComponentEntity(blockX, blockY, blockZ);
            }

            @Nullable
            public static final Ref<ChunkStore> getLocal(@Nonnull final WorldChunk worldChunk, final int blockIndex) {
                final var localCoords = BlockCoords.Local.get(blockIndex);
                return worldChunk.getBlockComponentEntity(localCoords.x, localCoords.y, localCoords.z);
            }

            // #endregion WorldChunk
            // #region BlockComponentChunk
            @Nullable
            public static final Ref<ChunkStore> get(
                @Nonnull final BlockComponentChunk blockComponentChunk,
                final int blockIndex
            ) {
                return Block.Ref_.getLocal0(blockComponentChunk, blockIndex);
            }

            @Nullable
            public static final Ref<ChunkStore> getLocal(
                @Nonnull final BlockComponentChunk blockComponentChunk,
                @Nonnull final Vector3i blockCoords
            ) {
                return Block.Ref_.getLocal0(blockComponentChunk, BlockCoords.Index.get(blockCoords));
            }

            @Nullable
            public static final Ref<ChunkStore> getLocal(
                @Nonnull final BlockComponentChunk blockComponentChunk,
                final int blockX,
                final int blockY,
                final int blockZ
            ) {
                return Block.Ref_.getLocal0(blockComponentChunk, BlockCoords.Index.get(blockX, blockY, blockZ));
            }

            @Nullable
            public static final Ref<ChunkStore> getLocal(
                @Nonnull final BlockComponentChunk blockComponentChunk,
                final int blockIndex
            ) {
                return Block.Ref_.getLocal0(blockComponentChunk, blockIndex);
            }

            @Nullable
            private static final Ref<ChunkStore> getLocal0(
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
                final var localIndex = BlockCoords.Index.get(blockX, blockY, blockZ);
                final var localCoords = BlockCoords.Local.get(localIndex);
                final var blockCoords = new Vector3i(blockX, blockY, blockZ);
                final var chunkIndex = ChunkCoords.Index.get(blockCoords);

                final var test = new TestUtil(commandBuffer, blockCoords);
                // functions to test
                final ArrayList<BlockStateInfo> refs = new ArrayList<>();

                // WorldProvider
                refs.add(Block.Info.get(test.worldProvider, blockCoords));
                refs.add(Block.Info.get(test.worldProvider, blockX, blockY, blockZ));
                refs.add(Block.Info.get(test.worldProvider, chunkIndex, localIndex));

                // World
                refs.add(Block.Info.get(test.world, blockCoords));
                refs.add(Block.Info.get(test.world, blockX, blockY, blockZ));
                refs.add(Block.Info.get(test.world, chunkIndex, localIndex));

                // CommandBuffer
                refs.add(Block.Info.get(test.commandBuffer, blockCoords));
                refs.add(Block.Info.get(test.commandBuffer, blockX, blockY, blockZ));
                refs.add(Block.Info.get(test.commandBuffer, chunkIndex, localIndex));

                // BlockStateInfo
                refs.add(Block.Info.get(test.info, blockCoords));
                refs.add(Block.Info.get(test.info, blockX, blockY, blockZ));
                refs.add(Block.Info.get(test.info, chunkIndex, localIndex));

                // ChunkRef (global)
                // - without specifying that it's a chunk ref we provided, letting the code figure that out
                refs.add(Block.Info.get(test.chunkRef, blockCoords));
                refs.add(Block.Info.get(test.chunkRef, blockX, blockY, blockZ));
                refs.add(Block.Info.get(test.chunkRef, chunkIndex, localIndex));
                // - specifying it's a chunk ref
                refs.add(Block.Info.get_chunkRef(test.chunkRef, blockCoords));
                refs.add(Block.Info.get_chunkRef(test.chunkRef, blockX, blockY, blockZ));
                refs.add(Block.Info.get_chunkRef(test.chunkRef, chunkIndex, localIndex));

                // ChunkRef (local)
                // - without specifying that it's a chunk ref we provided, letting the code figure that out
                refs.add(Block.Info.getLocal(test.chunkRef, localCoords));
                refs.add(Block.Info.getLocal(test.chunkRef, localCoords.x, localCoords.y, localCoords.z));
                refs.add(Block.Info.getLocal(test.chunkRef, localIndex));
                // - specifying it's a chunk ref
                refs.add(Block.Info.getLocal_chunkRef(test.chunkRef, localCoords));
                refs.add(Block.Info.getLocal_chunkRef(test.chunkRef, localCoords.x, localCoords.y, localCoords.z));
                refs.add(Block.Info.getLocal_chunkRef(test.chunkRef, localIndex));

                // Store
                refs.add(Block.Info.get(test.store, blockCoords));
                refs.add(Block.Info.get(test.store, blockX, blockY, blockZ));
                refs.add(Block.Info.get(test.store, chunkIndex, localIndex));

                // ChunkStore
                refs.add(Block.Info.get(test.chunkStore, blockCoords));
                refs.add(Block.Info.get(test.chunkStore, blockX, blockY, blockZ));
                refs.add(Block.Info.get(test.chunkStore, chunkIndex, localIndex));

                // BlockRef
                refs.add(Block.Info.get_blockRef(test.blockRef, blockCoords));
                refs.add(Block.Info.get_blockRef(test.blockRef, blockX, blockY, blockZ));
                refs.add(Block.Info.get_chunkRef(test.blockRef, chunkIndex, localIndex));

                // WorldChunk
                refs.add(Block.Info.get(test.worldChunk, blockCoords));
                refs.add(Block.Info.get(test.worldChunk, blockX, blockY, blockZ));
                refs.add(Block.Info.get(test.worldChunk, chunkIndex, localIndex));
                refs.add(Block.Info.getLocal(test.worldChunk, blockCoords));
                refs.add(Block.Info.getLocal(test.worldChunk, blockX, blockY, blockZ));
                refs.add(Block.Info.getLocal(test.worldChunk, localIndex));

                // BlockComponentChunk
                refs.add(Block.Info.get(test.blockComponentChunk, localIndex));
                refs.add(Block.Info.getLocal(test.blockComponentChunk, blockCoords));
                refs.add(Block.Info.getLocal(test.blockComponentChunk, blockX, blockY, blockZ));
                refs.add(Block.Info.getLocal(test.blockComponentChunk, localIndex));

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
            public static final BlockStateInfo get_blockRef(
                @Nonnull final Ref<ChunkStore> blockRef,
                @Nonnull final Vector3i coords
            ) {
                return Block.Info.get0(Block.Ref_.get_blockRef(blockRef, coords));
            }

            @Nullable
            public static final BlockStateInfo get_blockRef(
                @Nonnull final Ref<ChunkStore> blockRef,
                final int x,
                final int y,
                final int z
            ) {
                return Block.Info.get0(Block.Ref_.get_blockRef(blockRef, x, y, z));
            }

            @Nullable
            public static final BlockStateInfo get_blockRef(
                @Nonnull final Ref<ChunkStore> blockRef,
                final long chunkIndex,
                final int blockIndex
            ) {
                return Block.Info.get0(Block.Ref_.get_blockRef(blockRef, chunkIndex, blockIndex));
            }

            @Nullable
            public static final BlockStateInfo get_blockRef(
                @Nonnull final Ref<ChunkStore> blockRef,
                final int blockIndex
            ) {
                return Block.Info.get0(Block.Ref_.getLocal(blockRef, blockIndex));
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
            public static final BlockStateInfo get(
                @Nonnull final WorldProvider worldProvider,
                @Nonnull final Vector3i pos
            ) {
                // => BlockRef
                return Block.Info.get0(Block.Ref_.get(worldProvider, pos));
            }

            @Nullable
            public static final BlockStateInfo get(
                @Nonnull final WorldProvider worldProvider,
                final int x,
                final int y,
                final int z
            ) {
                // => BlockRef
                return Block.Info.get0(Block.Ref_.get(worldProvider, x, y, z));
            }

            @Nullable
            public static final BlockStateInfo get(
                @Nonnull final WorldProvider worldProvider,
                final long chunkIndex,
                final int blockIndex
            ) {
                // => BlockRef
                return Block.Info.get0(Block.Ref_.get(worldProvider, chunkIndex, blockIndex));
            }

            // #endregion WorldProvider
            // #region World

            @Nullable
            public static final BlockStateInfo get(@Nonnull final World world, @Nonnull final Vector3i pos) {
                // => BlockRef
                return Block.Info.get0(Block.Ref_.get(world, pos));
            }

            @Nullable
            public static final BlockStateInfo get(@Nonnull final World world, final int x, final int y, final int z) {
                // => BlockRef
                return Block.Info.get0(Block.Ref_.get(world, x, y, z));
            }

            @Nullable
            public static final BlockStateInfo get(
                @Nonnull final World world,
                final long chunkIndex,
                final int blockIndex
            ) {
                // => BlockRef
                return Block.Info.get0(Block.Ref_.get(world, chunkIndex, blockIndex));
            }

            // #endregion World
            // #region CommandBuffer

            @Nullable
            public static final BlockStateInfo get(
                @Nonnull final CommandBuffer<ChunkStore> commandBuffer,
                @Nonnull final Vector3i pos
            ) {
                // => BlockRef
                return Block.Info.get0(Block.Ref_.get(commandBuffer, pos));
            }

            @Nullable
            public static final BlockStateInfo get(
                @Nonnull final CommandBuffer<ChunkStore> commandBuffer,
                final int x,
                final int y,
                final int z
            ) {
                // => BlockRef
                return Block.Info.get0(Block.Ref_.get(commandBuffer, x, y, z));
            }

            @Nullable
            public static final BlockStateInfo get(
                @Nonnull final CommandBuffer<ChunkStore> commandBuffer,
                final long chunkIndex,
                final int blockIndex
            ) {
                // => BlockRef
                return Block.Info.get0(Block.Ref_.get(commandBuffer, chunkIndex, blockIndex));
            }

            // #endregion CommandBuffer
            // #region BlockStateInfo

            @Nullable
            public static final BlockStateInfo get(
                @Nonnull final BlockStateInfo info,
                @Nonnull final Vector3i blockCoords
            ) {
                return Block.Info.get0(Block.Ref_.get(info, blockCoords));
            }

            @Nullable
            public static final BlockStateInfo get(
                @Nonnull final BlockStateInfo info,
                final int blockX,
                final int blockY,
                final int blockZ
            ) {
                return Block.Info.get0(Block.Ref_.get(info, blockX, blockY, blockZ));
            }

            @Nullable
            public static final BlockStateInfo get(
                @Nonnull final BlockStateInfo info,
                final long chunkIndex,
                final int blockIndex
            ) {
                return Block.Info.get0(Block.Ref_.get(info, chunkIndex, blockIndex));
            }

            // #endregion BlockStateInfo
            // #region anyRef
            @Nullable
            public static final BlockStateInfo get(@Nonnull final Ref<ChunkStore> ref, @Nonnull final Vector3i coords) {
                // Potential: the ref you passed me is a BLOCK ref. slay. thats the good shit. that's what we're after
                final var info = Block.Info.get(ref);
                if (info != null) {
                    return info;
                }

                // If there wasn't a block state info on your ref, we know its a chunk ref
                return get_chunkRef(ref, coords);
            }

            @Nullable
            public static final BlockStateInfo get(
                @Nonnull final Ref<ChunkStore> ref,
                final int blockX,
                final int blockY,
                final int blockZ
            ) {
                // Potential: the ref you passed me is a BLOCK ref. slay. thats the good shit. that's what we're after
                final var info = Block.Info.get(ref);
                if (info != null) {
                    return info;
                }

                // If there wasn't a block state info on your ref, we know its a chunk ref
                return Block.Info.get_chunkRef(ref, blockX, blockY, blockZ);
            }

            @Nullable
            public static final BlockStateInfo get(
                @Nonnull final Ref<ChunkStore> ref,
                final long chunkIndex,
                final int blockIndex
            ) {
                // Potential: the ref you passed me is a BLOCK ref. slay. thats the good shit. that's what we're after
                final var info = Block.Info.get(ref);
                if (info != null) {
                    return info;
                }

                // If there wasn't a block state info on your ref, we know its a chunk ref
                return Block.Info.get_chunkRef(ref, chunkIndex, blockIndex);
            }

            // #endregion anyRef
            // #region Ref<ChunkStore> (ChunkRef)

            @Nullable
            public static final BlockStateInfo get_chunkRef(
                @Nonnull final Ref<ChunkStore> chunkRef,
                @Nonnull final Vector3i coords
            ) {
                return Block.Info.get0(Block.Ref_.get_chunkRef(chunkRef, coords));
            }

            @Nullable
            public static final BlockStateInfo get_chunkRef(
                @Nonnull final Ref<ChunkStore> chunkRef,
                final int x,
                final int y,
                final int z
            ) {
                return Block.Info.get0(Block.Ref_.get_chunkRef(chunkRef, x, y, z));
            }

            @Nullable
            public static final BlockStateInfo get_chunkRef(
                @Nonnull final Ref<ChunkStore> chunkRef,
                final long chunkIndex,
                final int blockIndex
            ) {
                return Block.Info.get0(Block.Ref_.get_chunkRef(chunkRef, chunkIndex, blockIndex));
            }

            // #endregion Ref<ChunkStore>
            // #region WorldChunk

            @Nullable
            public static final BlockStateInfo get(
                @Nonnull final WorldChunk worldChunk,
                @Nonnull final Vector3i blockCoords
            ) {
                final var chunkRef = worldChunk.getReference();
                if (chunkRef == null) {
                    return null;
                }

                return Block.Info.get0(Block.Ref_.get_chunkRef(chunkRef, blockCoords));
            }

            @Nullable
            public static final BlockStateInfo get(
                @Nonnull final WorldChunk worldChunk,
                final int blockX,
                final int blockY,
                final int blockZ
            ) {
                final var chunkRef = worldChunk.getReference();
                if (chunkRef == null) {
                    return null;
                }
                return Block.Info.get0(Block.Ref_.get_chunkRef(chunkRef, blockX, blockY, blockZ));
            }

            @Nullable
            public static final BlockStateInfo get(
                @Nonnull final WorldChunk worldChunk,
                final long chunkIndex,
                final int blockIndex
            ) {
                final var chunkRef = worldChunk.getReference();
                if (chunkRef == null) {
                    return null;
                }
                return Block.Info.get0(Block.Ref_.get_chunkRef(chunkRef, chunkIndex, blockIndex));
            }

            @Nullable
            public static final BlockStateInfo getLocal(
                @Nonnull final WorldChunk worldChunk,
                @Nonnull final Vector3i blockCoords
            ) {
                return Block.Info.get(worldChunk.getBlockComponentEntity(blockCoords.x, blockCoords.y, blockCoords.z));
            }

            @Nullable
            public static final BlockStateInfo getLocal(
                @Nonnull final WorldChunk worldChunk,
                final int blockX,
                final int blockY,
                final int blockZ
            ) {
                return Block.Info.get(worldChunk.getBlockComponentEntity(blockX, blockY, blockZ));
            }

            @Nullable
            public static final BlockStateInfo getLocal(@Nonnull final WorldChunk worldChunk, final int blockIndex) {
                final var localCoords = BlockCoords.Local.get(blockIndex);
                return Block.Info.get(worldChunk.getBlockComponentEntity(localCoords.x, localCoords.y, localCoords.z));
            }

            // #endregion WorldChunk
            // #region BlockComponentChunk

            @Nullable
            public static final BlockStateInfo get(
                @Nonnull final BlockComponentChunk blockComponentChunk,
                final int blockIndex
            ) {
                return blockComponentChunk.getComponent(blockIndex, BLOCK_STATE_INFO_COMPONENT);
            }

            @Nullable
            public static final BlockStateInfo getLocal(
                @Nonnull final BlockComponentChunk blockComponentChunk,
                @Nonnull final Vector3i blockCoords
            ) {
                final int blockIndex = BlockCoords.Index.get(blockCoords);
                return blockComponentChunk.getComponent(blockIndex, BLOCK_STATE_INFO_COMPONENT);
            }

            @Nullable
            public static final BlockStateInfo getLocal(
                @Nonnull final BlockComponentChunk blockComponentChunk,
                final int blockX,
                final int blockY,
                final int blockZ
            ) {
                final int blockIndex = BlockCoords.Index.get(blockX, blockY, blockZ);
                return blockComponentChunk.getComponent(blockIndex, BLOCK_STATE_INFO_COMPONENT);
            }

            @Nullable
            public static final BlockStateInfo getLocal(
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
            public static final BlockStateInfo get(
                @Nonnull final Store<ChunkStore> store,
                @Nonnull final Vector3i pos
            ) {
                // => BlockRef
                return Block.Info.get0(Block.Ref_.get(store, pos));
            }

            @Nullable
            public static final BlockStateInfo get(
                @Nonnull final Store<ChunkStore> store,
                final int x,
                final int y,
                final int z
            ) {
                // => BlockRef
                return Block.Info.get0(Block.Ref_.get(store, x, y, z));
            }

            @Nullable
            public static final BlockStateInfo get(
                @Nonnull final Store<ChunkStore> store,
                final long chunkIndex,
                final int blockIndex
            ) {
                // => BlockRef
                return Block.Info.get0(Block.Ref_.get(store, chunkIndex, blockIndex));
            }

            // #endregion Store<ChunkStore>
            // #region ChunkStore

            @Nullable
            public static final BlockStateInfo get(@Nonnull final ChunkStore chunkStore, @Nonnull final Vector3i pos) {
                // => BlockRef
                return Block.Info.get0(Block.Ref_.get(chunkStore, pos));
            }

            @Nullable
            public static final BlockStateInfo get(
                @Nonnull final ChunkStore chunkStore,
                final int x,
                final int y,
                final int z
            ) {
                // => BlockRef
                return Block.Info.get0(Block.Ref_.get(chunkStore, x, y, z));
            }

            @Nullable
            public static final BlockStateInfo get(
                @Nonnull final ChunkStore chunkStore,
                final long chunkIndex,
                final int blockIndex
            ) {
                // => BlockRef
                return Block.Info.get0(Block.Ref_.get(chunkStore, chunkIndex, blockIndex));
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
            public static final BlockStateInfo getLocal_chunkRef(
                @Nonnull final Ref<ChunkStore> chunkRef,
                @Nonnull final Vector3i blockCoords
            ) {
                return Block.Info.get0(Block.Ref_.getLocal_chunkRef(chunkRef, blockCoords));
            }

            // ChunkRef => component ON the chunk itself => block ref in the chunk
            @Nullable
            public static final BlockStateInfo getLocal_chunkRef(
                @Nonnull final Ref<ChunkStore> chunkRef,
                final int blockX,
                final int blockY,
                final int blockZ
            ) {
                return Block.Info.get0(Block.Ref_.getLocal_chunkRef(chunkRef, blockX, blockY, blockZ));
            }

            // ChunkRef => component ON the chunk itself => block ref in the chunk
            @Nullable
            public static final BlockStateInfo getLocal_chunkRef(
                @Nonnull final Ref<ChunkStore> chunkRef,
                final int blockIndex
            ) {
                return Block.Info.get0(Block.Ref_.getLocal_chunkRef(chunkRef, blockIndex));
            }

            // #endregion localChunkRef
            // #region localBlockRef

            @Nullable
            public static final BlockStateInfo getLocal_blockRef(
                @Nonnull final Ref<ChunkStore> blockRef,
                @Nonnull final Vector3i blockCoords
            ) {
                return Block.Info.get0(Block.Ref_.getLocal_blockRef(blockRef, blockCoords));
            }

            // ChunkRef => component ON the chunk itself => block ref in the chunk
            @Nullable
            public static final BlockStateInfo getLocal_blockRef(
                @Nonnull final Ref<ChunkStore> blockRef,
                final int blockX,
                final int blockY,
                final int blockZ
            ) {
                return Block.Info.get0(Block.Ref_.getLocal_blockRef(blockRef, blockX, blockY, blockZ));
            }

            // ChunkRef => component ON the chunk itself => block ref in the chunk
            @Nullable
            public static final BlockStateInfo getLocal_blockRef(
                @Nonnull final Ref<ChunkStore> blockRef,
                final int blockIndex
            ) {
                return Block.Info.get0(Block.Ref_.getLocal_blockRef(blockRef, blockIndex));
            }

            // #endregion localBlockRef
            // #region anyLocalRef

            @Nullable
            public static final BlockStateInfo getLocal(
                @Nonnull final Ref<ChunkStore> ref,
                @Nonnull final Vector3i blockCoords
            ) {
                if (Block.isBlockRef(ref)) {
                    return Block.Info.getLocal_blockRef(ref, blockCoords);
                } else {
                    return Block.Info.getLocal_chunkRef(ref, blockCoords);
                }
            }

            // ChunkRef => component ON the chunk itself => block ref in the chunk
            @Nullable
            public static final BlockStateInfo getLocal(
                @Nonnull final Ref<ChunkStore> ref,
                final int blockX,
                final int blockY,
                final int blockZ
            ) {
                if (Block.isBlockRef(ref)) {
                    return Block.Info.getLocal_blockRef(ref, blockX, blockY, blockZ);
                } else {
                    return Block.Info.getLocal_chunkRef(ref, blockX, blockY, blockZ);
                }
            }

            // ChunkRef => component ON the chunk itself => block ref in the chunk
            @Nullable
            public static final BlockStateInfo getLocal(@Nonnull final Ref<ChunkStore> ref, final int blockIndex) {
                if (Block.isBlockRef(ref)) {
                    return Block.Info.getLocal_blockRef(ref, blockIndex);
                } else {
                    return Block.Info.getLocal_chunkRef(ref, blockIndex);
                }
            }

            // #endregion anyLocalRef
            // #region localBlockStateInfo

            /**
             * Returns another block based on its local coords WITHIN THE SAME CHUNK
             */
            // BlockInfo => ChunkRef => component ON the chunk itself => block ref in the chunk
            @Nullable
            public static final BlockStateInfo getLocal(
                @Nonnull final BlockStateInfo info,
                @Nonnull final Vector3i blockCoords
            ) {
                return Block.Info.get0(Block.Ref_.getLocal(info, blockCoords));
            }

            /**
             * Returns another block based on its local coords WITHIN THE SAME CHUNK
             */
            // BlockInfo => ChunkRef => component ON the chunk itself => block ref in the chunk
            @Nullable
            public static final BlockStateInfo getLocal(
                @Nonnull final BlockStateInfo info,
                final int blockX,
                final int blockY,
                final int blockZ
            ) {
                return Block.Info.get0(Block.Ref_.getLocal(info, blockX, blockY, blockZ));
            }

            /**
             * Returns another block based on its local coords WITHIN THE SAME CHUNK
             */
            // BlockInfo => ChunkRef => component ON the chunk itself => block ref in the chunk
            @Nullable
            public static final BlockStateInfo getLocal(@Nonnull final BlockStateInfo info, final int blockIndex) {
                return Block.Info.get0(Block.Ref_.getLocal(info, blockIndex));
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
            public static final BlockStateInfo get(@Nullable final Ref<ChunkStore> blockRef) {
                return Block.Info.get0(blockRef);
            }

            // BlockRef
            @Nullable
            private static final BlockStateInfo get0(@Nullable final Ref<ChunkStore> blockRef) {
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
                @Nonnull final Vector3i providedCoords,
                @Nonnull final String blockId
            ) {
                final var blockX = providedCoords.x;
                final var blockY = providedCoords.y;
                final var blockZ = providedCoords.z;
                final var localIndex = BlockCoords.Index.get(blockX, blockY, blockZ);
                final var blockIndex = localIndex;
                final var blockCoords = new Vector3i(blockX, blockY, blockZ);
                final var chunkIndex = ChunkCoords.Index.get(blockCoords);

                final var test = new TestUtil(commandBuffer, blockCoords);

                // functions to test
                final ArrayList<Integer> refs = new ArrayList<>();

                // WorldProvider
                refs.add(Block.Id.get(test.worldProvider, blockCoords));
                refs.add(Block.Id.get(test.worldProvider, blockCoords.x, blockCoords.y, blockCoords.z));
                refs.add(Block.Id.get(test.worldProvider, chunkIndex, blockIndex));

                //  World
                refs.add(Block.Id.get(test.world, blockCoords));
                refs.add(Block.Id.get(test.world, blockCoords.x, blockCoords.y, blockCoords.z));
                refs.add(Block.Id.get(test.world, chunkIndex, blockIndex));

                //  CommandBuffer
                refs.add(Block.Id.get(test.commandBuffer, blockCoords));
                refs.add(Block.Id.get(test.commandBuffer, blockCoords.x, blockCoords.y, blockCoords.z)); // test 7 failed
                refs.add(Block.Id.get(test.commandBuffer, chunkIndex, blockIndex));

                //  Store<ChunkStore>
                refs.add(Block.Id.get(test.store, blockCoords));
                refs.add(Block.Id.get(test.store, blockCoords.x, blockCoords.y, blockCoords.z)); // test 10 failed
                refs.add(Block.Id.get(test.store, chunkIndex, blockIndex));

                //  ChunkStore
                refs.add(Block.Id.get(test.chunkStore, blockCoords));
                refs.add(Block.Id.get(test.chunkStore, blockCoords.x, blockCoords.y, blockCoords.z)); // test 13 failed
                refs.add(Block.Id.get(test.chunkStore, chunkIndex, blockIndex));

                //  Ref<ChunkStore>
                refs.add(Block.Id.get(test.chunkRef, blockCoords));
                refs.add(Block.Id.get(test.chunkRef, blockCoords.x, blockCoords.y, blockCoords.z)); // test 16 failed
                refs.add(Block.Id.get(test.chunkRef, chunkIndex, blockIndex));
                refs.add(Block.Id.get(blockId));

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
            public static final Integer get(
                @Nonnull final WorldProvider worldProvider,
                @Nonnull final Vector3i blockCoords
            ) {
                final var worldChunk = worldProvider.getWorld().getChunk(ChunkCoords.Index.get(blockCoords));
                if (worldChunk == null) {
                    return null;
                }

                return worldChunk.getBlock(blockCoords);
            }

            @Nullable
            public static final Integer get(
                @Nonnull final WorldProvider worldProvider,
                final int blockX,
                final int blockY,
                final int blockZ
            ) {
                final var worldChunk = worldProvider.getWorld().getChunk(ChunkCoords.Index.get(blockX, blockZ));
                if (worldChunk == null) {
                    return null;
                }

                return worldChunk.getBlock(blockX, blockY, blockZ);
            }

            @Nullable
            public static final Integer get(
                @Nonnull final WorldProvider worldProvider,
                final long chunkIndex,
                final int blockIndex
            ) {
                final var worldChunk = worldProvider.getWorld().getChunk(chunkIndex);
                if (worldChunk == null) {
                    return null;
                }

                return worldChunk.getBlock(BlockCoords.Local.get(blockIndex));
            }

            // #endregion WorldProvider
            // #region World
            // ====================================================================
            // World  =>  ChunkStore
            // + Global coords
            // ====================================================================

            @Nullable
            public static final Integer get(@Nonnull final World world, @Nonnull final Vector3i blockCoords) {
                final var worldChunk = world.getChunk(ChunkCoords.Index.get(blockCoords));
                if (worldChunk == null) {
                    return null;
                }

                return worldChunk.getBlock(blockCoords);
            }

            @Nullable
            public static final Integer get(
                @Nonnull final World world,
                final int blockX,
                final int blockY,
                final int blockZ
            ) {
                final var worldChunk = world.getChunk(ChunkCoords.Index.get(blockX, blockZ));
                if (worldChunk == null) {
                    return null;
                }

                return worldChunk.getBlock(blockX, blockY, blockZ);
            }

            @Nullable
            public static final Integer get(@Nonnull final World world, final long chunkIndex, final int blockIndex) {
                final var worldChunk = world.getChunk(chunkIndex);
                if (worldChunk == null) {
                    return null;
                }

                return worldChunk.getBlock(BlockCoords.Local.get(blockIndex));
            }

            // #endregion World
            // #region CommandBuffer
            // ====================================================================
            // CommandBuffer  =>  ChunkStore
            // + Global coords
            // ====================================================================

            @Nullable
            public static final Integer get(
                @Nonnull final CommandBuffer<ChunkStore> commandBuffer,
                @Nonnull final Vector3i blockCoords
            ) {
                final var worldChunk = Chunk.WorldChunk_.get(commandBuffer, blockCoords);
                if (worldChunk == null) {
                    return null;
                }

                return worldChunk.getBlock(blockCoords);
            }

            @Nullable
            public static final Integer get(
                @Nonnull final CommandBuffer<ChunkStore> commandBuffer,
                final int blockX,
                final int blockY,
                final int blockZ
            ) {
                final var worldChunk = Chunk.WorldChunk_.get(commandBuffer, blockX, blockZ);
                if (worldChunk == null) {
                    return null;
                }

                return worldChunk.getBlock(blockX, blockY, blockZ);
            }

            @Nullable
            public static final Integer get(
                @Nonnull final CommandBuffer<ChunkStore> commandBuffer,
                final long chunkIndex,
                final int blockIndex
            ) {
                final var worldChunk = Chunk.WorldChunk_.get(commandBuffer, chunkIndex);
                if (worldChunk == null) {
                    return null;
                }

                return worldChunk.getBlock(BlockCoords.Local.get(blockIndex));
            }

            // #endregion CommandBuffer
            // #region Store<ChunkStore>
            // ====================================================================
            // Store<ChunkStore>  =>  ChunkStore
            // + Global coords
            // ====================================================================

            @Nullable
            public static final Integer get(
                @Nonnull final Store<ChunkStore> store,
                @Nonnull final Vector3i blockCoords
            ) {
                final var worldChunk = Chunk.WorldChunk_.get(store, blockCoords);
                if (worldChunk == null) {
                    return null;
                }

                return worldChunk.getBlock(blockCoords);
            }

            @Nullable
            public static final Integer get(
                @Nonnull final Store<ChunkStore> store,
                final int blockX,
                final int blockY,
                final int blockZ
            ) {
                final var worldChunk = Chunk.WorldChunk_.get(store, blockX, blockZ);
                if (worldChunk == null) {
                    return null;
                }

                return worldChunk.getBlock(blockX, blockY, blockZ);
            }

            @Nullable
            public static final Integer get(
                @Nonnull final Store<ChunkStore> store,
                final long chunkIndex,
                final int blockIndex
            ) {
                final var worldChunk = Chunk.WorldChunk_.get(store, chunkIndex);
                if (worldChunk == null) {
                    return null;
                }

                return worldChunk.getBlock(BlockCoords.Local.get(blockIndex));
            }

            // #endregion Store<ChunkStore>
            // #region ChunkStore
            // ====================================================================
            // ChunkStore  =>  ChunkRef
            // + Global coords OR Chunk Coords
            // ====================================================================

            @Nullable
            public static final Integer get(@Nonnull final ChunkStore chunkStore, @Nonnull final Vector3i blockCoords) {
                final var worldChunk = Chunk.WorldChunk_.get(chunkStore, blockCoords);
                if (worldChunk == null) {
                    return null;
                }

                return worldChunk.getBlock(blockCoords);
            }

            @Nullable
            public static final Integer get(
                @Nonnull final ChunkStore chunkStore,
                final int blockX,
                final int blockY,
                final int blockZ
            ) {
                final var worldChunk = Chunk.WorldChunk_.get(chunkStore, blockX, blockZ);
                if (worldChunk == null) {
                    return null;
                }

                return worldChunk.getBlock(blockX, blockY, blockZ);
            }

            @Nullable
            public static final Integer get(
                @Nonnull final ChunkStore chunkStore,
                final long chunkIndex,
                final int blockIndex
            ) {
                final var worldChunk = Chunk.WorldChunk_.get(chunkStore, chunkIndex);
                if (worldChunk == null) {
                    return null;
                }

                return worldChunk.getBlock(BlockCoords.Local.get(blockIndex));
            }

            // #endregion ChunkStore
            // #region Ref<ChunkStore>
            // ====================================================================
            // Ref<ChunkStore> (ChunkRef)  =>  BlockComponentChunk
            // + Global OR Local coords
            // ====================================================================

            @Nullable
            public static final Integer get(
                @Nonnull final Ref<ChunkStore> chunkRef,
                @Nonnull final Vector3i blockCoords
            ) {
                final var worldChunk = Chunk.WorldChunk_.get(chunkRef, blockCoords);
                if (worldChunk == null) {
                    return null;
                }

                return worldChunk.getBlock(blockCoords);
            }

            @Nullable
            public static final Integer get(
                @Nonnull final Ref<ChunkStore> chunkRef,
                final int blockX,
                final int blockY,
                final int blockZ
            ) {
                final var worldChunk = Chunk.WorldChunk_.get(chunkRef, blockX, blockZ);
                if (worldChunk == null) {
                    return null;
                }

                return worldChunk.getBlock(blockX, blockY, blockZ);
            }

            @Nullable
            public static final Integer get(
                @Nonnull final Ref<ChunkStore> chunkRef,
                final long chunkIndex,
                final int blockIndex
            ) {
                final var worldChunk = Chunk.WorldChunk_.get(chunkRef, chunkIndex);
                if (worldChunk == null) {
                    return null;
                }

                return worldChunk.getBlock(BlockCoords.Local.get(blockIndex));
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
            public static final int get(@Nonnull final String blockId) {
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

                refs.add(Block.Type.get("TEST_BlockType"));

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
            public static final BlockType get(@Nonnull final String blockId) {
                return BlockType.getAssetMap().getAsset(blockId);
            }
        }

        // TESTS ADDED AND VERIFIED
        public static final boolean set(final @Nonnull World world, final @Nonnull Vector3i blockCoords, int blockId) {
            var chunk = Chunk.WorldChunk_.get(world, blockCoords);
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
            var chunk = Chunk.WorldChunk_.get(world, blockCoords);
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
                // final var localCoords = BlockCoords.Local.get(localIndex);
                final var blockCoords = new Vector3i(blockX, blockY, blockZ);
                final var chunkIndex = ChunkCoords.Index.get(blockCoords);
                final var chunkCoords = ChunkCoords.Global.get(blockCoords);
                final var chunkX = chunkCoords.x;
                final var chunkZ = chunkCoords.z;

                final var test = new TestUtil(commandBuffer, blockCoords);
                // functions to test
                final ArrayList<WorldChunk> refs = new ArrayList<>();

                // getWorldChunk of a given item directly (super easy minimal version)
                refs.add(Chunk.WorldChunk_.get_chunkRef(test.chunkRef));
                refs.add(Chunk.WorldChunk_.get_blockRef(test.blockRef));
                // - with: block ref (auto)
                refs.add(Chunk.WorldChunk_.get(blockRef));
                // - with: chunk ref (auto)
                refs.add(Chunk.WorldChunk_.get(test.chunkRef));
                refs.add(Chunk.WorldChunk_.get(test.info));

                // WorldProvider
                refs.add(Chunk.WorldChunk_.get(test.worldProvider, test.blockChunk));
                refs.add(Chunk.WorldChunk_.get(test.worldProvider, blockX, blockZ));
                refs.add(Chunk.WorldChunk_.get(test.worldProvider, chunkIndex));
                refs.add(Chunk.WorldChunk_.get(test.worldProvider, blockCoords));
                refs.add(Chunk.WorldChunk_.get_chunkCoords(test.worldProvider, chunkX, chunkZ));

                // World
                refs.add(Chunk.WorldChunk_.get(test.world, test.blockChunk));
                refs.add(Chunk.WorldChunk_.get(test.world, blockX, blockZ));
                refs.add(Chunk.WorldChunk_.get(test.world, chunkIndex));
                refs.add(Chunk.WorldChunk_.get(test.world, blockCoords));
                refs.add(Chunk.WorldChunk_.get_chunkCoords(test.world, chunkX, chunkZ));

                // BlockRef
                refs.add(Chunk.WorldChunk_.get(blockRef, test.blockChunk));
                refs.add(Chunk.WorldChunk_.get(blockRef, blockX, blockZ));
                refs.add(Chunk.WorldChunk_.get(blockRef, chunkIndex));
                refs.add(Chunk.WorldChunk_.get(blockRef, blockCoords));
                refs.add(Chunk.WorldChunk_.get_chunkCoords(blockRef, chunkX, chunkZ));

                // Info
                refs.add(Chunk.WorldChunk_.get(test.info, test.blockChunk));
                refs.add(Chunk.WorldChunk_.get(test.info, blockX, blockZ));
                refs.add(Chunk.WorldChunk_.get(test.info, chunkIndex));
                refs.add(Chunk.WorldChunk_.get(test.info, blockCoords));
                refs.add(Chunk.WorldChunk_.get_chunkCoords(test.info, chunkX, chunkZ));

                // CommandBuffer
                refs.add(Chunk.WorldChunk_.get(test.commandBuffer, test.blockChunk));
                refs.add(Chunk.WorldChunk_.get(test.commandBuffer, blockX, blockZ));
                refs.add(Chunk.WorldChunk_.get(test.commandBuffer, chunkIndex));
                refs.add(Chunk.WorldChunk_.get(test.commandBuffer, blockCoords));
                refs.add(Chunk.WorldChunk_.get_chunkCoords(test.commandBuffer, chunkX, chunkZ));

                // Store
                refs.add(Chunk.WorldChunk_.get(test.store, test.blockChunk));
                refs.add(Chunk.WorldChunk_.get(test.store, blockX, blockZ));
                refs.add(Chunk.WorldChunk_.get(test.store, chunkIndex));
                refs.add(Chunk.WorldChunk_.get(test.store, blockCoords));
                refs.add(Chunk.WorldChunk_.get_chunkCoords(test.store, chunkX, chunkZ));

                // ChunKStore
                refs.add(Chunk.WorldChunk_.get(test.chunkStore, test.blockChunk));
                refs.add(Chunk.WorldChunk_.get(test.chunkStore, blockX, blockZ));
                refs.add(Chunk.WorldChunk_.get(test.chunkStore, chunkIndex));
                refs.add(Chunk.WorldChunk_.get(test.chunkStore, blockCoords));
                refs.add(Chunk.WorldChunk_.get_chunkCoords(test.chunkStore, chunkX, chunkZ));

                // final test -> does it matter really if we pass a chunk ref or a block ref?
                // update: NO HECKING WAY, it doesn't. wild.
                refs.add(Chunk.WorldChunk_.get(blockRef, chunkIndex));
                refs.add(Chunk.WorldChunk_.get(test.chunkRef, chunkIndex));

                return refs;
            }

            // #region getWorldChunk

            @Nullable
            public static final WorldChunk get_chunkRef(@Nonnull final Ref<ChunkStore> chunkRef) {
                return Component_.get(chunkRef, WORLD_CHUNK_COMPONENT);
            }

            @Nullable
            public static final WorldChunk get_blockRef(@Nonnull final Ref<ChunkStore> blockRef) {
                final var info = Block.Info.get(blockRef);
                if (info == null) {
                    return null;
                }

                return Component_.get(info.getChunkRef(), WORLD_CHUNK_COMPONENT);
            }

            @Nullable
            public static final WorldChunk get(@Nonnull final Ref<ChunkStore> anyRef) {
                // Potential 1: The ref you passed me is a CHUNK ref. slay. thats the good shit. that's what we're after
                var worldChunk = Component_.get(anyRef, WORLD_CHUNK_COMPONENT);
                if (worldChunk != null) {
                    return worldChunk;
                }

                // Potential 2: it's a block, otherwise i've got no clue what's going on
                return get_blockRef(anyRef);
            }

            @Nullable
            public static final WorldChunk get(@Nonnull final BlockStateInfo info) {
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
            public static final WorldChunk get(
                @Nonnull final WorldProvider worldProvider,
                @Nonnull final BlockChunk blockChunk
            ) {
                return Chunk.WorldChunk_.get(worldProvider, blockChunk.getIndex());
            }

            @Nullable
            public static final WorldChunk get(
                @Nonnull final WorldProvider worldProvider,
                final int blockX,
                final int blockZ
            ) {
                return Chunk.WorldChunk_.get(worldProvider, ChunkCoords.Index.get(blockX, blockZ));
            }

            @Nullable
            public static final WorldChunk get(
                @Nonnull final WorldProvider worldProvider,
                @Nonnull final Vector3i blockCoords
            ) {
                return Chunk.WorldChunk_.get(worldProvider, ChunkCoords.Index.get(blockCoords));
            }

            @Nullable
            public static final WorldChunk get_chunkCoords(
                @Nonnull final WorldProvider worldProvider,
                final int chunkX,
                final int chunkZ
            ) {
                return Chunk.WorldChunk_.get(worldProvider, ChunkCoords.Index.get_chunkCoords(chunkX, chunkZ));
            }

            @Nullable
            public static final WorldChunk get(@Nonnull final WorldProvider worldProvider, final long chunkIndex) {
                return worldProvider.getWorld().getChunk(chunkIndex);
            }

            //   #endregion WorldProvider
            //   #region World
            //   ==================================================================
            //   World                -> WorldChunk
            //   ==================================================================

            @Nullable
            public static final WorldChunk get(@Nonnull final World world, @Nonnull final BlockChunk blockChunk) {
                return Chunk.WorldChunk_.get(world, blockChunk.getIndex());
            }

            @Nullable
            public static final WorldChunk get(@Nonnull final World world, final int blockX, final int blockZ) {
                return Chunk.WorldChunk_.get(world, ChunkCoords.Index.get(blockX, blockZ));
            }

            @Nullable
            public static final WorldChunk get(@Nonnull final World world, @Nonnull final Vector3i blockCoords) {
                return Chunk.WorldChunk_.get(world, ChunkCoords.Index.get(blockCoords));
            }

            @Nullable
            public static final WorldChunk get_chunkCoords(
                @Nonnull final World world,
                final int chunkX,
                final int chunkZ
            ) {
                return Chunk.WorldChunk_.get(world, ChunkCoords.Index.get_chunkCoords(chunkX, chunkZ));
            }

            @Nullable
            public static final WorldChunk get(@Nonnull final World world, final long chunkIndex) {
                return world.getChunk(chunkIndex);
            }

            //   #endregion World
            //   #region Ref<ChunkStore>
            //   ==================================================================
            //   Ref<ChunkStore>      -> WorldChunk
            //   ==================================================================

            @Nullable
            public static final WorldChunk get(
                @Nonnull final Ref<ChunkStore> anyRef,
                @Nonnull final BlockChunk blockChunk
            ) {
                return Chunk.WorldChunk_.get(anyRef, blockChunk.getIndex());
            }

            @Nullable
            public static final WorldChunk get(
                @Nonnull final Ref<ChunkStore> anyRef,
                final int blockX,
                final int blockZ
            ) {
                return Chunk.WorldChunk_.get(anyRef, ChunkCoords.Index.get(blockX, blockZ));
            }

            @Nullable
            public static final WorldChunk get(
                @Nonnull final Ref<ChunkStore> anyRef,
                @Nonnull final Vector3i blockCoords
            ) {
                return Chunk.WorldChunk_.get(anyRef, ChunkCoords.Index.get(blockCoords));
            }

            @Nullable
            public static final WorldChunk get_chunkCoords(
                @Nonnull final Ref<ChunkStore> anyRef,
                final int chunkX,
                final int chunkZ
            ) {
                return Chunk.WorldChunk_.get(anyRef, ChunkCoords.Index.get_chunkCoords(chunkX, chunkZ));
            }

            @Nullable
            public static final WorldChunk get(@Nonnull final Ref<ChunkStore> anyRef, final long chunkIndex) {
                // TODO use this EVERYWHERE (oh my GOD this seems useful)
                return anyRef.getStore().getExternalData().getChunkComponent(chunkIndex, WORLD_CHUNK_COMPONENT);
            }

            //   #endregion WorldChunk
            //   #region BlockStateInfo
            //   ==================================================================
            //   BlockStateInfo       -> WorldChunk
            //   ==================================================================

            @Nullable
            public static final WorldChunk get(
                @Nonnull final BlockStateInfo info,
                @Nonnull final BlockChunk blockChunk
            ) {
                return Chunk.WorldChunk_.get(info, blockChunk.getIndex());
            }

            /**
             * does NOT get the chunk that the info is in, it USES the info to get
             * the world.
             *
             * Then uses the world to get the WorldChunk at the coords provided
             */
            @Nullable
            public static final WorldChunk get(@Nonnull final BlockStateInfo info, final int blockX, final int blockZ) {
                return Chunk.WorldChunk_.get(info, ChunkCoords.Index.get(blockX, blockZ));
            }

            @Nullable
            public static final WorldChunk get(
                @Nonnull final BlockStateInfo info,
                @Nonnull final Vector3i blockCoords
            ) {
                return Chunk.WorldChunk_.get(info, ChunkCoords.Index.get(blockCoords));
            }

            @Nullable
            public static final WorldChunk get_chunkCoords(
                @Nonnull final BlockStateInfo info,
                final int chunkX,
                final int chunkZ
            ) {
                return Chunk.WorldChunk_.get(info, ChunkCoords.Index.get_chunkCoords(chunkX, chunkZ));
            }

            /**
             * does NOT get the chunk that the info is in, it USES the info to get
             * the world.
             *
             * Then uses the world to get the WorldChunk at the coords provided
             */
            @Nullable
            public static final WorldChunk get(@Nonnull final BlockStateInfo info, final long chunkIndex) {
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
            public static final WorldChunk get(
                @Nonnull final CommandBuffer<ChunkStore> commandBuffer, // << Doesn't need to get the world, it can access components directly
                @Nonnull final BlockChunk blockChunk
            ) {
                // but regardless we don't need the world to get the world chunk
                return Chunk.WorldChunk_.get(commandBuffer, blockChunk.getIndex());
            }

            @Nullable
            public static final WorldChunk get(
                @Nonnull final CommandBuffer<ChunkStore> commandBuffer, // << Doesn't need to get the world, it can access components directly
                final int blockX,
                final int blockZ
            ) {
                // but regardless we don't need the world to get the world chunk
                return Chunk.WorldChunk_.get(commandBuffer, ChunkCoords.Index.get(blockX, blockZ));
            }

            @Nullable
            public static final WorldChunk get(
                @Nonnull final CommandBuffer<ChunkStore> commandBuffer,
                @Nonnull final Vector3i blockCoords
            ) {
                return Chunk.WorldChunk_.get(commandBuffer, ChunkCoords.Index.get(blockCoords));
            }

            @Nullable
            public static final WorldChunk get_chunkCoords(
                @Nonnull final CommandBuffer<ChunkStore> commandBuffer,
                final int chunkX,
                final int chunkZ
            ) {
                return Chunk.WorldChunk_.get(commandBuffer, ChunkCoords.Index.get_chunkCoords(chunkX, chunkZ));
            }

            @Nullable
            public static final WorldChunk get(
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
            public static final WorldChunk get(
                @Nonnull final Store<ChunkStore> store,
                @Nonnull final BlockChunk blockChunk
            ) {
                return Chunk.WorldChunk_.get(store, blockChunk.getIndex());
            }

            @Nullable
            public static final WorldChunk get(
                @Nonnull final Store<ChunkStore> store,
                final int blockX,
                final int blockZ
            ) {
                return Chunk.WorldChunk_.get(store, ChunkCoords.Index.get(blockX, blockZ));
            }

            @Nullable
            public static final WorldChunk get(
                @Nonnull final Store<ChunkStore> store,
                @Nonnull final Vector3i blockCoords
            ) {
                return Chunk.WorldChunk_.get(store, ChunkCoords.Index.get(blockCoords));
            }

            @Nullable
            public static final WorldChunk get_chunkCoords(
                @Nonnull final Store<ChunkStore> store,
                final int chunkX,
                final int chunkZ
            ) {
                return Chunk.WorldChunk_.get(store, ChunkCoords.Index.get_chunkCoords(chunkX, chunkZ));
            }

            @Nullable
            public static final WorldChunk get(@Nonnull final Store<ChunkStore> store, final long chunkIndex) {
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
            public static final WorldChunk get(
                @Nonnull final ChunkStore chunkStore,
                @Nonnull final BlockChunk blockChunk
            ) {
                // ALT version: `chunkStore.getWorld().getChunk(blockChunk.getIndex());`
                return Chunk.WorldChunk_.get(chunkStore, blockChunk.getIndex());
            }

            @Nullable
            public static final WorldChunk get(
                @Nonnull final ChunkStore chunkStore,
                final int blockX,
                final int blockZ
            ) {
                // ALT version: `chunkStore.getWorld().getChunk(blockChunk.getIndex());`
                return Chunk.WorldChunk_.get(chunkStore, ChunkCoords.Index.get(blockX, blockZ));
            }

            @Nullable
            public static final WorldChunk get(
                @Nonnull final ChunkStore chunkStore,
                @Nonnull final Vector3i blockCoords
            ) {
                return Chunk.WorldChunk_.get(chunkStore, ChunkCoords.Index.get(blockCoords));
            }

            @Nullable
            public static final WorldChunk get_chunkCoords(
                @Nonnull final ChunkStore chunkStore,
                final int chunkX,
                final int chunkZ
            ) {
                return Chunk.WorldChunk_.get(chunkStore, ChunkCoords.Index.get_chunkCoords(chunkX, chunkZ));
            }

            @Nullable
            public static final WorldChunk get(@Nonnull final ChunkStore chunkStore, final long chunkIndex) {
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
                // final var localCoords = BlockCoords.Local.get(localIndex);
                final var blockCoords = new Vector3i(blockX, blockY, blockZ);
                final var chunkIndex = ChunkCoords.Index.get(blockCoords);
                final var chunkCoords = ChunkCoords.Global.get(blockCoords);
                final var chunkX = chunkCoords.x;
                final var chunkZ = chunkCoords.z;

                final var test = new TestUtil(commandBuffer, blockCoords);
                // functions to test
                final ArrayList<Ref<ChunkStore>> refs = new ArrayList<>();

                // World
                refs.add(Chunk.Ref_.get(test.world, blockCoords));
                refs.add(Chunk.Ref_.get(test.world, blockX, blockZ));
                refs.add(Chunk.Ref_.get(test.world, chunkIndex));
                refs.add(Chunk.Ref_.get_chunkCoords(test.world, chunkX, chunkZ));

                refs.add(Chunk.Ref_.get(test.commandBuffer, blockCoords));
                refs.add(Chunk.Ref_.get(test.commandBuffer, blockX, blockZ));
                refs.add(Chunk.Ref_.get(test.commandBuffer, chunkIndex));
                refs.add(Chunk.Ref_.get_chunkCoords(test.commandBuffer, chunkX, chunkZ));

                refs.add(Chunk.Ref_.get(test.store, blockCoords));
                refs.add(Chunk.Ref_.get(test.store, blockX, blockZ));
                refs.add(Chunk.Ref_.get(test.store, chunkIndex));
                refs.add(Chunk.Ref_.get_chunkCoords(test.store, chunkX, chunkZ));

                refs.add(Chunk.Ref_.get(test.chunkStore, blockCoords));
                refs.add(Chunk.Ref_.get(test.chunkStore, blockX, blockZ));
                refs.add(Chunk.Ref_.get(test.chunkStore, chunkIndex));
                refs.add(Chunk.Ref_.get_chunkCoords(test.chunkStore, chunkX, chunkZ));

                refs.add(Chunk.Ref_.get(test.chunkRef, blockCoords));
                refs.add(Chunk.Ref_.get(test.chunkRef, blockX, blockZ));
                refs.add(Chunk.Ref_.get(test.chunkRef, chunkIndex));
                refs.add(Chunk.Ref_.get_chunkCoords(test.chunkRef, chunkX, chunkZ));

                refs.add(Chunk.Ref_.get(test.info, blockCoords));
                refs.add(Chunk.Ref_.get(test.info, blockX, blockZ));
                refs.add(Chunk.Ref_.get(test.info, chunkIndex));
                refs.add(Chunk.Ref_.get_chunkCoords(test.info, chunkX, chunkZ));

                refs.add(Chunk.Ref_.get(test.blockRef));
                refs.add(Chunk.Ref_.get(test.info));

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
            public static final Ref<ChunkStore> get(@Nonnull final World world, @Nonnull final Vector3i blockCoords) {
                return Chunk.Ref_.get(world, ChunkCoords.Index.get(blockCoords));
            }

            @Nullable
            public static final Ref<ChunkStore> get(@Nonnull final World world, final int blockX, final int blockZ) {
                return Chunk.Ref_.get(world, ChunkCoords.Index.get(blockX, blockZ));
            }

            @Nullable
            public static final Ref<ChunkStore> get_chunkCoords(
                @Nonnull final World world,
                final int chunkX,
                final int chunkZ
            ) {
                return Chunk.Ref_.get(world, ChunkCoords.Index.get_chunkCoords(chunkX, chunkZ));
            }

            @Nullable
            public static final Ref<ChunkStore> get(@Nonnull final World world, final long chunkIndex) {
                return world.getChunkStore().getChunkReference(chunkIndex);
            }

            // #endregion World
            // #region CommandBuffer
            // ====================================================================
            // CommandBuffer
            // ====================================================================

            @Nullable
            public static final Ref<ChunkStore> get(
                @Nonnull final CommandBuffer<ChunkStore> commandBuffer,
                @Nonnull final Vector3i blockCoords
            ) {
                return Chunk.Ref_.get(commandBuffer, ChunkCoords.Index.get(blockCoords));
            }

            @Nullable
            public static final Ref<ChunkStore> get(
                @Nonnull final CommandBuffer<ChunkStore> commandBuffer,
                final int blockX,
                final int blockZ
            ) {
                return Chunk.Ref_.get(commandBuffer, ChunkCoords.Index.get(blockX, blockZ));
            }

            @Nullable
            public static final Ref<ChunkStore> get_chunkCoords(
                @Nonnull final CommandBuffer<ChunkStore> commandBuffer,
                final int chunkX,
                final int chunkZ
            ) {
                return Chunk.Ref_.get(commandBuffer, ChunkCoords.Index.get_chunkCoords(chunkX, chunkZ));
            }

            @Nullable
            public static final Ref<ChunkStore> get(
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
            public static final Ref<ChunkStore> get(
                @Nonnull final Store<ChunkStore> store,
                @Nonnull final Vector3i blockCoords
            ) {
                return Chunk.Ref_.get(store, ChunkCoords.Index.get(blockCoords));
            }

            @Nullable
            public static final Ref<ChunkStore> get(
                @Nonnull final Store<ChunkStore> store,
                final int blockX,
                final int blockZ
            ) {
                return Chunk.Ref_.get(store, ChunkCoords.Index.get(blockX, blockZ));
            }

            @Nullable
            public static final Ref<ChunkStore> get_chunkCoords(
                @Nonnull final Store<ChunkStore> store,
                final int chunkX,
                final int chunkZ
            ) {
                return Chunk.Ref_.get(store, ChunkCoords.Index.get_chunkCoords(chunkX, chunkZ));
            }

            @Nullable
            public static final Ref<ChunkStore> get(@Nonnull final Store<ChunkStore> store, final long chunkIndex) {
                return store.getExternalData().getChunkReference(chunkIndex);
            }

            // #endregion Store<ChunkStore>
            // #region ChunkStore
            // ====================================================================
            // ChunkStore
            // ====================================================================

            @Nullable
            public static final Ref<ChunkStore> get(
                @Nonnull final ChunkStore chunkStore,
                @Nonnull final Vector3i blockCoords
            ) {
                return Chunk.Ref_.get(chunkStore, ChunkCoords.Index.get(blockCoords));
            }

            @Nullable
            public static final Ref<ChunkStore> get(
                @Nonnull final ChunkStore chunkStore,
                final int blockX,
                final int blockZ
            ) {
                return Chunk.Ref_.get(chunkStore, ChunkCoords.Index.get(blockX, blockZ));
            }

            @Nullable
            public static final Ref<ChunkStore> get_chunkCoords(
                @Nonnull final ChunkStore chunkStore,
                final int chunkX,
                final int chunkZ
            ) {
                return Chunk.Ref_.get(chunkStore, ChunkCoords.Index.get_chunkCoords(chunkX, chunkZ));
            }

            @Nullable
            public static final Ref<ChunkStore> get(@Nonnull final ChunkStore chunkStore, final long chunkIndex) {
                return chunkStore.getChunkReference(chunkIndex);
            }

            // #endregion ChunkStore
            // #region Ref<ChunkStore>

            @Nullable
            public static final Ref<ChunkStore> get(
                @Nonnull final Ref<ChunkStore> chunkRef,
                @Nonnull final Vector3i blockCoords
            ) {
                return Chunk.Ref_.get(chunkRef, ChunkCoords.Index.get(blockCoords));
            }

            @Nullable
            public static final Ref<ChunkStore> get(
                @Nonnull final Ref<ChunkStore> chunkRef,
                final int blockX,
                final int blockZ
            ) {
                return Chunk.Ref_.get(chunkRef, ChunkCoords.Index.get(blockX, blockZ));
            }

            @Nullable
            public static final Ref<ChunkStore> get_chunkCoords(
                @Nonnull final Ref<ChunkStore> chunkRef,
                final int chunkX,
                final int chunkZ
            ) {
                return Chunk.Ref_.get(chunkRef, ChunkCoords.Index.get_chunkCoords(chunkX, chunkZ));
            }

            @Nullable
            public static final Ref<ChunkStore> get(@Nonnull final Ref<ChunkStore> chunkRef, final long chunkIndex) {
                return chunkRef.getStore().getExternalData().getChunkReference(chunkIndex);
            }

            // #endregion Ref<ChunkStore>
            // #region BlockStateInfo
            // ====================================================================
            // BlockStateInfo
            // ====================================================================

            @Nullable
            public static final Ref<ChunkStore> get(
                @Nonnull final BlockStateInfo info,
                @Nonnull final Vector3i blockCoords
            ) {
                return Chunk.Ref_.get(info, ChunkCoords.Index.get(blockCoords));
            }

            @Nullable
            public static final Ref<ChunkStore> get(
                @Nonnull final BlockStateInfo info,
                final int blockX,
                final int blockZ
            ) {
                return Chunk.Ref_.get(info, ChunkCoords.Index.get(blockX, blockZ));
            }

            @Nullable
            public static final Ref<ChunkStore> get_chunkCoords(
                @Nonnull final BlockStateInfo info,
                final int chunkX,
                final int chunkZ
            ) {
                return Chunk.Ref_.get(info, ChunkCoords.Index.get_chunkCoords(chunkX, chunkZ));
            }

            @Nullable
            public static final Ref<ChunkStore> get(@Nonnull final BlockStateInfo info, final long chunkIndex) {
                return info.getChunkRef().getStore().getExternalData().getChunkReference(chunkIndex);
            }

            // #endregion BlockStateInfo
            // #region BlockRef
            // ====================================================================
            // BlockRef
            // ====================================================================

            @Nullable
            public static final Ref<ChunkStore> get(@Nonnull final Ref<ChunkStore> blockRef) {
                final var info = Block.Info.get(blockRef);
                if (info == null) {
                    return null;
                }
                return info.getChunkRef();
            }

            // please don't use this one for getting it from info... just here for completeness so you know you CAN get the chunk ref out of info - in fact, that's (in my understanding) the preferred way
            @Nullable
            public static final Ref<ChunkStore> get(@Nonnull final BlockStateInfo info) {
                return info.getChunkRef();
            }

            // #endregion BlockRef
            // #endregion getChunkRef
        }
    }

    // TESTS ADDED AND VERIFIED
    public static final class Component_ {

        /**
         * Tests all methods i've defined for getWorldChunk
         */
        public static final void test(
            @Nonnull final Ref<ChunkStore> blockRef,
            @Nonnull final WorldChunk worldChunk,
            @Nonnull final CommandBuffer<ChunkStore> commandBuffer,
            @Nonnull final Vector3i providedCoords,
            Boolean verbose
        ) {
            final var blockX = providedCoords.x;
            final var blockY = providedCoords.y;
            final var blockZ = providedCoords.z;
            final var blockCoords = new Vector3i(blockX, blockY, blockZ);

            final var test = new TestUtil(commandBuffer, blockCoords);

            final var chunkRef = test.chunkRef;

            final var blockRefComponent = new TwunkDevTestComponent().setVal(1);
            final var chunkRefComponent = new TwunkDevTestComponent().setVal(2);

            // ensure BlockStateInfo is on the BlockRef
            Component<ChunkStore> component;
            component = Component_.get(blockRef, BLOCK_STATE_INFO_COMPONENT_TYPE);
            if (component == null) {
                throw new RuntimeException("!! No BlockStateInfo component on BlockRef");
            }

            // Chunk should contain WorldChunk component
            component = Component_.get(chunkRef, WORLD_CHUNK_COMPONENT);
            if (component == null) {
                throw new RuntimeException("!! No WorldChunk component on ChunkRef");
            }

            // Chunk should contain BlockComponentChunk component
            component = Component_.get(chunkRef, BLOCK_COMPONENT_CHUNK);
            if (component == null) {
                throw new RuntimeException("!! No BlockComponentChunk component on ChunkRef (base method)");
            }
            component = Component_.getBlockComponentChunk(chunkRef);
            if (component == null) {
                throw new RuntimeException("!! No BlockComponentChunk component on ChunkRef (higher level method)");
            }

            // validate the components AREN'T on there yet (BlockRef)
            component = Component_.get(blockRef, TwunkDevTestComponent.COMPONENT_TYPE);
            if (Component_.has(blockRef, TwunkDevTestComponent.COMPONENT_TYPE) || component != null) {
                throw new RuntimeException("!! ERROR: BlockRef contained TwunkDevTestComponent before we added it");
            }

            // validate the components AREN'T on there yet (ChunkRef)
            component = Component_.get(chunkRef, TwunkDevTestComponent.COMPONENT_TYPE);
            if (Component_.has(chunkRef, TwunkDevTestComponent.COMPONENT_TYPE) || component != null) {
                throw new RuntimeException("!! ERROR: ChunkRef contained TwunkDevTestComponent before we added it");
            }

            // Add components
            commandBuffer.run(componentAccessor -> {
                try {
                    try {
                        componentAccessor.putComponent(
                            blockRef,
                            TwunkDevTestComponent.COMPONENT_TYPE,
                            blockRefComponent
                        );
                    } catch (Exception e) {
                        throw new RuntimeException("!! !! ERROR: Failed to put component onto block");
                    }

                    // and we'll check that it's there
                    var blockRefComponentREFETCHED = Component_.get(blockRef, TwunkDevTestComponent.COMPONENT_TYPE);
                    if (
                        !Component_.has(blockRef, TwunkDevTestComponent.COMPONENT_TYPE) ||
                        blockRefComponentREFETCHED == null
                    ) {
                        throw new RuntimeException("!! !! ERROR: Failed to get component we just added from blockRef");
                    }

                    if (blockRefComponent != blockRefComponentREFETCHED || blockRefComponentREFETCHED.getVal() != 1) {
                        throw new RuntimeException(
                            "new component on our block wasn't the same one we put on there " +
                                blockRefComponent +
                                " " +
                                blockRefComponent.getVal() +
                                " | new - " +
                                blockRefComponentREFETCHED +
                                " " +
                                blockRefComponentREFETCHED.getVal()
                        );
                    }

                    try {
                        componentAccessor
                            .getExternalData()
                            .getStore()
                            .putComponent(chunkRef, TwunkDevTestComponent.COMPONENT_TYPE, chunkRefComponent);
                    } catch (Exception e) {
                        throw new RuntimeException("!! !! ERROR: Failed to put component onto chunk");
                    }

                    var chunkRefComponentREFETCHED = Component_.get(chunkRef, TwunkDevTestComponent.COMPONENT_TYPE);
                    if (
                        !Component_.has(chunkRef, TwunkDevTestComponent.COMPONENT_TYPE) ||
                        chunkRefComponentREFETCHED == null
                    ) {
                        throw new RuntimeException("!! !! ERROR: Failed to get component we just added from chunkRef");
                    }

                    if (chunkRefComponent != chunkRefComponentREFETCHED || chunkRefComponentREFETCHED.getVal() != 2) {
                        throw new RuntimeException(
                            "new component on our chunk wasn't the same one we put on there " +
                                chunkRefComponent +
                                " " +
                                chunkRefComponent.getVal() +
                                " | new - " +
                                chunkRefComponentREFETCHED +
                                " " +
                                chunkRefComponentREFETCHED.getVal()
                        );
                    }

                    // we'll mutate one and validate it is mutated on the other
                    blockRefComponent.setVal(3);
                    chunkRefComponent.setVal(4);
                    if (
                        blockRefComponent.getVal() != blockRefComponentREFETCHED.getVal() ||
                        blockRefComponent.getVal() != 3
                    ) {
                        throw new RuntimeException("new component on our block doesn't respond to updates");
                    }
                    if (
                        chunkRefComponent.getVal() != chunkRefComponentREFETCHED.getVal() ||
                        chunkRefComponent.getVal() != 4
                    ) {
                        throw new RuntimeException("new component on our chunk doesn't respond to updates");
                    }

                    var component2 = Utils.Component_.get_blockCoords(
                        test.world,
                        TwunkDevTestComponent.COMPONENT_TYPE,
                        blockX,
                        blockY,
                        blockZ
                    );
                    if (component2 == null) {
                        throw new RuntimeException("Failed to get component by block coords");
                    }

                    final var localCoords = BlockCoords.Local.get(blockCoords);
                    var component3 = Utils.Component_.get_localCoords(
                        test.blockComponentChunk,
                        TwunkDevTestComponent.COMPONENT_TYPE,
                        localCoords.x,
                        localCoords.y,
                        localCoords.z
                    );
                    if (component3 == null) {
                        throw new RuntimeException("Failed to get component by local coords");
                    }

                    // then we'll remove it from one, check its still on the other and not on the one we removed it from
                    // and then remove it from the other
                    blockRef.getStore().removeComponent(blockRef, TwunkDevTestComponent.COMPONENT_TYPE);
                    chunkRef.getStore().removeComponent(chunkRef, TwunkDevTestComponent.COMPONENT_TYPE);
                    blockRefComponentREFETCHED = Component_.get(blockRef, TwunkDevTestComponent.COMPONENT_TYPE);
                    chunkRefComponentREFETCHED = Component_.get(chunkRef, TwunkDevTestComponent.COMPONENT_TYPE);
                    if (
                        Component_.has(blockRef, TwunkDevTestComponent.COMPONENT_TYPE) ||
                        blockRefComponentREFETCHED != null
                    ) {
                        throw new RuntimeException("failed to remove our new component from the block lmao");
                    }
                    if (
                        Component_.has(chunkRef, TwunkDevTestComponent.COMPONENT_TYPE) ||
                        chunkRefComponentREFETCHED != null
                    ) {
                        throw new RuntimeException("failed to remove our new component from the block lmao");
                    }

                    component2 = Utils.Component_.get_blockCoords(
                        test.world,
                        TwunkDevTestComponent.COMPONENT_TYPE,
                        blockX,
                        blockY,
                        blockZ
                    );
                    if (component2 != null) {
                        throw new RuntimeException(
                            "Should NOT have been able to get component after deleting it (from blockCoords)"
                        );
                    }

                    component3 = Utils.Component_.get_localCoords(
                        test.blockComponentChunk,
                        TwunkDevTestComponent.COMPONENT_TYPE,
                        localCoords.x,
                        localCoords.y,
                        localCoords.z
                    );
                    if (component3 != null) {
                        throw new RuntimeException(
                            "Should NOT have been able to get component after deleting it (from localCoords)"
                        );
                    }
                    // yeah these next two logs are useless, but it helps to easily verify all my tests are working by them all having the same structure visually in their responses
                    if (verbose != null) {
                        console.log("Ran alot of test(s)");
                        console.log("10/10 tests successful");
                        console.log("+ All tests successful");
                    }
                    if (verbose == null) {
                        console.log("+ (11) SUCCESS: TEST_Component_");
                    }
                } catch (Exception e) {
                    if (verbose != null) {
                        console.log("ERROR IN TESTS" + e);
                    }
                    if (verbose == null) {
                        console.log("- (11) FAILED:  TEST_Component_");
                    }
                }
            });
        }

        public static final <T extends Component<ChunkStore>> boolean has(
            @Nonnull final Ref<ChunkStore> ref,
            @Nonnull final Supplier<ComponentType<ChunkStore, T>> getComponentType
        ) {
            final var componentType = getComponentType.get();
            if (componentType == null) {
                return false;
            }

            return Component_.has(ref, componentType);
        }

        public static final <T extends Component<ChunkStore>> boolean has(
            @Nonnull final Ref<ChunkStore> ref,
            @Nonnull final ComponentType<ChunkStore, T> componentType
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

        /**
         * Get a component from a chunk, can either provide the ref to the chunk itself OR
         * a ref to the block that's in the chunk
         */
        public static final <T extends Component<ChunkStore>> T getChunkComponent(
            @Nonnull final Ref<ChunkStore> anyRef,
            @Nonnull final ComponentType<ChunkStore, T> componentType
        ) {
            if (Block.isChunkRef(anyRef)) {
                return Component_.get(anyRef, componentType);
            } else if (Block.isBlockRef(anyRef)) {
                final var chunkIndex = ChunkCoords.Index.get_blockRef(anyRef);
                if (chunkIndex == null) {
                    return null;
                }

                return anyRef.getStore().getExternalData().getChunkComponent(chunkIndex, componentType);
            }

            console.log("WARNING WARNING RILEY WARNING WE HAD A REF THAT WASN'T A BLOCK AND WASN'T A CHUNK");
            console.log("WARNING WARNING RILEY WARNING WE HAD A REF THAT WASN'T A BLOCK AND WASN'T A CHUNK");
            console.log("WARNING WARNING RILEY WARNING WE HAD A REF THAT WASN'T A BLOCK AND WASN'T A CHUNK");
            return null;
        }

        @Nullable
        public static final BlockComponentChunk getBlockComponentChunk(@Nonnull final Ref<ChunkStore> anyRef) {
            if (Block.isChunkRef(anyRef)) {
                return anyRef.getStore().getComponent(anyRef, BLOCK_COMPONENT_CHUNK);
            } else if (Block.isBlockRef(anyRef)) {
                // get the chunkRef first
                final var chunkRef = Chunk.Ref_.get(anyRef);
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
            @Nonnull final World world,
            @Nonnull final ComponentType<ChunkStore, T> componentType,
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
            @Nonnull final BlockComponentChunk chunk,
            @Nonnull final Supplier<ComponentType<ChunkStore, T>> getComponentType,
            final int localX,
            final int localY,
            final int localZ
        ) {
            final var ref = Block.Ref_.getLocal(chunk, localX, localY, localZ);
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
            @Nonnull final BlockComponentChunk chunk,
            @Nonnull final ComponentType<ChunkStore, T> componentType,
            final int localX,
            final int localY,
            final int localZ
        ) {
            final var ref = Block.Ref_.getLocal(chunk, localX, localY, localZ);
            if (ref == null) {
                return null;
            }
            return ref.getStore().getComponent(ref, componentType);
        }
    }

    public static final class BlockCoords {

        /// -> get LOCAL coordinates within chunk    (Vector3i)
        public static final class Local {

            public static final void test(
                @Nonnull final Ref<ChunkStore> blockRef,
                @Nonnull final WorldChunk worldChunk,
                @Nonnull final CommandBuffer<ChunkStore> commandBuffer,
                @Nonnull final Vector3i providedCoords
            ) {
                final var blockX = providedCoords.x;
                final var blockY = providedCoords.y;
                final var blockZ = providedCoords.z;
                final var index = BlockCoords.Index.get(blockX, blockY, blockZ);
                final var localCoords = BlockCoords.Local.get(index);
                final var blockCoords = new Vector3i(blockX, blockY, blockZ);

                final var test = new TestUtil(commandBuffer, blockCoords);

                var local = BlockCoords.Local.get(test.blockRef);
                if (local == null || !local.equals(localCoords)) {
                    throw new RuntimeException("Failed to convert blockRef to localCoords");
                }
                local = BlockCoords.Local.get(test.info);
                if (local == null || !local.equals(localCoords)) {
                    throw new RuntimeException("Failed to convert info to localCoords");
                }
                local = BlockCoords.Local.get(blockCoords);
                if (local == null || !local.equals(localCoords)) {
                    throw new RuntimeException("Failed to convert blockCoords to localCoords");
                }
                local = BlockCoords.Local.get(localCoords);
                if (local == null || !local.equals(localCoords)) {
                    throw new RuntimeException("Failed to convert LOCAL coords to localCoords");
                }
                local = BlockCoords.Local.get(blockCoords.x, blockCoords.y, blockCoords.z);
                if (local == null || !local.equals(localCoords)) {
                    throw new RuntimeException("Failed to convert coords (split) to localCoords");
                }
                local = BlockCoords.Local.get(localCoords.x, localCoords.y, localCoords.z);
                if (local == null || !local.equals(localCoords)) {
                    throw new RuntimeException("Failed to convert local coords (split) to localCoords");
                }
                local = BlockCoords.Local.get(index);
                if (local == null || !local.equals(localCoords)) {
                    throw new RuntimeException("Failed to convert block index to localCoords");
                }
            }

            // #region get
            // ====================================================================
            // /\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\
            // ====================================================================
            //
            // Purpose:   Getting the local coordinates of the block (within its chunk)
            // Requires:  BlockStateInfo component of the relevant block (or a method of getting this -> see Info.get() and thus -> Entity.getRef)
            // importantly, most methods of getInfo are pointless, as usually these require the coordinates of a block (and if you have that, you don't need this)

            @Nullable
            public static final Vector3i get(@Nonnull final Ref<ChunkStore> blockRef) {
                final var info = Block.Info.get(blockRef);
                if (info == null) {
                    return null;
                }

                // => blockIndex
                return BlockCoords.Local.get(info.getIndex());
            }

            @Nonnull
            public static final Vector3i get(@Nonnull final BlockStateInfo info) {
                // => blockIndex
                return BlockCoords.Local.get(info.getIndex());
            }

            // ====================================================================
            // Global coords to local coords
            // ====================================================================

            @Nonnull
            public static final Vector3i get(@Nonnull final Vector3i coords) {
                return new Vector3i(coords.x & 31, coords.y, coords.z & 31);
            }

            // You're telling me i can get local coordinates from coordinates?
            // yeah, cause, well, if you're calling this its definitely with global coords
            //
            // thus, all we need to do is do is keep x % 32, y, z % 32
            //
            // notably, 32 = 2^5, thus we only need to keep the 5 lowest bits
            @Nonnull
            public static final Vector3i get(final int x, final int y, final int z) {
                return new Vector3i(x & 31, y, z & 31);
            }

            // ====================================================================
            // index -> this is how we actually get coords of a block throughout their system
            // ====================================================================

            @Nonnull
            public static final Vector3i get(final int blockIndex) {
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

            // #endregion get
        }

        /// -> get LOCAL index                       (int | Integer) (returns Integer so it can be nullable on method that can fail)
        public static final class Index {

            public static final void test(
                @Nonnull final Ref<ChunkStore> blockRef,
                @Nonnull final WorldChunk worldChunk,
                @Nonnull final CommandBuffer<ChunkStore> commandBuffer,
                @Nonnull final Vector3i providedCoords
            ) {
                final var blockX = providedCoords.x;
                final var blockY = providedCoords.y;
                final var blockZ = providedCoords.z;
                final var index = BlockCoords.Index.get(blockX, blockY, blockZ);
                final var localCoords = BlockCoords.Local.get(index);
                final var blockCoords = new Vector3i(blockX, blockY, blockZ);

                final var test = new TestUtil(commandBuffer, blockCoords);

                var testIndex = BlockCoords.Index.get(test.blockRef);
                if (testIndex == null || !testIndex.equals(index)) {
                    throw new RuntimeException("Failed to convert blockRef to blockIndex");
                }
                testIndex = BlockCoords.Index.get(test.info);
                if (testIndex == null || !testIndex.equals(index)) {
                    throw new RuntimeException("Failed to convert info to blockIndex");
                }
                testIndex = BlockCoords.Index.get(blockCoords);
                if (testIndex == null || !testIndex.equals(index)) {
                    throw new RuntimeException("Failed to convert blockCoords to blockIndex");
                }
                testIndex = BlockCoords.Index.get(localCoords);
                if (testIndex == null || !testIndex.equals(index)) {
                    throw new RuntimeException("Failed to convert LOCAL coords to blockIndex");
                }
                testIndex = BlockCoords.Index.get(blockCoords.x, blockCoords.y, blockCoords.z);
                if (testIndex == null || !testIndex.equals(index)) {
                    throw new RuntimeException("Failed to convert coords (split) to blockIndex");
                }
                testIndex = BlockCoords.Index.get(localCoords.x, localCoords.y, localCoords.z);
                if (testIndex == null || !testIndex.equals(index)) {
                    throw new RuntimeException("Failed to convert local coords (split) to blockIndex");
                }
            }

            // #region getLocalIndex

            // Integer cause, nullable
            @Nullable
            public static final Integer get(@Nonnull final Ref<ChunkStore> blockRef) {
                final var info = Block.Info.get(blockRef);
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
            public static final int get(@Nonnull final BlockStateInfo info) {
                return info.getIndex();
            }

            public static final int get(@Nonnull final Vector3i coords) {
                return ChunkUtil.indexBlockInColumn(coords.x, coords.y, coords.z);
            }

            public static final int get(final int x, final int y, final int z) {
                return ChunkUtil.indexBlockInColumn(x, y, z);
            }

            // #endregion getLocalIndex}
        }

        /// -> get GLOBAL coordinates                (Vector3i)
        public static final class Global {

            public static final void test(
                @Nonnull final Ref<ChunkStore> blockRef,
                @Nonnull final WorldChunk worldChunk,
                @Nonnull final CommandBuffer<ChunkStore> commandBuffer,
                @Nonnull final Vector3i providedCoords
            ) {
                final var blockX = providedCoords.x;
                final var blockY = providedCoords.y;
                final var blockZ = providedCoords.z;
                final var index = BlockCoords.Index.get(blockX, blockY, blockZ);
                final var localCoords = BlockCoords.Local.get(index);
                final var blockCoords = new Vector3i(blockX, blockY, blockZ);

                final var test = new TestUtil(commandBuffer, blockCoords);

                var coords = BlockCoords.Global.get(test.blockRef);
                if (coords == null || !coords.equals(blockCoords)) {
                    throw new RuntimeException("Failed to convert blockRef to blockCoords");
                }
                coords = BlockCoords.Global.get(test.info);
                if (coords == null || !coords.equals(blockCoords)) {
                    throw new RuntimeException("Failed to convert info to blockCoords");
                }
                coords = BlockCoords.Global.get(test.chunkRef, localCoords);
                if (coords == null || !coords.equals(blockCoords)) {
                    throw new RuntimeException("Failed to convert chunkRef + LOCAL coords to blockCoords");
                }
                coords = BlockCoords.Global.get(test.chunkRef, localCoords.x, localCoords.y, localCoords.z);
                if (coords == null || !coords.equals(blockCoords)) {
                    throw new RuntimeException("Failed to convert chunkRef + LOCAL coords (split) to blockCoords");
                }
                coords = BlockCoords.Global.get(test.chunkRef, index);
                if (coords == null || !coords.equals(blockCoords)) {
                    throw new RuntimeException("Failed to convert chunkRef + blockIndex to blockCoords");
                }
                coords = BlockCoords.Global.get(test.worldChunk, localCoords);
                if (coords == null || !coords.equals(blockCoords)) {
                    throw new RuntimeException("Failed to convert worldChunk + LOCAL coords to blockCoords");
                }
                coords = BlockCoords.Global.get(test.worldChunk, localCoords.x, localCoords.y, localCoords.z);
                if (coords == null || !coords.equals(blockCoords)) {
                    throw new RuntimeException("Failed to convert worldChunk + LOCAL coords (split) to blockCoords");
                }
                coords = BlockCoords.Global.get(test.worldChunk, index);
                if (coords == null || !coords.equals(blockCoords)) {
                    throw new RuntimeException("Failed to convert worldChunk + blockIndex to blockCoords");
                }
                coords = BlockCoords.Global.get(test.blockChunk, localCoords);
                if (coords == null || !coords.equals(blockCoords)) {
                    throw new RuntimeException("Failed to convert blockChunk + LOCAL coords to blockCoords");
                }
                coords = BlockCoords.Global.get(test.blockChunk, localCoords.x, localCoords.y, localCoords.z);
                if (coords == null || !coords.equals(blockCoords)) {
                    throw new RuntimeException("Failed to convert blockChunk + LOCAL coords (split) to blockCoords");
                }
                coords = BlockCoords.Global.get(test.blockChunk, index);
                if (coords == null || !coords.equals(blockCoords)) {
                    throw new RuntimeException("Failed to convert blockChunk + blockIndex to blockCoords");
                }
                final var chunkCoords = ChunkCoords.Global.get(blockCoords);
                coords = BlockCoords.Global.get(chunkCoords.x, chunkCoords.z, localCoords);
                if (coords == null || !coords.equals(blockCoords)) {
                    throw new RuntimeException("Failed to convert chunk coords (split) + localCoords to blockCoords");
                }
                coords = BlockCoords.Global.get(
                    chunkCoords.x,
                    chunkCoords.z,
                    localCoords.x,
                    localCoords.y,
                    localCoords.z
                );
                if (coords == null || !coords.equals(blockCoords)) {
                    throw new RuntimeException(
                        "Failed to convert chunk coords (split) + localCoords (split) to blockCoords"
                    );
                }
                coords = BlockCoords.Global.get(chunkCoords.x, chunkCoords.z, index);
                if (coords == null || !coords.equals(blockCoords)) {
                    throw new RuntimeException("Failed to convert chunk coords (split) + blockIndex to blockCoords");
                }

                final var chunkIndex = ChunkCoords.Index.get(blockCoords);
                coords = BlockCoords.Global.get(chunkIndex, localCoords);
                if (coords == null || !coords.equals(blockCoords)) {
                    throw new RuntimeException("Failed to convert chunkIndex + localCoords to blockCoords");
                }
                coords = BlockCoords.Global.get(chunkIndex, localCoords.x, localCoords.y, localCoords.z);
                if (coords == null || !coords.equals(blockCoords)) {
                    throw new RuntimeException(
                        "Failed to convert chunk chunkIndex + localCoords (split) to blockCoords"
                    );
                }
                coords = BlockCoords.Global.get(chunkIndex, index);
                if (coords == null || !coords.equals(blockCoords)) {
                    throw new RuntimeException("Failed to convert chunkIndex + blockIndex to blockCoords");
                }
                coords = BlockCoords.Global.get(test.worldChunk, test.info);
                if (coords == null || !coords.equals(blockCoords)) {
                    throw new RuntimeException("Failed to convert worldChunk + info to blockCoords");
                }
                coords = BlockCoords.Global.get(test.blockChunk, test.info);
                if (coords == null || !coords.equals(blockCoords)) {
                    throw new RuntimeException("Failed to convert blockChunk + info to blockCoords");
                }
                coords = BlockCoords.Global.get(chunkCoords.x, chunkCoords.z, test.info);
                if (coords == null || !coords.equals(blockCoords)) {
                    throw new RuntimeException("Failed to convert chunkCoords (split) + info to blockCoords");
                }
                coords = BlockCoords.Global.get(chunkIndex, test.info);
                if (coords == null || !coords.equals(blockCoords)) {
                    throw new RuntimeException("Failed to convert chunkIndex + info to blockCoords");
                }
            }

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
            public static final Vector3i get(@Nonnull final Ref<ChunkStore> blockRef) {
                final var info = Block.Info.get(blockRef);
                if (info == null) {
                    return null;
                }

                return BlockCoords.Global.get(info.getChunkRef(), info.getIndex());
            }

            @Nullable
            public static final Vector3i get(@Nonnull final BlockStateInfo info) {
                return BlockCoords.Global.get(info.getChunkRef(), info.getIndex());
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
            public static final Vector3i get(
                @Nonnull final Ref<ChunkStore> chunkRef,
                @Nonnull final Vector3i localCoords
            ) {
                final var worldChunk = Chunk.WorldChunk_.get_chunkRef(chunkRef);
                if (worldChunk == null) {
                    return null;
                }

                return BlockCoords.Global.get(worldChunk.getX(), worldChunk.getZ(), localCoords);
            }

            @Nullable
            public static final Vector3i get(
                @Nonnull final Ref<ChunkStore> chunkRef,
                final int localX,
                final int localY,
                final int localZ
            ) {
                final var worldChunk = Chunk.WorldChunk_.get_chunkRef(chunkRef);
                if (worldChunk == null) {
                    return null;
                }

                return BlockCoords.Global.get(worldChunk.getX(), worldChunk.getZ(), localX, localY, localZ);
            }

            @Nullable
            public static final Vector3i get(@Nonnull final Ref<ChunkStore> chunkRef, final int blockIndex) {
                final var worldChunk = Chunk.WorldChunk_.get_chunkRef(chunkRef);
                if (worldChunk == null) {
                    return null;
                }

                return BlockCoords.Global.get(worldChunk.getX(), worldChunk.getZ(), blockIndex);
            }

            //     #endregion chunkRef
            //     #region BlockAccessor
            //     |=================================================================
            //     | BlockAccessor OR WorldChunk
            //     |=================================================================

            @Nonnull
            public static final Vector3i get(@Nonnull final WorldChunk chunk, @Nonnull final Vector3i localCoords) {
                return BlockCoords.Global.get(chunk.getX(), chunk.getZ(), localCoords);
            }

            @Nonnull
            public static final Vector3i get(
                @Nonnull final WorldChunk chunk,
                final int localX,
                final int localY,
                final int localZ
            ) {
                return BlockCoords.Global.get(chunk.getX(), chunk.getZ(), localX, localY, localZ);
            }

            @Nonnull
            public static final Vector3i get(@Nonnull final WorldChunk chunk, final int blockIndex) {
                return BlockCoords.Global.get(chunk.getX(), chunk.getZ(), blockIndex);
            }

            // this seems pointless, but its for anyone who already has a worldChunk so i don't have to fetch it
            @Nonnull
            public static final Vector3i get(@Nonnull final WorldChunk chunk, @Nonnull final BlockStateInfo info) {
                return BlockCoords.Global.get(chunk.getX(), chunk.getZ(), info.getIndex());
            }

            //     #endregion chunkRef
            //     #region BlockAccessor
            //     |=================================================================
            //     | BlockAccessor OR WorldChunk
            //     |=================================================================

            @Nonnull
            public static final Vector3i get(@Nonnull final BlockAccessor chunk, @Nonnull final Vector3i localCoords) {
                return BlockCoords.Global.get(chunk.getX(), chunk.getZ(), localCoords);
            }

            @Nonnull
            public static final Vector3i get(
                @Nonnull final BlockAccessor chunk,
                final int localX,
                final int localY,
                final int localZ
            ) {
                return BlockCoords.Global.get(chunk.getX(), chunk.getZ(), localX, localY, localZ);
            }

            @Nonnull
            public static final Vector3i get(@Nonnull final BlockAccessor chunk, final int blockIndex) {
                return BlockCoords.Global.get(chunk.getX(), chunk.getZ(), blockIndex);
            }

            @Nonnull
            public static final Vector3i get(@Nonnull final BlockAccessor chunk, @Nonnull final BlockStateInfo info) {
                return BlockCoords.Global.get(chunk.getX(), chunk.getZ(), info.getIndex());
            }

            //     #endregion BlockAccessor
            //     #region BlockChunk
            //     |=================================================================
            //     | BlockChunk
            //     |=================================================================

            @Nonnull
            public static final Vector3i get(@Nonnull final BlockChunk chunk, @Nonnull final Vector3i localCoords) {
                return BlockCoords.Global.get(chunk.getX(), chunk.getZ(), localCoords);
            }

            @Nonnull
            public static final Vector3i get(@Nonnull final BlockChunk chunk, final int blockIndex) {
                return BlockCoords.Global.get(chunk.getX(), chunk.getZ(), blockIndex);
            }

            @Nonnull
            public static final Vector3i get(@Nonnull final BlockChunk chunk, @Nonnull final BlockStateInfo info) {
                return BlockCoords.Global.get(chunk.getX(), chunk.getZ(), info.getIndex());
            }

            @Nonnull
            public static final Vector3i get(
                @Nonnull final BlockChunk chunk,
                final int localX,
                final int localY,
                final int localZ
            ) {
                return BlockCoords.Global.get(chunk.getX(), chunk.getZ(), localX, localY, localZ);
            }

            //     #endregion BlockChunk
            //     #region done
            //     |=================================================================
            //     | Done!
            //     |=================================================================

            // Chunk Coords AND Block Coords
            @Nonnull
            public static final Vector3i get(final int chunkX, final int chunkZ, @Nonnull final Vector3i localCoords) {
                return new Vector3i(localCoords.x + (chunkX << 5), localCoords.y, localCoords.z + (chunkZ << 5));
            }

            // Chunk Coords AND Block Coords
            @Nonnull
            public static final Vector3i get(
                final int chunkX,
                final int chunkZ,
                final int localX,
                final int localY,
                final int localZ
            ) {
                return new Vector3i(localX + (chunkX << 5), localY, localZ + (chunkZ << 5));
            }

            // Chunk coords AND Block Index
            @Nonnull
            public static final Vector3i get(final int chunkX, final int chunkZ, final int blockIndex) {
                // remember: 5 bits -> 32
                final int globalX = (blockIndex & 31) + (chunkX << 5); // globalX = local X + 32 * chunkX (chunk is 32x32 for x,z)
                final int globalY = (blockIndex >> 10) & ChunkUtil.HEIGHT_MASK; // same as local y, there is no distinction between the two
                final int globalZ = ((blockIndex >> 5) & 31) + (chunkZ << 5); // globalX = local z + 32 * chunkZ (chunk is 32x32 for x,z)

                return new Vector3i(globalX, globalY, globalZ);
            }

            // may seem pointless, but its so if you already have the chunkX and chunkZ i don't have to fetch them
            @Nonnull
            public static final Vector3i get(final int chunkX, final int chunkZ, @Nonnull final BlockStateInfo info) {
                final var blockIndex = info.getIndex();

                // remember: 5 bits -> 32
                final int globalX = (blockIndex & 31) + (chunkX << 5); // globalX = local X + 32 * chunkX (chunk is 32x32 for x,z)
                final int globalY = (blockIndex >> 10) & ChunkUtil.HEIGHT_MASK; // same as local y, there is no distinction between the two
                final int globalZ = ((blockIndex >> 5) & 31) + (chunkZ << 5); // globalX = local z + 32 * chunkZ (chunk is 32x32 for x,z)

                return new Vector3i(globalX, globalY, globalZ);
            }

            // Chunk Coords AND Block Coords
            @Nonnull
            public static final Vector3i get(final long chunkIndex, @Nonnull final Vector3i localCoords) {
                final int chunkX = (int) (chunkIndex >> 32);
                final int chunkZ = (int) (chunkIndex);

                return new Vector3i(localCoords.x + (chunkX << 5), localCoords.y, localCoords.z + (chunkZ << 5));
            }

            // Chunk Coords AND Block Coords
            @Nonnull
            public static final Vector3i get(
                final long chunkIndex,
                final int localX,
                final int localY,
                final int localZ
            ) {
                final int chunkX = (int) (chunkIndex >> 32);
                final int chunkZ = (int) (chunkIndex);

                return new Vector3i(localX + (chunkX << 5), localY, localZ + (chunkZ << 5));
            }

            // Chunk coords AND Block Index
            @Nonnull
            public static final Vector3i get(final long chunkIndex, final int blockIndex) {
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
            public static final Vector3i get(final long chunkIndex, @Nonnull final BlockStateInfo info) {
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
            // Requires:  BlockStateInfo component of the relevant block (or a method of getting this -> see Info.get() and thus -> Entity.getRef)
            // importantly, most methods of getInfo are pointless, as usually these require the coordinates of a block (and if you have that, you don't need this)

            // ====================================================================
            // Chunk Coordinates FROM block coordinates
            // ====================================================================

            // With GLOBAL block coords
            @Nonnull
            public static final ChunkCoordinates get(final int blockX, final int blockZ) {
                // see ChunkUtil.chunkCoordinate
                return new ChunkCoordinates(blockX >> 5, blockZ >> 5);
            }

            // With GLOBAL block coords
            @Nonnull
            public static final ChunkCoordinates get(@Nonnull final Vector3i blockCoords) {
                // see ChunkUtil.chunkCoordinate
                return new ChunkCoordinates(blockCoords.x >> 5, blockCoords.z >> 5);
            }

            /**
             * With GLOBAL block coords
             *
             * returned "y" in vector is actually the chunk's "z" coordinate
             */
            @Nonnull
            public static final Vector2i getVector2i(final int blockX, final int blockZ) {
                // see ChunkUtil.chunkCoordinate
                return new Vector2i(blockX >> 5, blockZ >> 5);
            }

            /**
             * With GLOBAL block coords
             *
             * returned "y" in vector is actually the chunk's "z" coordinate
             */
            @Nonnull
            public static final Vector2i getVector2i(@Nonnull final Vector3i blockCoords) {
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

            @Nullable
            public static final Long get(@Nonnull final Ref<ChunkStore> anyRef) {
                final var worldChunk = Chunk.WorldChunk_.get(anyRef);
                if (worldChunk == null) {
                    return null;
                }

                return ChunkCoords.Index.get(worldChunk);
            }

            @Nullable
            public static final Long get(@Nonnull final BlockStateInfo info) {
                return ChunkCoords.Index.get_chunkRef(info.getChunkRef());
            }

            @Nullable
            public static final Long get_chunkRef(@Nonnull final Ref<ChunkStore> chunkRef) {
                final var worldChunk = Chunk.WorldChunk_.get_chunkRef(chunkRef);
                if (worldChunk == null) {
                    return null;
                }

                return ChunkCoords.Index.get(worldChunk);
            }

            @Nullable
            public static final Long get_blockRef(@Nonnull final Ref<ChunkStore> blockRef) {
                final var worldChunk = Chunk.WorldChunk_.get_blockRef(blockRef);
                if (worldChunk == null) {
                    return null;
                }

                return ChunkCoords.Index.get(worldChunk);
            }

            public static final long get(@Nonnull final WorldChunk worldChunk) {
                // TODO sus if this works
                console.log(
                    "worldChunk index: " +
                        worldChunk.getIndex() +
                        " | " +
                        ChunkCoords.Index.get_chunkCoords(worldChunk.getX(), worldChunk.getZ())
                );
                console.log(
                    "worldChunk index: " +
                        worldChunk.getIndex() +
                        " | " +
                        ChunkCoords.Index.get_chunkCoords(worldChunk.getX(), worldChunk.getZ())
                );
                console.log(
                    "worldChunk index: " +
                        worldChunk.getIndex() +
                        " | " +
                        ChunkCoords.Index.get_chunkCoords(worldChunk.getX(), worldChunk.getZ())
                );
                console.log(
                    "worldChunk index: " +
                        worldChunk.getIndex() +
                        " | " +
                        ChunkCoords.Index.get_chunkCoords(worldChunk.getX(), worldChunk.getZ())
                );
                console.log(
                    "worldChunk index: " +
                        worldChunk.getIndex() +
                        " | " +
                        ChunkCoords.Index.get_chunkCoords(worldChunk.getX(), worldChunk.getZ())
                );

                return ChunkCoords.Index.get_chunkCoords(worldChunk.getX(), worldChunk.getZ());
            }

            /**
             * a `long` is just a bit-packed version of chunk coords.
             * int: 32 bits
             * long: 64 bits
             *
             * hence, a long is basically 2 ints back to back
             */
            public static final long get(final int blockX, final int blockZ) {
                // see ChunkUtil.indexChunkFromBlock
                return ((long) (blockX >> 5) << 32) | ((long) (blockZ >> 5) & 4294967295L);
            }

            public static final long get(@Nonnull final Vector3i blockCoords) {
                // see ChunkUtil.indexChunkFromBlock
                return ((long) (blockCoords.x >> 5) << 32) | ((long) (blockCoords.z >> 5) & 4294967295L);
            }

            public static final long get_chunkCoords(final int chunkX, final int chunkZ) {
                // see ChunkUtil.indexChunkFromBlock
                return ((long) chunkX << 32) | ((long) chunkZ & 4294967295L);
            }

            public static final long get_chunkCoords(@Nonnull final ChunkCoordinates coords) {
                // see ChunkUtil.indexChunkFromBlock
                return ((long) coords.x << 32) | ((long) coords.z & 4294967295L);
            }

            /**
             * WARNING: the `y` value MUST be the `z` value for the chunk
             */
            public static final long get_chunkCoords(@Nonnull final Vector2i chunkCoords) {
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
            final var info = Block.Info.get(ref);
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
            final var worldChunk = Chunk.WorldChunk_.get(info);
            if (worldChunk == null) {
                console.log("World chunk was null");
                return false;
            }

            final var coords = BlockCoords.Local.get(info);
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
            final var coords = BlockCoords.Local.get(info);
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
