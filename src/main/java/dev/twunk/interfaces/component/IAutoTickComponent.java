package dev.twunk.interfaces.component;

import com.hypixel.hytale.server.core.universe.world.WorldProvider;

public interface IAutoTickComponent<ECS_STORE extends WorldProvider> extends ITickComponent<ECS_STORE> {}
