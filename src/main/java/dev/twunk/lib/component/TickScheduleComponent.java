package dev.twunk.lib.component;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.server.core.universe.world.WorldProvider;
import dev.twunk.hytale.codec.auto.Serializable;
import dev.twunk.hytale.codec.auto.Serialize;
import dev.twunk.lib.event.scheduled.TickSchedule;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

@Serializable
public final class TickScheduleComponent<ECS_TYPE extends WorldProvider> implements Component<ECS_TYPE> {

    public TickScheduleComponent() {
        this.allSchedules = new HashMap<>();
    }

    public TickScheduleComponent(Map<String, TickSchedule> allSchedules) {
        this.allSchedules = allSchedules;
    }

    // Stores ALL schedules for ALL scheduled tick systems that query this entity.
    // means I only need to generate 1x component for each scheduled tick system.
    // Also means that this component could get huge if someone does something dumb,
    // but that's true regardless of my approach so that means we're good to go using this.
    @Serialize
    public Map<String, TickSchedule> allSchedules;

    @Nullable
    public TickSchedule getSchedule(String systemId) {
        return this.allSchedules.get(systemId);
    }

    public void setSchedule(String systemId, TickSchedule schedule) {
        this.allSchedules.put(systemId, schedule);
    }

    @SuppressWarnings("MethodDoesntCallSuperMethod")
    public TickScheduleComponent<ECS_TYPE> clone() {
        return new TickScheduleComponent<>(allSchedules);
    }
}
