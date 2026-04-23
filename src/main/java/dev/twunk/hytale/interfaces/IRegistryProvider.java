package dev.twunk.hytale.interfaces;

import com.hypixel.hytale.server.core.universe.world.WorldProvider;
import dev.twunk.hytale.interfaces.methods.IRegistry;

public interface IRegistryProvider<ECS_TYPE extends WorldProvider> {
    public abstract IRegistry<ECS_TYPE> getRegistry();
}
