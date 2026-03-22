package dev.twunk;

import com.hypixel.hytale.server.core.universe.world.WorldProvider;
import dev.twunk.subsystem.composite.interfaces.IRegistry;

public interface IRegistryProvider<ECS_STORE extends WorldProvider> {
    public IRegistry<ECS_STORE> getRegistry();
}
