package dev.twunk.hytale.resource;

import com.hypixel.hytale.component.Resource;
import com.hypixel.hytale.component.ResourceType;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.WorldProvider;
import dev.twunk.hytale.codec.auto.Serializable;
import dev.twunk.hytale.codec.auto.Serialize;
import dev.twunk.hytale.event.composite.CompositeSystem;
import dev.twunk.hytale.interfaces.event.IOnUniverseTick;
import dev.twunk.hytale.interfaces.methods.IRegistry;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@Serializable
public final class WorldTickResource<ECS_TYPE extends WorldProvider> implements Resource<ECS_TYPE> {

    @Serialize
    private long worldTick = 0;

    public final long getTick() {
        return this.worldTick;
    }

    @Nonnull
    public final WorldTickResource<ECS_TYPE> clone() {
        var data = new WorldTickResource<ECS_TYPE>();
        data.worldTick = this.worldTick;
        return data;
    }

    public static class WorldTickRunner<ECS_TYPE extends WorldProvider>
        extends CompositeSystem<ECS_TYPE>
        implements IOnUniverseTick<ECS_TYPE>
    {

        private final ResourceType<ECS_TYPE, WorldTickResource<ECS_TYPE>> resourceType;

        @Nullable
        private UUID id = null;

        @SuppressWarnings({ "unchecked", "null" })
        protected WorldTickRunner(IRegistry<ECS_TYPE> registry) {
            super(registry);
            this.resourceType = registry.getResourceType(WorldTickResource.class);
        }

        @Override
        public void onUniverseTick(float dt, int storeIndex, Store<ECS_TYPE> store) {
            var resource = store.getResource(this.resourceType);
            if (this.id == null) {
                var world = store.getExternalData().getWorld();
                this.id = world.getWorldConfig().getUuid();
            }

            resource.worldTick++;
        }
    }
}
