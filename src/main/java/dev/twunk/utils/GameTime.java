package dev.twunk.utils;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.server.core.modules.time.WorldTimeResource;
import com.hypixel.hytale.server.core.universe.world.WorldProvider;
import java.time.Instant;
import javax.annotation.Nonnull;

public abstract class GameTime {

    /**
     * Get the current game time. Not sure if this can fail, pretty sure it can't, but
     * their endpoint seems to be nullable,
     */
    public static <ECS_STORE extends WorldProvider> Instant get(final @Nonnull CommandBuffer<ECS_STORE> commandBuffer) {
        return commandBuffer
            .getExternalData()
            .getWorld()
            .getEntityStore()
            .getStore()
            .getResource(WorldTimeResource.getResourceType())
            .getGameTime();
    }
}
