package dev.twunk.ticking.response;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import javax.annotation.Nonnull;

public interface TickResponse extends Component<ChunkStore> {

    @Nonnull
    public static final TickResponse CONTINUE = (TickResponse) new TickContinue();
    @Nonnull
    public static final TickResponse SLEEP = (TickResponse) new TickSleep();
    @Nonnull
    public static final TickResponse STOP = (TickResponse) new TickStop();

    @Nonnull
    public abstract ComponentType<ChunkStore, ? extends Component<ChunkStore>> getComponentType();

    @Nonnull
    public abstract TickResponse clone();
}
