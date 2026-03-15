package dev.twunk.common;

import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import javax.annotation.Nonnull;

public interface IQuery {
    @Nonnull
    public Query<ChunkStore> getQuery();
}
