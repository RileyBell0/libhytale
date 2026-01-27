package com.example.plugin;

import javax.annotation.Nonnull;

import com.example.plugin.structs.ExampleBlock;
import com.example.plugin.utils.BlockUtils;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.server.core.modules.block.BlockModule;
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
        return Query.and(BlockModule.BlockStateInfo.getComponentType(),
                ExampleBlock.getComponentType());
    }

    /**
     * Tick blocks!!
     */
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
