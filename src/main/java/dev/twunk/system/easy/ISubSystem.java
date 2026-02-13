package dev.twunk.system.easy;

import com.hypixel.hytale.component.system.ISystem;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import dev.twunk.plugin.ModPlugin;

public interface ISubSystem extends ISystem<ChunkStore> {
    public default void registerTo(ModPlugin plugin) {
        plugin.getChunkStoreRegistry().registerSystem(this);
    }
}
