package dev.twunk.lib.system;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import dev.twunk.hytale.LibHytale;
import dev.twunk.hytale.refs.AnyRef;
import dev.twunk.hytale.refs.BlockRef;
import dev.twunk.hytale.system.SubSystemOwner;
import dev.twunk.hytale.system.TickSubSystem;
import dev.twunk.hytale.utils.BlockUtils;
import dev.twunk.hytale.utils.ComponentUtils;
import dev.twunk.interfaces.component.IBlockTickComponent;
import dev.twunk.interfaces.methods.IRegistry;
import dev.twunk.interfaces.subsystem.ITickSystem;
import javax.annotation.Nonnull;

/**
 * A reusable system for ticking block components. Reusable by ME really, just
 * tbh its just the system that's auto-registered for tickable block components
 *
 * GOAL: Tick ALL block entities that have the provided component
 *
 * Marked as final since, this is really a one and done sorta deal, just clone
 * its src and edit if you need alterations, because, then its just not this
 * specific thing anymore.
 *
 * How to use:
 * - create a new instance `new TickableBlockComponent<YourComponent>(YourComponentType)`
 * - register the instance to your plugin
 */
public final class AutoBlockTickSystem<T extends IBlockTickComponent>
    extends SubSystemOwner<ChunkStore>
    implements ITickSystem<ChunkStore>
{

    private static final HytaleLogger logger = HytaleLogger.forEnclosingClass();

    private final @Nonnull ComponentType<ChunkStore, T> componentType;

    public AutoBlockTickSystem(final @Nonnull ComponentType<ChunkStore, T> componentType) {
        super(Query.and(componentType));
        this.componentType = componentType;

        this.appendSubSystem(TickSubSystem.newSubsystemFor(this));
    }

    public void onEntityTick(
        final float dt,
        final @Nonnull AnyRef<ChunkStore> ref,
        final @Nonnull CommandBuffer<ChunkStore> commandBuffer
    ) {
        // Get your component that implements IBlockTickComponent from the block
        final var component = ComponentUtils.get(ref, this.componentType);

        try {
            // and call the tick method you defined on your component
            component.onBlockTick(new BlockRef(ref), commandBuffer);
        } catch (Throwable e) {
            final var coords = BlockUtils.Coords.Global.get(ref);
            if (coords != null) {
                logger
                    .atSevere()
                    .log(String.format("ERROR: Failed to tick block at (%d, %d, %d)", coords.x, coords.y, coords.z));
            } else {
                logger
                    .atSevere()
                    .log(
                        String.format(
                            "ERROR: Failed to tick block (and failed to get coords) | ref: " +
                                ref +
                                " | component: " +
                                component
                        )
                    );
            }
            return;
        }
    }

    @Override
    public IRegistry<ChunkStore> getRegistry() {
        return LibHytale.CHUNK_REGISTRY;
    }
}
