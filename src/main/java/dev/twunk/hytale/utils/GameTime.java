package dev.twunk.hytale.utils;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.server.core.modules.time.WorldTimeResource;
import com.hypixel.hytale.server.core.universe.world.WorldProvider;
import java.time.Instant;
import javax.annotation.Nullable;

public abstract class GameTime {

    /**
     * Get the current game time. Not sure if this can fail, pretty sure it can't, but
     * their endpoint seems to be nullable,
     */
    @Nullable
    public static <ECS_TYPE extends WorldProvider> Instant get(final CommandBuffer<ECS_TYPE> commandBuffer) {
        return commandBuffer
            .getExternalData()
            .getWorld()
            .getEntityStore()
            .getStore()
            .getResource(WorldTimeResource.getResourceType())
            .getGameTime();
    }
}
