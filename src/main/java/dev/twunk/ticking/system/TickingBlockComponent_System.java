package dev.twunk.ticking.system;

import com.hypixel.hytale.builtin.blocktick.BlockTickPlugin;
import com.hypixel.hytale.builtin.blocktick.system.ChunkBlockTickSystem;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.dependency.SystemDependency;
import com.hypixel.hytale.component.dependency.Dependency;
import com.hypixel.hytale.component.dependency.Order;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.asset.type.blocktick.BlockTickManager;
import com.hypixel.hytale.server.core.asset.type.blocktick.BlockTickStrategy;
import com.hypixel.hytale.server.core.asset.type.blocktick.config.TickProcedure;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import dev.twunk.interfaces.GlobalTickScheduler;
import dev.twunk.ticking.component.TickingBlockComponent;
import java.util.Set;
import java.util.function.Supplier;
import javax.annotation.Nonnull;

/// TODO look into BlockTickManager

/**
 * This is the "main loop" kind of code for your ticking blocks
 * Every single block you have gets ticked through here
 *
 * As far as systems go, this one's pretty straightforward
 * - SCOPE: all blocks that match the query
 * - ACTION: run their tick method
 * - WHEN: every tick
 *
 * this one is designed to be generic as so i can implement systems ontop of it
 * really
 * really easily
 *
 * its designed to "just work" for people that don't need super advanced
 * features (or all
 * the variables they might not be using)
 */
public class TickingBlockComponent_System<T extends TickingBlockComponent> extends ChunkBlockTickSystem.Ticking {
    @SuppressWarnings("null")
    @Nonnull
    private static final Set<Dependency<ChunkStore>> DEPENDENCIES = Set
            .of(new SystemDependency<ChunkStore, GlobalTickScheduler>(Order.AFTER, GlobalTickScheduler.class));

    @Nonnull
    public Set<Dependency<ChunkStore>> getDependencies() {
        return DEPENDENCIES;
    }

    @Nonnull
    private ComponentType<ChunkStore, T> tickingComponentType;

    /**
     * @param supplier A function that gives the type of the component you're
     *                 wanting to tick
     *                 e.g. I'd normally use MyComponent::getComponentType
     *
     *                 but HOW do I use that? easy -> dodgy (ish) code. When my
     *                 component is
     *                 registered i initialise its ComponentType field with what i
     *                 got back from
     *                 registering it to the plugin
     *
     *                 so what if this static method gets called before its
     *                 registered?
     *
     *                 shit breaks
     *
     *                 just don't do something weird and try and use a
     *                 non-registered component and you'll be fine
     *
     *                 it will ALWAYS work after the block is regsitered. so yeah,
     *                 it's a bit dodgy
     *                 but, importantly, who cares, it works!
     */
    public TickingBlockComponent_System(@Nonnull Supplier<ComponentType<ChunkStore, T>> supplier) {
        super();
        var val = supplier.get();
        if (val == null) {
            throw new RuntimeException("HECK supplier failed");
        }

        this.tickingComponentType = val;
    }

    public TickingBlockComponent_System(@Nonnull Class<T> componentClass) {
        super();
        this.tickingComponentType = TickingBlockComponent.getComponentType(componentClass);
    }

    public TickingBlockComponent_System(@Nonnull ComponentType<ChunkStore, T> tickingComponentType) {
        super();
        this.tickingComponentType = tickingComponentType;
    }

    protected static BlockTickStrategy tickProcedure(@Nonnull World world, @Nonnull WorldChunk chunk, int blockX,
            int blockY, int blockZ, int blockId) {
        HytaleLogger.forEnclosingClass().atInfo()
                .log(String.format("Ticking block at %d %d %d", blockX, blockY, blockZ));
        if (!world.getWorldConfig().isBlockTicking() || !BlockTickManager.hasBlockTickProvider()) {
            return BlockTickStrategy.IGNORED;
        }

        TickProcedure procedure = BlockTickPlugin.get().getTickProcedure(blockId);
        if (procedure == null) {
            return BlockTickStrategy.IGNORED;
        }

        try {
            return procedure.onTick(world, chunk, blockX, blockY, blockZ, blockId);
        } catch (Throwable e) {
            ((HytaleLogger.Api) HytaleLogger.forEnclosingClass().atWarning().withCause(e)).log(
                    "Failed to tick block at (%d, %d, %d) in world %s:", blockX, blockY, blockZ, world.getName());
            return BlockTickStrategy.SLEEP;
        }
    }

    /**
     * Tick blocks!!
     * feel free to override my method, i don't even use the Store or delta time
     * they give us
     *
     * note: very useful to override my method in cases where you need more than (or
     * don't want) my default testing block being ticked
     */
    // @Override
    // public void tick(
    // float dt,
    // int index,
    // @Nonnull ArchetypeChunk<ChunkStore> archetypeChunk,
    // @Nonnull Store<ChunkStore> store,
    // @Nonnull CommandBuffer<ChunkStore> commandBuffer) {
    // // IF YOU WANT TO OVERWRITE THIS, simply @Override the tick method itself,
    // // because, well, i just kinda wrote stuff here until stuff worked

    // var blockInfoComponentType = BlockModule.BlockStateInfo.getComponentType();
    // if (blockInfoComponentType == null) {
    // return;
    // }
    // var info = archetypeChunk.getComponent(index, blockInfoComponentType);
    // if (info == null) {
    // return;
    // }

    // var block = archetypeChunk.getComponent(index, this.tickingComponentType);
    // if (block == null) {
    // return;
    // }

    // // Get the chunk it's located in
    // var worldChunk = BlockUtils.getWorldChunk(commandBuffer, info);
    // if (worldChunk == null) {
    // return;
    // }

    // var localCoords = BlockUtils.getLocalCoords(info);
    // var coords = BlockUtils.toGlobalCoords(worldChunk, localCoords);
    // var world = worldChunk.getWorld();
    // if (world == null) {
    // return;
    // }

    // block.onTick(world, worldChunk, coords.x, coords.y, coords.z,
    // worldChunk.getBlock(coords));

    // var gameTime = GameTime.get(commandBuffer);
    // if (gameTime == null) {
    // return;
    // }

    // BlockUtils.setTicking(worldChunk, coords, false);
    // GlobalTickScheduler.scheduleTick(new TickRequest(info.getChunkRef(),
    // gameTime.plusSeconds(100)));
    // }

    // No touchy unless you know what you're doing. You probably don't need to touch
    // this
    // heck, i dont even know what it does really
    @Override
    public Query<ChunkStore> getQuery() {
        return Query.and(this.tickingComponentType);
    }
}
