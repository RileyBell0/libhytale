package com.example.plugin;

import com.example.plugin.structs.ExampleBlock;
import com.example.plugin.utils.BlockUtils;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.server.core.asset.type.blocktick.BlockTickStrategy;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockComponentChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.section.BlockSection;
import com.hypixel.hytale.server.core.universe.world.chunk.section.ChunkSection;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import javax.annotation.Nonnull;

/**
 * If you have a component you want to tick, make its class extend the
 * `TickProcedure` class
 *
 * keep in mind, you might have to have a class that JUST extends TickProcedure
 * depending on how complex ur component is. E.g. imagine a scenario where you
 * need to extend something other than TickProcedure aswell. You're out of luck,
 * just implement another class please
 *
 * --------------------
 *
 * This goes hand in hand with a plugin
 *
 * This 'lil guy is responsible for effectivelly piping bits together to get
 * your
 * code getting called at the right times
 *
 * If you've run into the issue like I did of "ok great, I've got my
 * block/entity/etc and now I want to set some stuff up when it loads in" -
 * you've come to the right place
 *
 * This is where you hook up all the good stuff like `onEntityAdded`.
 *
 * Yup. Your code calls your own code. Hytale does not call any `onEntityAdded`
 * stuff for us. YOU have to listen to these events yourself and dispatch them
 */
public class TickingSystem extends BlockTickingSystem {

    /** SCOPE: runs on these blocks */
    @Override
    public Query<ChunkStore> getQuery() {
        return Query.and(BlockSection.getComponentType(), ChunkSection.getComponentType());
    }

    private static BlockSection getTickingBlocks(@Nonnull ArchetypeChunk<ChunkStore> archetypeChunk, int index) {
        BlockSection blocks = (BlockSection) archetypeChunk.getComponent(index, BlockSection.getComponentType());
        if (blocks == null || blocks.getTickingBlocksCountCopy() == 0) {
            return null;
        }
        return blocks;
    }

    private static ChunkSection getChunkSection(@Nonnull ArchetypeChunk<ChunkStore> archetypeChunk, int index) {
        ChunkSection section = (ChunkSection) archetypeChunk.getComponent(index, ChunkSection.getComponentType());
        if (section == null) {
            return null;
        }
        return section;
    }

    private static BlockComponentChunk getBlockComponentChunk(
        @Nonnull CommandBuffer<ChunkStore> commandBuffer,
        ChunkSection section
    ) {
        var chunkColumnReference = section.getChunkColumnReference();
        if (chunkColumnReference == null) {
            return null;
        }
        BlockComponentChunk blockComponentChunk = (BlockComponentChunk) commandBuffer.getComponent(
            chunkColumnReference,
            BlockComponentChunk.getComponentType()
        );
        if (blockComponentChunk == null) {
            return null;
        }
        return blockComponentChunk;
    }

    /**
     * Tick blocks!!
     */
    public void tick(
        float dt,
        int index,
        @Nonnull ArchetypeChunk<ChunkStore> archetypeChunk,
        @Nonnull Store<ChunkStore> store,
        @Nonnull CommandBuffer<ChunkStore> commandBuffer
    ) {
        BlockSection blocks = TickingSystem.getTickingBlocks(archetypeChunk, index);
        ChunkSection section = TickingSystem.getChunkSection(archetypeChunk, index);
        if (blocks == null || section == null) {
            return;
        }

        BlockComponentChunk blockComponentChunk = TickingSystem.getBlockComponentChunk(commandBuffer, section);
        if (blockComponentChunk == null) {
            return;
        }

        blocks.forEachTicking(blockComponentChunk, commandBuffer, section.getY(), TickingSystem::tickPassthrough);
    }

    private static Ref<ChunkStore> getBlockRef(BlockComponentChunk chunk, int localX, int localY, int localZ) {
        return chunk.getEntityReference(ChunkUtil.indexBlockInColumn(localX, localY, localZ));
    }

    private static ExampleBlock getBlock(
        CommandBuffer<ChunkStore> commandBuffer,
        BlockComponentChunk chunk,
        int localX,
        int localY,
        int localZ
    ) {
        var block = TickingSystem.getBlockRef(chunk, localX, localY, localZ);
        if (block == null) {
            return null;
        }

        return commandBuffer.getComponent(block, ExampleBlock.getComponentType());
    }

    @Nonnull
    private static BlockTickStrategy tickPassthrough(
        BlockComponentChunk chunk,
        CommandBuffer<ChunkStore> commandBuffer,
        int localX,
        int localY,
        int localZ,
        int blockId
    ) {
        var ref = chunk.getEntityReference(ChunkUtil.indexBlockInColumn(localX, localY, localZ));
        if (ref == null) {
            return BlockTickStrategy.IGNORED;
        }

        var block = TickingSystem.getBlock(commandBuffer, chunk, localX, localY, localZ);
        if (block == null) {
            return BlockTickStrategy.IGNORED;
        }

        BlockUtils.getWorldChunk(commandBuffer, ref);
        var wcct = WorldChunk.getComponentType();
        if (wcct == null) {
            return BlockTickStrategy.CONTINUE;
        }

        WorldChunk worldChunk = BlockUtils.getWorldChunk(commandBuffer, ref);
        if (worldChunk == null) {
            return BlockTickStrategy.CONTINUE;
        }

        var world = worldChunk.getWorld();
        if (world == null) {
            return BlockTickStrategy.CONTINUE;
        }

        int globalX = localX + (worldChunk.getX() * 32);
        int globalZ = localZ + (worldChunk.getZ() * 32);
        block.onTick(world, worldChunk, globalX, localY, globalZ, BlockUtils.getBlockId("RileysBlock"));
        return BlockTickStrategy.CONTINUE;
    }

    // @Override
    // public void tick(
    //     float dt,
    //     int index,
    //     @Nonnull ArchetypeChunk<ChunkStore> archetypeChunk,
    //     @Nonnull Store<ChunkStore> store,
    //     @Nonnull CommandBuffer<ChunkStore> commandBuffer
    // ) {
    //     // Sanity check - validate our component is on here
    //     Ref<ChunkStore> ref = archetypeChunk.getReferenceTo(index);
    //     if (!BlockUtils.hasComponent(commandBuffer, ref, RileysTickingComponent::getComponentType)) {
    //         return;
    //     }

    //     // TICK
    //     TickingSystem.tickBlock(commandBuffer, ref);
    // }
}
