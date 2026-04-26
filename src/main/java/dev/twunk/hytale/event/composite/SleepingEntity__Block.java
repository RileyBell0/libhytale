package dev.twunk.hytale.event.composite;

import dev.twunk.lib.event.scheduled.TickSchedule.Sleeping;
import java.util.UUID;
import javax.annotation.Nullable;

public final class SleepingEntity__Block extends SleepingEntity {

    public final long chunkCoords;
    public final int localCoords;

    public SleepingEntity__Block(UUID uuid, Sleeping schedule, long chunkIndex, int blockIndex) {
        this.chunkCoords = chunkIndex;
        this.localCoords = blockIndex;

        super(uuid, schedule);
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (o instanceof SleepingEntity__Block s) {
            return s.nextTick == this.nextTick;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return (int) nextTick;
    }
}
