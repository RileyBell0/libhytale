package dev.twunk.lib.system;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.RefSystem;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import dev.twunk.annotations.EventRunners;
import dev.twunk.hytale.refs.AnyRef;
import dev.twunk.hytale.system.LifetimeSubSystem;
import dev.twunk.hytale.system.SubSystemOwner;
import dev.twunk.hytale.utils.ComponentUtils;
import dev.twunk.interfaces.component.ILifetimeComponent;
import dev.twunk.interfaces.methods.ILifetime;

/**
 * A reusable system for running onEntityAdded and onEntityRemove functions on
 * block lifetime components. Reusable by ME really, just tbh its just the system
 * that's auto-registered for block lifetime components
 *
 * GOAL: run the events `onEntityAdded` and `onEntityRemove` defined on a given
 * component
 *
 * Marked as final since, this is really a one and done sorta deal, just clone
 * its src and edit if you need alterations, because, then its just not this
 * specific thing anymore.
 *
 * My code
 * @see ILifetime       - Methods for listening to entity add/remove events
 * @see LifetimeSubSystem      - The base subsystem that "runs" something with "IEntityLifetime"
 *
 * Hytale's code
 * @see RefSystem - Hytale's underlying system that provides the `onEntityAdded` and `onEntityRemove` events
 */
@EventRunners.Chunk(LifetimeSubSystem.class)
public class AutoBlockLifetimeSystem<T extends ILifetimeComponent<ChunkStore>>
    extends SubSystemOwner<ChunkStore>
    implements ILifetime<ChunkStore>
{

    private static final HytaleLogger logger = HytaleLogger.forEnclosingClass();

    private final ComponentType<ChunkStore, T> componentType;

    ///////////////////////////////////////////////////////////////////////////
    // \/======================\/-  Methods  -\/==========================\/ //
    ///////////////////////////////////////////////////////////////////////////

    public AutoBlockLifetimeSystem(ComponentType<ChunkStore, T> componentType) {
        super(Query.and(componentType));
        this.componentType = componentType;

        // TODO remove this when the subsystem attachement / events system is working
        // this.appendSubSystem(LifetimeSubSystem.newSubsystemFor(this, super.getQuery(), LibHytale.CHUNK_REGISTRY));
    }

    @Override
    public void onEntityAdded(
        final AnyRef<ChunkStore> ref,
        final AddReason reason,
        final CommandBuffer<ChunkStore> commandBuffer
    ) {
        // Since our query is based on your component, we KNOW it has to have your
        // component, so, we just, get it
        final var component = ComponentUtils.get(ref, this.componentType);
        if (component == null) {
            logger.atSevere().log(String.format("ERROR: Failed to run onEntityAdded - " + ref));
            return;
        }

        component.onEntityAdded(ref, reason, commandBuffer);
    }

    @Override
    public void onEntityRemove(
        final AnyRef<ChunkStore> ref,
        final RemoveReason reason,
        final CommandBuffer<ChunkStore> commandBuffer
    ) {
        // Since our query is based on your component, we KNOW it has to have your
        // component, so, we just, get it
        final var component = ComponentUtils.get(ref, this.componentType);
        if (component == null) {
            logger.atSevere().log(String.format("ERROR: Failed to run onEntityRemove - " + ref));
            return;
        }

        component.onEntityRemove(ref, reason, commandBuffer);
    }
}
