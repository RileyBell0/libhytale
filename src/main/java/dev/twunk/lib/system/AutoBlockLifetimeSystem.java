package dev.twunk.lib.system;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.RefSystem;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import dev.twunk.hytale.LibHytale;
import dev.twunk.hytale.system.LifetimeSubSystem;
import dev.twunk.hytale.system.SubSystemOwner;
import dev.twunk.hytale.utils.ComponentUtils;
import dev.twunk.interfaces.IRegistry;
import dev.twunk.interfaces.component.ILifetimeComponent;
import dev.twunk.interfaces.methods.ILifetime;
import dev.twunk.interfaces.subsystem.ILifetimeSystem;
import java.util.function.Supplier;
import javax.annotation.Nonnull;

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
 * @see ILifetimeSystem - Additional requirements that an implementor of IEntityLifetime must satisfy
 *                              in order to register a subsystem to run itself
 * @see LifetimeSubSystem      - The base subsystem that "runs" something with "IEntityLifetime"
 *
 * Hytale's code
 * @see RefSystem - Hytale's underlying system that provides the `onEntityAdded` and `onEntityRemove` events
 */
public class AutoBlockLifetimeSystem<T extends ILifetimeComponent<ChunkStore>>
    extends SubSystemOwner<ChunkStore>
    implements ILifetimeSystem<ChunkStore>
{

    private static HytaleLogger.Api console = HytaleLogger.forEnclosingClass().atInfo();

    private final @Nonnull ComponentType<ChunkStore, T> componentType;

    public AutoBlockLifetimeSystem(final @Nonnull Supplier<ComponentType<ChunkStore, T>> supplier) {
        super(Query.and(supplier.get()));
        final var component = supplier.get();
        if (component == null) {
            throw new RuntimeException("Failed to get component type for Component Ticking System | " + supplier);
        }
        this.componentType = component;

        this.appendSubSystem(LifetimeSubSystem.newSubsystemFor(this));
    }

    public AutoBlockLifetimeSystem(@Nonnull Class<T> componentClass) {
        super(Query.and(LibHytale.getChunkComponentType(componentClass)));
        this.componentType = LibHytale.getChunkComponentType(componentClass);

        this.appendSubSystem(LifetimeSubSystem.newSubsystemFor(this));
    }

    public AutoBlockLifetimeSystem(@Nonnull ComponentType<ChunkStore, T> componentType) {
        super(Query.and(componentType));
        this.componentType = componentType;

        this.appendSubSystem(LifetimeSubSystem.newSubsystemFor(this));
    }

    @Override
    public void onEntityAdded(
        final @Nonnull Ref<ChunkStore> ref,
        final @Nonnull AddReason reason,
        final @Nonnull Store<ChunkStore> store,
        final @Nonnull CommandBuffer<ChunkStore> commandBuffer
    ) {
        // Since our query is based on your component, we KNOW it has to have your
        // component, so, we just, get it
        final var component = ComponentUtils.get(ref, this.componentType);
        try {
            // and call the tick method you defined on your component, which,
            // i know is sort of heresy for ECS systems, but, it makes doing
            // easy things easy. and i'm all for that
            component.onEntityAdded(ref, reason, store, commandBuffer);
        } catch (Throwable e) {
            console.log(String.format("ERROR: Failed to run onEntityAdded - " + e));
            return;
        }
    }

    @Override
    public void onEntityRemove(
        final @Nonnull Ref<ChunkStore> ref,
        final @Nonnull RemoveReason reason,
        final @Nonnull Store<ChunkStore> store,
        final @Nonnull CommandBuffer<ChunkStore> commandBuffer
    ) {
        // Since our query is based on your component, we KNOW it has to have your
        // component, so, we just, get it
        final var component = ComponentUtils.get(ref, this.componentType);
        try {
            // and call the tick method you defined on your component, which,
            // i know is sort of heresy for ECS systems, but, it makes doing
            // easy things easy. and i'm all for that
            component.onEntityRemove(ref, reason, store, commandBuffer);
        } catch (Throwable e) {
            console.log(String.format("ERROR: Failed to run onEntityRemove - " + e));
            return;
        }
    }

    @Override
    public IRegistry<ChunkStore> getRegistry() {
        return LibHytale.CHUNK_REGISTRY;
    }
}
