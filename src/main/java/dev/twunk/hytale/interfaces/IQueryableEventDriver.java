package dev.twunk.hytale.interfaces;

import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.universe.world.WorldProvider;
import dev.twunk.hytale.interfaces.config.IQuery;

public interface IQueryableEventDriver<
    ECS_TYPE extends WorldProvider
> extends IEventDriver<ECS_TYPE>, IQuery<ECS_TYPE> {
    public default void onRegister(JavaPlugin plugin) {
        this.getRegistry().registerEventListeners(plugin, this);
    }
}
