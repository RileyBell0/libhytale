package dev.twunk.interfaces;

import com.hypixel.hytale.server.core.universe.world.WorldProvider;
import dev.twunk.interfaces.methods.IRegistry;

public interface IRegistryProvider<ECS_STORE extends WorldProvider> {
    public abstract IRegistry<ECS_STORE> getRegistry();
}
