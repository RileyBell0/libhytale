package dev.twunk.hytale.interfaces;

import com.hypixel.hytale.server.core.universe.world.WorldProvider;

public interface IRegistryProvider<ECS_STORE extends WorldProvider> {
    public IRegistry<ECS_STORE> getRegistry();
}
