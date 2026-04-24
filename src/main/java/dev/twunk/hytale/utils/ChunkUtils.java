package dev.twunk.hytale.utils;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector2i;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.modules.block.BlockModule.BlockStateInfo;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.WorldProvider;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import dev.twunk.hytale.LibHytaleException;
import dev.twunk.hytale.types.ChunkCoordinates;
import dev.twunk.lib.test.TestUtil;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class ChunkUtils {

    // ==================================================
    // Component types (trust me bro, i swear they're not null)
    // ==================================================

    @SuppressWarnings("null")
    private static final ComponentType<ChunkStore, WorldChunk> WORLD_CHUNK_COMPONENT = WorldChunk.getComponentType();

    public static final class Coords {

        /// -> get CHUNK coordinates                 (long | Vector2i | ChunkCoordinates)
        public static final class Global {

            public static final void test(final Vector3i providedCoords) {
                final var blockX = providedCoords.x;
                final var blockY = providedCoords.y;
                final var blockZ = providedCoords.z;
                final var blockCoords = new Vector3i(blockX, blockY, blockZ);

                @Nonnull
                var coords = ChunkUtils.Coords.Global.get(blockX, blockZ);
                final var mycoords = coords;
                coords = ChunkUtils.Coords.Global.get(blockCoords);
                if (!coords.equals(mycoords)) {
                    throw new LibHytaleException("Failed to convert blockCoords to chunkCoords");
                }

                @Nonnull
                var coords2 = ChunkUtils.Coords.Global.getVector2i(blockX, blockZ);
                final var myVec2 = new Vector2i(mycoords.x, mycoords.z);
                if (!coords2.equals(myVec2)) {
                    throw new LibHytaleException("Failed to convert blockX,blockZ to chunkCoords (vector2i)");
                }

                coords2 = ChunkUtils.Coords.Global.getVector2i(blockCoords);
                if (!coords2.equals(myVec2)) {
                    throw new LibHytaleException("Failed to convert blockCoords to chunkCoords (vector2i)");
                }
            }

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
            public static final ChunkCoordinates get(final int blockX, final int blockZ) {
                // see ChunkUtil.chunkCoordinate
                return new ChunkCoordinates(blockX >> 5, blockZ >> 5);
            }

            // With GLOBAL block coords
            public static final ChunkCoordinates get(final Vector3i blockCoords) {
                // see ChunkUtil.chunkCoordinate
                return new ChunkCoordinates(blockCoords.x >> 5, blockCoords.z >> 5);
            }

            public ChunkCoordinates get(long chunkIndex) {
                final int chunkX = (int) (chunkIndex >> 32);
                final int chunkZ = (int) (chunkIndex);

                return new ChunkCoordinates(chunkX, chunkZ);
            }

            /**
             * With GLOBAL block coords
             *
             * returned "y" in vector is actually the chunk's "z" coordinate
             */
            public static final Vector2i getVector2i(final int blockX, final int blockZ) {
                // see ChunkUtil.chunkCoordinate
                return new Vector2i(blockX >> 5, blockZ >> 5);
            }

            /**
             * With GLOBAL block coords
             *
             * returned "y" in vector is actually the chunk's "z" coordinate
             */
            public static final Vector2i getVector2i(final Vector3i blockCoords) {
                // see ChunkUtil.chunkCoordinate
                return new Vector2i(blockCoords.x >> 5, blockCoords.z >> 5);
            }

            public Vector2i getVector2i(long chunkIndex) {
                final int chunkX = (int) (chunkIndex >> 32);
                final int chunkZ = (int) (chunkIndex);

                return new Vector2i(chunkX, chunkZ);
            }

            // #endregion getChunkCoords
        }

        // ====================================================================
        // Chunk Index (index is interchangable with coordinates) FROM block coordinates
        // ====================================================================

        public static final class Index {

            public static final void test(
                final Ref<ChunkStore> blockRef,
                final WorldChunk worldChunk,
                final CommandBuffer<ChunkStore> commandBuffer,
                final Vector3i providedCoords
            ) {
                final var blockX = providedCoords.x;
                final var blockY = providedCoords.y;
                final var blockZ = providedCoords.z;
                final var blockCoords = new Vector3i(blockX, blockY, blockZ);
                final var test = new TestUtil(commandBuffer, blockCoords);
                final var chunkCoords = ChunkUtils.Coords.Global.get(blockX, blockZ);
                final var chunkIndex = ChunkUtils.Coords.Index.get(blockRef); // shush, i know....
                if (chunkIndex == null) {
                    throw new LibHytaleException("Failed to convert blockRef to chunkIndex");
                }

                // Long get(Ref<ChunkStore> anyRef)   @Nullable
                Long nullableTestIndex = ChunkUtils.Coords.Index.get(test.chunkRef);
                if (nullableTestIndex == null || !nullableTestIndex.equals(chunkIndex)) {
                    throw new LibHytaleException("Failed to convert chunkRef (anyref) to chunkIndex");
                }
                nullableTestIndex = ChunkUtils.Coords.Index.get(blockRef);
                if (nullableTestIndex == null || !nullableTestIndex.equals(chunkIndex)) {
                    throw new LibHytaleException("Failed to convert blockRef (anyref) to chunkIndex");
                }
                // Long get(BlockStateInfo info)
                nullableTestIndex = ChunkUtils.Coords.Index.get(test.info);
                if (nullableTestIndex == null || !nullableTestIndex.equals(chunkIndex)) {
                    throw new LibHytaleException("Failed to convert info to chunkIndex");
                }

                // Long get_chunkRef(Ref<ChunkStore> chunkRef)
                nullableTestIndex = ChunkUtils.Coords.Index.get_chunkRef(test.chunkRef);
                if (nullableTestIndex == null || !nullableTestIndex.equals(chunkIndex)) {
                    throw new LibHytaleException("Failed to convert chunkRef (specific method) to chunkIndex");
                }

                // Long get_blockRef(Ref<ChunkStore> blockRef)
                nullableTestIndex = ChunkUtils.Coords.Index.get_blockRef(blockRef);
                if (nullableTestIndex == null || !nullableTestIndex.equals(chunkIndex)) {
                    throw new LibHytaleException("Failed to convert blockRef (specific method) to chunkIndex");
                }

                // long get(WorldChunk worldChunk)
                nullableTestIndex = ChunkUtils.Coords.Index.get(worldChunk);
                if (nullableTestIndex == null || !nullableTestIndex.equals(chunkIndex)) {
                    throw new LibHytaleException("Failed to convert worldChunk to chunkIndex");
                }

                // long get(int blockX, int blockZ)
                long testIndex = ChunkUtils.Coords.Index.get(blockX, blockZ);
                if (testIndex != chunkIndex) {
                    throw new LibHytaleException("Failed to convert blockX, blockZ to chunkIndex");
                }

                // long get(Vector3i blockCoords)
                testIndex = ChunkUtils.Coords.Index.get(blockCoords);
                if (testIndex != chunkIndex) {
                    throw new LibHytaleException("Failed to convert blockCoords to chunkIndex");
                }

                // long get_chunkCoords(int chunkX, int chunkZ)
                testIndex = ChunkUtils.Coords.Index.get_chunkCoords(chunkCoords.x, chunkCoords.z);
                if (testIndex != chunkIndex) {
                    throw new LibHytaleException("Failed to convert chunkCoords.x, chunkCoords.z to chunkIndex");
                }

                // long get_chunkCoords(ChunkCoordinates coords)
                testIndex = ChunkUtils.Coords.Index.get_chunkCoords(chunkCoords);
                if (testIndex != chunkIndex) {
                    throw new LibHytaleException("Failed to convert chunkCoords to chunkIndex");
                }

                // long get_chunkCoords(Vector2i chunkCoords)
                testIndex = ChunkUtils.Coords.Index.get_chunkCoords(new Vector2i(chunkCoords.x, chunkCoords.z));
                if (testIndex != chunkIndex) {
                    throw new LibHytaleException("Failed to convert chunkCoords (vector2i) to chunkIndex");
                }
            }

            // #region getChunkIndex

            @Nullable
            public static final Long get(final Ref<ChunkStore> anyRef) {
                final var worldChunk = ChunkUtils.WorldChunks.get(anyRef);
                if (worldChunk == null) {
                    return null;
                }

                return worldChunk.getIndex();
            }

            @Nullable
            public static final Long get(final BlockStateInfo info) {
                return ChunkUtils.Coords.Index.get_chunkRef(info.getChunkRef());
            }

            @Nullable
            public static final Long get_chunkRef(final Ref<ChunkStore> chunkRef) {
                final var worldChunk = ChunkUtils.WorldChunks.get_chunkRef(chunkRef);
                if (worldChunk == null) {
                    return null;
                }

                return worldChunk.getIndex();
            }

            @Nullable
            public static final Long get_blockRef(final Ref<ChunkStore> blockRef) {
                final var worldChunk = ChunkUtils.WorldChunks.get_blockRef(blockRef);
                if (worldChunk == null) {
                    return null;
                }

                return worldChunk.getIndex();
            }

            // TODO worldChunk.getIndex()  and      Chunk.Coords.Index.get_chunkCoords(worldChunk.getX(), worldChunk.getZ()) do the same thing
            public static final long get(final WorldChunk worldChunk) {
                return worldChunk.getIndex();
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
                return ((long) (blockX >> 5) << 32) | ((blockZ >> 5) & 4294967295L);
            }

            public static final long get(final Vector3i blockCoords) {
                // see ChunkUtil.indexChunkFromBlock
                return ((long) (blockCoords.x >> 5) << 32) | ((blockCoords.z >> 5) & 4294967295L);
            }

            public static final long get_chunkCoords(final int chunkX, final int chunkZ) {
                // see ChunkUtil.indexChunkFromBlock
                return ((long) chunkX << 32) | (chunkZ & 4294967295L);
            }

            public static final long get_chunkCoords(final ChunkCoordinates coords) {
                // see ChunkUtil.indexChunkFromBlock
                return ((long) coords.x << 32) | (coords.z & 4294967295L);
            }

            /**
             * WARNING: the `y` value MUST be the `z` value for the chunk
             */
            public static final long get_chunkCoords(final Vector2i chunkCoords) {
                // see ChunkUtil.indexChunkFromBlock
                return ((long) chunkCoords.x << 32) | (chunkCoords.y & 4294967295L);
            }
            // #endregion getChunkIndex
        }
    }

    /// -> get WorldChunk
    public static final class WorldChunks {

        /**
         * Tests all methods i've defined for getWorldChunk
         */
        public static final List<WorldChunk> test(
            final Ref<ChunkStore> blockRef,
            final CommandBuffer<ChunkStore> commandBuffer,
            final Vector3i providedCoords
        ) {
            final var blockX = providedCoords.x;
            final var blockY = providedCoords.y;
            final var blockZ = providedCoords.z;
            final var blockCoords = new Vector3i(blockX, blockY, blockZ);
            final var chunkIndex = ChunkUtils.Coords.Index.get(blockCoords);
            final var chunkCoords = ChunkUtils.Coords.Global.get(blockCoords);
            final var chunkX = chunkCoords.x;
            final var chunkZ = chunkCoords.z;

            final var test = new TestUtil(commandBuffer, blockCoords);
            // functions to test
            final ArrayList<WorldChunk> refs = new ArrayList<>();

            // getWorldChunk of a given item directly (super easy minimal version)
            refs.add(ChunkUtils.WorldChunks.get_chunkRef(test.chunkRef));
            refs.add(ChunkUtils.WorldChunks.get_blockRef(test.blockRef));
            // - with: block ref (auto)
            refs.add(ChunkUtils.WorldChunks.get(blockRef));
            // - with: chunk ref (auto)
            refs.add(ChunkUtils.WorldChunks.get(test.chunkRef));
            refs.add(ChunkUtils.WorldChunks.get(test.info));

            // WorldProvider
            refs.add(ChunkUtils.WorldChunks.get(test.worldProvider, test.blockChunk));
            refs.add(ChunkUtils.WorldChunks.get(test.worldProvider, blockX, blockZ));
            refs.add(ChunkUtils.WorldChunks.get(test.worldProvider, chunkIndex));
            refs.add(ChunkUtils.WorldChunks.get(test.worldProvider, blockCoords));
            refs.add(ChunkUtils.WorldChunks.get_chunkCoords(test.worldProvider, chunkX, chunkZ));

            // World
            refs.add(ChunkUtils.WorldChunks.get(test.world, test.blockChunk));
            refs.add(ChunkUtils.WorldChunks.get(test.world, blockX, blockZ));
            refs.add(ChunkUtils.WorldChunks.get(test.world, chunkIndex));
            refs.add(ChunkUtils.WorldChunks.get(test.world, blockCoords));
            refs.add(ChunkUtils.WorldChunks.get_chunkCoords(test.world, chunkX, chunkZ));

            // BlockRef
            refs.add(ChunkUtils.WorldChunks.get(blockRef, test.blockChunk));
            refs.add(ChunkUtils.WorldChunks.get(blockRef, blockX, blockZ));
            refs.add(ChunkUtils.WorldChunks.get(blockRef, chunkIndex));
            refs.add(ChunkUtils.WorldChunks.get(blockRef, blockCoords));
            refs.add(ChunkUtils.WorldChunks.get_chunkCoords(blockRef, chunkX, chunkZ));

            // Info
            refs.add(ChunkUtils.WorldChunks.get(test.info, test.blockChunk));
            refs.add(ChunkUtils.WorldChunks.get(test.info, blockX, blockZ));
            refs.add(ChunkUtils.WorldChunks.get(test.info, chunkIndex));
            refs.add(ChunkUtils.WorldChunks.get(test.info, blockCoords));
            refs.add(ChunkUtils.WorldChunks.get_chunkCoords(test.info, chunkX, chunkZ));

            // CommandBuffer
            refs.add(ChunkUtils.WorldChunks.get(test.commandBuffer, test.blockChunk));
            refs.add(ChunkUtils.WorldChunks.get(test.commandBuffer, blockX, blockZ));
            refs.add(ChunkUtils.WorldChunks.get(test.commandBuffer, chunkIndex));
            refs.add(ChunkUtils.WorldChunks.get(test.commandBuffer, blockCoords));
            refs.add(ChunkUtils.WorldChunks.get_chunkCoords(test.commandBuffer, chunkX, chunkZ));

            // Store
            refs.add(ChunkUtils.WorldChunks.get(test.store, test.blockChunk));
            refs.add(ChunkUtils.WorldChunks.get(test.store, blockX, blockZ));
            refs.add(ChunkUtils.WorldChunks.get(test.store, chunkIndex));
            refs.add(ChunkUtils.WorldChunks.get(test.store, blockCoords));
            refs.add(ChunkUtils.WorldChunks.get_chunkCoords(test.store, chunkX, chunkZ));

            // ChunKStore
            refs.add(ChunkUtils.WorldChunks.get(test.chunkStore, test.blockChunk));
            refs.add(ChunkUtils.WorldChunks.get(test.chunkStore, blockX, blockZ));
            refs.add(ChunkUtils.WorldChunks.get(test.chunkStore, chunkIndex));
            refs.add(ChunkUtils.WorldChunks.get(test.chunkStore, blockCoords));
            refs.add(ChunkUtils.WorldChunks.get_chunkCoords(test.chunkStore, chunkX, chunkZ));

            // final test -> does it matter really if we pass a chunk ref or a block ref?
            // update: NO HECKING WAY, it doesn't. wild.
            refs.add(ChunkUtils.WorldChunks.get(blockRef, chunkIndex));
            refs.add(ChunkUtils.WorldChunks.get(test.chunkRef, chunkIndex));

            return refs;
        }

        // #region getWorldChunk

        @Nullable
        public static final WorldChunk get_chunkRef(final Ref<ChunkStore> chunkRef) {
            return ComponentUtils.get(chunkRef, WORLD_CHUNK_COMPONENT);
        }

        @Nullable
        public static final WorldChunk get_blockRef(final Ref<ChunkStore> blockRef) {
            final var info = BlockUtils.Info.get(blockRef);
            if (info == null) {
                return null;
            }

            return ComponentUtils.get(info.getChunkRef(), WORLD_CHUNK_COMPONENT);
        }

        @Nullable
        public static final WorldChunk get(final Ref<ChunkStore> anyRef) {
            // Potential 1: The ref you passed me is a CHUNK ref. slay. thats the good shit. that's what we're after
            final var worldChunk = ComponentUtils.get(anyRef, WORLD_CHUNK_COMPONENT);
            if (worldChunk != null) {
                return worldChunk;
            }

            // Potential 2: it's a block, otherwise i've got no clue what's going on
            return get_blockRef(anyRef);
        }

        @Nullable
        public static final WorldChunk get(final BlockStateInfo info) {
            return ComponentUtils.get(info.getChunkRef(), WORLD_CHUNK_COMPONENT);
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
        public static final WorldChunk get(final WorldProvider worldProvider, final BlockChunk blockChunk) {
            return ChunkUtils.WorldChunks.get(worldProvider, blockChunk.getIndex());
        }

        @Nullable
        public static final WorldChunk get(final WorldProvider worldProvider, final int blockX, final int blockZ) {
            return ChunkUtils.WorldChunks.get(worldProvider, ChunkUtils.Coords.Index.get(blockX, blockZ));
        }

        @Nullable
        public static final WorldChunk get(final WorldProvider worldProvider, final Vector3i blockCoords) {
            return ChunkUtils.WorldChunks.get(worldProvider, ChunkUtils.Coords.Index.get(blockCoords));
        }

        @Nullable
        public static final WorldChunk get_chunkCoords(
            final WorldProvider worldProvider,
            final int chunkX,
            final int chunkZ
        ) {
            return ChunkUtils.WorldChunks.get(worldProvider, ChunkUtils.Coords.Index.get_chunkCoords(chunkX, chunkZ));
        }

        @Nullable
        public static final WorldChunk get(final WorldProvider worldProvider, final long chunkIndex) {
            return worldProvider.getWorld().getChunk(chunkIndex);
        }

        //   #endregion WorldProvider
        //   #region World
        //   ==================================================================
        //   World                -> WorldChunk
        //   ==================================================================

        @Nullable
        public static final WorldChunk get(final World world, final BlockChunk blockChunk) {
            return ChunkUtils.WorldChunks.get(world, blockChunk.getIndex());
        }

        @Nullable
        public static final WorldChunk get(final World world, final int blockX, final int blockZ) {
            return ChunkUtils.WorldChunks.get(world, ChunkUtils.Coords.Index.get(blockX, blockZ));
        }

        @Nullable
        public static final WorldChunk get(final World world, final Vector3i blockCoords) {
            return ChunkUtils.WorldChunks.get(world, ChunkUtils.Coords.Index.get(blockCoords));
        }

        @Nullable
        public static final WorldChunk get_chunkCoords(final World world, final int chunkX, final int chunkZ) {
            return ChunkUtils.WorldChunks.get(world, ChunkUtils.Coords.Index.get_chunkCoords(chunkX, chunkZ));
        }

        @Nullable
        public static final WorldChunk get(final World world, final long chunkIndex) {
            return world.getChunk(chunkIndex);
        }

        //   #endregion World
        //   #region Ref<ChunkStore>
        //   ==================================================================
        //   Ref<ChunkStore>      -> WorldChunk
        //   ==================================================================

        @Nullable
        public static final WorldChunk get(final Ref<ChunkStore> anyRef, final BlockChunk blockChunk) {
            return ChunkUtils.WorldChunks.get(anyRef, blockChunk.getIndex());
        }

        @Nullable
        public static final WorldChunk get(final Ref<ChunkStore> anyRef, final int blockX, final int blockZ) {
            return ChunkUtils.WorldChunks.get(anyRef, ChunkUtils.Coords.Index.get(blockX, blockZ));
        }

        @Nullable
        public static final WorldChunk get(final Ref<ChunkStore> anyRef, final Vector3i blockCoords) {
            return ChunkUtils.WorldChunks.get(anyRef, ChunkUtils.Coords.Index.get(blockCoords));
        }

        @Nullable
        public static final WorldChunk get_chunkCoords(
            final Ref<ChunkStore> anyRef,
            final int chunkX,
            final int chunkZ
        ) {
            return ChunkUtils.WorldChunks.get(anyRef, ChunkUtils.Coords.Index.get_chunkCoords(chunkX, chunkZ));
        }

        @Nullable
        public static final WorldChunk get(final Ref<ChunkStore> anyRef, final long chunkIndex) {
            return anyRef.getStore().getExternalData().getChunkComponent(chunkIndex, WORLD_CHUNK_COMPONENT);
        }

        //   #endregion WorldChunk
        //   #region BlockStateInfo
        //   ==================================================================
        //   BlockStateInfo       -> WorldChunk
        //   ==================================================================

        @Nullable
        public static final WorldChunk get(final BlockStateInfo info, final BlockChunk blockChunk) {
            return ChunkUtils.WorldChunks.get(info, blockChunk.getIndex());
        }

        /**
         * does NOT get the chunk that the info is in, it USES the info to get
         * the world.
         *
         * Then uses the world to get the WorldChunk at the coords provided
         */
        @Nullable
        public static final WorldChunk get(final BlockStateInfo info, final int blockX, final int blockZ) {
            return ChunkUtils.WorldChunks.get(info, ChunkUtils.Coords.Index.get(blockX, blockZ));
        }

        @Nullable
        public static final WorldChunk get(final BlockStateInfo info, final Vector3i blockCoords) {
            return ChunkUtils.WorldChunks.get(info, ChunkUtils.Coords.Index.get(blockCoords));
        }

        @Nullable
        public static final WorldChunk get_chunkCoords(final BlockStateInfo info, final int chunkX, final int chunkZ) {
            return ChunkUtils.WorldChunks.get(info, ChunkUtils.Coords.Index.get_chunkCoords(chunkX, chunkZ));
        }

        /**
         * does NOT get the chunk that the info is in, it USES the info to get
         * the world.
         *
         * Then uses the world to get the WorldChunk at the coords provided
         */
        @Nullable
        public static final WorldChunk get(final BlockStateInfo info, final long chunkIndex) {
            return info.getChunkRef().getStore().getExternalData().getChunkComponent(chunkIndex, WORLD_CHUNK_COMPONENT);
        }

        //   #endregion BlockStateInfo
        //   #region CommandBuffer
        //   ==================================================================
        //   CommandBuffer        -> WorldChunk
        //   ==================================================================

        @Nullable
        public static final WorldChunk get(
            final CommandBuffer<ChunkStore> commandBuffer, // << Doesn't need to get the world, it can access components directly
            final BlockChunk blockChunk
        ) {
            // but regardless we don't need the world to get the world chunk
            return ChunkUtils.WorldChunks.get(commandBuffer, blockChunk.getIndex());
        }

        @Nullable
        public static final WorldChunk get(
            final CommandBuffer<ChunkStore> commandBuffer, // << Doesn't need to get the world, it can access components directly
            final int blockX,
            final int blockZ
        ) {
            // but regardless we don't need the world to get the world chunk
            return ChunkUtils.WorldChunks.get(commandBuffer, ChunkUtils.Coords.Index.get(blockX, blockZ));
        }

        @Nullable
        public static final WorldChunk get(final CommandBuffer<ChunkStore> commandBuffer, final Vector3i blockCoords) {
            return ChunkUtils.WorldChunks.get(commandBuffer, ChunkUtils.Coords.Index.get(blockCoords));
        }

        @Nullable
        public static final WorldChunk get_chunkCoords(
            final CommandBuffer<ChunkStore> commandBuffer,
            final int chunkX,
            final int chunkZ
        ) {
            return ChunkUtils.WorldChunks.get(commandBuffer, ChunkUtils.Coords.Index.get_chunkCoords(chunkX, chunkZ));
        }

        @Nullable
        public static final WorldChunk get(
            final CommandBuffer<ChunkStore> commandBuffer, // << Doesn't need to get the world, it can access components directly
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
        public static final WorldChunk get(final Store<ChunkStore> store, final BlockChunk blockChunk) {
            return ChunkUtils.WorldChunks.get(store, blockChunk.getIndex());
        }

        @Nullable
        public static final WorldChunk get(final Store<ChunkStore> store, final int blockX, final int blockZ) {
            return ChunkUtils.WorldChunks.get(store, ChunkUtils.Coords.Index.get(blockX, blockZ));
        }

        @Nullable
        public static final WorldChunk get(final Store<ChunkStore> store, final Vector3i blockCoords) {
            return ChunkUtils.WorldChunks.get(store, ChunkUtils.Coords.Index.get(blockCoords));
        }

        @Nullable
        public static final WorldChunk get_chunkCoords(
            final Store<ChunkStore> store,
            final int chunkX,
            final int chunkZ
        ) {
            return ChunkUtils.WorldChunks.get(store, ChunkUtils.Coords.Index.get_chunkCoords(chunkX, chunkZ));
        }

        @Nullable
        public static final WorldChunk get(final Store<ChunkStore> store, final long chunkIndex) {
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
        public static final WorldChunk get(final ChunkStore chunkStore, final BlockChunk blockChunk) {
            // ALT version: `chunkStore.getWorld().getChunk(blockChunk.getIndex());`
            return ChunkUtils.WorldChunks.get(chunkStore, blockChunk.getIndex());
        }

        @Nullable
        public static final WorldChunk get(final ChunkStore chunkStore, final int blockX, final int blockZ) {
            // ALT version: `chunkStore.getWorld().getChunk(blockChunk.getIndex());`
            return ChunkUtils.WorldChunks.get(chunkStore, ChunkUtils.Coords.Index.get(blockX, blockZ));
        }

        @Nullable
        public static final WorldChunk get(final ChunkStore chunkStore, final Vector3i blockCoords) {
            return ChunkUtils.WorldChunks.get(chunkStore, ChunkUtils.Coords.Index.get(blockCoords));
        }

        @Nullable
        public static final WorldChunk get_chunkCoords(
            final ChunkStore chunkStore,
            final int chunkX,
            final int chunkZ
        ) {
            return ChunkUtils.WorldChunks.get(chunkStore, ChunkUtils.Coords.Index.get_chunkCoords(chunkX, chunkZ));
        }

        @Nullable
        public static final WorldChunk get(final ChunkStore chunkStore, final long chunkIndex) {
            // ALT version: `chunkStore.getWorld().getChunk(blockChunk.getIndex());`
            return chunkStore.getChunkComponent(chunkIndex, WORLD_CHUNK_COMPONENT);
        }

        //   #endregion ChunkStore
        // #endregion getWorldChunk
    }

    /// -> get Ref<ChunkStore>  (ChunkRef)
    public static final class Refs {

        /**
         * Tests all methods i've defined for getWorldChunk
         */
        public static final List<Ref<ChunkStore>> test(
            final CommandBuffer<ChunkStore> commandBuffer,
            final Vector3i providedCoords
        ) {
            final var blockX = providedCoords.x;
            final var blockY = providedCoords.y;
            final var blockZ = providedCoords.z;
            final var blockCoords = new Vector3i(blockX, blockY, blockZ);
            final var chunkIndex = ChunkUtils.Coords.Index.get(blockCoords);
            final var chunkCoords = ChunkUtils.Coords.Global.get(blockCoords);
            final var chunkX = chunkCoords.x;
            final var chunkZ = chunkCoords.z;

            final var test = new TestUtil(commandBuffer, blockCoords);
            // functions to test
            final ArrayList<Ref<ChunkStore>> refs = new ArrayList<>();

            // World
            refs.add(ChunkUtils.Refs.get(test.world, blockCoords));
            refs.add(ChunkUtils.Refs.get(test.world, blockX, blockZ));
            refs.add(ChunkUtils.Refs.get(test.world, chunkIndex));
            refs.add(ChunkUtils.Refs.get_chunkCoords(test.world, chunkX, chunkZ));

            refs.add(ChunkUtils.Refs.get(test.commandBuffer, blockCoords));
            refs.add(ChunkUtils.Refs.get(test.commandBuffer, blockX, blockZ));
            refs.add(ChunkUtils.Refs.get(test.commandBuffer, chunkIndex));
            refs.add(ChunkUtils.Refs.get_chunkCoords(test.commandBuffer, chunkX, chunkZ));

            refs.add(ChunkUtils.Refs.get(test.store, blockCoords));
            refs.add(ChunkUtils.Refs.get(test.store, blockX, blockZ));
            refs.add(ChunkUtils.Refs.get(test.store, chunkIndex));
            refs.add(ChunkUtils.Refs.get_chunkCoords(test.store, chunkX, chunkZ));

            refs.add(ChunkUtils.Refs.get(test.chunkStore, blockCoords));
            refs.add(ChunkUtils.Refs.get(test.chunkStore, blockX, blockZ));
            refs.add(ChunkUtils.Refs.get(test.chunkStore, chunkIndex));
            refs.add(ChunkUtils.Refs.get_chunkCoords(test.chunkStore, chunkX, chunkZ));

            refs.add(ChunkUtils.Refs.get(test.chunkRef, blockCoords));
            refs.add(ChunkUtils.Refs.get(test.chunkRef, blockX, blockZ));
            refs.add(ChunkUtils.Refs.get(test.chunkRef, chunkIndex));
            refs.add(ChunkUtils.Refs.get_chunkCoords(test.chunkRef, chunkX, chunkZ));

            refs.add(ChunkUtils.Refs.get(test.info, blockCoords));
            refs.add(ChunkUtils.Refs.get(test.info, blockX, blockZ));
            refs.add(ChunkUtils.Refs.get(test.info, chunkIndex));
            refs.add(ChunkUtils.Refs.get_chunkCoords(test.info, chunkX, chunkZ));

            refs.add(ChunkUtils.Refs.get(test.blockRef));
            refs.add(ChunkUtils.Refs.get(test.info));

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
        public static final Ref<ChunkStore> get(final World world, final Vector3i blockCoords) {
            return ChunkUtils.Refs.get(world, ChunkUtils.Coords.Index.get(blockCoords));
        }

        @Nullable
        public static final Ref<ChunkStore> get(final World world, final int blockX, final int blockZ) {
            return ChunkUtils.Refs.get(world, ChunkUtils.Coords.Index.get(blockX, blockZ));
        }

        @Nullable
        public static final Ref<ChunkStore> get_chunkCoords(final World world, final int chunkX, final int chunkZ) {
            return ChunkUtils.Refs.get(world, ChunkUtils.Coords.Index.get_chunkCoords(chunkX, chunkZ));
        }

        @Nullable
        public static final Ref<ChunkStore> get(final World world, final long chunkIndex) {
            return world.getChunkStore().getChunkReference(chunkIndex);
        }

        // #endregion World
        // #region CommandBuffer
        // ====================================================================
        // CommandBuffer
        // ====================================================================

        @Nullable
        public static final Ref<ChunkStore> get(
            final CommandBuffer<ChunkStore> commandBuffer,
            final Vector3i blockCoords
        ) {
            return ChunkUtils.Refs.get(commandBuffer, ChunkUtils.Coords.Index.get(blockCoords));
        }

        @Nullable
        public static final Ref<ChunkStore> get(
            final CommandBuffer<ChunkStore> commandBuffer,
            final int blockX,
            final int blockZ
        ) {
            return ChunkUtils.Refs.get(commandBuffer, ChunkUtils.Coords.Index.get(blockX, blockZ));
        }

        @Nullable
        public static final Ref<ChunkStore> get_chunkCoords(
            final CommandBuffer<ChunkStore> commandBuffer,
            final int chunkX,
            final int chunkZ
        ) {
            return ChunkUtils.Refs.get(commandBuffer, ChunkUtils.Coords.Index.get_chunkCoords(chunkX, chunkZ));
        }

        @Nullable
        public static final Ref<ChunkStore> get(final CommandBuffer<ChunkStore> commandBuffer, final long chunkIndex) {
            return commandBuffer.getExternalData().getChunkReference(chunkIndex);
        }

        // #endregion CommandBuffer
        // #region Store<ChunkStore>
        // ====================================================================
        // Store<ChunkStore>
        // ====================================================================

        @Nullable
        public static final Ref<ChunkStore> get(final Store<ChunkStore> store, final Vector3i blockCoords) {
            return ChunkUtils.Refs.get(store, ChunkUtils.Coords.Index.get(blockCoords));
        }

        @Nullable
        public static final Ref<ChunkStore> get(final Store<ChunkStore> store, final int blockX, final int blockZ) {
            return ChunkUtils.Refs.get(store, ChunkUtils.Coords.Index.get(blockX, blockZ));
        }

        @Nullable
        public static final Ref<ChunkStore> get_chunkCoords(
            final Store<ChunkStore> store,
            final int chunkX,
            final int chunkZ
        ) {
            return ChunkUtils.Refs.get(store, ChunkUtils.Coords.Index.get_chunkCoords(chunkX, chunkZ));
        }

        @Nullable
        public static final Ref<ChunkStore> get(final Store<ChunkStore> store, final long chunkIndex) {
            return store.getExternalData().getChunkReference(chunkIndex);
        }

        // #endregion Store<ChunkStore>
        // #region ChunkStore
        // ====================================================================
        // ChunkStore
        // ====================================================================

        @Nullable
        public static final Ref<ChunkStore> get(final ChunkStore chunkStore, final Vector3i blockCoords) {
            return ChunkUtils.Refs.get(chunkStore, ChunkUtils.Coords.Index.get(blockCoords));
        }

        @Nullable
        public static final Ref<ChunkStore> get(final ChunkStore chunkStore, final int blockX, final int blockZ) {
            return ChunkUtils.Refs.get(chunkStore, ChunkUtils.Coords.Index.get(blockX, blockZ));
        }

        @Nullable
        public static final Ref<ChunkStore> get_chunkCoords(
            final ChunkStore chunkStore,
            final int chunkX,
            final int chunkZ
        ) {
            return ChunkUtils.Refs.get(chunkStore, ChunkUtils.Coords.Index.get_chunkCoords(chunkX, chunkZ));
        }

        @Nullable
        public static final Ref<ChunkStore> get(final ChunkStore chunkStore, final long chunkIndex) {
            return chunkStore.getChunkReference(chunkIndex);
        }

        // #endregion ChunkStore
        // #region Ref<ChunkStore>

        @Nullable
        public static final Ref<ChunkStore> get(final Ref<ChunkStore> chunkRef, final Vector3i blockCoords) {
            return ChunkUtils.Refs.get(chunkRef, ChunkUtils.Coords.Index.get(blockCoords));
        }

        @Nullable
        public static final Ref<ChunkStore> get(final Ref<ChunkStore> chunkRef, final int blockX, final int blockZ) {
            return ChunkUtils.Refs.get(chunkRef, ChunkUtils.Coords.Index.get(blockX, blockZ));
        }

        @Nullable
        public static final Ref<ChunkStore> get_chunkCoords(
            final Ref<ChunkStore> chunkRef,
            final int chunkX,
            final int chunkZ
        ) {
            return ChunkUtils.Refs.get(chunkRef, ChunkUtils.Coords.Index.get_chunkCoords(chunkX, chunkZ));
        }

        @Nullable
        public static final Ref<ChunkStore> get(final Ref<ChunkStore> chunkRef, final long chunkIndex) {
            return chunkRef.getStore().getExternalData().getChunkReference(chunkIndex);
        }

        // #endregion Ref<ChunkStore>
        // #region BlockStateInfo
        // ====================================================================
        // BlockStateInfo
        // ====================================================================

        @Nullable
        public static final Ref<ChunkStore> get(final BlockStateInfo info, final Vector3i blockCoords) {
            return ChunkUtils.Refs.get(info, ChunkUtils.Coords.Index.get(blockCoords));
        }

        @Nullable
        public static final Ref<ChunkStore> get(final BlockStateInfo info, final int blockX, final int blockZ) {
            return ChunkUtils.Refs.get(info, ChunkUtils.Coords.Index.get(blockX, blockZ));
        }

        @Nullable
        public static final Ref<ChunkStore> get_chunkCoords(
            final BlockStateInfo info,
            final int chunkX,
            final int chunkZ
        ) {
            return ChunkUtils.Refs.get(info, ChunkUtils.Coords.Index.get_chunkCoords(chunkX, chunkZ));
        }

        @Nullable
        public static final Ref<ChunkStore> get(final BlockStateInfo info, final long chunkIndex) {
            return info.getChunkRef().getStore().getExternalData().getChunkReference(chunkIndex);
        }

        // #endregion BlockStateInfo
        // #region BlockRef
        // ====================================================================
        // BlockRef
        // ====================================================================

        @Nullable
        public static final Ref<ChunkStore> get(final Ref<ChunkStore> blockRef) {
            final var info = BlockUtils.Info.get(blockRef);
            if (info == null) {
                return null;
            }
            return info.getChunkRef();
        }

        // please don't use this one for getting it from info... just here for completeness so you know you CAN get the chunk ref out of info - in fact, that's (in my understanding) the preferred way
        @Nullable
        public static final Ref<ChunkStore> get(final BlockStateInfo info) {
            return info.getChunkRef();
        }

        // #endregion BlockRef
        // #endregion getChunkRef
    }
}
