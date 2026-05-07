package dev.twunk.lib.event.scheduled;

import java.util.UUID;
import javax.annotation.Nullable;

public record SleepingEntity(UUID uuid, long nextTick) implements Comparable<SleepingEntity> {


    @SuppressWarnings("null")
    @Override
    public int compareTo(@Nullable SleepingEntity o) {
        return Long.compare(this.nextTick, o != null ? o.nextTick : 0);
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
