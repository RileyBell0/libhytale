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
public abstract class BlockUtils {

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
        // Function to get a reference to a block entity at given coordinates
        // can use any of the following
        // - param 1:
        //   * World
        //   * CommandBuffer<ChunkStore>
        //   * ChunkStore
        //   * Ref<ChunkStore> << to your CHUNK
        //   * BlockComponentChunk
        // param2:
        //   * global coords: x, y, z
        //   * Vector3i
        //   * index

        // #region int
        // ====================================================================
        // GET REF: from seperate int coords
        // ====================================================================

        @Nullable
        public static Ref<ChunkStore> getRef(@Nonnull World world, int x, int y, int z) {
            return Entity.getRef(world.getChunkStore(), ChunkUtil.indexBlockInColumn(x, y, z));
        }

        @Nullable
        public static Ref<ChunkStore> getRef(@Nonnull CommandBuffer<ChunkStore> commandBuffer, int x, int y, int z) {
            return Entity.getRef(commandBuffer.getExternalData(), ChunkUtil.indexBlockInColumn(x, y, z));
        }

        @Nullable
        public static Ref<ChunkStore> getRef(@Nonnull ChunkStore chunkStore, int x, int y, int z) {
            return Entity.getRef(chunkStore, ChunkUtil.indexBlockInColumn(x, y, z));
        }

        @Nullable
        public static Ref<ChunkStore> getRef(@Nonnull Ref<ChunkStore> chunkRef, int x, int y, int z) {
            return Entity.getRef(chunkRef, ChunkUtil.indexBlockInColumn(x, y, z));
        }

        @Nullable
        public static Ref<ChunkStore> getRef(@Nonnull BlockComponentChunk blockComponentChunk, int x, int y, int z) {
            return Entity.getRef(blockComponentChunk, ChunkUtil.indexBlockInColumn(x, y, z));
        }

        // #endregion int

        // #region vector
        // ====================================================================
        // GET REF: from seperate vector coords -> redirects to index version

        @Nullable
        public static Ref<ChunkStore> getRef(@Nonnull World world, @Nonnull Vector3i coords) {
            return Entity.getRef(world, ChunkUtil.indexBlockInColumn(coords.x, coords.y, coords.z));
        }

        @Nullable
        public static Ref<ChunkStore> getRef(
            @Nonnull CommandBuffer<ChunkStore> commandBuffer,
            @Nonnull Vector3i coords
        ) {
            return Entity.getRef(commandBuffer, ChunkUtil.indexBlockInColumn(coords.x, coords.y, coords.z));
        }

        @Nullable
        public static Ref<ChunkStore> getRef(@Nonnull ChunkStore chunkStore, @Nonnull Vector3i coords) {
            return Entity.getRef(chunkStore, ChunkUtil.indexBlockInColumn(coords.x, coords.y, coords.z));
        }

        @Nullable
        public static Ref<ChunkStore> getRef(@Nonnull Ref<ChunkStore> chunkRef, @Nonnull Vector3i coords) {
            return Entity.getRef(chunkRef, ChunkUtil.indexBlockInColumn(coords.x, coords.y, coords.z));
        }

        @Nullable
        public static Ref<ChunkStore> getRef(
            @Nonnull BlockComponentChunk blockComponentChunk,
            @Nonnull Vector3i coords
        ) {
            return Entity.getRef(blockComponentChunk, ChunkUtil.indexBlockInColumn(coords.x, coords.y, coords.z));
        }

        // #endregion vector

        // ====================================================================
        // ====================================================================
        // GET REF: from chunk index
        // ====================================================================
        // ====================================================================

        @Nullable
        public static Ref<ChunkStore> getRef(@Nonnull World world, int blockIndex) {
            return Entity.getRef(world.getChunkStore(), blockIndex);
        }

        @Nullable
        public static Ref<ChunkStore> getRef(@Nonnull CommandBuffer<ChunkStore> commandBuffer, int blockIndex) {
            return Entity.getRef(commandBuffer.getExternalData(), blockIndex);
        }

        @Nullable
        public static Ref<ChunkStore> getRef(@Nonnull ChunkStore chunkStore, int blockIndex) {
            var chunkRef = Chunk.getChunkRef(chunkStore, blockIndex);
            if (chunkRef == null) {
                return null;
            }

            return Entity.getRef(chunkRef, blockIndex);
        }

        @Nullable
        public static Ref<ChunkStore> getRef(@Nonnull Ref<ChunkStore> chunkRef, int blockIndex) {
            var blockComponentChunk = BlockComponent.getBlockComponentChunk(chunkRef);
            if (blockComponentChunk == null) {
                return null;
            }

            return Entity.getRef(blockComponentChunk, blockIndex);
        }

        @Nullable
        public static Ref<ChunkStore> getRef(@Nonnull BlockComponentChunk blockComponentChunk, int blockIndex) {
            var blockRef = blockComponentChunk.getEntityReference(blockIndex);
            if (blockRef == null || !blockRef.isValid()) {
                return null;
            }

            return blockRef;
        }

        // #endregion getRef
    }

    public static final class Info {

        @Nonnull
        @SuppressWarnings("null")
        private static final ComponentType<ChunkStore, BlockStateInfo> BLOCK_STATE_INFO_COMPONENT =
            BlockStateInfo.getComponentType();

        // #region get
        // Function to get `BlockStateInfo` component for a block. You can provide
        // - a ref to the block
        // - the local coordinates of the block and the chunk it's in
        // - the global coordinates of the block #TODO

        @Nullable
        public static final BlockStateInfo get(
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

            return get(ref);
        }

        @Nullable
        public static BlockStateInfo get(@Nonnull Ref<ChunkStore> blockRef) {
            return BlockComponent.getComponent(blockRef, BLOCK_STATE_INFO_COMPONENT);
        }

        // #endregion get
    }

    public static final class Coords {

        // #region getLocalCoords
        // Function: given some aspect of a block and i'll find a way to get the coords of it.
        // - BlockStateInfo => (coords inbuilt, but, i make it easier to access)
        // - Ref to the block itself

        // Get the local coords of the block in its chunk
        @Nullable
        public static Vector3i getLocalCoords(@Nonnull Ref<ChunkStore> blockRef) {
            var info = Info.get(blockRef);
            if (info == null) {
                return null;
            }

            return getLocalCoords(info.getIndex());
        }

        @Nonnull
        public static Vector3i getLocalCoords(@Nonnull BlockStateInfo info) {
            return getLocalCoords(info.getIndex());
        }

        // remember kids: multidimensional arrays are like birds - a lie
        @Nonnull
        public static Vector3i getLocalCoords(int index) {
            int x = ChunkUtil.xFromBlockInColumn(index);
            int y = ChunkUtil.yFromBlockInColumn(index);
            int z = ChunkUtil.zFromBlockInColumn(index);

            return new Vector3i(x, y, z);
        }

        // #endregion getLocalCoords

        // #region getGlobalCoords
        // Function: Get the coordinates of a block from any one of the following
        // - blockRef                    << reference to the block itself
        // - BlockStateInfo              << the info about the block (the way we actually get its local coords)
        // - BlockChunk AND info         << chunk gives us the transform from local to global coords
        // - WorldChunk AND info         << chunk gives us the transform from local to global coords
        // - [BlockAccessor] AND info    << chunk gives us the transform from local to global coords

        // Block ref isn't useful on its own. We grab the "info" and handball over
        // to an actual entrypoint for getting the coords
        @Nullable
        public static Vector3i getGlobalCoords(@Nonnull Ref<ChunkStore> blockRef) {
            var info = Info.get(blockRef);
            if (info == null) {
                return null;
            }

            return getGlobalCoords(info);
        }

        // Info IS useful on its own, just, apparently it can fail to get the chunk??
        // anyway, we simply grab the chunk, and return the coords
        //
        // The second we have the chunk, it's non-nullable
        @Nullable
        public static Vector3i getGlobalCoords(@Nonnull BlockStateInfo info) {
            var chunk = Chunk.getWorldChunk(info);
            if (chunk == null) {
                return null;
            }

            return getGlobalCoords(chunk, info);
        }

        // Info                      => coordinates of the block relative to its chunk
        // WorldChunk/BlockAccessor  => coordinates of the chunk
        //
        // local coords + chunk offset = global coords
        @Nonnull
        public static Vector3i getGlobalCoords(@Nonnull BlockAccessor chunk, @Nonnull BlockStateInfo info) {
            var localCoords = Coords.getLocalCoords(info);
            return toGlobalCoords(chunk, localCoords.x, localCoords.y, localCoords.z);
        }

        // block index               => if i understand right, hytale is using that classic
        //                              CS idea of "a multidimensional array is secretly just
        //                              a really big array", so index is the same as coordinates
        //                              when using cool maths.
        //                              Thus, if we have the index, we already have the local coordinates
        // WorldChunk/BlockAccessor  => coordinates of the chunk
        //
        // local coords + chunk offset = global coords
        @Nonnull
        public static Vector3i getGlobalCoords(@Nonnull BlockAccessor chunk, int blockIndex) {
            var localCoords = Coords.getLocalCoords(blockIndex);
            return toGlobalCoords(chunk, localCoords.x, localCoords.y, localCoords.z);
        }

        // Local coords              => coordinates of the block WITHIN the chunk
        // WorldChunk/BlockAccessor  => coordinates of the chunk
        //
        // local coords + chunk offset = global coords
        @Nonnull
        public static Vector3i getGlobalCoords(@Nonnull BlockAccessor chunk, @Nonnull Vector3i localCoords) {
            return toGlobalCoords(chunk, localCoords.x, localCoords.y, localCoords.z);
        }

        // #region blockChunk
        // SPECIAL CASE: BlockChunk
        // For some reason "BlockChunk" isn't a block accessor, but World chunk is (and world chunk USES block chunk, so, ??)
        // so, the code is identical, but hey, fuck it, if it works, it works.

        @Nonnull
        public static Vector3i getGlobalCoords(@Nonnull BlockChunk chunk, @Nonnull BlockStateInfo info) {
            return getGlobalCoords(chunk, Coords.getLocalCoords(info));
        }

        @Nonnull
        public static Vector3i getGlobalCoords(@Nonnull BlockChunk chunk, @Nonnull Vector3i localCoords) {
            return toGlobalCoords(chunk, localCoords.x, localCoords.y, localCoords.z);
        }

        @Nonnull
        public static Vector3i toGlobalCoords(@Nonnull BlockChunk chunk, int localX, int localY, int localZ) {
            int globalX = localX + (chunk.getX() * 32);
            int globalZ = localZ + (chunk.getZ() * 32);

            return new Vector3i(globalX, localY, globalZ);
        }

        // #endregion blockChunk
        // #endregion getGlobalCoords

        @Nonnull
        public static Vector3i toGlobalCoords(@Nonnull BlockAccessor chunk, int localX, int localY, int localZ) {
            int globalX = localX + (chunk.getX() * 32);
            int globalZ = localZ + (chunk.getZ() * 32);

            return new Vector3i(globalX, localY, globalZ);
        }
    }

    public static final class Chunk {

        // #region getWorldChunk

        @Nullable
        public static WorldChunk getWorldChunkFromChunkRef(@Nonnull Ref<ChunkStore> chunkRef) {
            return BlockComponent.getComponent(chunkRef, WORLD_CHUNK_COMPONENT);
        }

        @Nullable
        public static WorldChunk getWorldChunkFromBlockRef(@Nonnull Ref<ChunkStore> blockRef) {
            var info = Info.get(blockRef);
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
        public static WorldChunk getWorldChunk(@Nonnull Ref<ChunkStore> ref) {
            // Potential 1: The ref you passed me is a CHUNK ref. slay. thats the good shit.
            var maybeChunk = BlockComponent.getComponent(ref, WORLD_CHUNK_COMPONENT);
            if (maybeChunk != null) {
                return maybeChunk;
            }

            // Potential 2: the ref you passed me is a BLOCK ref. GOOD.
            var info = Info.get(ref);
            if (info == null) {
                return null;
            }

            return BlockComponent.getComponent(info.getChunkRef(), WORLD_CHUNK_COMPONENT);
        }

        @Nullable
        public static WorldChunk getWorldChunk(@Nonnull BlockStateInfo info) {
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
        public static WorldChunk getWorldChunk(@Nonnull BlockStateInfo info, int chunkX, int chunkZ) {
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
        public static WorldChunk getWorldChunk(@Nonnull BlockStateInfo info, long chunkIndex) {
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
        public static WorldChunk getWorldChunk(@Nonnull Ref<ChunkStore> ref, int chunkX, int chunkZ) {
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
        public static WorldChunk getWorldChunk(@Nonnull Ref<ChunkStore> ref, long chunkIndex) {
            throw new RuntimeException("ERROR: Riley you need to TEST THIS FUNCTION. Remove me if intentional.");
            // return ref.getStore().getExternalData().getChunkComponent(chunkIndex, WORLD_CHUNK_COMPONENT);
            // ALT VERSION: use the ref as above in the single ref param version of this function to figure out if we're a block ref or a chunk ref and get the right store based on that
        }

        // UNTESTED (most methods are untested, or, kind of half tested. some i'm pretty sure work, some i've got no idea, at some point i'll go through and verify them all)
        @Nullable
        public static WorldChunk getWorldChunk(
            @Nonnull CommandBuffer<ChunkStore> commandBuffer, // << Doesn't need to get the world, it can access components directly
            @Nonnull BlockChunk blockChunk
        ) {
            // but regardless we don't need the world to get the world chunk
            return getWorldChunk(commandBuffer.getExternalData(), blockChunk);
        }

        // UNTESTED
        @Nullable
        public static WorldChunk getWorldChunk(@Nonnull Store<ChunkStore> store, @Nonnull BlockChunk blockChunk) {
            return getWorldChunk(store.getExternalData(), blockChunk);
        }

        // UNTESTED
        // NOTE: ChunkStore IS a world provider, but, there's seemingly other easier ways to get the world chunk component? maybe? cause otherwise we'd have to do
        // `chunkStore.getWorld().getChunk(blockChunk.getIndex());`
        @Nullable
        public static WorldChunk getWorldChunk(@Nonnull ChunkStore chunkStore, @Nonnull BlockChunk blockChunk) {
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
        public static WorldChunk getWorldChunk(
            @Nonnull World world, // well, it is the world, sooo
            @Nonnull BlockChunk blockChunk
        ) {
            return world.getChunk(blockChunk.getIndex());
        }

        // not sure if there's a way to get a ref from a block chunk directly, need
        // some way to tie it to a world.
        // UNTESTED
        @Nullable
        public static WorldChunk getWorldChunk(
            @Nonnull World world, // well, it is the world, sooo
            long chunkIndex
        ) {
            return world.getChunk(chunkIndex);
        }

        // WorldProvider can be
        // - World
        // - ChunkStore
        // - idk, probably other stuff too
        // UNTESTED
        @Nullable
        public static WorldChunk getWorldChunk(@Nonnull WorldProvider worldProvider, @Nonnull BlockChunk blockChunk) {
            return worldProvider.getWorld().getChunk(blockChunk.getIndex());
        }

        // UNTESTED
        @Nullable
        public static WorldChunk getWorldChunk(@Nonnull WorldProvider worldProvider, long chunkIndex) {
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
        public static Ref<ChunkStore> getChunkRef(@Nonnull CommandBuffer<ChunkStore> commandBuffer, Vector3i coords) {
            return getChunkRef(commandBuffer.getExternalData(), ChunkUtil.indexChunkFromBlock(coords.x, coords.z));
        }

        @Nullable
        public static Ref<ChunkStore> getChunkRef(@Nonnull World world, Vector3i coords) {
            return getChunkRef(world, ChunkUtil.indexChunkFromBlock(coords.x, coords.z));
        }

        @Nullable
        public static Ref<ChunkStore> getChunkRef(@Nonnull ChunkStore chunkStore, @Nonnull Vector3i coords) {
            return getChunkRef(chunkStore, ChunkUtil.indexChunkFromBlock(coords.x, coords.z));
        }

        // please don't use this one.. just here for completeness so you know you CAN get the chunk ref out of info - in fact, that's (in my understanding) the preferred way
        @Nullable
        public static Ref<ChunkStore> getChunkRef(@Nonnull BlockStateInfo info) {
            return info.getChunkRef();
        }

        @Nullable
        public static Ref<ChunkStore> getChunkRef(@Nonnull Ref<ChunkStore> blockRef) {
            var info = Info.get(blockRef);
            if (info == null) {
                return null;
            }
            return info.getChunkRef();
        }

        // With coords (x, z) -> redirects to the chunk index version
        @Nullable
        public static Ref<ChunkStore> getChunkRef(@Nonnull CommandBuffer<ChunkStore> commandBuffer, int x, int z) {
            return getChunkRef(commandBuffer.getExternalData(), ChunkUtil.indexChunkFromBlock(x, z));
        }

        @Nullable
        public static Ref<ChunkStore> getChunkRef(@Nonnull ChunkStore chunkStore, int x, int z) {
            return getChunkRef(chunkStore, ChunkUtil.indexChunkFromBlock(x, z));
        }

        @Nullable
        public static Ref<ChunkStore> getChunkRef(@Nonnull World world, int x, int z) {
            return getChunkRef(world, ChunkUtil.indexChunkFromBlock(x, z));
        }

        // With chunk index
        @Nullable
        public static Ref<ChunkStore> getChunkRef(@Nonnull CommandBuffer<ChunkStore> commandBuffer, long chunkIndex) {
            return getChunkRef(commandBuffer.getExternalData(), chunkIndex);
        }

        @Nullable
        public static Ref<ChunkStore> getChunkRef(@Nonnull World world, long chunkIndex) {
            return getChunkRef(world.getChunkStore(), chunkIndex);
        }

        @Nullable
        public static Ref<ChunkStore> getChunkRef(@Nonnull ChunkStore chunkStore, long chunkIndex) {
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
        public static Integer getBlockId(@Nonnull CommandBuffer<ChunkStore> commandBuffer, @Nonnull Vector3i coords) {
            var chunkRef = Chunk.getChunkRef(commandBuffer, coords.x, coords.z);
            if (chunkRef == null) {
                return null;
            }

            var worldChunk = Chunk.getWorldChunk(chunkRef);
            if (worldChunk == null) {
                return null;
            }

            return worldChunk.getBlock(coords.x, coords.y, coords.z);
        }

        @Nullable
        public static Integer getBlockId(@Nonnull CommandBuffer<ChunkStore> commandBuffer, int x, int y, int z) {
            var chunkRef = Chunk.getChunkRef(commandBuffer, x, z);
            if (chunkRef == null) {
                return null;
            }

            var worldChunk = BlockComponent.getComponent(chunkRef, WORLD_CHUNK_COMPONENT);
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
        public static int getBlockId(@Nonnull String blockId) {
            return BlockType.getAssetMap().getIndex(blockId);
        }
    }

    public static final class TickProcedure {

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

        public static boolean setTicking(@Nonnull BlockStateInfo info) {
            return setTicking(info, true);
        }

        public static boolean setTicking(@Nonnull BlockStateInfo info, boolean ticking) {
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

        public static boolean setTicking(@Nonnull BlockChunk chunk, @Nonnull BlockStateInfo info, boolean ticking) {
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

    public static final class BlockComponent {

        @Nullable
        public <T extends Component<ChunkStore>> T getComponent(
            @Nonnull ComponentType<ChunkStore, T> componentType,
            @Nonnull World world,
            int x,
            int y,
            int z
        ) {
            var chunkRef = world.getChunkStore().getChunkReference(ChunkUtil.indexChunkFromBlock(x, z));
            if (chunkRef == null) {
                return null;
            }

            var chunkStore = world.getChunkStore().getStore();
            var blockComponentChunk = BlockComponent.getBlockComponentChunk(chunkRef);
            if (blockComponentChunk == null) {
                return null;
            }

            var blockRef = blockComponentChunk.getEntityReference(ChunkUtil.indexBlockInColumn(x, y, z));
            if (blockRef == null || !blockRef.isValid()) {
                return null;
            }

            return chunkStore.getComponent(blockRef, componentType);
        }

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
            @Nonnull Ref<ChunkStore> ref,
            @Nonnull Supplier<ComponentType<ChunkStore, T>> getComponentType
        ) {
            var componentType = getComponentType.get();
            if (componentType == null) {
                return null;
            }
            return ref.getStore().getComponent(ref, componentType);
        }

        public static <T extends Component<ChunkStore>> T getComponent(
            @Nonnull Ref<ChunkStore> ref,
            @Nonnull ComponentType<ChunkStore, T> componentType
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
        public static BlockType getBlockType(@Nonnull String blockId) {
            return BlockType.getAssetMap().getAsset(blockId);
        }
    }
}
