package dev.twunk.hytale.interfaces.config;

import com.hypixel.hytale.server.core.universe.world.WorldProvider;

public interface IEventConfig<ECS_TYPE extends WorldProvider> extends IGroup<ECS_TYPE>, IDependencies<ECS_TYPE> {}
