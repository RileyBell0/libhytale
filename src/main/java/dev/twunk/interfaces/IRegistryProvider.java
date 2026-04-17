package dev.twunk.interfaces;

import com.hypixel.hytale.server.core.universe.world.WorldProvider;
import dev.twunk.interfaces.methods.IRegistry;

public interface IRegistryProvider<ECS_TYPE extends WorldProvider> {
    public abstract IRegistry<ECS_TYPE> getRegistry();
}
