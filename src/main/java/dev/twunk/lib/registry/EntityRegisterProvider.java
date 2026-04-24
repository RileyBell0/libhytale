package dev.twunk.lib.registry;

import com.hypixel.hytale.component.ComponentRegistryProxy;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.twunk.hytale.interfaces.methods.IRegistry;

public final class EntityRegisterProvider
    extends ComponentRegistryHelper<EntityStore>
    implements IRegistry<EntityStore>
{

    @Override
    public ComponentRegistryProxy<EntityStore> getStoreRegistry(JavaPlugin plugin) {
        return plugin.getEntityStoreRegistry();
    }
}
