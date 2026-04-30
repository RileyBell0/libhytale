package dev.twunk.lib;

import com.hypixel.hytale.component.Resource;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import dev.twunk.hytale.codec.auto.Serializable;
import dev.twunk.hytale.codec.auto.Serialize;
import javax.annotation.Nonnull;

@Serializable
public final class WorldTickResource implements Resource<ChunkStore> {

    @Serialize
    public long worldTick = 0;

    @Nonnull
    public final Resource<ChunkStore> clone() {
        WorldTickResource data = new WorldTickResource();
        data.worldTick = this.worldTick;
        return data;
    }
}
