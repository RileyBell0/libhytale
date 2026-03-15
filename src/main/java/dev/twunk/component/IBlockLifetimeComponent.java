package dev.twunk.component;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import dev.twunk.common.IEntityLifetime;

public interface IBlockLifetimeComponent extends IEntityLifetime, Component<ChunkStore> {}
