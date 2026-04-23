package dev.twunk.hytale.interfaces;

import com.hypixel.hytale.component.system.ISystem;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.universe.world.WorldProvider;

public interface ISystemEventDriver<ECS_TYPE extends WorldProvider> extends IEventDriver<ECS_TYPE>, ISystem<ECS_TYPE> {
    public default void onRegister(JavaPlugin plugin) {
        this.getRegistry().registerSystem(plugin, this);
    }
}
