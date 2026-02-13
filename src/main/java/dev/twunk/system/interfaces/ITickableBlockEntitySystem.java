package dev.twunk.system.interfaces;

import com.hypixel.hytale.component.system.QuerySystem;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import dev.twunk.utils.ITickableBlockEntity;

public interface ITickableBlockEntitySystem extends QuerySystem<ChunkStore>, ITickableBlockEntity {}
