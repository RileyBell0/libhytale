package dev.twunk.ticking.component.system;

import com.hypixel.hytale.builtin.blocktick.system.ChunkBlockTickSystem;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.asset.type.blocktick.BlockTickManager;
import com.hypixel.hytale.server.core.asset.type.blocktick.BlockTickStrategy;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import dev.twunk.ticking.component.ITickingComponent;
import dev.twunk.ticking.component.ModTickingAwakeComponent;
import dev.twunk.ticking.strategy.TickStrategy;
import dev.twunk.utils.BlockUtils;
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
 * this one is designed to be generic as so i can implement systems ontop of it
 * really
 * really easily
 *
 * its designed to "just work" for people that don't need super advanced
 * features (or all
 * the variables they might not be using)
 *
 * also note, we should extend ArchetypeTickingSystem, and then we simply call
 * store.tick(this) and it goes
 * me(tick) -> store(tick) -> me(archetype's other ticking method)
 */
public class TickingBlockComponent_System<T extends ITickingComponent> extends ChunkBlockTickSystem.Ticking {
    private static HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private static HytaleLogger.Api console = HytaleLogger.forEnclosingClass().atInfo();

    @Nonnull
    private Query<ChunkStore> query;

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
        this.query = Query.and(ModTickingAwakeComponent.getComponentType(), this.tickingComponentType);
    }

    public TickingBlockComponent_System(@Nonnull Class<T> componentClass) {
        super();

        this.tickingComponentType = ITickingComponent.getComponentType(componentClass);
        this.query = Query.and(ModTickingAwakeComponent.getComponentType(), this.tickingComponentType);
    }

    public TickingBlockComponent_System(@Nonnull ComponentType<ChunkStore, T> tickingComponentType) {
        super();

        this.tickingComponentType = tickingComponentType;
        this.query = Query.and(ModTickingAwakeComponent.getComponentType(), this.tickingComponentType);
    }

    @SuppressWarnings("removal")
    public void tick(float dt, int index, @Nonnull ArchetypeChunk<ChunkStore> archetypeChunk,
            @Nonnull Store<ChunkStore> store, @Nonnull CommandBuffer<ChunkStore> commandBuffer) {
        var ref = archetypeChunk.getReferenceTo(index);

        var worldChunkComponentType = WorldChunk.getComponentType();
        if (worldChunkComponentType == null) {
            return;
        }

        WorldChunk worldChunk = (WorldChunk) archetypeChunk.getComponent(index, worldChunkComponentType);
        if (worldChunk == null) {
            return;
        }

        var chunk = worldChunk.getBlockChunk();
        if (chunk == null) {
            return;
        }

        try {
            int ticked = chunk.forEachTicking(ref, worldChunk,
                    (r, c, localX, localY, localZ, blockId) -> {
                        console.log(String.format("Ticking block at (%d, %d, %d)", localX, localY, localZ));
                        World world = c.getWorld();
                        if (world == null) {
                            return BlockTickStrategy.IGNORED;
                        }

                        int blockX = c.getX() << 5 | localX;
                        int blockZ = c.getZ() << 5 | localZ;

                        if (!world.getWorldConfig().isBlockTicking() || !BlockTickManager.hasBlockTickProvider()) {
                            return BlockTickStrategy.IGNORED;
                        }

                        var tickComponent = BlockUtils.getComponent(this.tickingComponentType, commandBuffer,
                                ref);
                        if (tickComponent == null) {
                            return BlockTickStrategy.IGNORED;
                        }

                        try {
                            return tickComponent.onTick(world, worldChunk, commandBuffer, blockX, localY, blockZ,
                                    blockId);
                        } catch (Throwable var9) {
                            LOGGER.atWarning().withCause(var9).log(
                                    "Failed to tick block at (%d, %d, %d) ID %s in world %s:", blockX, localY, blockZ,
                                    blockId, world.getName());

                            return BlockTickStrategy.SLEEP;
                        }
                    });

            if (ticked > 0) {
                LOGGER.atFiner().log("Ticked %d blocks in chunk (%d, %d)", ticked,
                        worldChunk.getX(), worldChunk.getZ());
            }
        } catch (Throwable var9) {
            LOGGER.atSevere().withCause(var9)
                    .log("Failed to tick chunk: %s", worldChunk);
        }
    }

    /**
     * Define how often you want your system to tick
     */
    @Nonnull
    public TickStrategy getTickStrategy() {
        return TickStrategy.always();
    }

    // No touchy unless you know what you're doing. You probably don't need to touch
    // this
    // heck, i dont even know what it does really
    @Override
    public Query<ChunkStore> getQuery() {
        return this.query;
    }
}
