package dev.twunk.ticking.component.system;

import com.hypixel.hytale.builtin.blocktick.system.ChunkBlockTickSystem;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import dev.twunk.ticking.component.ITickingComponent;
import dev.twunk.ticking.response.TickResponse;
import dev.twunk.ticking.response.TickContinue;
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
        this.query = Query.and(TickContinue.COMPONENT_TYPE, this.tickingComponentType);
    }

    public TickingBlockComponent_System(@Nonnull Class<T> componentClass) {
        super();

        this.tickingComponentType = ITickingComponent.getComponentType(componentClass);
        this.query = Query.and(TickContinue.COMPONENT_TYPE, this.tickingComponentType);
    }

    public TickingBlockComponent_System(@Nonnull ComponentType<ChunkStore, T> tickingComponentType) {
        super();

        this.tickingComponentType = tickingComponentType;
        this.query = Query.and(TickContinue.COMPONENT_TYPE, this.tickingComponentType);
    }

    public void tick(float dt, int index, @Nonnull ArchetypeChunk<ChunkStore> archetypeChunk,
            @Nonnull Store<ChunkStore> store, @Nonnull CommandBuffer<ChunkStore> commandBuffer) {
        var ref = archetypeChunk.getReferenceTo(index);

        var info = BlockUtils.getInfo(commandBuffer, ref);
        if (info == null) {
            return;
        }
        var worldChunk = BlockUtils.getWorldChunk(commandBuffer, info);
        if (worldChunk == null) {
            return;
        }
        var world = worldChunk.getWorld();
        if (world == null) {
            return;
        }

        var component = BlockUtils.getComponent(this.tickingComponentType, commandBuffer, ref);
        var tickComponent = BlockUtils.getComponent(TickContinue.COMPONENT_TYPE, commandBuffer, ref);
        var coords = BlockUtils.getGlobalCoords(worldChunk, info);
        try {
            TickResponse tickResponse = component.onTick(world, worldChunk, commandBuffer, coords.x, coords.y, coords.z,
                    worldChunk.getBlock(coords));

            // Transition to the state returned by the block
            if (tickResponse != null && !tickResponse.equals(tickComponent)) {
                if (tickResponse.getComponentType() == TickContinue.COMPONENT_TYPE) {
                    commandBuffer.replaceComponent(ref, TickContinue.COMPONENT_TYPE,
                            (TickContinue) tickResponse);
                } else {
                    commandBuffer.removeComponent(ref, component.getComponentType());
                    commandBuffer.addComponent(ref, tickResponse.getComponentType());
                }
            }
        } catch (Throwable e) {
            console.log(String.format("ERROR: Failed to tick block at (%d, %d, %d)", coords.x, coords.y, coords.z));
            return;
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
