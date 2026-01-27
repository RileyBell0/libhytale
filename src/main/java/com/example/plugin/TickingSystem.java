package com.example.plugin;

import com.example.plugin.structs.TickingBlockEntity;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.server.core.universe.world.chunk.section.BlockSection;
import com.hypixel.hytale.server.core.universe.world.chunk.section.ChunkSection;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import java.util.function.Supplier;

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
public class TickingSystem<T extends TickingBlockEntity> extends BlockTickingSystem<T> {

    public TickingSystem(Supplier<ComponentType<ChunkStore, T>> supplier) {
        super(supplier);
    }

    /** SCOPE: runs on these blocks */
    @Override
    public Query<ChunkStore> getQuery() {
        return Query.and(BlockSection.getComponentType(), ChunkSection.getComponentType());
    }
}
