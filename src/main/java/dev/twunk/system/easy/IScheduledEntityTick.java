package dev.twunk.system.easy;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import dev.twunk.system.response.TickResponse;
import dev.twunk.system.smart.TickingEntityMetadata;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface IScheduledEntityTick {
    @Nullable
    public abstract TickResponse onEntityTick(
        @Nonnull TickingEntityMetadata state,
        float dt,
        @Nonnull Store<ChunkStore> store,
        @Nonnull CommandBuffer<ChunkStore> commandBuffer
    );
}
