package dev.twunk.hytale.resource;

import com.hypixel.hytale.component.Resource;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import dev.twunk.hytale.codec.auto.Serializable;
import dev.twunk.hytale.codec.auto.Serialize;
import javax.annotation.Nonnull;

@Serializable
public final class CurrentWorldTick implements Resource<ChunkStore> {

    @Serialize
    public long worldTick = 0;

    @Nonnull
    public final Resource<ChunkStore> clone() {
        CurrentWorldTick data = new CurrentWorldTick();
        data.worldTick = this.worldTick;
        return data;
    }
}
