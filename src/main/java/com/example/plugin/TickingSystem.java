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
        var blockSectionComponentType = BlockSection.getComponentType();
        if (blockSectionComponentType == null) {
            return;
        }

        BlockSection blocks = (BlockSection) archetypeChunk.getComponent(index, blockSectionComponentType);
        if (blocks == null || blocks.getTickingBlocksCountCopy() == 0) {
            return;
        }

        var chunkSectionComponentType = ChunkSection.getComponentType();
        if (chunkSectionComponentType == null) {
            return;
        }
        ChunkSection section = (ChunkSection) archetypeChunk.getComponent(index, chunkSectionComponentType);
        if (section == null) {
            return;
        }

        var chunkColumnReference = section.getChunkColumnReference();
        if (chunkColumnReference == null) {
            return;
        }

        var blockChunkComponentType = BlockComponentChunk.getComponentType();
        if (blockChunkComponentType == null) {
            return;
        }

        BlockComponentChunk blockComponentChunk = (BlockComponentChunk) commandBuffer.getComponent(
            chunkColumnReference,
            blockChunkComponentType
        );
        assert blockComponentChunk != null;

        blocks.forEachTicking(
            blockComponentChunk,
            commandBuffer,
            section.getY(),
            (blockComponentChunk1, commandBuffer1, localX, localY, localZ, blockId) -> {
                Ref<ChunkStore> blockRef = blockComponentChunk1.getEntityReference(
                    ChunkUtil.indexBlockInColumn(localX, localY, localZ)
                );
                if (blockRef == null) {
                    return BlockTickStrategy.IGNORED;
                }

                ExampleBlock exampleBlock = (ExampleBlock) commandBuffer1.getComponent(
                    blockRef,
                    ExampleBlock.getComponentType()
                );
                if (exampleBlock == null) {
                    return BlockTickStrategy.IGNORED;
                }

                var wcct = WorldChunk.getComponentType();
                if (wcct == null) {
                    return BlockTickStrategy.CONTINUE;
                }

                WorldChunk worldChunk = (WorldChunk) commandBuffer.getComponent(chunkColumnReference, wcct);
                var world = worldChunk.getWorld();
                if (world == null) {
                    return BlockTickStrategy.CONTINUE;
                }

                int globalX = localX + (worldChunk.getX() * 32);
                int globalZ = localZ + (worldChunk.getZ() * 32);
                return exampleBlock.onTick(
                    world,
                    worldChunk,
                    globalX,
                    localY,
                    globalZ,
                    BlockUtils.getBlockId("RileysBlock")
                );
            }
        );
    }
    // @Override
    // public void tick(float dt, int index, @Nonnull ArchetypeChunk<ChunkStore>
    // archetypeChunk,
    // @Nonnull Store<ChunkStore> store, @Nonnull CommandBuffer<ChunkStore>
    // commandBuffer) {

    // // Sanity check - validate our component is on here
    // Ref<ChunkStore> ref = archetypeChunk.getReferenceTo(index);
    // if (!BlockUtils.hasComponent(commandBuffer, ref,
    // RileysTickingComponent::getComponentType)) {
    // return;
    // }

    // // TICK
    // TickingSystem.tickBlock(commandBuffer, ref);
    // }
}
