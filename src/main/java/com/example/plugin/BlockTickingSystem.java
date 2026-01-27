package com.example.plugin;

import com.example.plugin.structs.TickingBlockEntity;
import com.example.plugin.utils.BlockUtils;
import com.hypixel.hytale.builtin.blocktick.system.ChunkBlockTickSystem;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.asset.type.blocktick.BlockTickStrategy;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockComponentChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.section.BlockSection;
import com.hypixel.hytale.server.core.universe.world.chunk.section.ChunkSection;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import java.util.function.Supplier;
import javax.annotation.Nonnull;

/**
 * This is the "main loop" kind of code for your ticking blocks
 * Every single block you have gets ticked through here
 *
 * As far as systems go, this one's pretty straightforward
 * - SCOPE: all blocks that match the query
 * - ACTION: run their tick method
 * - WHEN: every tick
 *
 * this one is designed to be generic as so i can implement systems ontop of it really
 * really easily
 *
 * its designed to "just work" for people that don't need super advanced features (or all
 * the variables they might not be using)
 */
public class BlockTickingSystem<T extends TickingBlockEntity> extends ChunkBlockTickSystem.Ticking {

    private Supplier<ComponentType<ChunkStore, T>> supplier;

    // e.g. i use ExampleBlock::getComponentType for my supplier
    public BlockTickingSystem(Supplier<ComponentType<ChunkStore, T>> supplier) {
        super();
        this.supplier = supplier;
    }

    /**
     * Tick blocks!!
     * feel free to override my method, i don't even use the Store or delta time they give us
     *
     * note: very useful to override my method in cases where you need more than (or
     * don't want) my default testing block being ticked
     */
    @Override
    public void tick(
        float dt,
        int index,
        @Nonnull ArchetypeChunk<ChunkStore> archetypeChunk,
        @Nonnull Store<ChunkStore> store,
        @Nonnull CommandBuffer<ChunkStore> commandBuffer
    ) {
        // IF YOU WANT TO OVERWRITE THIS, simply @Override the tick method itself,
        // because, well, if you're out here overwriting my _tickAllBlocks method
        // you probably know more than i do, write it from scratch, if you have improvements
        // please submit a PR they're much appreciated
        this._tickAllBlocks(index, archetypeChunk, commandBuffer);
    }

    /**
     * Tick blocks!!
     * this is my default implementation, seemed general enough and good enough that i don't wanna
     * touch it anymore. Feel free to modify it if you need, but i made it private specifically
     * to say "hey, don't like try and do schenanigans, just overwrite my process, seriously this is not a stable API"
     */
    private final void _tickAllBlocks(
        int index,
        @Nonnull ArchetypeChunk<ChunkStore> archetypeChunk,
        @Nonnull CommandBuffer<ChunkStore> commandBuffer
    ) {
        BlockSection blocks = BlockTickingSystem.getTickingBlocks(archetypeChunk, index);
        ChunkSection section = BlockTickingSystem.getChunkSection(archetypeChunk, index);
        if (blocks == null || section == null) {
            return;
        }

        BlockComponentChunk blockComponentChunk = BlockTickingSystem.getBlockComponentChunk(commandBuffer, section);
        if (blockComponentChunk == null) {
            return;
        }

        blocks.forEachTicking(blockComponentChunk, commandBuffer, section.getY(), this::tickBlock);
    }

    /**
     * Literally just the contents of BlockSection::forEachTicking (cause i just, hate indentation)
     */
    @Nonnull
    protected final BlockTickStrategy tickBlock(
        BlockComponentChunk chunk,
        CommandBuffer<ChunkStore> commandBuffer,
        int localX,
        int localY,
        int localZ,
        int blockId
    ) {
        // Get a ref to the block we're ticking
        var ref = BlockUtils.getRef(chunk, localX, localY, localZ);
        if (ref == null) {
            return BlockTickStrategy.IGNORED;
        }

        // Get the chunk it's located in
        var worldChunk = BlockUtils.getWorldChunk(commandBuffer, ref);
        if (worldChunk == null) {
            return BlockTickStrategy.CONTINUE;
        }

        // Get a ref to the component we're actually interested in
        var coords = BlockUtils.toGlobalCoords(worldChunk, localX, localY, localZ);
        T block = BlockUtils.getComponent(this.supplier, commandBuffer, ref);

        return block.onTick(
            worldChunk.getWorld(),
            worldChunk,
            coords.x,
            coords.y,
            coords.z,
            worldChunk.getBlock(coords)
        );
    }

    protected static final BlockSection getTickingBlocks(
        @Nonnull ArchetypeChunk<ChunkStore> archetypeChunk,
        int index
    ) {
        BlockSection blocks = (BlockSection) archetypeChunk.getComponent(index, BlockSection.getComponentType());
        if (blocks == null || blocks.getTickingBlocksCountCopy() == 0) {
            return null;
        }
        return blocks;
    }

    protected static final ChunkSection getChunkSection(@Nonnull ArchetypeChunk<ChunkStore> archetypeChunk, int index) {
        ChunkSection section = (ChunkSection) archetypeChunk.getComponent(index, ChunkSection.getComponentType());
        if (section == null) {
            return null;
        }
        return section;
    }

    protected static final BlockComponentChunk getBlockComponentChunk(
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
}
