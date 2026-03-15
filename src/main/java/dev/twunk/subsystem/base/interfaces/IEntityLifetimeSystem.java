package dev.twunk.subsystem.base.interfaces;

import com.hypixel.hytale.component.system.QuerySystem;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import dev.twunk.common.IEntityLifetime;

/**
 * Gives your system the event handler function it needs to listen/react to
 * entities (that match your query) being loaded/added/unloaded/removed from the world
 *
 * When you want your system to benefit from LifetimeSubSystem
 * - implement ILifetimeSystem on your system
 * - extend SubSystemOwner (or look into its code to see what it does and dupe that)
 * - call `this.appendSubSystem`, passing in the sub system(s) IN THE ORDER you want them to run
 */
public interface IEntityLifetimeSystem extends IEntityLifetime, QuerySystem<ChunkStore> {}
