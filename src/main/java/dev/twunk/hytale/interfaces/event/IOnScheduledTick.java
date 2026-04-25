package dev.twunk.hytale.interfaces.event;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.server.core.universe.world.WorldProvider;
import dev.twunk.hytale.ref.AnyRef;
import dev.twunk.lib.event.scheduled.TickPlan;
import javax.annotation.Nullable;

/**
 * TODO really not working that well atm, needs to be cleaned up BUT it worked
 * well in concept and practice. Just, yeah, needs to be cleaned up and extended to support
 * more advanced ticking ideas like "every x ticks" or "every x seconds" etc etc
 *
 * Gives your system the event handler function it needs to run code for every entity
 * that matches your system, AND for that entity to be able to sleep etc (and remember
 * if it was awake/sleeping etc when it loads back in)
 *
 * When you want your system to benefit from ScheduledTickSubSystem
 * - implement IScheduledTickSystem on your system
 * - extend SubSystemOwner (or look into its code to see what it does and dupe that)
 * - call `this.appendSubSystem`, passing in the sub system(s) IN THE ORDER you want them to run
 */
public interface IOnScheduledTick<ECS_TYPE extends WorldProvider> {
    @Nullable
    public abstract TickPlan onScheduledTick(float dt, AnyRef<ECS_TYPE> ref, CommandBuffer<ECS_TYPE> commandBuffer);
}
