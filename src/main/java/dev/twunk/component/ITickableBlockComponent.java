package dev.twunk.component;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import dev.twunk.utils.tick.IBlockTick;

/**
 * A component that has an onBlockTick method
 */
public interface ITickableBlockComponent extends IBlockTick, Component<ChunkStore> {}
