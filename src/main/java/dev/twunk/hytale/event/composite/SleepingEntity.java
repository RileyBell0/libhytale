package dev.twunk.hytale.event.composite;

import dev.twunk.lib.event.scheduled.TickSchedule;
import java.util.UUID;
import javax.annotation.Nullable;

public sealed class SleepingEntity implements Comparable<SleepingEntity> permits SleepingEntity__Block {

    public final UUID uuid;
    public final long nextTick;

    protected SleepingEntity(UUID uuid, TickSchedule.Sleeping schedule) {
        this.uuid = uuid;
        this.nextTick = schedule.wakeUpAt;
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
}
