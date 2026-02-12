package dev.twunk.system.easy;

import com.hypixel.hytale.builtin.blocktick.system.ChunkBlockTickSystem;
import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.RefSystem;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import dev.twunk.component.IRegisteredComponent;
import dev.twunk.component.ITickingComponent;
import dev.twunk.system.response.TickContinue;
import dev.twunk.utils.BlockUtils;
import java.util.function.Supplier;
import javax.annotation.Nonnull;

public abstract class EntitySystem<T extends ITickingComponent> {

    @Nonnull
    private ComponentType<ChunkStore, T> tickingComponentType;

    @SuppressWarnings("unused")
    private static HytaleLogger.Api console = HytaleLogger.forEnclosingClass().atInfo();

    private Query<ChunkStore> query;

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
    public EntitySystem(@Nonnull Supplier<ComponentType<ChunkStore, T>> supplier) {
        var val = supplier.get();
        if (val == null) {
            throw new RuntimeException("HECK supplier failed");
        }
        this.tickingComponentType = val;
        this.query = Query.and(TickContinue.COMPONENT_TYPE, this.tickingComponentType);
    }

    public EntitySystem(@Nonnull Class<T> componentClass) {
        this.tickingComponentType = IRegisteredComponent.getComponentType(componentClass);
        this.query = Query.and(TickContinue.COMPONENT_TYPE, this.tickingComponentType);
    }

    public EntitySystem(@Nonnull ComponentType<ChunkStore, T> tickingComponentType) {
        this.tickingComponentType = tickingComponentType;
        this.query = Query.and(TickContinue.COMPONENT_TYPE, this.tickingComponentType);
    }

    /**
     * Run in the constructor of the sub system `LifetimeSystem`
     *
     * useful in specific cases i'm sure so i've left it here
     *
     * i don't use this
     *
     * just to make it clear you CAN and SHOULD edit my code if you need to
     */
    public void onInit() {}

    /**
     * Pulled down from sub-system `LifetimeSystem`. runs when an entity is added
     * @param ref
     * @param reason
     * @param store
     * @param commandBuffer
     */
    public void onEntityAdded(
        @Nonnull Ref<ChunkStore> ref,
        @Nonnull AddReason reason,
        @Nonnull Store<ChunkStore> store,
        @Nonnull CommandBuffer<ChunkStore> commandBuffer
    ) {}

    /**
     * Pulled down from sub-system `LifetimeSystem`. runs when an entity is removed
     * @param ref
     * @param reason
     * @param store
     * @param commandBuffer
     */
    public void onEntityRemove(
        @Nonnull Ref<ChunkStore> ref,
        @Nonnull RemoveReason reason,
        @Nonnull Store<ChunkStore> store,
        @Nonnull CommandBuffer<ChunkStore> commandBuffer
    ) {}

    /**
     * No touchy. just read. overwrite it if you need
     */
    protected void onTickInternal(
        float dt,
        int index,
        @Nonnull ArchetypeChunk<ChunkStore> archetypeChunk,
        @Nonnull Store<ChunkStore> store,
        @Nonnull CommandBuffer<ChunkStore> commandBuffer
    ) {
        // blockEntityRef --(has)-> blockInfo --(is in)-> worldChunk --(is in)-> world
        // blockInfo has local coords
        // blockInfo & worldChunk --(implies)-> global coords

        // ref to our block entity (ref to the entity at the current
        // index that matches the query - imagine the index as an arbtrary
        // `i` in a for loop. think nothing of it)
        var blockRef = archetypeChunk.getReferenceTo(index);

        // From my understanding, this seems to be a sort of inherent component
        // on block entites. You can always seem to get it. This stores its LOCAL
        // coordinates (sort of)
        //
        // It stores the `index` that the block is within its `chunk`. we do some
        // funky maths to turn that 1 dimensional index (1, 2, 3...) into a 3 dimensional
        // position [(x: 0, y: 0, z: 0), (x: 1, y: 0, z: 0)... etc]
        //
        // note: ^^ above numbers made up, really never checked which order they
        // index their blocks into the chunk
        var blockInfo = BlockUtils.getInfo(commandBuffer, blockRef);
        if (blockInfo == null) {
            return;
        }

        // you guessed it, the chunk the block is in. the chunk stores its own coordinates
        // (as in where the chunk itself is located within the world at large)
        //
        // we need this to effectively just add its coordinates to our block
        // -> block local coords + chunk coords ~= global position
        var worldChunk = BlockUtils.getWorldChunk(commandBuffer, blockInfo);
        if (worldChunk == null) {
            return;
        }

        // the world our chunk is in
        // the best way i've found so far to get the world that the entity is in
        // is to go
        // blockEntityRef --(has)-> blockInfo --(is in)-> worldChunk --(is in)-> world
        var world = worldChunk.getWorld();
        if (world == null) {
            return;
        }
        var coords = BlockUtils.getGlobalCoords(worldChunk, blockInfo);

        // Since our query is based on your component, we KNOW it has to have your
        // component, so, we just, get it
        var component = BlockUtils.getComponent(EntitySystem.this.tickingComponentType, commandBuffer, blockRef);
        try {
            // and call the tick method you defined on your component, which,
            // i know is sort of heresy for ECS systems, but, it makes doing
            // easy things easy. and i'm all for that
            component.onTick(
                world,
                worldChunk,
                commandBuffer,
                coords.x,
                coords.y,
                coords.z,
                worldChunk.getBlock(coords)
            );
        } catch (Throwable e) {
            console.log(String.format("ERROR: Failed to tick block at (%d, %d, %d)", coords.x, coords.y, coords.z));
            return;
        }
    }

    /**
     * Tiny sub-system to simply tell our parent system when we added entities
     *
     * Subscribes to the query defined in the parent EntitySystem and listens for
     * onEntiyAdd and remove events
     */
    public class LifetimeSystem extends RefSystem<ChunkStore> {

        public LifetimeSystem() {
            EntitySystem.this.onInit();
        }

        @Override
        public void onEntityAdded(
            @Nonnull Ref<ChunkStore> ref,
            @Nonnull AddReason reason,
            @Nonnull Store<ChunkStore> store,
            @Nonnull CommandBuffer<ChunkStore> commandBuffer
        ) {
            EntitySystem.this.onEntityAdded(ref, reason, store, commandBuffer);
        }

        @Override
        public void onEntityRemove(
            @Nonnull Ref<ChunkStore> ref,
            @Nonnull RemoveReason reason,
            @Nonnull Store<ChunkStore> store,
            @Nonnull CommandBuffer<ChunkStore> commandBuffer
        ) {
            EntitySystem.this.onEntityRemove(ref, reason, store, commandBuffer);
        }

        @Override
        public Query<ChunkStore> getQuery() {
            return EntitySystem.this.query;
        }
    }

    /**
     * Another tiny sub-system.
     * Simply runs your component's tick method every tick. its uuh, yeah. basic. and based
     *
     * Subscribes to the query defined in the parent EntitySystem (the abstract class this class is defined in)
     * and runs every tick on that query
     */
    public class TickSystem extends ChunkBlockTickSystem.Ticking {

        /**
         * i've done some really specific names for vars and comments in here to highlight what
         * i think is happening. notably, this is written whilst there's no comments
         * in their src code so it's very much my best guess from spending uuh,
         * too many hours reading their src code
         *
         * its the best view of my current undestanding of how hytale works internally
         *
         * it should NOT be treated as fact. they're very direct statements since
         * it's how i understand it currently. This will change with time as my understanding
         * improves
         *
         * this is the bit that eventually can call your element
         */
        public void tick(
            float dt,
            int index,
            @Nonnull ArchetypeChunk<ChunkStore> archetypeChunk,
            @Nonnull Store<ChunkStore> store,
            @Nonnull CommandBuffer<ChunkStore> commandBuffer
        ) {
            EntitySystem.this.onTickInternal(dt, index, archetypeChunk, store, commandBuffer);
        }

        @Override
        public Query<ChunkStore> getQuery() {
            return EntitySystem.this.query;
        }
    }
}
