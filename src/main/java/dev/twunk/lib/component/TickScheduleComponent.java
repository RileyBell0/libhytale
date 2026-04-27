package dev.twunk.lib.component;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.server.core.universe.world.WorldProvider;
import dev.twunk.hytale.codec.auto.Serializable;
import dev.twunk.hytale.codec.auto.Serialize;
import dev.twunk.lib.event.scheduled.TickSchedule;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;

@Serializable
public final class TickScheduleComponent<ECS_TYPE extends WorldProvider> implements Component<ECS_TYPE> {

    // Stores ALL schedules for ALL scheduled tick systems that query this entity.
    // means i only need to generate 1x component for each scheduled tick system
    // and also means that this component could get huge if someone does something dumb,
    // but thats true regardless of my approach so that means we're good to go using this.
    @Serialize
    private final Map<String, TickSchedule> allSchedules = new HashMap<>();

    @Nullable
    public TickSchedule getSchedule(String systemId) {
        return this.allSchedules.get(systemId);
    }

    public void setSchedule(String systemId, TickSchedule schedule) {
        this.allSchedules.put(systemId, schedule);
    }

    public TickScheduleComponent<ECS_TYPE> clone() {
        return new TickScheduleComponent<>();
    }
}
