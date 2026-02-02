package dev.twunk.utils;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.server.core.modules.time.WorldTimeResource;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import java.time.Instant;

public abstract class GameTime {

    public static Instant get(CommandBuffer<ChunkStore> commandBuffer) {

        return commandBuffer.getExternalData().getWorld().getEntityStore().getStore()
                .getResource(WorldTimeResource.getResourceType()).getGameTime();
    }
}
