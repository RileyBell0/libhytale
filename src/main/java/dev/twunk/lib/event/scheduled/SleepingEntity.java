package dev.twunk.lib.event.scheduled;

import dev.twunk.lib.event.scheduled.TickSchedule.Sleeping;
import java.util.UUID;
import javax.annotation.Nullable;

public sealed class SleepingEntity implements Comparable<SleepingEntity> {

    public final UUID uuid;
    public final long nextTick;

    public SleepingEntity(UUID uuid, TickSchedule.Sleeping schedule) {
        this.uuid = uuid;
        this.nextTick = schedule.nextTick;
    }

    @SuppressWarnings("null")
    @Override
    public int compareTo(@Nullable SleepingEntity o) {
        return Long.compare(this.nextTick, o.nextTick);
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (o instanceof SleepingEntity s) {
            return s.nextTick == this.nextTick;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return (int) nextTick;
    }

    public static final class SleepingBlockEntity extends SleepingEntity {

        public final long chunkCoords;
        public final int localCoords;

        public SleepingBlockEntity(UUID uuid, Sleeping schedule, long chunkIndex, int blockIndex) {
            this.chunkCoords = chunkIndex;
            this.localCoords = blockIndex;

            super(uuid, schedule);
        }
    }
}
