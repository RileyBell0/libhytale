package dev.twunk.subsystem.base;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.RefSystem;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import dev.twunk.subsystem.ISubSystem;
import dev.twunk.subsystem.base.interfaces.IEntityLifetimeSystem;
import java.lang.invoke.MethodHandles;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.dynamic.scaffold.subclass.ConstructorStrategy;

/**
 * Tiny Subsystem to simply tell our parent system when we added/removed entities
 * that match our parent's query
 *
 * GOAL: Need to know when entities load/unload (and optionally why they got added/removed)
 *
 * REQUIRES:
 * - N/A (this is a leaf)
 * PRODUCES:
 * - ILifetimeSystem runner
 */
public abstract class EntityLifetimeSubSystem extends RefSystem<ChunkStore> implements ISubSystem {

    private final @Nonnull IEntityLifetimeSystem parent;
    private final @Nullable Query<ChunkStore> query;

    public static <T extends EntityLifetimeSubSystem> EntityLifetimeSubSystem create(
        @Nonnull final IEntityLifetimeSystem parent
    ) {
        final Class<? extends EntityLifetimeSubSystem> clazz = new ByteBuddy()
            .subclass(EntityLifetimeSubSystem.class, ConstructorStrategy.Default.IMITATE_SUPER_CLASS)
            .make()
            .load(
                EntityLifetimeSubSystem.class.getClassLoader(),
                ClassLoadingStrategy.UsingLookup.of(MethodHandles.lookup())
            )
            .getLoaded();

        try {
            var constructor = clazz.getDeclaredConstructor(IEntityLifetimeSystem.class);
            return constructor.newInstance(parent);
        } catch (Exception e) {
            throw new RuntimeException(
                "RILEY, you called a constructor that doesnt exist for " +
                    EntityLifetimeSubSystem.class +
                    " | " +
                    parent +
                    " | " +
                    e
            );
        }
    }

    EntityLifetimeSubSystem(@Nonnull final IEntityLifetimeSystem parent) {
        this.parent = parent;
        this.query = parent.getQuery();
    }

    @Override
    public void onEntityAdded(
        @Nonnull Ref<ChunkStore> ref,
        @Nonnull AddReason reason,
        @Nonnull Store<ChunkStore> store,
        @Nonnull CommandBuffer<ChunkStore> commandBuffer
    ) {
        parent.onEntityAdded(ref, reason, store, commandBuffer);
    }

    @Override
    public void onEntityRemove(
        @Nonnull Ref<ChunkStore> ref,
        @Nonnull RemoveReason reason,
        @Nonnull Store<ChunkStore> store,
        @Nonnull CommandBuffer<ChunkStore> commandBuffer
    ) {
        parent.onEntityRemove(ref, reason, store, commandBuffer);
    }

    @Override
    public Query<ChunkStore> getQuery() {
        return this.query;
    }
}
