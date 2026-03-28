package dev.twunk.hytale.interfaces.subsystem;

import com.hypixel.hytale.component.system.tick.ArchetypeTickingSystem;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.universe.world.WorldProvider;
import dev.twunk.hytale.interfaces.IRegistryProvider;
import dev.twunk.hytale.interfaces.methods.IEntityTick;
import dev.twunk.hytale.interfaces.methods.IQuery;
import dev.twunk.hytale.subsystem.base.EntityTickSubSystem;

/**
 * Gives your system the event handler function it needs to listen/react to
 * ticks for every entity that matches your query
 *
 * When you want your system to benefit from EntityTickSubSystem
 * - implement IEntityTickSystem on your system
 * - extend SubSystemOwner (or look into its code to see what it does and dupe that)
 * - call `this.appendSubSystem`, passing in the sub system(s) IN THE ORDER you want them to run
 *
 *
 * My code
 * @see EntityTickSubSystem - Underlying SubSystem that powers the IEntityTick methods
 *                            for IEntityTickSystems that register an EntityTickSubSystem
 * @see IEntityTick         - Underlying method for ticking an entity
 *
 * Hytale's code
 * @see EntityTickingSystem    - Baseline hytale system for ticking entities.
 *                               It's the underlying driver of IEntityTickSubSystem
 * @see ArchetypeTickingSystem - Underlying sort of baseline ticking system (that i know how to implement).
 *                               Runs ONCE per tick (global, not per matching entity, just runs a single
 *                               time per tick) and has an inbuilt query
 */
public interface IEntityTickSystem<
    ECS_STORE extends WorldProvider
> extends IQuery<ECS_STORE>, IRegistryProvider<ECS_STORE>, IEntityTick<ECS_STORE> {}
