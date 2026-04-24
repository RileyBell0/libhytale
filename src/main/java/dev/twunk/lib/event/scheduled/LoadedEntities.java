package dev.twunk.lib.event.scheduled;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.server.core.universe.world.WorldProvider;
import dev.twunk.hytale.ref.TrackedRef;
import dev.twunk.lib.component.TickSchedule;
import dev.twunk.lib.registry.RegistryProvider;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

// TODO make a heap kinda thing with constant time access by assigning
// and freeing IDs for for entities. why? i forget, knew i needed something
// like that ages ago

// my guy that's responsible for keeping track of entity states, well,
// more so just responsible for keeping the ticking ones in a nice easy
// to loop bucket
//
// OH and responsible for finding the block info when they're loaded (coords,
// the world and chunk its in, etc)
public class LoadedEntities<ECS_TYPE extends WorldProvider> {

    // A unique and STABLE identifier for the system. you cannot change this.
    // once you decide on an ID your players REQUIRE it to be stable (or everything
    // in their worlds will break)
    //
    // This is what is used for entities to "remember" if they're sleeping, ticking etc
    // in your system when they're loaded back in.
    //
    // hence, if you change this ID all entities will still store but forever forget
    // what state they held
    private final String id;

    private final ComponentType<ECS_TYPE, TickSchedule<ECS_TYPE>> componentType;

    public LoadedEntities(String id, RegistryProvider<ECS_TYPE> registry) {
        this.id = id;

        @SuppressWarnings({ "null", "unchecked" })
        @Nonnull
        ComponentType<ECS_TYPE, TickSchedule<ECS_TYPE>> compType = registry.getComponentType(TickSchedule.class);

        this.componentType = compType;
    }

    // \/ \/ \/ \/ \/ \/ \/ \/ \/ \/ \/
    // Non-static implementation begins
    // \/ \/ \/ \/ \/ \/ \/ \/ \/ \/ \/

    public final List<TrackedRef<ECS_TYPE>> ticking = new ArrayList<>();
    private final List<TrackedRef<ECS_TYPE>> sleeping = new ArrayList<>();
    private final List<TrackedRef<ECS_TYPE>> comatose = new ArrayList<>();
    private final List<TrackedRef<ECS_TYPE>> stopped = new ArrayList<>();
    private final List<TrackedRef<ECS_TYPE>> broken = new ArrayList<>();

    public void track(final Ref<ECS_TYPE> ref, final CommandBuffer<ECS_TYPE> commandBuffer) {
        // figure out the current/initial ticking state our entity has
        final var tickingInfo = this.loadEntityTickingState(ref, commandBuffer);
        final var initialState = tickingInfo.getTickingInfo(this.id);

        // Figure out which tick group we should put our entity in based on its
        // state
        final var area = this.getOwner(initialState);

        final var trackedRef = new TrackedRef<ECS_TYPE>(ref, area);

        // we'll put chuck our cache into the right ticking group (ready to go)
        area.add(trackedRef);

        // and finally, we'll write down the area that we put said cache
        // so our component (found by ref) can remove itself easily
        // when it gets removed
        tickingInfo._setMemoryLocation(this.id, trackedRef);
    }

    public void untrack(final Ref<ECS_TYPE> ref, final RemoveReason reason) {
        ref.getStore().getComponent(ref, this.componentType).drop(this.id, reason);
    }

    private TickSchedule<ECS_TYPE> loadEntityTickingState(
        final Ref<ECS_TYPE> ref,
        final CommandBuffer<ECS_TYPE> commandBuffer
    ) {
        // Setup a tickingInfo component to track the state of our entitiy
        // so it can resume ticking/sleeping/etc when the server reboots. really
        // we just want to store shit so the lifetime extends past (NOW), and
        // so we can QUICKLY remove the entity again later
        final var tickingInfo = commandBuffer.ensureAndGetComponent(ref, this.componentType);
        var systemState = tickingInfo.getTickingInfo(this.id);
        if (systemState == null) {
            systemState = new TickPlan.Active();
            tickingInfo.setTickingInfo(this.id, systemState);
        }

        return tickingInfo;
    }

    /**
     * Get a ref to the list in which this entity should be placed based on its
     * current ticking state (active, sleeping, stopped etc)
     * @return
     */
    private List<TrackedRef<ECS_TYPE>> getOwner(@Nullable TickPlan currentState) {
        // and finally, we'll store it in the right place
        if (currentState instanceof TickPlan.Active) {
            return ticking;
        } else if (currentState instanceof TickPlan.Sleeping state) {
            if (state.isIndefinite()) {
                return comatose;
            } else {
                return sleeping;
            }
        } else if (currentState instanceof TickPlan.Stopped) {
            return stopped;
        } else {
            return broken;
        }
    }
}
