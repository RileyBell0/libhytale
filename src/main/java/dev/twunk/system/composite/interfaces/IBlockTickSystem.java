package dev.twunk.system.composite.interfaces;

import com.hypixel.hytale.component.system.QuerySystem;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import dev.twunk.utils.ITickableBlockEntity;

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
 */
public interface IBlockTickSystem extends QuerySystem<ChunkStore>, ITickableBlockEntity {}
