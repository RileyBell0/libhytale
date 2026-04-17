package dev.twunk.lib.system;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import dev.twunk.hytale.LibHytale;
import dev.twunk.hytale.refs.AnyRef;
import dev.twunk.hytale.refs.BlockRef;
import dev.twunk.hytale.system.OnTickSystem;
import dev.twunk.hytale.system.SubSystemOwner;
import dev.twunk.hytale.utils.BlockUtils;
import dev.twunk.hytale.utils.ComponentUtils;
import dev.twunk.interfaces.methods.IOnBlockTick;
import dev.twunk.interfaces.methods.IOnTick;
import dev.twunk.interfaces.methods.IRegistry;

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
public final class AutoBlockTickSystem<T extends Component<ChunkStore>>
    extends SubSystemOwner<ChunkStore>
    implements IOnTick<ChunkStore>
{

    private static final HytaleLogger logger = HytaleLogger.forEnclosingClass();

    private final ComponentType<ChunkStore, T> componentType;

    ///////////////////////////////////////////////////////////////////////////
    // \/======================\/-  Methods  -\/==========================\/ //
    ///////////////////////////////////////////////////////////////////////////

    public AutoBlockTickSystem(ComponentType<ChunkStore, T> componentType) {
        super(Query.and(componentType));
        this.componentType = componentType;

        this.appendSubSystem(OnTickSystem.constructNewSystemClass(this, Query.and(componentType), this.getRegistry()));
    }

    public void onEntityTick(
        final float dt,
        final AnyRef<ChunkStore> ref,
        final CommandBuffer<ChunkStore> commandBuffer
    ) {
        // Get your component that implements IBlockTickComponent from the block
        final var component = ComponentUtils.get(ref, this.componentType);

        if (component == null) {
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

        // and call the tick method you defined on your component
        ((IOnBlockTick) component).onBlockTick(new BlockRef(ref), commandBuffer);
    }

    public IRegistry<ChunkStore> getRegistry() {
        return LibHytale.CHUNK_REGISTRY;
    }
}
