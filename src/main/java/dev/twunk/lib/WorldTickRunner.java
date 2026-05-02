package dev.twunk.lib;

import com.hypixel.hytale.component.ResourceType;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import dev.twunk.hytale.LibHytale;
import dev.twunk.hytale.interfaces.IRegistryProvider;
import dev.twunk.hytale.interfaces.event.IOnUniverseTick;
import dev.twunk.hytale.interfaces.methods.IRegistry;
import dev.twunk.hytale.resource.CurrentWorldTick;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nullable;

public class WorldTickRunner implements IOnUniverseTick<ChunkStore>, IRegistryProvider<ChunkStore> {

    protected static final Map<UUID, CurrentWorldTick> worldTimeMap = new HashMap<>();

    @SuppressWarnings("null")
    private final ResourceType<ChunkStore, CurrentWorldTick> resourceType = LibHytale.CHUNK_REGISTRY.getResourceType(
        CurrentWorldTick.class
    );

    @Nullable
    private UUID id = null;

    @Nullable
    private CurrentWorldTick resource;

    @Override
    public final IRegistry<ChunkStore> getRegistry() {
        return LibHytale.CHUNK_REGISTRY;
    }

    @Override
    @SuppressWarnings("null")
    public void onUniverseTick(float dt, int storeIndex, Store<ChunkStore> store) {
        if (this.id == null) {
            var world = store.getExternalData().getWorld();
            this.id = world.getWorldConfig().getUuid();
        }
        if (this.resource == null) {
            this.resource = store.getResource(this.resourceType);
            WorldTickRunner.worldTimeMap.put(this.id, this.resource);
        }

        this.resource.worldTick++;
    }
}
