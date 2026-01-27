package com.example.plugin;

import javax.annotation.Nonnull;

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
public class TickingSystem extends ChunkTickingSystem {
    /**
     * KEEP THIS AT THE TOP OF YOUR CLASS. ALWAYS. it is by far the most important
     * thing. this is where you say "my class will be responsible for running the
     * ticks for my blocks/entities that match this stuff"
     */
    @Override
    public Query<ChunkStore> getQuery() {
        return Query.and(BlockSection.getComponentType(), ChunkSection.getComponentType());
    }

    /**
     * Tick blocks!!
     */

    public void tick(float dt, int index, @Nonnull ArchetypeChunk<ChunkStore> archetypeChunk,
            @Nonnull Store<ChunkStore> store,
            @Nonnull CommandBuffer<ChunkStore> commandBuffer) {
        BlockSection blocks = (BlockSection) archetypeChunk.getComponent(index, BlockSection.getComponentType());
        assert blocks != null;
        if (blocks.getTickingBlocksCountCopy() == 0) {
            return;
        }

        ChunkSection section = (ChunkSection) archetypeChunk.getComponent(index, ChunkSection.getComponentType());
        assert section != null;

        BlockComponentChunk blockComponentChunk = (BlockComponentChunk) commandBuffer
                .getComponent(section.getChunkColumnReference(), BlockComponentChunk.getComponentType());
        assert blockComponentChunk != null;

        blocks.forEachTicking(blockComponentChunk, commandBuffer, section.getY(),
                (blockComponentChunk1, commandBuffer1, localX, localY, localZ, blockId) -> {
                    Ref<ChunkStore> blockRef = blockComponentChunk1
                            .getEntityReference(ChunkUtil.indexBlockInColumn(localX, localY, localZ));
                    if (blockRef == null) {
                        return BlockTickStrategy.IGNORED;
                    } else {
                        ExampleBlock exampleBlock = (ExampleBlock) commandBuffer1.getComponent(blockRef,
                                ExampleBlock.getComponentType());
                        if (exampleBlock != null) {
                            WorldChunk worldChunk = (WorldChunk) commandBuffer
                                    .getComponent(section.getChunkColumnReference(), WorldChunk.getComponentType());
                            int globalX = localX + (worldChunk.getX() * 32);
                            int globalZ = localZ + (worldChunk.getZ() * 32);
                            exampleBlock.onTick(worldChunk.getWorld(), worldChunk, globalX, localY, globalZ,
                                    BlockUtils.getBlockId("RileysBlock"));
                            return BlockTickStrategy.CONTINUE;
                        } else {
                            return BlockTickStrategy.IGNORED;
                        }
                    }
                });
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
