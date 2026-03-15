package dev.twunk.system;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import dev.twunk.component.IBlockLifetimeComponent;
import dev.twunk.subsystem.SubSystemOwner;
import dev.twunk.subsystem.base.EntityLifetimeSubSystem;
import dev.twunk.subsystem.base.interfaces.IEntityLifetimeSystem;
import dev.twunk.utils.TwunkLib;
import dev.twunk.utils.world.Utils;
import java.util.function.Supplier;
import javax.annotation.Nonnull;

/**
 * Intended for re-use as is. Also intended for minimal usage (mainly testing etc)
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
public class BlockLifetimeComponentSystem<T extends IBlockLifetimeComponent>
    extends SubSystemOwner
    implements IEntityLifetimeSystem
{

    private static HytaleLogger.Api console = HytaleLogger.forEnclosingClass().atInfo();

    private final @Nonnull ComponentType<ChunkStore, T> componentType;

    public BlockLifetimeComponentSystem(@Nonnull Supplier<ComponentType<ChunkStore, T>> supplier) {
        super(Query.and(supplier.get()));
        var component = supplier.get();
        if (component == null) {
            throw new RuntimeException("Failed to get component type for Component Ticking System | " + supplier);
        }
        this.componentType = component;
        this.appendSubSystem(EntityLifetimeSubSystem.create(this));
    }

    public BlockLifetimeComponentSystem(@Nonnull Class<T> componentClass) {
        super(Query.and(TwunkLib.getComponentType(componentClass)));
        this.componentType = TwunkLib.getComponentType(componentClass);
        this.appendSubSystem(EntityLifetimeSubSystem.create(this));
    }

    public BlockLifetimeComponentSystem(@Nonnull ComponentType<ChunkStore, T> componentType) {
        super(Query.and(componentType));
        this.componentType = componentType;
        this.appendSubSystem(EntityLifetimeSubSystem.create(this));
    }

    @Override
    public void onEntityAdded(
        @Nonnull Ref<ChunkStore> ref,
        @Nonnull AddReason reason,
        @Nonnull Store<ChunkStore> store,
        @Nonnull CommandBuffer<ChunkStore> commandBuffer
    ) {
        // Since our query is based on your component, we KNOW it has to have your
        // component, so, we just, get it
        var component = Utils.Component_.getComponent(ref, this.componentType);
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
        @Nonnull Ref<ChunkStore> ref,
        @Nonnull RemoveReason reason,
        @Nonnull Store<ChunkStore> store,
        @Nonnull CommandBuffer<ChunkStore> commandBuffer
    ) {
        // Since our query is based on your component, we KNOW it has to have your
        // component, so, we just, get it
        var component = Utils.Component_.getComponent(ref, this.componentType);
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
}
