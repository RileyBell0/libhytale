package com.example.plugin.interfaces;

import com.example.plugin.utils.BlockUtils;
import com.hypixel.hytale.builtin.blocktick.system.ChunkBlockTickSystem;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.server.core.modules.block.BlockModule;
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
public class TickingBlockComponent_System<T extends TickingBlockComponent> extends ChunkBlockTickSystem.Ticking {

    @Nonnull
    private ComponentType<ChunkStore, T> tickingComponentType;

    /**
     * @param supplier A function that gives the type of the component you're wanting to tick
     *                 e.g. I'd normally use MyComponent::getComponentType
     *
     *                 but HOW do I use that? easy -> dodgy (ish) code. When my component is
     *                 registered i initialise its ComponentType field with what i got back from
     *                 registering it to the plugin
     *
     *                 so what if this static method gets called before its registered?
     *
     *                 shit breaks
     *
     *                 just don't do something weird and try and use a non-registered component and you'll be fine
     *
     *                 it will ALWAYS work after the block is regsitered. so yeah, it's a bit dodgy
     *                 but, importantly, who cares, it works!
     */
    public TickingBlockComponent_System(Supplier<ComponentType<ChunkStore, T>> supplier) {
        super();
        this.tickingComponentType = supplier.get();
    }

    public TickingBlockComponent_System(Class<T> componentClass) {
        super();
        this.tickingComponentType = TickingBlockComponent.getComponentType(componentClass);
    }

    public TickingBlockComponent_System(ComponentType<ChunkStore, T> tickingComponentType) {
        super();
        this.tickingComponentType = tickingComponentType;
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
        var ref = archetypeChunk.getComponent(index, BlockModule.BlockStateInfo.getComponentType());
        if (ref == null) {
            return;
        }

        var block = archetypeChunk.getComponent(index, this.tickingComponentType);
        if (block == null) {
            return;
        }

        // Get the chunk it's located in
        var worldChunk = BlockUtils.getWorldChunk(commandBuffer, ref);
        if (worldChunk == null) {
            return;
        }

        var coords = BlockUtils.getGlobalCoords(worldChunk, ref);
        if (coords == null) {
            return;
        }

        block.onTick(worldChunk.getWorld(), worldChunk, coords.x, coords.y, coords.z, worldChunk.getBlock(coords));
    }

    // No touchy unless you know what you're doing. You probably don't need to touch this
    // heck, i dont even know what it does really
    @Override
    public Query<ChunkStore> getQuery() {
        return Query.and(this.tickingComponentType);
    }
}
