package dev.twunk.ticking.response;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import javax.annotation.Nonnull;

public interface TickResponse extends Component<ChunkStore> {

    public static final String TYPE_CONTINUE = "continue";
    public static final String TYPE_SLEEP = "sleep";
    public static final String TYPE_STOP = "stop";
    public static final String TYPE_BROKEN = "broken";

    @Nonnull
    public String getType();

    @Nonnull
    public static final TickResponse CONTINUE = (TickResponse) new TickContinue();
    @Nonnull
    public static final TickResponse SLEEP = (TickResponse) new TickSleep();
    @Nonnull
    public static final TickResponse STOP = (TickResponse) new TickStop();
    @Nonnull
    public static final TickResponse BROKEN = (TickResponse) new TickBroken();

    @Nonnull
    public abstract ComponentType<ChunkStore, ? extends Component<ChunkStore>> getComponentType();

    @Nonnull
    public abstract TickResponse clone();
}
