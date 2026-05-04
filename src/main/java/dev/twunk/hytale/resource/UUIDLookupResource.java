package dev.twunk.hytale.resource;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Resource;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.universe.world.WorldProvider;
import dev.twunk.hytale.codec.auto.Serializable;
import dev.twunk.hytale.component.UUIDComponent;
import dev.twunk.hytale.event.composite.QueryableCompositeSystem;
import dev.twunk.hytale.interfaces.event.IOnAddRemove;
import dev.twunk.hytale.interfaces.methods.IRegistry;
import dev.twunk.hytale.ref.AnyRef;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * In memory only: nothing stored
 */
@Serializable
public final class UUIDLookupResource<ECS_TYPE extends WorldProvider> implements Resource<ECS_TYPE> {

    private final Map<UUID, AnyRef<ECS_TYPE>> entitiesByUuid = new HashMap<>();

    @Nullable
    public final AnyRef<ECS_TYPE> getRefByUUID(UUID uuid) {
        return this.entitiesByUuid.get(uuid);
    }

    @Nonnull
    public final Resource<ECS_TYPE> clone() {
        return this;
    }

    /**
     * system that tracks entities in BOTH stores by their UUID (my uuid component - hytale's one is locked to entity stores)
     */
    public static class TrackEntitiesByUUIDSystem<ECS_TYPE extends WorldProvider>
        extends QueryableCompositeSystem<ECS_TYPE>
        implements IOnAddRemove<ECS_TYPE>
    {

        private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

        private final ComponentType<ECS_TYPE, UUIDComponent<ECS_TYPE>> uuidComponentType;

        public TrackEntitiesByUUIDSystem(IRegistry<ECS_TYPE> registry) {
            @SuppressWarnings({ "null", "unchecked" })
            @Nonnull
            var componentType = registry.getComponentType(
                (Class<UUIDComponent<ECS_TYPE>>) (Class<?>) UUIDComponent.class
            );
            super(registry, Query.and(componentType));

            this.uuidComponentType = componentType;
        }

        @Override
        @SuppressWarnings("null")
        public void onAdd(AnyRef<ECS_TYPE> ref, AddReason reason, CommandBuffer<ECS_TYPE> commandBuffer) {
            @SuppressWarnings("unchecked")
            final var resource = commandBuffer.getResource(this.registry.getResourceType(UUIDLookupResource.class));

            final UUIDComponent<ECS_TYPE> uuidComponent = commandBuffer.ensureAndGetComponent(
                ref,
                this.uuidComponentType
            );
            assert uuidComponent != null;

            @SuppressWarnings("unchecked")
            final var currentRef = resource.entitiesByUuid.putIfAbsent(uuidComponent.getUuid(), ref);
            if (currentRef != null) {
                LOGGER.atWarning().log("Removing duplicate entity with UUID: %s", uuidComponent.getUuid());
                commandBuffer.removeEntity(ref, RemoveReason.REMOVE);
            }
        }

        @Override
        @SuppressWarnings("null")
        public void onRemove(AnyRef<ECS_TYPE> ref, RemoveReason reason, CommandBuffer<ECS_TYPE> commandBuffer) {
            @SuppressWarnings("unchecked")
            final var resource = commandBuffer.getResource(this.registry.getResourceType(UUIDLookupResource.class));

            UUIDComponent<ECS_TYPE> uuidComponent = commandBuffer.getComponent(ref, this.uuidComponentType);
            assert uuidComponent != null;

            resource.entitiesByUuid.remove(uuidComponent.getUuid(), ref);
        }
    }
}
