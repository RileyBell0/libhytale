package dev.twunk.lib.registry;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentRegistryProxy;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.twunk.hytale.interfaces.methods.IRegistry;
import java.util.HashMap;
import java.util.Map;

public final class EntityRegisterProvider implements IRegistry<EntityStore> {

    protected static final Map<
        Class<? extends Component<EntityStore>>,
        ComponentType<EntityStore, ? extends Component<EntityStore>>
    > ENTITY_COMPONENTS = new HashMap<>();

    protected static final Map<
        String,
        ComponentType<EntityStore, ? extends Component<EntityStore>>
    > ENTITY_COMPONENTS_BY_ID = new HashMap<>();

    public final Map<
        Class<? extends Component<EntityStore>>,
        ComponentType<EntityStore, ? extends Component<EntityStore>>
    > getComponentMap() {
        return ENTITY_COMPONENTS;
    }

    public final Map<String, ComponentType<EntityStore, ? extends Component<EntityStore>>> getComponentByIdMap() {
        return ENTITY_COMPONENTS_BY_ID;
    }

    // ////////////////////////////////////////////////////////////////////////
    // \/======================\/-  Methods  -\/==========================\/ //
    // ////////////////////////////////////////////////////////////////////////

    @Override
    public ComponentRegistryProxy<EntityStore> getStoreRegistry(JavaPlugin plugin) {
        return plugin.getEntityStoreRegistry();
    }
}
