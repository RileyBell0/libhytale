package dev.twunk.system.interfaces;

import com.hypixel.hytale.component.system.ISystem;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import dev.twunk.plugin.ModPlugin;

/**
 * Helper interface that means you can easily register sub systems by default
 *
 * Only useful for sub systems that extend actual hytale systems.
 *
 * No touchy.
 */
public interface ISubSystem extends ISystem<ChunkStore> {
    public default void registerTo(ModPlugin plugin) {
        plugin.getChunkStoreRegistry().registerSystem(this);
    }
}
