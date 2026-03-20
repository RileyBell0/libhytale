package dev.twunk.interfaces.component;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import dev.twunk.interfaces.methods.IBlockTick;

/**
 * A component that has an onBlockTick method
 */
public interface ITickableBlockComponent extends IBlockTick, Component<ChunkStore> {}
