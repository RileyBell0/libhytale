package dev.twunk.hytale.interfaces.event;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.server.core.universe.world.WorldProvider;
import dev.twunk.hytale.interfaces.config.IEventConfig;
import dev.twunk.hytale.interfaces.config.IQuery;
import dev.twunk.hytale.ref.AnyRef;
import dev.twunk.lib.event.scheduled.TickSchedule;

import javax.annotation.Nullable;

/// Gives your system the event handler function it needs to run code for every entity
/// that matches your system, AND for that entity to be able to sleep etc. (and remember
/// if it was awake/sleeping etc. when it loads back in)
///
/// When you want your system to benefit from ScheduledTickSubSystem
/// - implement IScheduledTickSystem on your system
/// - extend SubSystemOwner (or look into its code to see what it does and dupe that)
/// - call `this.appendSubSystem`, passing in the sub system(s) IN THE ORDER you want them to run
public interface IOnScheduledTick<ECS_TYPE extends WorldProvider> extends IEventConfig<ECS_TYPE>, IQuery<ECS_TYPE> {
    @Nullable
    TickSchedule onScheduledTick(
            float dt,
            long worldTick,
            AnyRef<ECS_TYPE> ref,
            CommandBuffer<ECS_TYPE> commandBuffer
    );
}
