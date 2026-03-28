package dev.twunk.interfaces.subsystem;

import com.hypixel.hytale.component.system.tick.ArchetypeTickingSystem;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import dev.twunk.hytale.system.TickSubSystem;
import dev.twunk.interfaces.methods.IBlockTick;
import dev.twunk.interfaces.methods.IQuery;
import dev.twunk.interfaces.methods.ITick;
import dev.twunk.lib.system.AutoBlockTickSystem;

/**
 * Gives your system the event handler function it needs to run code for ticking
 * an entity that matches your query
 *
 * This handler will be called every tick for all entities that match your query
 *
 * very useful for getting all those pesky fields like "world" and "chunk" and "coords"
 * that seem weirdly annoying to find in a decompiled jar without comments. this is
 * going to be so much easier when they're free to release a version with comments but
 * alas, such is life
 *
 * When you want your system to benefit from QueryTickingSubSystem
 * - implement IQueryTickingSystem on your system
 * - extend SubSystemOwner (or look into its code to see what it does and dupe that)
 * - call `this.appendSubSystem`, passing in the sub system(s) IN THE ORDER you want them to run
 *
 * My code
 * @see AutoBlockTickSystem - System for specifically ticking block entities (gets
 *                                     block position, id, etc. then gives that to your tick method)
 * @see TickSubSystem          - SubSystem used by TickableBlockComponentSystem
 *                                     that ticks entities (in our case block entities)
 * @see ITickSystem            - Something that can be ticked by EntityTickSubSystem
 *                                     (satisfies IEntityTick)
 * @see ITick                  - Underlying method for ticking an entity
 *
 * Hytale's code
 * @see EntityTickingSystem    - Baseline hytale system for ticking entities.
 *                               It's the underlying driver of IEntityTickSubSystem
 * @see ArchetypeTickingSystem - Underlying sort of baseline ticking system itself (that i know how to implement).
 *                               Runs ONCE per tick (global, not per block, just runs a single
 *                               time per tick) and has an inbuilt query
 */
public interface IBlockTickSystem extends IQuery<ChunkStore>, IBlockTick {}
