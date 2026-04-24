package dev.twunk.hytale.utils;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
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
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import dev.twunk.hytale.LibHytaleException;
import dev.twunk.lib.test.TestUtil;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

// BlockStateInfo is your friend. All block entities have it.
// - If you want to get the world a block is in
// - If you want to get the CHUNK a block is in
// - If you want to get OTHER blocks
//
// All of those ^^ are gotten THROUGH BlockStateInfo
// effectively, BlockStateInfo + Ref to your block => your dream come true

// Utils for blocks. Slowly figuring out what this should look like
// NOTE - its current state is broken

public abstract class BlockUtils {

    // ==================================================
    // Component types (trust me bro, i swear they're not null)
    // ==================================================

    @SuppressWarnings("null")
    private static final ComponentType<ChunkStore, WorldChunk> WORLD_CHUNK_COMPONENT = WorldChunk.getComponentType();

    @SuppressWarnings("null")
    public static final ComponentType<ChunkStore, BlockStateInfo> BLOCK_STATE_INFO_COMPONENT_TYPE =
        BlockStateInfo.getComponentType();

    public static final List<Boolean> testRefDetection(final Ref<ChunkStore> blockRef) {
        // functions to test
        final ArrayList<Boolean> refs = new ArrayList<>();

        final var info = BlockUtils.Info.get(blockRef);
        if (info == null) {
            throw new LibHytaleException("Error: failed to get info for test in asfiuogrt71t7o83");
        }

        final var chunkRef = info.getChunkRef();

        // BLOCK ref passed to isBlockRef (expect TRUE)
        refs.add(BlockUtils.isBlockRef(blockRef));
        // BLOCK ref passed to isChunkRef (expect FALSE)
        refs.add(BlockUtils.isChunkRef(blockRef));

        // CHUNK ref passed to isChunkRef (expect TRUE)
        refs.add(BlockUtils.isChunkRef(chunkRef));
        // CHUNK ref passed to isBlockRef (expect FALSE)
        refs.add(BlockUtils.isBlockRef(chunkRef));

        return refs;
    }

    public static final boolean isBlockRef(final Ref<ChunkStore> ref) {
        return ComponentUtils.has(ref, BLOCK_STATE_INFO_COMPONENT_TYPE);
    }

    public static final boolean isChunkRef(final Ref<ChunkStore> ref) {
        return ComponentUtils.has(ref, WORLD_CHUNK_COMPONENT);
    }

    /// -> get Ref<ChunkStore>  (BlockRef)
    /// -> get BlockStateInfo
    /// -> get ID               (int)
    /// -> get BlockType

    public static final class Coords {

        /// -> get LOCAL coordinates within chunk    (Vector3i)
        public static final class Local {

            public static final void test(
                final CommandBuffer<ChunkStore> commandBuffer,
                final Vector3i providedCoords
            ) {
                final var blockX = providedCoords.x;
                final var blockY = providedCoords.y;
                final var blockZ = providedCoords.z;
                final var index = Coords.Index.get(blockX, blockY, blockZ);
                final var localCoords = Coords.Local.get(index);
                final var blockCoords = new Vector3i(blockX, blockY, blockZ);

                final var test = new TestUtil(commandBuffer, blockCoords);

                var localFromBlockRef = Coords.Local.get(test.blockRef);
                if (localFromBlockRef == null || !localFromBlockRef.equals(localCoords)) {
                    throw new LibHytaleException("Failed to convert blockRef to localCoords");
                }
                @Nonnull
                var local = Coords.Local.get(test.info);
                if (!local.equals(localCoords)) {
                    throw new LibHytaleException("Failed to convert info to localCoords");
                }
                local = Coords.Local.get(blockCoords);
                if (!local.equals(localCoords)) {
                    throw new LibHytaleException("Failed to convert blockCoords to localCoords");
                }
                local = Coords.Local.get(localCoords);
                if (!local.equals(localCoords)) {
                    throw new LibHytaleException("Failed to convert LOCAL coords to localCoords");
                }
                local = Coords.Local.get(blockCoords.x, blockCoords.y, blockCoords.z);
                if (!local.equals(localCoords)) {
                    throw new LibHytaleException("Failed to convert coords (split) to localCoords");
                }
                local = Coords.Local.get(localCoords.x, localCoords.y, localCoords.z);
                if (!local.equals(localCoords)) {
                    throw new LibHytaleException("Failed to convert local coords (split) to localCoords");
                }
                local = Coords.Local.get(index);
                if (!local.equals(localCoords)) {
                    throw new LibHytaleException("Failed to convert block index to localCoords");
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
            public static final Vector3i get(final Ref<ChunkStore> blockRef) {
                final var info = BlockUtils.Info.get(blockRef);
                if (info == null) {
                    return null;
                }

                // => blockIndex
                return Coords.Local.get(info.getIndex());
            }

            public static final Vector3i get(final BlockStateInfo info) {
                // => blockIndex
                return Coords.Local.get(info.getIndex());
            }

            // ====================================================================
            // Global coords to local coords
            // ====================================================================

            public static final Vector3i get(final Vector3i coords) {
                return new Vector3i(coords.x & 31, coords.y, coords.z & 31);
            }

            // You're telling me i can get local coordinates from coordinates?
            // yeah, cause, well, if you're calling this its definitely with global coords
            //
            // thus, all we need to do is do is keep x % 32, y, z % 32
            //
            // notably, 32 = 2^5, thus we only need to keep the 5 lowest bits
            public static final Vector3i get(final int x, final int y, final int z) {
                return new Vector3i(x & 31, y, z & 31);
            }

            // ====================================================================
            // index -> this is how we actually get coords of a block throughout their system
            // ====================================================================

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
                final CommandBuffer<ChunkStore> commandBuffer,
                final Vector3i providedCoords
            ) {
                final var blockX = providedCoords.x;
                final var blockY = providedCoords.y;
                final var blockZ = providedCoords.z;
                final var index = Coords.Index.get(blockX, blockY, blockZ);
                final var localCoords = Coords.Local.get(index);
                final var blockCoords = new Vector3i(blockX, blockY, blockZ);

                final var test = new TestUtil(commandBuffer, blockCoords);

                var testIndex = Coords.Index.get(test.blockRef);
                if (testIndex == null || !testIndex.equals(index)) {
                    throw new LibHytaleException("Failed to convert blockRef to blockIndex");
                }
                testIndex = Coords.Index.get(test.info);
                if (testIndex == null || !testIndex.equals(index)) {
                    throw new LibHytaleException("Failed to convert info to blockIndex");
                }
                testIndex = Coords.Index.get(blockCoords);
                if (testIndex == null || !testIndex.equals(index)) {
                    throw new LibHytaleException("Failed to convert blockCoords to blockIndex");
                }
                testIndex = Coords.Index.get(localCoords);
                if (testIndex == null || !testIndex.equals(index)) {
                    throw new LibHytaleException("Failed to convert LOCAL coords to blockIndex");
                }
                testIndex = Coords.Index.get(blockCoords.x, blockCoords.y, blockCoords.z);
                if (testIndex == null || !testIndex.equals(index)) {
                    throw new LibHytaleException("Failed to convert coords (split) to blockIndex");
                }
                testIndex = Coords.Index.get(localCoords.x, localCoords.y, localCoords.z);
                if (testIndex == null || !testIndex.equals(index)) {
                    throw new LibHytaleException("Failed to convert local coords (split) to blockIndex");
                }
            }

            // #region getLocalIndex

            // Integer cause, nullable
            @Nullable
            public static final Integer get(final Ref<ChunkStore> blockRef) {
                final var info = BlockUtils.Info.get(blockRef);
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
            public static final int get(final BlockStateInfo info) {
                return info.getIndex();
            }

            public static final int get(final Vector3i coords) {
                return ChunkUtil.indexBlockInColumn(coords.x, coords.y, coords.z);
            }

            public static final int get(final int x, final int y, final int z) {
                return ChunkUtil.indexBlockInColumn(x, y, z);
            }

            // #endregion getLocalIndex
        }

        /// -> get GLOBAL coordinates                (Vector3i)
        public static final class Global {

            public static final void test(
                final CommandBuffer<ChunkStore> commandBuffer,
                final Vector3i providedCoords
            ) {
                final var blockX = providedCoords.x;
                final var blockY = providedCoords.y;
                final var blockZ = providedCoords.z;
                final var index = Coords.Index.get(blockX, blockY, blockZ);
                final var localCoords = Coords.Local.get(index);
                final var blockCoords = new Vector3i(blockX, blockY, blockZ);

                final var test = new TestUtil(commandBuffer, blockCoords);

                var nullableCoords = Coords.Global.get(test.blockRef);
                if (nullableCoords == null || !nullableCoords.equals(blockCoords)) {
                    throw new LibHytaleException("Failed to convert blockRef to blockCoords");
                }
                nullableCoords = Coords.Global.get(test.info);
                if (nullableCoords == null || !nullableCoords.equals(blockCoords)) {
                    throw new LibHytaleException("Failed to convert info to blockCoords");
                }
                nullableCoords = Coords.Global.get(test.chunkRef, localCoords);
                if (nullableCoords == null || !nullableCoords.equals(blockCoords)) {
                    throw new LibHytaleException("Failed to convert chunkRef + LOCAL coords to blockCoords");
                }
                nullableCoords = Coords.Global.get(test.chunkRef, localCoords.x, localCoords.y, localCoords.z);
                if (nullableCoords == null || !nullableCoords.equals(blockCoords)) {
                    throw new LibHytaleException("Failed to convert chunkRef + LOCAL coords (split) to blockCoords");
                }
                nullableCoords = Coords.Global.get(test.chunkRef, index);
                if (nullableCoords == null || !nullableCoords.equals(blockCoords)) {
                    throw new LibHytaleException("Failed to convert chunkRef + blockIndex to blockCoords");
                }
                @Nonnull
                var coords = Coords.Global.get(test.worldChunk, localCoords);
                if (!coords.equals(blockCoords)) {
                    throw new LibHytaleException("Failed to convert worldChunk + LOCAL coords to blockCoords");
                }
                coords = Coords.Global.get(test.worldChunk, localCoords.x, localCoords.y, localCoords.z);
                if (!coords.equals(blockCoords)) {
                    throw new LibHytaleException("Failed to convert worldChunk + LOCAL coords (split) to blockCoords");
                }
                coords = Coords.Global.get(test.worldChunk, index);
                if (!coords.equals(blockCoords)) {
                    throw new LibHytaleException("Failed to convert worldChunk + blockIndex to blockCoords");
                }
                coords = Coords.Global.get(test.blockChunk, localCoords);
                if (!coords.equals(blockCoords)) {
                    throw new LibHytaleException("Failed to convert blockChunk + LOCAL coords to blockCoords");
                }
                coords = Coords.Global.get(test.blockChunk, localCoords.x, localCoords.y, localCoords.z);
                if (!coords.equals(blockCoords)) {
                    throw new LibHytaleException("Failed to convert blockChunk + LOCAL coords (split) to blockCoords");
                }
                coords = Coords.Global.get(test.blockChunk, index);
                if (!coords.equals(blockCoords)) {
                    throw new LibHytaleException("Failed to convert blockChunk + blockIndex to blockCoords");
                }
                final var chunkCoords = ChunkUtils.Coords.Global.get(blockCoords);
                coords = Coords.Global.get(chunkCoords.x, chunkCoords.z, localCoords);
                if (!coords.equals(blockCoords)) {
                    throw new LibHytaleException("Failed to convert chunk coords (split) + localCoords to blockCoords");
                }
                coords = Coords.Global.get(chunkCoords.x, chunkCoords.z, localCoords.x, localCoords.y, localCoords.z);
                if (!coords.equals(blockCoords)) {
                    throw new LibHytaleException(
                        "Failed to convert chunk coords (split) + localCoords (split) to blockCoords"
                    );
                }
                coords = Coords.Global.get(chunkCoords.x, chunkCoords.z, index);
                if (!coords.equals(blockCoords)) {
                    throw new LibHytaleException("Failed to convert chunk coords (split) + blockIndex to blockCoords");
                }

                final var chunkIndex = ChunkUtils.Coords.Index.get(blockCoords);
                coords = Coords.Global.get(chunkIndex, localCoords);
                if (!coords.equals(blockCoords)) {
                    throw new LibHytaleException("Failed to convert chunkIndex + localCoords to blockCoords");
                }
                coords = Coords.Global.get(chunkIndex, localCoords.x, localCoords.y, localCoords.z);
                if (!coords.equals(blockCoords)) {
                    throw new LibHytaleException(
                        "Failed to convert chunk chunkIndex + localCoords (split) to blockCoords"
                    );
                }
                coords = Coords.Global.get(chunkIndex, index);
                if (!coords.equals(blockCoords)) {
                    throw new LibHytaleException("Failed to convert chunkIndex + blockIndex to blockCoords");
                }
                coords = Coords.Global.get(test.worldChunk, test.info);
                if (!coords.equals(blockCoords)) {
                    throw new LibHytaleException("Failed to convert worldChunk + info to blockCoords");
                }
                coords = Coords.Global.get(test.blockChunk, test.info);
                if (!coords.equals(blockCoords)) {
                    throw new LibHytaleException("Failed to convert blockChunk + info to blockCoords");
                }
                coords = Coords.Global.get(chunkCoords.x, chunkCoords.z, test.info);
                if (!coords.equals(blockCoords)) {
                    throw new LibHytaleException("Failed to convert chunkCoords (split) + info to blockCoords");
                }
                coords = Coords.Global.get(chunkIndex, test.info);
                if (!coords.equals(blockCoords)) {
                    throw new LibHytaleException("Failed to convert chunkIndex + info to blockCoords");
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
            public static final Vector3i get(final Ref<ChunkStore> blockRef) {
                final var info = BlockUtils.Info.get(blockRef);
                if (info == null) {
                    return null;
                }

                return Coords.Global.get(info.getChunkRef(), info.getIndex());
            }

            @Nullable
            public static final Vector3i get(final BlockStateInfo info) {
                return Coords.Global.get(info.getChunkRef(), info.getIndex());
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
            public static final Vector3i get(final Ref<ChunkStore> chunkRef, final Vector3i localCoords) {
                final var worldChunk = ChunkUtils.WorldChunks.get_chunkRef(chunkRef);
                if (worldChunk == null) {
                    return null;
                }

                return Coords.Global.get(worldChunk.getX(), worldChunk.getZ(), localCoords);
            }

            @Nullable
            public static final Vector3i get(
                final Ref<ChunkStore> chunkRef,
                final int localX,
                final int localY,
                final int localZ
            ) {
                final var worldChunk = ChunkUtils.WorldChunks.get_chunkRef(chunkRef);
                if (worldChunk == null) {
                    return null;
                }

                return Coords.Global.get(worldChunk.getX(), worldChunk.getZ(), localX, localY, localZ);
            }

            @Nullable
            public static final Vector3i get(final Ref<ChunkStore> chunkRef, final int blockIndex) {
                final var worldChunk = ChunkUtils.WorldChunks.get_chunkRef(chunkRef);
                if (worldChunk == null) {
                    return null;
                }

                return Coords.Global.get(worldChunk.getX(), worldChunk.getZ(), blockIndex);
            }

            //     #endregion chunkRef
            //     #region BlockAccessor
            //     |=================================================================
            //     | BlockAccessor OR WorldChunk
            //     |=================================================================

            public static final Vector3i get(final WorldChunk chunk, final Vector3i localCoords) {
                return Coords.Global.get(chunk.getX(), chunk.getZ(), localCoords);
            }

            public static final Vector3i get(
                final WorldChunk chunk,
                final int localX,
                final int localY,
                final int localZ
            ) {
                return Coords.Global.get(chunk.getX(), chunk.getZ(), localX, localY, localZ);
            }

            public static final Vector3i get(final WorldChunk chunk, final int blockIndex) {
                return Coords.Global.get(chunk.getX(), chunk.getZ(), blockIndex);
            }

            // this seems pointless, but its for anyone who already has a worldChunk so i don't have to fetch it
            public static final Vector3i get(final WorldChunk chunk, final BlockStateInfo info) {
                return Coords.Global.get(chunk.getX(), chunk.getZ(), info.getIndex());
            }

            //     #endregion chunkRef
            //     #region BlockAccessor
            //     |=================================================================
            //     | BlockAccessor OR WorldChunk
            //     |=================================================================

            public static final Vector3i get(final BlockAccessor chunk, final Vector3i localCoords) {
                return Coords.Global.get(chunk.getX(), chunk.getZ(), localCoords);
            }

            public static final Vector3i get(
                final BlockAccessor chunk,
                final int localX,
                final int localY,
                final int localZ
            ) {
                return Coords.Global.get(chunk.getX(), chunk.getZ(), localX, localY, localZ);
            }

            public static final Vector3i get(final BlockAccessor chunk, final int blockIndex) {
                return Coords.Global.get(chunk.getX(), chunk.getZ(), blockIndex);
            }

            public static final Vector3i get(final BlockAccessor chunk, final BlockStateInfo info) {
                return Coords.Global.get(chunk.getX(), chunk.getZ(), info.getIndex());
            }

            //     #endregion BlockAccessor
            //     #region BlockChunk
            //     |=================================================================
            //     | BlockChunk
            //     |=================================================================

            public static final Vector3i get(final BlockChunk chunk, final Vector3i localCoords) {
                return Coords.Global.get(chunk.getX(), chunk.getZ(), localCoords);
            }

            public static final Vector3i get(final BlockChunk chunk, final int blockIndex) {
                return Coords.Global.get(chunk.getX(), chunk.getZ(), blockIndex);
            }

            public static final Vector3i get(final BlockChunk chunk, final BlockStateInfo info) {
                return Coords.Global.get(chunk.getX(), chunk.getZ(), info.getIndex());
            }

            public static final Vector3i get(
                final BlockChunk chunk,
                final int localX,
                final int localY,
                final int localZ
            ) {
                return Coords.Global.get(chunk.getX(), chunk.getZ(), localX, localY, localZ);
            }

            //     #endregion BlockChunk
            //     #region done
            //     |=================================================================
            //     | Done!
            //     |=================================================================

            // Chunk Coords AND Block Coords
            public static final Vector3i get(final int chunkX, final int chunkZ, final Vector3i localCoords) {
                return new Vector3i(localCoords.x + (chunkX << 5), localCoords.y, localCoords.z + (chunkZ << 5));
            }

            // Chunk Coords AND Block Coords
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
            public static final Vector3i get(final int chunkX, final int chunkZ, final int blockIndex) {
                // remember: 5 bits -> 32
                final int globalX = (blockIndex & 31) + (chunkX << 5); // globalX = local X + 32 * chunkX (chunk is 32x32 for x,z)
                final int globalY = (blockIndex >> 10) & ChunkUtil.HEIGHT_MASK; // same as local y, there is no distinction between the two
                final int globalZ = ((blockIndex >> 5) & 31) + (chunkZ << 5); // globalX = local z + 32 * chunkZ (chunk is 32x32 for x,z)

                return new Vector3i(globalX, globalY, globalZ);
            }

            // may seem pointless, but its so if you already have the chunkX and chunkZ i don't have to fetch them
            public static final Vector3i get(final int chunkX, final int chunkZ, final BlockStateInfo info) {
                final var blockIndex = info.getIndex();

                // remember: 5 bits -> 32
                final int globalX = (blockIndex & 31) + (chunkX << 5); // globalX = local X + 32 * chunkX (chunk is 32x32 for x,z)
                final int globalY = (blockIndex >> 10) & ChunkUtil.HEIGHT_MASK; // same as local y, there is no distinction between the two
                final int globalZ = ((blockIndex >> 5) & 31) + (chunkZ << 5); // globalX = local z + 32 * chunkZ (chunk is 32x32 for x,z)

                return new Vector3i(globalX, globalY, globalZ);
            }

            // Chunk Coords AND Block Coords
            public static final Vector3i get(final long chunkIndex, final Vector3i localCoords) {
                final int chunkX = (int) (chunkIndex >> 32);
                final int chunkZ = (int) (chunkIndex);

                return new Vector3i(localCoords.x + (chunkX << 5), localCoords.y, localCoords.z + (chunkZ << 5));
            }

            // Chunk Coords AND Block Coords
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
            public static final Vector3i get(final long chunkIndex, final BlockStateInfo info) {
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

    public static final class Refs {

        /**
         * figured out how to
         * - break blocks (depends on if they're block entities or not on how you can do
         *     it, both can be done the same way if you do world.execute(()->world.breakBlock)
         *     BUT, if it's NOT a block entity you can just call breakBlock
         * - get block
         * - get block type
         * - get chunk (in various methods)
         */
        public static final void testWorldMethods(final World world, final Vector3i coords) {
            try {
                /**
                 * world.breakBlock -> not allowed to call on blocks of your own store, well, rather you're
                 * not allowed to call it on BLOCK ENTITIES, so no chests etc
                 *
                 * or, seems like they only call it on non-ticking chunks?? nah just not allowed to kill a block entity that might run
                 * this works
                 * ```
                 * var ref = Ref_.get(cmd, coords.x, coords.y - 1, coords.z);
                 * ```
                 */

                /** We're allowed to call break block if it's not also a block entity, otherwise it says the store
                 * is currently running some code and tells us NO BAD #illegal
                 *
                 * so, generally, just "execute" break block through world
                 * ```
                 * if (ref == null) {
                 *     world.breakBlock(coords.x, coords.y - 1, coords.z, 0);
                 * } else {
                 *     world.execute(() -> world.breakBlock(coords.x, coords.y - 1, coords.z, 0));
                 * }
                 * ```
                 */
                // >>>>>>>> UPDATE: heck deleting the block below, that is annoying as
                world.execute(() -> world.breakBlock(coords.x, coords.y + 1, coords.z, 0));

                /**
                 * These five work as is
                 */
                world.getBlock(coords.x, coords.y, coords.z);
                world.getBlockType(coords);
                world.getChunkIfInMemory(ChunkUtils.Coords.Index.get(coords));
                world.getChunkIfLoaded(ChunkUtils.Coords.Index.get(coords));
                world.getChunk(ChunkUtils.Coords.Index.get(coords));

                /**
                 * get chunk if non ticking seems to be of no value
                 * idk how to get it to not-fail, always fails for me
                 * ```
                 * var c = world.getChunkIfNonTicking(Chunk.Coords.Index.getChunkIndex(coords));
                 * if (c == null) {
                 *     console.log("getChunkIfNonTicking failed");
                 * }
                 * ```
                 */
            } catch (Exception e) {
                Chat.log("ERROR! " + e);
            }
        }

        /**
         * Tests all methods i've defined for getRef, good news - they work :)
         */
        public static final List<Ref<ChunkStore>> test(
            final Ref<ChunkStore> blockRef,
            final WorldChunk worldChunk,
            final CommandBuffer<ChunkStore> commandBuffer,
            final Vector3i providedCoords
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
                throw new LibHytaleException("ERROR: world was null in test func 12351h9fovasbidlv");
            }
            BlockUtils.Refs.testWorldMethods(testWorld, providedCoords);
            BlockUtils.Refs.testWorldMethods(commandBuffer.getExternalData().getWorld(), providedCoords);
            BlockUtils.Refs.testWorldMethods(commandBuffer.getStore().getExternalData().getWorld(), providedCoords);
            BlockUtils.Refs.testWorldMethods(blockRef.getStore().getExternalData().getWorld(), providedCoords);

            final var blockX = providedCoords.x;
            final var blockY = providedCoords.y;
            final var blockZ = providedCoords.z;
            final var index = Coords.Index.get(blockX, blockY, blockZ);
            final var localCoords = Coords.Local.get(index);
            final var blockCoords = new Vector3i(blockX, blockY, blockZ);
            final var chunkIndex = ChunkUtils.Coords.Index.get(blockCoords);

            final var test = new TestUtil(commandBuffer, blockCoords);
            // functions to test
            final ArrayList<Ref<ChunkStore>> refs = new ArrayList<>();

            // WorldProvider
            refs.add(BlockUtils.Refs.get(test.worldProvider, blockCoords));
            refs.add(BlockUtils.Refs.get(test.worldProvider, blockX, blockY, blockZ));
            refs.add(BlockUtils.Refs.get(test.worldProvider, chunkIndex, index));

            // World
            refs.add(BlockUtils.Refs.get(test.world, blockCoords));
            refs.add(BlockUtils.Refs.get(test.world, blockX, blockY, blockZ));
            refs.add(BlockUtils.Refs.get(test.world, chunkIndex, index));

            // CommandBuffer
            refs.add(BlockUtils.Refs.get(test.commandBuffer, blockCoords));
            refs.add(BlockUtils.Refs.get(test.commandBuffer, blockX, blockY, blockZ));
            refs.add(BlockUtils.Refs.get(test.commandBuffer, chunkIndex, index));

            // BlockStateInfo
            refs.add(BlockUtils.Refs.get(test.info, blockCoords));
            refs.add(BlockUtils.Refs.get(test.info, blockX, blockY, blockZ));
            refs.add(BlockUtils.Refs.get(test.info, chunkIndex, index));

            // ChunkRef (global)
            refs.add(BlockUtils.Refs.get_chunkRef(test.chunkRef, blockCoords));
            refs.add(BlockUtils.Refs.get_chunkRef(test.chunkRef, blockX, blockY, blockZ));
            refs.add(BlockUtils.Refs.get_chunkRef(test.chunkRef, chunkIndex, index));
            // ChunkRef (local)
            refs.add(BlockUtils.Refs.getLocal(test.chunkRef, blockCoords));
            refs.add(BlockUtils.Refs.getLocal(test.chunkRef, localCoords));
            refs.add(BlockUtils.Refs.getLocal(test.chunkRef, blockX, blockY, blockZ));
            refs.add(BlockUtils.Refs.getLocal(test.chunkRef, localCoords.x, localCoords.y, localCoords.z));
            refs.add(BlockUtils.Refs.getLocal(test.chunkRef, index));

            // Store
            refs.add(BlockUtils.Refs.get(test.store, blockCoords));
            refs.add(BlockUtils.Refs.get(test.store, blockX, blockY, blockZ));
            refs.add(BlockUtils.Refs.get(test.store, chunkIndex, index));

            // ChunkStore
            refs.add(BlockUtils.Refs.get(test.chunkStore, blockCoords));
            refs.add(BlockUtils.Refs.get(test.chunkStore, blockX, blockY, blockZ));
            refs.add(BlockUtils.Refs.get(test.chunkStore, chunkIndex, index));

            // BlockRef
            refs.add(BlockUtils.Refs.get_blockRef(test.blockRef, blockCoords));
            refs.add(BlockUtils.Refs.get_blockRef(test.blockRef, blockX, blockY, blockZ));
            refs.add(BlockUtils.Refs.get_blockRef(test.blockRef, chunkIndex, index));

            // WorldChunk
            refs.add(BlockUtils.Refs.get(test.worldChunk, blockCoords));
            refs.add(BlockUtils.Refs.get(test.worldChunk, blockX, blockY, blockZ));
            refs.add(BlockUtils.Refs.get(test.worldChunk, chunkIndex, index));
            refs.add(BlockUtils.Refs.getLocal(test.worldChunk, blockCoords));
            refs.add(BlockUtils.Refs.getLocal(test.worldChunk, blockX, blockY, blockZ));
            refs.add(BlockUtils.Refs.getLocal(test.worldChunk, index));

            // BlockComponentChunk
            refs.add(BlockUtils.Refs.get(test.blockComponentChunk, index));
            refs.add(BlockUtils.Refs.getLocal(test.blockComponentChunk, blockCoords));
            refs.add(BlockUtils.Refs.getLocal(test.blockComponentChunk, blockX, blockY, blockZ));
            refs.add(BlockUtils.Refs.getLocal(test.blockComponentChunk, index));

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
        public static final Ref<ChunkStore> get_blockRef(final Ref<ChunkStore> blockRef, final Vector3i blockCoords) {
            // "other" because it's not necessarily THIS chunk that i'm
            // using to get a block ref for the coords `blockCoords`
            final var otherChunkRef = ChunkUtils.Refs.get(blockRef);
            if (otherChunkRef == null) {
                return null;
            }

            return BlockUtils.Refs.get2(
                otherChunkRef.getStore().getExternalData(),
                ChunkUtils.Coords.Index.get(blockCoords),
                Coords.Index.get(blockCoords)
            );
        }

        // => (ChunkStore, chunkIndex, blockIndex)
        @Nullable
        public static final Ref<ChunkStore> get_blockRef(
            final Ref<ChunkStore> blockRef,
            final int blockX,
            final int blockY,
            final int blockZ
        ) {
            // "other" because it's not necessarily THIS chunk that i'm
            // using to get a block ref for the coords `blockCoords`
            final var otherChunkRef = ChunkUtils.Refs.get(blockRef);
            if (otherChunkRef == null) {
                return null;
            }

            return BlockUtils.Refs.get2(
                otherChunkRef.getStore().getExternalData(),
                ChunkUtils.Coords.Index.get(blockX, blockZ),
                Coords.Index.get(blockX, blockY, blockZ)
            );
        }

        // => (ChunkStore, chunkIndex, blockIndex)
        @Nullable
        public static final Ref<ChunkStore> get_blockRef(
            final Ref<ChunkStore> blockRef,
            final long chunkIndex,
            final int blockIndex
        ) {
            final var otherChunkRef = ChunkUtils.Refs.get(blockRef);
            if (otherChunkRef == null) {
                return null;
            }

            return BlockUtils.Refs.get2(otherChunkRef.getStore().getExternalData(), chunkIndex, blockIndex);
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
        public static final Ref<ChunkStore> get(final WorldProvider worldProvider, final Vector3i coords) {
            // return worldProvider
            //     .getWorld()
            //     .getChunk(Chunk.Coords.Index.getChunkIndex(coords))
            //     .getBlockComponentEntity(coords.x, coords.y, coords.z);
            return BlockUtils.Refs.get2(
                worldProvider.getWorld().getChunkStore(),
                ChunkUtils.Coords.Index.get(coords),
                Coords.Index.get(coords)
            );
        }

        // => (ChunkStore, chunkIndex, blockIndex)
        @Nullable
        public static final Ref<ChunkStore> get(
            final WorldProvider worldProvider,
            final int blockX,
            final int blockY,
            final int blockZ
        ) {
            return BlockUtils.Refs.get2(
                worldProvider.getWorld().getChunkStore(),
                ChunkUtils.Coords.Index.get(blockX, blockZ),
                Coords.Index.get(blockX, blockY, blockZ)
            );
        }

        // => (ChunkStore, chunkIndex, blockIndex)
        @Nullable
        public static final Ref<ChunkStore> get(
            final WorldProvider worldProvider,
            final long chunkIndex,
            final int blockIndex
        ) {
            // final var localCoords = BlockCoords.Local.get(blockIndex);
            // return worldProvider
            //     .getWorld()
            //     .getChunk(chunkIndex)
            //     .getBlockComponentEntity(localCoords.x, localCoords.y, localCoords.z);
            return BlockUtils.Refs.get2(worldProvider.getWorld().getChunkStore(), chunkIndex, blockIndex);
        }

        // #endregion WorldProvider
        // #region World

        // => (ChunkStore, chunkIndex, blockIndex)
        @Nullable
        public static final Ref<ChunkStore> get(final World world, final Vector3i coords) {
            // return world
            //     .getChunk(Chunk.Coords.Index.getChunkIndex(coords))
            //     .getBlockComponentEntity(coords.x, coords.y, coords.z);
            return BlockUtils.Refs.get2(
                world.getChunkStore(),
                ChunkUtils.Coords.Index.get(coords),
                Coords.Index.get(coords)
            );
        }

        // => (ChunkStore, chunkIndex, blockIndex)
        @Nullable
        public static final Ref<ChunkStore> get(
            final World world,
            final int blockX,
            final int blockY,
            final int blockZ
        ) {
            // return world
            //     .getChunk(Chunk.Coords.Index.getChunkIndex(blockX, blockZ))
            //     .getBlockComponentEntity(blockX, blockY, blockZ);
            return BlockUtils.Refs.get2(
                world.getChunkStore(),
                ChunkUtils.Coords.Index.get(blockX, blockZ),
                Coords.Index.get(blockX, blockY, blockZ)
            );
        }

        // => (ChunkStore, chunkIndex, blockIndex)
        @Nullable
        public static final Ref<ChunkStore> get(final World world, final long chunkIndex, final int blockIndex) {
            // final var localCoords = BlockCoords.Local.get(blockIndex);
            // return world.getChunk(chunkIndex).getBlockComponentEntity(localCoords.x, localCoords.y, localCoords.z);
            return BlockUtils.Refs.get2(world.getChunkStore(), chunkIndex, blockIndex);
        }

        // #endregion World
        // #region CommandBuffer

        // => (ChunkStore, chunkIndex, blockIndex)
        @Nullable
        public static final Ref<ChunkStore> get(final CommandBuffer<ChunkStore> commandBuffer, final Vector3i coords) {
            return BlockUtils.Refs.get2(
                commandBuffer.getExternalData(),
                ChunkUtils.Coords.Index.get(coords),
                Coords.Index.get(coords)
            );
        }

        // => (ChunkStore, chunkIndex, blockIndex)
        @Nullable
        public static final Ref<ChunkStore> get(
            final CommandBuffer<ChunkStore> commandBuffer,
            final int blockX,
            final int blockY,
            final int blockZ
        ) {
            return BlockUtils.Refs.get2(
                commandBuffer.getExternalData(),
                ChunkUtils.Coords.Index.get(blockX, blockZ),
                Coords.Index.get(blockX, blockY, blockZ)
            );
        }

        // => (ChunkStore, chunkIndex, blockIndex)
        @Nullable
        public static final Ref<ChunkStore> get(
            final CommandBuffer<ChunkStore> commandBuffer,
            final long chunkIndex,
            final int blockIndex
        ) {
            return BlockUtils.Refs.get2(commandBuffer.getExternalData(), chunkIndex, blockIndex);
        }

        // #endregion CommandBuffer
        // #region BlockStateInfo

        @Nullable
        public static final Ref<ChunkStore> get(final BlockStateInfo info, final Vector3i blockCoords) {
            return BlockUtils.Refs.get2(
                info.getChunkRef().getStore().getExternalData(),
                ChunkUtils.Coords.Index.get(blockCoords),
                Coords.Index.get(blockCoords)
            );
        }

        @Nullable
        public static final Ref<ChunkStore> get(
            final BlockStateInfo info,
            final int blockX,
            final int blockY,
            final int blockZ
        ) {
            return BlockUtils.Refs.get2(
                info.getChunkRef().getStore().getExternalData(),
                ChunkUtils.Coords.Index.get(blockX, blockZ),
                Coords.Index.get(blockX, blockY, blockZ)
            );
        }

        @Nullable
        public static final Ref<ChunkStore> get(
            final BlockStateInfo info,
            final long chunkIndex,
            final int blockIndex
        ) {
            return BlockUtils.Refs.get2(info.getChunkRef().getStore().getExternalData(), chunkIndex, blockIndex);
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
            final Ref<ChunkStore> otherChunkRef,
            final Vector3i blockCoords
        ) {
            return BlockUtils.Refs.get2(
                otherChunkRef.getStore().getExternalData(),
                ChunkUtils.Coords.Index.get(blockCoords),
                Coords.Index.get(blockCoords)
            );
        }

        // some ChunkRef => store of all chunks in the world => our chunk => component ON the chunk itself => block ref in the chunk
        @Nullable
        public static final Ref<ChunkStore> get_chunkRef(
            final Ref<ChunkStore> otherChunkRef,
            final int blockX,
            final int blockY,
            final int blockZ
        ) {
            return BlockUtils.Refs.get2(
                otherChunkRef.getStore().getExternalData(),
                ChunkUtils.Coords.Index.get(blockX, blockZ),
                Coords.Index.get(blockX, blockY, blockZ)
            );
        }

        // some ChunkRef => store of all chunks in the world => our chunk => component ON the chunk itself => block ref in the chunk
        @Nullable
        public static final Ref<ChunkStore> get_chunkRef(
            final Ref<ChunkStore> otherChunkRef,
            final long chunkIndex,
            final int blockIndex
        ) {
            return BlockUtils.Refs.get2(otherChunkRef.getStore().getExternalData(), chunkIndex, blockIndex);
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
        public static final Ref<ChunkStore> get(final Store<ChunkStore> store, final Vector3i blockCoords) {
            final var chunkRef = store.getExternalData().getChunkReference(ChunkUtils.Coords.Index.get(blockCoords));
            if (chunkRef == null) {
                return null;
            }

            return BlockUtils.Refs.getLocal1_chunkRef(chunkRef, Coords.Index.get(blockCoords));
        }

        // => (ChunkStore, chunkIndex, blockIndex)
        @Nullable
        public static final Ref<ChunkStore> get(
            final Store<ChunkStore> store,
            final int blockX,
            final int blockY,
            final int blockZ
        ) {
            final var chunkRef = store.getExternalData().getChunkReference(ChunkUtils.Coords.Index.get(blockX, blockZ));
            if (chunkRef == null) {
                return null;
            }

            return BlockUtils.Refs.getLocal1_chunkRef(chunkRef, Coords.Index.get(blockX, blockY, blockZ));
        }

        // => (ChunkStore, chunkIndex, blockIndex)
        @Nullable
        public static final Ref<ChunkStore> get(
            final Store<ChunkStore> store,
            final long chunkIndex,
            final int blockIndex
        ) {
            final var chunkRef = store.getExternalData().getChunkReference(chunkIndex);
            if (chunkRef == null) {
                return null;
            }

            return BlockUtils.Refs.getLocal1_chunkRef(chunkRef, blockIndex);
        }

        // #endregion Store<ChunkStore>
        // #region ChunkStore

        // => (ChunkStore, chunkIndex, blockIndex)
        @Nullable
        public static final Ref<ChunkStore> get(final ChunkStore chunkStore, final Vector3i blockCoords) {
            final var chunkRef = chunkStore.getChunkReference(ChunkUtils.Coords.Index.get(blockCoords));
            if (chunkRef == null) {
                return null;
            }

            return BlockUtils.Refs.getLocal1_chunkRef(chunkRef, Coords.Index.get(blockCoords));
        }

        // => (ChunkStore, chunkIndex, blockIndex)
        @Nullable
        public static final Ref<ChunkStore> get(
            final ChunkStore chunkStore,
            final int blockX,
            final int blockY,
            final int blockZ
        ) {
            final var chunkRef = chunkStore.getChunkReference(ChunkUtils.Coords.Index.get(blockX, blockZ));
            if (chunkRef == null) {
                return null;
            }

            return BlockUtils.Refs.getLocal1_chunkRef(chunkRef, Coords.Index.get(blockX, blockY, blockZ));
        }

        @Nullable
        public static final Ref<ChunkStore> get(
            final ChunkStore chunkStore,
            final long chunkIndex,
            final int blockIndex
        ) {
            final var chunkRef = chunkStore.getChunkReference(chunkIndex);
            if (chunkRef == null) {
                return null;
            }

            return BlockUtils.Refs.getLocal1_chunkRef(chunkRef, blockIndex);
        }

        @Nullable
        private static final Ref<ChunkStore> get2(
            final ChunkStore chunkStore,
            final long chunkIndex,
            final int blockIndex
        ) {
            final var chunkRef = chunkStore.getChunkReference(chunkIndex);
            if (chunkRef == null) {
                return null;
            }

            return BlockUtils.Refs.getLocal1_chunkRef(chunkRef, blockIndex);
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
        public static final Ref<ChunkStore> getLocal(final Ref<ChunkStore> ref, final Vector3i blockCoords) {
            if (BlockUtils.isBlockRef(ref)) {
                return BlockUtils.Refs.getLocal_blockRef(ref, blockCoords);
            } else {
                return BlockUtils.Refs.getLocal_chunkRef(ref, blockCoords);
            }
        }

        @Nullable
        public static final Ref<ChunkStore> getLocal(
            final Ref<ChunkStore> ref,
            final int blockX,
            final int blockY,
            final int blockZ
        ) {
            if (BlockUtils.isBlockRef(ref)) {
                return BlockUtils.Refs.getLocal_blockRef(ref, blockX, blockY, blockZ);
            } else {
                return BlockUtils.Refs.getLocal_chunkRef(ref, blockX, blockY, blockZ);
            }
        }

        @Nullable
        public static final Ref<ChunkStore> getLocal(final Ref<ChunkStore> ref, final int blockIndex) {
            if (BlockUtils.isBlockRef(ref)) {
                return BlockUtils.Refs.getLocal_blockRef(ref, blockIndex);
            } else {
                return BlockUtils.Refs.getLocal_chunkRef(ref, blockIndex);
            }
        }

        // #endregion anyLocalRef
        // #region localBlockRef
        @Nullable
        public static final Ref<ChunkStore> getLocal_blockRef(
            final Ref<ChunkStore> blockRef,
            final Vector3i blockCoords
        ) {
            final var info = ComponentUtils.get(blockRef, BLOCK_STATE_INFO_COMPONENT_TYPE);
            if (info == null) {
                return null;
            }

            final var blockIndex = Coords.Index.get(blockCoords);

            return BlockUtils.Refs.getLocal1_chunkRef(info.getChunkRef(), blockIndex);
        }

        @Nullable
        public static final Ref<ChunkStore> getLocal_blockRef(
            final Ref<ChunkStore> blockRef,
            final int blockX,
            final int blockY,
            final int blockZ
        ) {
            final var info = ComponentUtils.get(blockRef, BLOCK_STATE_INFO_COMPONENT_TYPE);
            if (info == null) {
                return null;
            }

            final var blockIndex = Coords.Index.get(blockX, blockY, blockZ);

            return BlockUtils.Refs.getLocal1_chunkRef(info.getChunkRef(), blockIndex);
        }

        @Nullable
        public static final Ref<ChunkStore> getLocal_blockRef(final Ref<ChunkStore> blockRef, final int blockIndex) {
            final var info = ComponentUtils.get(blockRef, BLOCK_STATE_INFO_COMPONENT_TYPE);
            if (info == null) {
                return null;
            }

            return BlockUtils.Refs.getLocal1_chunkRef(info.getChunkRef(), blockIndex);
        }

        // #endregion localBlockRef
        // #region localChunkRef

        // ChunkRef => component ON the chunk itself => block ref in the chunk
        @Nullable
        public static final Ref<ChunkStore> getLocal_chunkRef(
            final Ref<ChunkStore> chunkRef,
            final Vector3i blockCoords
        ) {
            final var blockComponentChunk = ComponentUtils.getBlockComponentChunk(chunkRef);
            if (blockComponentChunk == null) {
                return null;
            }

            return BlockUtils.Refs.getLocal0(blockComponentChunk, Coords.Index.get(blockCoords));
        }

        // ChunkRef => component ON the chunk itself => block ref in the chunk
        @Nullable
        public static final Ref<ChunkStore> getLocal_chunkRef(
            final Ref<ChunkStore> chunkRef,
            final int blockX,
            final int blockY,
            final int blockZ
        ) {
            final var blockComponentChunk = ComponentUtils.getBlockComponentChunk(chunkRef);
            if (blockComponentChunk == null) {
                return null;
            }

            return BlockUtils.Refs.getLocal0(blockComponentChunk, Coords.Index.get(blockX, blockY, blockZ));
        }

        // ChunkRef => component ON the chunk itself => block ref in the chunk
        @Nullable
        public static final Ref<ChunkStore> getLocal_chunkRef(final Ref<ChunkStore> chunkRef, final int blockIndex) {
            return BlockUtils.Refs.getLocal1_chunkRef(chunkRef, blockIndex);
        }

        @Nullable
        private static final Ref<ChunkStore> getLocal1_chunkRef(final Ref<ChunkStore> chunkRef, final int blockIndex) {
            final var blockComponentChunk = ComponentUtils.getBlockComponentChunk(chunkRef);
            if (blockComponentChunk == null) {
                return null;
            }

            return BlockUtils.Refs.getLocal0(blockComponentChunk, blockIndex);
        }

        // #endregion localChunkRef
        // #region localBlockStateInfo

        /**
         * Returns another block based on its local coords WITHIN THE SAME CHUNK
         */
        // BlockInfo => ChunkRef => component ON the chunk itself => block ref in the chunk
        @Nullable
        public static final Ref<ChunkStore> getLocal(final BlockStateInfo info, final Vector3i blockCoords) {
            final var blockComponentChunk = ComponentUtils.getBlockComponentChunk(info.getChunkRef());
            if (blockComponentChunk == null) {
                return null;
            }

            return BlockUtils.Refs.getLocal0(blockComponentChunk, Coords.Index.get(blockCoords));
        }

        /**
         * Returns another block based on its local coords WITHIN THE SAME CHUNK
         */
        // BlockInfo => ChunkRef => component ON the chunk itself => block ref in the chunk
        @Nullable
        public static final Ref<ChunkStore> getLocal(
            final BlockStateInfo info,
            final int blockX,
            final int blockY,
            final int blockZ
        ) {
            final var blockComponentChunk = ComponentUtils.getBlockComponentChunk(info.getChunkRef());
            if (blockComponentChunk == null) {
                return null;
            }

            return BlockUtils.Refs.getLocal0(blockComponentChunk, Coords.Index.get(blockX, blockY, blockZ));
        }

        /**
         * Returns another block based on its local coords WITHIN THE SAME CHUNK
         */
        // BlockInfo => ChunkRef => component ON the chunk itself => block ref in the chunk
        @Nullable
        public static final Ref<ChunkStore> getLocal(final BlockStateInfo info, final int blockIndex) {
            final var blockComponentChunk = ComponentUtils.getBlockComponentChunk(info.getChunkRef());
            if (blockComponentChunk == null) {
                return null;
            }

            return BlockUtils.Refs.getLocal0(blockComponentChunk, blockIndex);
        }

        // #endregion localBlockStateInfo
        // #region WorldChunk

        @Nullable
        public static final Ref<ChunkStore> get(final WorldChunk worldChunk, final Vector3i blockCoords) {
            return BlockUtils.Refs.get2(
                worldChunk.getReference().getStore().getExternalData(),
                ChunkUtils.Coords.Index.get(blockCoords),
                Coords.Index.get(blockCoords)
            );
        }

        /** SHOULD REALLY BE CALLED GET LOCAL REF */
        // component ON the chunk itself => block ref in the chunk
        @Nullable
        public static final Ref<ChunkStore> get(
            final WorldChunk worldChunk,
            final int blockX,
            final int blockY,
            final int blockZ
        ) {
            return BlockUtils.Refs.get2(
                worldChunk.getReference().getStore().getExternalData(),
                ChunkUtils.Coords.Index.get(blockX, blockZ),
                Coords.Index.get(blockX, blockY, blockZ)
            );
        }

        @Nullable
        public static final Ref<ChunkStore> get(
            final WorldChunk worldChunk,
            final long chunkIndex,
            final int blockIndex
        ) {
            return BlockUtils.Refs.get2(worldChunk.getReference().getStore().getExternalData(), chunkIndex, blockIndex);
        }

        @Nullable
        public static final Ref<ChunkStore> getLocal(final WorldChunk worldChunk, final Vector3i blockCoords) {
            return worldChunk.getBlockComponentEntity(blockCoords.x, blockCoords.y, blockCoords.z);
        }

        /** SHOULD REALLY BE CALLED GET LOCAL REF */
        // component ON the chunk itself => block ref in the chunk
        @Nullable
        public static final Ref<ChunkStore> getLocal(
            final WorldChunk worldChunk,
            final int blockX,
            final int blockY,
            final int blockZ
        ) {
            return worldChunk.getBlockComponentEntity(blockX, blockY, blockZ);
        }

        @Nullable
        public static final Ref<ChunkStore> getLocal(final WorldChunk worldChunk, final int blockIndex) {
            final var localCoords = Coords.Local.get(blockIndex);
            return worldChunk.getBlockComponentEntity(localCoords.x, localCoords.y, localCoords.z);
        }

        // #endregion WorldChunk
        // #region BlockComponentChunk
        @Nullable
        public static final Ref<ChunkStore> get(final BlockComponentChunk blockComponentChunk, final int blockIndex) {
            return BlockUtils.Refs.getLocal0(blockComponentChunk, blockIndex);
        }

        @Nullable
        public static final Ref<ChunkStore> getLocal(
            final BlockComponentChunk blockComponentChunk,
            final Vector3i blockCoords
        ) {
            return BlockUtils.Refs.getLocal0(blockComponentChunk, Coords.Index.get(blockCoords));
        }

        @Nullable
        public static final Ref<ChunkStore> getLocal(
            final BlockComponentChunk blockComponentChunk,
            final int blockX,
            final int blockY,
            final int blockZ
        ) {
            return BlockUtils.Refs.getLocal0(blockComponentChunk, Coords.Index.get(blockX, blockY, blockZ));
        }

        @Nullable
        public static final Ref<ChunkStore> getLocal(
            final BlockComponentChunk blockComponentChunk,
            final int blockIndex
        ) {
            return BlockUtils.Refs.getLocal0(blockComponentChunk, blockIndex);
        }

        @Nullable
        private static final Ref<ChunkStore> getLocal0(
            final BlockComponentChunk blockComponentChunk,
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

    public static final class Info {

        /**
         * Tests all methods i've defined for getInfo
         */
        public static final List<BlockStateInfo> test(
            final CommandBuffer<ChunkStore> commandBuffer,
            final Vector3i providedCoords
        ) {
            final var blockX = providedCoords.x;
            final var blockY = providedCoords.y;
            final var blockZ = providedCoords.z;
            final var localIndex = Coords.Index.get(blockX, blockY, blockZ);
            final var localCoords = Coords.Local.get(localIndex);
            final var blockCoords = new Vector3i(blockX, blockY, blockZ);
            final var chunkIndex = ChunkUtils.Coords.Index.get(blockCoords);

            final var test = new TestUtil(commandBuffer, blockCoords);
            // functions to test
            final ArrayList<BlockStateInfo> refs = new ArrayList<>();

            // WorldProvider
            refs.add(BlockUtils.Info.get(test.worldProvider, blockCoords));
            refs.add(BlockUtils.Info.get(test.worldProvider, blockX, blockY, blockZ));
            refs.add(BlockUtils.Info.get(test.worldProvider, chunkIndex, localIndex));

            // World
            refs.add(BlockUtils.Info.get(test.world, blockCoords));
            refs.add(BlockUtils.Info.get(test.world, blockX, blockY, blockZ));
            refs.add(BlockUtils.Info.get(test.world, chunkIndex, localIndex));

            // CommandBuffer
            refs.add(BlockUtils.Info.get(test.commandBuffer, blockCoords));
            refs.add(BlockUtils.Info.get(test.commandBuffer, blockX, blockY, blockZ));
            refs.add(BlockUtils.Info.get(test.commandBuffer, chunkIndex, localIndex));

            // BlockStateInfo
            refs.add(BlockUtils.Info.get(test.info, blockCoords));
            refs.add(BlockUtils.Info.get(test.info, blockX, blockY, blockZ));
            refs.add(BlockUtils.Info.get(test.info, chunkIndex, localIndex));

            // ChunkRef (global)
            // - without specifying that it's a chunk ref we provided, letting the code figure that out
            refs.add(BlockUtils.Info.get(test.chunkRef, blockCoords));
            refs.add(BlockUtils.Info.get(test.chunkRef, blockX, blockY, blockZ));
            refs.add(BlockUtils.Info.get(test.chunkRef, chunkIndex, localIndex));
            // - specifying it's a chunk ref
            refs.add(BlockUtils.Info.get_chunkRef(test.chunkRef, blockCoords));
            refs.add(BlockUtils.Info.get_chunkRef(test.chunkRef, blockX, blockY, blockZ));
            refs.add(BlockUtils.Info.get_chunkRef(test.chunkRef, chunkIndex, localIndex));

            // ChunkRef (local)
            // - without specifying that it's a chunk ref we provided, letting the code figure that out
            refs.add(BlockUtils.Info.getLocal(test.chunkRef, localCoords));
            refs.add(BlockUtils.Info.getLocal(test.chunkRef, localCoords.x, localCoords.y, localCoords.z));
            refs.add(BlockUtils.Info.getLocal(test.chunkRef, localIndex));
            // - specifying it's a chunk ref
            refs.add(BlockUtils.Info.getLocal_chunkRef(test.chunkRef, localCoords));
            refs.add(BlockUtils.Info.getLocal_chunkRef(test.chunkRef, localCoords.x, localCoords.y, localCoords.z));
            refs.add(BlockUtils.Info.getLocal_chunkRef(test.chunkRef, localIndex));

            // Store
            refs.add(BlockUtils.Info.get(test.store, blockCoords));
            refs.add(BlockUtils.Info.get(test.store, blockX, blockY, blockZ));
            refs.add(BlockUtils.Info.get(test.store, chunkIndex, localIndex));

            // ChunkStore
            refs.add(BlockUtils.Info.get(test.chunkStore, blockCoords));
            refs.add(BlockUtils.Info.get(test.chunkStore, blockX, blockY, blockZ));
            refs.add(BlockUtils.Info.get(test.chunkStore, chunkIndex, localIndex));

            // BlockRef
            refs.add(BlockUtils.Info.get_blockRef(test.blockRef, blockCoords));
            refs.add(BlockUtils.Info.get_blockRef(test.blockRef, blockX, blockY, blockZ));
            refs.add(BlockUtils.Info.get_chunkRef(test.blockRef, chunkIndex, localIndex));

            // WorldChunk
            refs.add(BlockUtils.Info.get(test.worldChunk, blockCoords));
            refs.add(BlockUtils.Info.get(test.worldChunk, blockX, blockY, blockZ));
            refs.add(BlockUtils.Info.get(test.worldChunk, chunkIndex, localIndex));
            refs.add(BlockUtils.Info.getLocal(test.worldChunk, blockCoords));
            refs.add(BlockUtils.Info.getLocal(test.worldChunk, blockX, blockY, blockZ));
            refs.add(BlockUtils.Info.getLocal(test.worldChunk, localIndex));

            // BlockComponentChunk
            refs.add(BlockUtils.Info.get(test.blockComponentChunk, localIndex));
            refs.add(BlockUtils.Info.getLocal(test.blockComponentChunk, blockCoords));
            refs.add(BlockUtils.Info.getLocal(test.blockComponentChunk, blockX, blockY, blockZ));
            refs.add(BlockUtils.Info.getLocal(test.blockComponentChunk, localIndex));

            return refs;
        }

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
        public static final BlockStateInfo get_blockRef(final Ref<ChunkStore> blockRef, final Vector3i coords) {
            return BlockUtils.Info.get0(BlockUtils.Refs.get_blockRef(blockRef, coords));
        }

        @Nullable
        public static final BlockStateInfo get_blockRef(
            final Ref<ChunkStore> blockRef,
            final int x,
            final int y,
            final int z
        ) {
            return BlockUtils.Info.get0(BlockUtils.Refs.get_blockRef(blockRef, x, y, z));
        }

        @Nullable
        public static final BlockStateInfo get_blockRef(
            final Ref<ChunkStore> blockRef,
            final long chunkIndex,
            final int blockIndex
        ) {
            return BlockUtils.Info.get0(BlockUtils.Refs.get_blockRef(blockRef, chunkIndex, blockIndex));
        }

        @Nullable
        public static final BlockStateInfo get_blockRef(final Ref<ChunkStore> blockRef, final int blockIndex) {
            return BlockUtils.Info.get0(BlockUtils.Refs.getLocal(blockRef, blockIndex));
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
        public static final BlockStateInfo get(final WorldProvider worldProvider, final Vector3i pos) {
            // => BlockRef
            return BlockUtils.Info.get0(BlockUtils.Refs.get(worldProvider, pos));
        }

        @Nullable
        public static final BlockStateInfo get(
            final WorldProvider worldProvider,
            final int x,
            final int y,
            final int z
        ) {
            // => BlockRef
            return BlockUtils.Info.get0(BlockUtils.Refs.get(worldProvider, x, y, z));
        }

        @Nullable
        public static final BlockStateInfo get(
            final WorldProvider worldProvider,
            final long chunkIndex,
            final int blockIndex
        ) {
            // => BlockRef
            return BlockUtils.Info.get0(BlockUtils.Refs.get(worldProvider, chunkIndex, blockIndex));
        }

        // #endregion WorldProvider
        // #region World

        @Nullable
        public static final BlockStateInfo get(final World world, final Vector3i pos) {
            // => BlockRef
            return BlockUtils.Info.get0(BlockUtils.Refs.get(world, pos));
        }

        @Nullable
        public static final BlockStateInfo get(final World world, final int x, final int y, final int z) {
            // => BlockRef
            return BlockUtils.Info.get0(BlockUtils.Refs.get(world, x, y, z));
        }

        @Nullable
        public static final BlockStateInfo get(final World world, final long chunkIndex, final int blockIndex) {
            // => BlockRef
            return BlockUtils.Info.get0(BlockUtils.Refs.get(world, chunkIndex, blockIndex));
        }

        // #endregion World
        // #region CommandBuffer

        @Nullable
        public static final BlockStateInfo get(final CommandBuffer<ChunkStore> commandBuffer, final Vector3i pos) {
            // => BlockRef
            return BlockUtils.Info.get0(BlockUtils.Refs.get(commandBuffer, pos));
        }

        @Nullable
        public static final BlockStateInfo get(
            final CommandBuffer<ChunkStore> commandBuffer,
            final int x,
            final int y,
            final int z
        ) {
            // => BlockRef
            return BlockUtils.Info.get0(BlockUtils.Refs.get(commandBuffer, x, y, z));
        }

        @Nullable
        public static final BlockStateInfo get(
            final CommandBuffer<ChunkStore> commandBuffer,
            final long chunkIndex,
            final int blockIndex
        ) {
            // => BlockRef
            return BlockUtils.Info.get0(BlockUtils.Refs.get(commandBuffer, chunkIndex, blockIndex));
        }

        // #endregion CommandBuffer
        // #region BlockStateInfo

        @Nullable
        public static final BlockStateInfo get(final BlockStateInfo info, final Vector3i blockCoords) {
            return BlockUtils.Info.get0(BlockUtils.Refs.get(info, blockCoords));
        }

        @Nullable
        public static final BlockStateInfo get(
            final BlockStateInfo info,
            final int blockX,
            final int blockY,
            final int blockZ
        ) {
            return BlockUtils.Info.get0(BlockUtils.Refs.get(info, blockX, blockY, blockZ));
        }

        @Nullable
        public static final BlockStateInfo get(final BlockStateInfo info, final long chunkIndex, final int blockIndex) {
            return BlockUtils.Info.get0(BlockUtils.Refs.get(info, chunkIndex, blockIndex));
        }

        // #endregion BlockStateInfo
        // #region anyRef
        @Nullable
        public static final BlockStateInfo get(final Ref<ChunkStore> ref, final Vector3i coords) {
            // Potential: the ref you passed me is a BLOCK ref. slay. thats the good shit. that's what we're after
            final var info = BlockUtils.Info.get(ref);
            if (info != null) {
                return info;
            }

            // If there wasn't a block state info on your ref, we know its a chunk ref
            return get_chunkRef(ref, coords);
        }

        @Nullable
        public static final BlockStateInfo get(
            final Ref<ChunkStore> ref,
            final int blockX,
            final int blockY,
            final int blockZ
        ) {
            // Potential: the ref you passed me is a BLOCK ref. slay. thats the good shit. that's what we're after
            final var info = BlockUtils.Info.get(ref);
            if (info != null) {
                return info;
            }

            // If there wasn't a block state info on your ref, we know its a chunk ref
            return BlockUtils.Info.get_chunkRef(ref, blockX, blockY, blockZ);
        }

        @Nullable
        public static final BlockStateInfo get(final Ref<ChunkStore> ref, final long chunkIndex, final int blockIndex) {
            // Potential: the ref you passed me is a BLOCK ref. slay. thats the good shit. that's what we're after
            final var info = BlockUtils.Info.get(ref);
            if (info != null) {
                return info;
            }

            // If there wasn't a block state info on your ref, we know its a chunk ref
            return BlockUtils.Info.get_chunkRef(ref, chunkIndex, blockIndex);
        }

        // #endregion anyRef
        // #region Ref<ChunkStore> (ChunkRef)

        @Nullable
        public static final BlockStateInfo get_chunkRef(final Ref<ChunkStore> chunkRef, final Vector3i coords) {
            return BlockUtils.Info.get0(BlockUtils.Refs.get_chunkRef(chunkRef, coords));
        }

        @Nullable
        public static final BlockStateInfo get_chunkRef(
            final Ref<ChunkStore> chunkRef,
            final int x,
            final int y,
            final int z
        ) {
            return BlockUtils.Info.get0(BlockUtils.Refs.get_chunkRef(chunkRef, x, y, z));
        }

        @Nullable
        public static final BlockStateInfo get_chunkRef(
            final Ref<ChunkStore> chunkRef,
            final long chunkIndex,
            final int blockIndex
        ) {
            return BlockUtils.Info.get0(BlockUtils.Refs.get_chunkRef(chunkRef, chunkIndex, blockIndex));
        }

        // #endregion Ref<ChunkStore>
        // #region WorldChunk

        @Nullable
        public static final BlockStateInfo get(final WorldChunk worldChunk, final Vector3i blockCoords) {
            final var chunkRef = worldChunk.getReference();
            if (chunkRef == null) {
                return null;
            }

            return BlockUtils.Info.get0(BlockUtils.Refs.get_chunkRef(chunkRef, blockCoords));
        }

        @Nullable
        public static final BlockStateInfo get(
            final WorldChunk worldChunk,
            final int blockX,
            final int blockY,
            final int blockZ
        ) {
            final var chunkRef = worldChunk.getReference();
            if (chunkRef == null) {
                return null;
            }
            return BlockUtils.Info.get0(BlockUtils.Refs.get_chunkRef(chunkRef, blockX, blockY, blockZ));
        }

        @Nullable
        public static final BlockStateInfo get(
            final WorldChunk worldChunk,
            final long chunkIndex,
            final int blockIndex
        ) {
            final var chunkRef = worldChunk.getReference();
            if (chunkRef == null) {
                return null;
            }
            return BlockUtils.Info.get0(BlockUtils.Refs.get_chunkRef(chunkRef, chunkIndex, blockIndex));
        }

        @Nullable
        public static final BlockStateInfo getLocal(final WorldChunk worldChunk, final Vector3i blockCoords) {
            return BlockUtils.Info.get(worldChunk.getBlockComponentEntity(blockCoords.x, blockCoords.y, blockCoords.z));
        }

        @Nullable
        public static final BlockStateInfo getLocal(
            final WorldChunk worldChunk,
            final int blockX,
            final int blockY,
            final int blockZ
        ) {
            return BlockUtils.Info.get(worldChunk.getBlockComponentEntity(blockX, blockY, blockZ));
        }

        @Nullable
        public static final BlockStateInfo getLocal(final WorldChunk worldChunk, final int blockIndex) {
            final var localCoords = Coords.Local.get(blockIndex);
            return BlockUtils.Info.get(worldChunk.getBlockComponentEntity(localCoords.x, localCoords.y, localCoords.z));
        }

        // #endregion WorldChunk
        // #region BlockComponentChunk

        @Nullable
        public static final BlockStateInfo get(final BlockComponentChunk blockComponentChunk, final int blockIndex) {
            return blockComponentChunk.getComponent(blockIndex, BLOCK_STATE_INFO_COMPONENT);
        }

        @Nullable
        public static final BlockStateInfo getLocal(
            final BlockComponentChunk blockComponentChunk,
            final Vector3i blockCoords
        ) {
            final int blockIndex = Coords.Index.get(blockCoords);
            return blockComponentChunk.getComponent(blockIndex, BLOCK_STATE_INFO_COMPONENT);
        }

        @Nullable
        public static final BlockStateInfo getLocal(
            final BlockComponentChunk blockComponentChunk,
            final int blockX,
            final int blockY,
            final int blockZ
        ) {
            final int blockIndex = Coords.Index.get(blockX, blockY, blockZ);
            return blockComponentChunk.getComponent(blockIndex, BLOCK_STATE_INFO_COMPONENT);
        }

        @Nullable
        public static final BlockStateInfo getLocal(
            final BlockComponentChunk blockComponentChunk,
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
        public static final BlockStateInfo get(final Store<ChunkStore> store, final Vector3i pos) {
            // => BlockRef
            return BlockUtils.Info.get0(BlockUtils.Refs.get(store, pos));
        }

        @Nullable
        public static final BlockStateInfo get(final Store<ChunkStore> store, final int x, final int y, final int z) {
            // => BlockRef
            return BlockUtils.Info.get0(BlockUtils.Refs.get(store, x, y, z));
        }

        @Nullable
        public static final BlockStateInfo get(
            final Store<ChunkStore> store,
            final long chunkIndex,
            final int blockIndex
        ) {
            // => BlockRef
            return BlockUtils.Info.get0(BlockUtils.Refs.get(store, chunkIndex, blockIndex));
        }

        // #endregion Store<ChunkStore>
        // #region ChunkStore

        @Nullable
        public static final BlockStateInfo get(final ChunkStore chunkStore, final Vector3i pos) {
            // => BlockRef
            return BlockUtils.Info.get0(BlockUtils.Refs.get(chunkStore, pos));
        }

        @Nullable
        public static final BlockStateInfo get(final ChunkStore chunkStore, final int x, final int y, final int z) {
            // => BlockRef
            return BlockUtils.Info.get0(BlockUtils.Refs.get(chunkStore, x, y, z));
        }

        @Nullable
        public static final BlockStateInfo get(
            final ChunkStore chunkStore,
            final long chunkIndex,
            final int blockIndex
        ) {
            // => BlockRef
            return BlockUtils.Info.get0(BlockUtils.Refs.get(chunkStore, chunkIndex, blockIndex));
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
            final Ref<ChunkStore> chunkRef,
            final Vector3i blockCoords
        ) {
            return BlockUtils.Info.get0(BlockUtils.Refs.getLocal_chunkRef(chunkRef, blockCoords));
        }

        // ChunkRef => component ON the chunk itself => block ref in the chunk
        @Nullable
        public static final BlockStateInfo getLocal_chunkRef(
            final Ref<ChunkStore> chunkRef,
            final int blockX,
            final int blockY,
            final int blockZ
        ) {
            return BlockUtils.Info.get0(BlockUtils.Refs.getLocal_chunkRef(chunkRef, blockX, blockY, blockZ));
        }

        // ChunkRef => component ON the chunk itself => block ref in the chunk
        @Nullable
        public static final BlockStateInfo getLocal_chunkRef(final Ref<ChunkStore> chunkRef, final int blockIndex) {
            return BlockUtils.Info.get0(BlockUtils.Refs.getLocal_chunkRef(chunkRef, blockIndex));
        }

        // #endregion localChunkRef
        // #region localBlockRef

        @Nullable
        public static final BlockStateInfo getLocal_blockRef(
            final Ref<ChunkStore> blockRef,
            final Vector3i blockCoords
        ) {
            return BlockUtils.Info.get0(BlockUtils.Refs.getLocal_blockRef(blockRef, blockCoords));
        }

        // ChunkRef => component ON the chunk itself => block ref in the chunk
        @Nullable
        public static final BlockStateInfo getLocal_blockRef(
            final Ref<ChunkStore> blockRef,
            final int blockX,
            final int blockY,
            final int blockZ
        ) {
            return BlockUtils.Info.get0(BlockUtils.Refs.getLocal_blockRef(blockRef, blockX, blockY, blockZ));
        }

        // ChunkRef => component ON the chunk itself => block ref in the chunk
        @Nullable
        public static final BlockStateInfo getLocal_blockRef(final Ref<ChunkStore> blockRef, final int blockIndex) {
            return BlockUtils.Info.get0(BlockUtils.Refs.getLocal_blockRef(blockRef, blockIndex));
        }

        // #endregion localBlockRef
        // #region anyLocalRef

        @Nullable
        public static final BlockStateInfo getLocal(final Ref<ChunkStore> ref, final Vector3i blockCoords) {
            if (BlockUtils.isBlockRef(ref)) {
                return BlockUtils.Info.getLocal_blockRef(ref, blockCoords);
            } else {
                return BlockUtils.Info.getLocal_chunkRef(ref, blockCoords);
            }
        }

        // ChunkRef => component ON the chunk itself => block ref in the chunk
        @Nullable
        public static final BlockStateInfo getLocal(
            final Ref<ChunkStore> ref,
            final int blockX,
            final int blockY,
            final int blockZ
        ) {
            if (BlockUtils.isBlockRef(ref)) {
                return BlockUtils.Info.getLocal_blockRef(ref, blockX, blockY, blockZ);
            } else {
                return BlockUtils.Info.getLocal_chunkRef(ref, blockX, blockY, blockZ);
            }
        }

        // ChunkRef => component ON the chunk itself => block ref in the chunk
        @Nullable
        public static final BlockStateInfo getLocal(final Ref<ChunkStore> ref, final int blockIndex) {
            if (BlockUtils.isBlockRef(ref)) {
                return BlockUtils.Info.getLocal_blockRef(ref, blockIndex);
            } else {
                return BlockUtils.Info.getLocal_chunkRef(ref, blockIndex);
            }
        }

        // #endregion anyLocalRef
        // #region localBlockStateInfo

        /**
         * Returns another block based on its local coords WITHIN THE SAME CHUNK
         */
        // BlockInfo => ChunkRef => component ON the chunk itself => block ref in the chunk
        @Nullable
        public static final BlockStateInfo getLocal(final BlockStateInfo info, final Vector3i blockCoords) {
            return BlockUtils.Info.get0(BlockUtils.Refs.getLocal(info, blockCoords));
        }

        /**
         * Returns another block based on its local coords WITHIN THE SAME CHUNK
         */
        // BlockInfo => ChunkRef => component ON the chunk itself => block ref in the chunk
        @Nullable
        public static final BlockStateInfo getLocal(
            final BlockStateInfo info,
            final int blockX,
            final int blockY,
            final int blockZ
        ) {
            return BlockUtils.Info.get0(BlockUtils.Refs.getLocal(info, blockX, blockY, blockZ));
        }

        /**
         * Returns another block based on its local coords WITHIN THE SAME CHUNK
         */
        // BlockInfo => ChunkRef => component ON the chunk itself => block ref in the chunk
        @Nullable
        public static final BlockStateInfo getLocal(final BlockStateInfo info, final int blockIndex) {
            return BlockUtils.Info.get0(BlockUtils.Refs.getLocal(info, blockIndex));
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
        public static final BlockStateInfo get(final @Nullable Ref<ChunkStore> blockRef) {
            return BlockUtils.Info.get0(blockRef);
        }

        // BlockRef
        @Nullable
        private static final BlockStateInfo get0(final @Nullable Ref<ChunkStore> blockRef) {
            if (blockRef == null) {
                return null;
            }

            return ComponentUtils.get(blockRef, BLOCK_STATE_INFO_COMPONENT);
        }
        // #endregion BlockRef

        // TODO multiple endpoints:
        // - blockRef + store => store gets OTHER component on entity with ref blockRef
        // - blockChunk -> gets the componnet on a block (doesn't require a ref!!, just the local coords of the block)

        // #endregion getInfo
    }

    public static final class Id {

        /**
         * Tests all methods i've defined for getId
         */
        public static final List<Integer> test(
            final CommandBuffer<ChunkStore> commandBuffer,
            final Vector3i providedCoords,
            final String blockId
        ) {
            final var blockX = providedCoords.x;
            final var blockY = providedCoords.y;
            final var blockZ = providedCoords.z;
            final var localIndex = Coords.Index.get(blockX, blockY, blockZ);
            final var blockIndex = localIndex;
            final var blockCoords = new Vector3i(blockX, blockY, blockZ);
            final var chunkIndex = ChunkUtils.Coords.Index.get(blockCoords);

            final var test = new TestUtil(commandBuffer, blockCoords);

            // functions to test
            final ArrayList<Integer> refs = new ArrayList<>();

            // WorldProvider
            refs.add(BlockUtils.Id.get(test.worldProvider, blockCoords));
            refs.add(BlockUtils.Id.get(test.worldProvider, blockCoords.x, blockCoords.y, blockCoords.z));
            refs.add(BlockUtils.Id.get(test.worldProvider, chunkIndex, blockIndex));

            //  World
            refs.add(BlockUtils.Id.get(test.world, blockCoords));
            refs.add(BlockUtils.Id.get(test.world, blockCoords.x, blockCoords.y, blockCoords.z));
            refs.add(BlockUtils.Id.get(test.world, chunkIndex, blockIndex));

            //  CommandBuffer
            refs.add(BlockUtils.Id.get(test.commandBuffer, blockCoords));
            refs.add(BlockUtils.Id.get(test.commandBuffer, blockCoords.x, blockCoords.y, blockCoords.z)); // test 7 failed
            refs.add(BlockUtils.Id.get(test.commandBuffer, chunkIndex, blockIndex));

            //  Store<ChunkStore>
            refs.add(BlockUtils.Id.get(test.store, blockCoords));
            refs.add(BlockUtils.Id.get(test.store, blockCoords.x, blockCoords.y, blockCoords.z)); // test 10 failed
            refs.add(BlockUtils.Id.get(test.store, chunkIndex, blockIndex));

            //  ChunkStore
            refs.add(BlockUtils.Id.get(test.chunkStore, blockCoords));
            refs.add(BlockUtils.Id.get(test.chunkStore, blockCoords.x, blockCoords.y, blockCoords.z)); // test 13 failed
            refs.add(BlockUtils.Id.get(test.chunkStore, chunkIndex, blockIndex));

            //  Ref<ChunkStore>
            refs.add(BlockUtils.Id.get(test.chunkRef, blockCoords));
            refs.add(BlockUtils.Id.get(test.chunkRef, blockCoords.x, blockCoords.y, blockCoords.z)); // test 16 failed
            refs.add(BlockUtils.Id.get(test.chunkRef, chunkIndex, blockIndex));
            refs.add(BlockUtils.Id.get(blockId));

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
        public static final Integer get(final WorldProvider worldProvider, final Vector3i blockCoords) {
            final var worldChunk = worldProvider.getWorld().getChunk(ChunkUtils.Coords.Index.get(blockCoords));
            if (worldChunk == null) {
                return null;
            }

            return worldChunk.getBlock(blockCoords);
        }

        @Nullable
        public static final Integer get(
            final WorldProvider worldProvider,
            final int blockX,
            final int blockY,
            final int blockZ
        ) {
            final var worldChunk = worldProvider.getWorld().getChunk(ChunkUtils.Coords.Index.get(blockX, blockZ));
            if (worldChunk == null) {
                return null;
            }

            return worldChunk.getBlock(blockX, blockY, blockZ);
        }

        @Nullable
        public static final Integer get(
            final WorldProvider worldProvider,
            final long chunkIndex,
            final int blockIndex
        ) {
            final var worldChunk = worldProvider.getWorld().getChunk(chunkIndex);
            if (worldChunk == null) {
                return null;
            }

            return worldChunk.getBlock(Coords.Local.get(blockIndex));
        }

        // #endregion WorldProvider
        // #region World
        // ====================================================================
        // World  =>  ChunkStore
        // + Global coords
        // ====================================================================

        @Nullable
        public static final Integer get(final World world, final Vector3i blockCoords) {
            final var worldChunk = world.getChunk(ChunkUtils.Coords.Index.get(blockCoords));
            if (worldChunk == null) {
                return null;
            }

            return worldChunk.getBlock(blockCoords);
        }

        @Nullable
        public static final Integer get(final World world, final int blockX, final int blockY, final int blockZ) {
            final var worldChunk = world.getChunk(ChunkUtils.Coords.Index.get(blockX, blockZ));
            if (worldChunk == null) {
                return null;
            }

            return worldChunk.getBlock(blockX, blockY, blockZ);
        }

        @Nullable
        public static final Integer get(final World world, final long chunkIndex, final int blockIndex) {
            final var worldChunk = world.getChunk(chunkIndex);
            if (worldChunk == null) {
                return null;
            }

            return worldChunk.getBlock(Coords.Local.get(blockIndex));
        }

        // #endregion World
        // #region CommandBuffer
        // ====================================================================
        // CommandBuffer  =>  ChunkStore
        // + Global coords
        // ====================================================================

        @Nullable
        public static final Integer get(final CommandBuffer<ChunkStore> commandBuffer, final Vector3i blockCoords) {
            final var worldChunk = ChunkUtils.WorldChunks.get(commandBuffer, blockCoords);
            if (worldChunk == null) {
                return null;
            }

            return worldChunk.getBlock(blockCoords);
        }

        @Nullable
        public static final Integer get(
            final CommandBuffer<ChunkStore> commandBuffer,
            final int blockX,
            final int blockY,
            final int blockZ
        ) {
            final var worldChunk = ChunkUtils.WorldChunks.get(commandBuffer, blockX, blockZ);
            if (worldChunk == null) {
                return null;
            }

            return worldChunk.getBlock(blockX, blockY, blockZ);
        }

        @Nullable
        public static final Integer get(
            final CommandBuffer<ChunkStore> commandBuffer,
            final long chunkIndex,
            final int blockIndex
        ) {
            final var worldChunk = ChunkUtils.WorldChunks.get(commandBuffer, chunkIndex);
            if (worldChunk == null) {
                return null;
            }

            return worldChunk.getBlock(Coords.Local.get(blockIndex));
        }

        // #endregion CommandBuffer
        // #region Store<ChunkStore>
        // ====================================================================
        // Store<ChunkStore>  =>  ChunkStore
        // + Global coords
        // ====================================================================

        @Nullable
        public static final Integer get(final Store<ChunkStore> store, final Vector3i blockCoords) {
            final var worldChunk = ChunkUtils.WorldChunks.get(store, blockCoords);
            if (worldChunk == null) {
                return null;
            }

            return worldChunk.getBlock(blockCoords);
        }

        @Nullable
        public static final Integer get(
            final Store<ChunkStore> store,
            final int blockX,
            final int blockY,
            final int blockZ
        ) {
            final var worldChunk = ChunkUtils.WorldChunks.get(store, blockX, blockZ);
            if (worldChunk == null) {
                return null;
            }

            return worldChunk.getBlock(blockX, blockY, blockZ);
        }

        @Nullable
        public static final Integer get(final Store<ChunkStore> store, final long chunkIndex, final int blockIndex) {
            final var worldChunk = ChunkUtils.WorldChunks.get(store, chunkIndex);
            if (worldChunk == null) {
                return null;
            }

            return worldChunk.getBlock(Coords.Local.get(blockIndex));
        }

        // #endregion Store<ChunkStore>
        // #region ChunkStore
        // ====================================================================
        // ChunkStore  =>  ChunkRef
        // + Global coords OR Chunk Coords
        // ====================================================================

        @Nullable
        public static final Integer get(final ChunkStore chunkStore, final Vector3i blockCoords) {
            final var worldChunk = ChunkUtils.WorldChunks.get(chunkStore, blockCoords);
            if (worldChunk == null) {
                return null;
            }

            return worldChunk.getBlock(blockCoords);
        }

        @Nullable
        public static final Integer get(
            final ChunkStore chunkStore,
            final int blockX,
            final int blockY,
            final int blockZ
        ) {
            final var worldChunk = ChunkUtils.WorldChunks.get(chunkStore, blockX, blockZ);
            if (worldChunk == null) {
                return null;
            }

            return worldChunk.getBlock(blockX, blockY, blockZ);
        }

        @Nullable
        public static final Integer get(final ChunkStore chunkStore, final long chunkIndex, final int blockIndex) {
            final var worldChunk = ChunkUtils.WorldChunks.get(chunkStore, chunkIndex);
            if (worldChunk == null) {
                return null;
            }

            return worldChunk.getBlock(Coords.Local.get(blockIndex));
        }

        // #endregion ChunkStore
        // #region Ref<ChunkStore>
        // ====================================================================
        // Ref<ChunkStore> (ChunkRef)  =>  BlockComponentChunk
        // + Global OR Local coords
        // ====================================================================

        @Nullable
        public static final Integer get(final Ref<ChunkStore> chunkRef, final Vector3i blockCoords) {
            final var worldChunk = ChunkUtils.WorldChunks.get(chunkRef, blockCoords);
            if (worldChunk == null) {
                return null;
            }

            return worldChunk.getBlock(blockCoords);
        }

        @Nullable
        public static final Integer get(
            final Ref<ChunkStore> chunkRef,
            final int blockX,
            final int blockY,
            final int blockZ
        ) {
            final var worldChunk = ChunkUtils.WorldChunks.get(chunkRef, blockX, blockZ);
            if (worldChunk == null) {
                return null;
            }

            return worldChunk.getBlock(blockX, blockY, blockZ);
        }

        @Nullable
        public static final Integer get(final Ref<ChunkStore> chunkRef, final long chunkIndex, final int blockIndex) {
            final var worldChunk = ChunkUtils.WorldChunks.get(chunkRef, chunkIndex);
            if (worldChunk == null) {
                return null;
            }

            return worldChunk.getBlock(Coords.Local.get(blockIndex));
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
        public static final int get(final String blockId) {
            return BlockType.getAssetMap().getIndex(blockId);
        }
    }

    // Not even sure what BlockType is or what i was using it for
    public static final class Type {

        /**
         * Tests all methods i've defined for getType
         */
        public static final List<BlockType> test() {
            final ArrayList<BlockType> refs = new ArrayList<>();

            refs.add(BlockUtils.Type.get("TEST_BlockType"));

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
        @Nullable
        public static final BlockType get(final String blockId) {
            return BlockType.getAssetMap().getAsset(blockId);
        }
    }

    public static final boolean set(final World world, final Vector3i blockCoords, final int blockId) {
        var chunk = ChunkUtils.WorldChunks.get(world, blockCoords);
        if (chunk == null) {
            return false;
        }

        return set0(chunk, blockCoords, blockId);
    }

    public static final boolean set0(final WorldChunk chunk, final Vector3i blockCoords, final int blockId) {
        return chunk.setBlock(blockCoords.x, blockCoords.y, blockCoords.z, blockId);
    }

    public static final int get(final World world, final Vector3i blockCoords) {
        var chunk = ChunkUtils.WorldChunks.get(world, blockCoords);
        if (chunk == null) {
            return -1;
        }

        return get0(chunk, blockCoords);
    }

    public static final int get0(final WorldChunk chunk, final Vector3i blockCoords) {
        return chunk.getBlock(blockCoords);
    }
}
