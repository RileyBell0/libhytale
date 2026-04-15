package dev.twunk.interfaces.methods;

import com.hypixel.hytale.builtin.blocktick.system.ChunkBlockTickSystem;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.system.tick.ArchetypeTickingSystem;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import dev.twunk.hytale.refs.BlockRef;
import dev.twunk.hytale.system.OnTickSystem;
import dev.twunk.lib.system.AutoBlockTickSystem;

/**
 * Methods for my subsytem version of `ChunkBlockTickSystem.Ticking`
 * @see ChunkBlockTickSystem a waking nightmare, works with almost entirely with
 *                           TickProcedures (a seemingly deprecated-ish or at
 *                           the very least highly dodgily implemented throught
 *                           the server code. Seems to be prone to errors)
 *
 * My code
 * @see AutoBlockTickSystem - System for specifically ticking block entities (gets
 *                                     block position, id, etc. then gives that to your tick method)
 * @see OnTickSystem          - SubSystem used by TickableBlockComponentSystem
 *                                     that ticks entities (in our case block entities)
 * @see IOnTick                  - Underlying method for ticking an entity
 *
 * Hytale's code
 * @see EntityTickingSystem    - Baseline hytale system for ticking entities.
 *                               It's the underlying driver of IEntityTickSubSystem
 * @see ArchetypeTickingSystem - Underlying sort of baseline ticking system itself (that i know how to implement).
 *                               Runs ONCE per tick (global, not per block, just runs a single
 *                               time per tick) and has an inbuilt query
 *
 * Good for
 * - auto-ticking block components
 * - systems that tick blocks
 * - etc
 *
 * TODO:
 * - overall goal here is to provide a consistent (and hopefully smaller) interface
 *   for interacting with block components.
 * - Might consider making a `blockRef` class that extends `Ref<ChunkStore>` that i
 *   can instead store all these things in INDIRECTLY rather than computing them for
 *   everything. not all `onBlockTick` implementations will require all these, so its
 *   just wasted compute
 */
public interface IOnBlockTick {
    public void onBlockTick(final BlockRef blockRef, final CommandBuffer<ChunkStore> commandBuffer);
}
