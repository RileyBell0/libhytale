package dev.twunk.interfaces.methods;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.WorldProvider;
import javax.annotation.Nonnull;

public interface IEntityLifetime<ECS_STORE extends WorldProvider> {
    public void onEntityAdded(
        @Nonnull Ref<ECS_STORE> ref,
        @Nonnull AddReason reason,
        @Nonnull Store<ECS_STORE> store,
        @Nonnull CommandBuffer<ECS_STORE> commandBuffer
    );

    public void onEntityRemove(
        @Nonnull Ref<ECS_STORE> ref,
        @Nonnull RemoveReason reason,
        @Nonnull Store<ECS_STORE> store,
        @Nonnull CommandBuffer<ECS_STORE> commandBuffer
    );
}
