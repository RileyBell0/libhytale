package dev.twunk.hytale.interfaces;

import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.universe.world.WorldProvider;

public interface ISystemEventDriver<ECS_TYPE extends WorldProvider> extends IEventDriver<ECS_TYPE> {
    default void onRegister(JavaPlugin plugin) {
        this.getRegistry().getStoreRegistry(plugin).registerSystem(this);
        this.getRegistry().registerEventListeners(plugin, this);
    }
}
