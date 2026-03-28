package dev.twunk.interfaces.subsystem;

import com.hypixel.hytale.component.system.RefSystem;
import com.hypixel.hytale.server.core.universe.world.WorldProvider;
import dev.twunk.hytale.system.LifetimeSubSystem;
import dev.twunk.interfaces.IRegistryProvider;
import dev.twunk.interfaces.methods.ILifetime;
import dev.twunk.interfaces.methods.IQuery;

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
 * @see ILifetime              - Methods for listening to entity add/remove events
 * @see ILifetimeSystem        - Additional requirements that an implementor of IEntityLifetime must satisfy
 *                                     in order to register a subsystem to run itself
 * @see LifetimeSubSystem      - The base subsystem that "runs" something with "IEntityLifetime"
 *
 * Hytale's code
 * @see RefSystem - Hytale's underlying system that provides the `onEntityAdded` and `onEntityRemove` events
 */
public interface ILifetimeSystem<
    ECS_STORE extends WorldProvider
> extends ILifetime<ECS_STORE>, IQuery<ECS_STORE>, IRegistryProvider<ECS_STORE> {}
