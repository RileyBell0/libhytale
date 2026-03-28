package dev.twunk.hytale.interfaces.subsystem;

import com.hypixel.hytale.component.system.RefSystem;
import com.hypixel.hytale.server.core.universe.world.WorldProvider;
import dev.twunk.hytale.interfaces.IRegistryProvider;
import dev.twunk.hytale.interfaces.methods.IEntityLifetime;
import dev.twunk.hytale.interfaces.methods.IQuery;
import dev.twunk.hytale.subsystem.base.EntityLifetimeSubSystem;

/**
 * Gives your system the event handler function it needs to listen/react to
 * entities (that match your query) being loaded/added/unloaded/removed from the world
 *
 * When you want your system to benefit from LifetimeSubSystem
 * - implement ILifetimeSystem on your system
 * - extend SubSystemOwner (or look into its code to see what it does and dupe that)
 * - call `this.appendSubSystem`, passing in the sub system(s) IN THE ORDER you want them to run
 *
 * My code
 * @see IEntityLifetime              - Methods for listening to entity add/remove events
 * @see IEntityLifetimeSystem        - Additional requirements that an implementor of IEntityLifetime must satisfy
 *                                     in order to register a subsystem to run itself
 * @see EntityLifetimeSubSystem      - The base subsystem that "runs" something with "IEntityLifetime"
 *
 * Hytale's code
 * @see RefSystem - Hytale's underlying system that provides the `onEntityAdded` and `onEntityRemove` events
 */
public interface IEntityLifetimeSystem<
    ECS_STORE extends WorldProvider
> extends IEntityLifetime<ECS_STORE>, IQuery<ECS_STORE>, IRegistryProvider<ECS_STORE> {}
