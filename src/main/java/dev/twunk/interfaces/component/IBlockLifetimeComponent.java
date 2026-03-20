package dev.twunk.interfaces.component;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import dev.twunk.interfaces.methods.IEntityLifetime;

public interface IBlockLifetimeComponent extends IEntityLifetime, Component<ChunkStore> {}
