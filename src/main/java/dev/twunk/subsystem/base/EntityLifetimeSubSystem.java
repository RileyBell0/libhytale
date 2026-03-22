package dev.twunk.subsystem.base;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.RefSystem;
import com.hypixel.hytale.server.core.universe.world.WorldProvider;
import dev.twunk.subsystem.ISubSystem;
import dev.twunk.subsystem.base.interfaces.IEntityLifetimeSystem;
import dev.twunk.subsystem.composite.interfaces.IRegistry;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

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
public abstract class EntityLifetimeSubSystem<ECS_STORE extends WorldProvider>
    extends RefSystem<ECS_STORE>
    implements ISubSystem<ECS_STORE>
{

    private final @Nonnull IEntityLifetimeSystem<ECS_STORE> parent;
    private final @Nullable Query<ECS_STORE> query;

    /**
     * Hytale expects a new "class" for each system you register. Thus, to have these composable modules
     * of subsystems, each one must secretly create a new class each and every time you call it
     */
    @SuppressWarnings("unchecked")
    @Nonnull
    public static <
        ECS_STORE extends WorldProvider,
        T extends EntityLifetimeSubSystem<ECS_STORE>
    > EntityLifetimeSubSystem<ECS_STORE> newSubsystemFor(@Nonnull final IEntityLifetimeSystem<ECS_STORE> parent) {
        return ISubSystem.__newSubSystem(EntityLifetimeSubSystem.class, IEntityLifetimeSystem.class, parent);
    }

    protected EntityLifetimeSubSystem(@Nonnull final IEntityLifetimeSystem<ECS_STORE> parent) {
        this.parent = parent;
        this.query = parent.getQuery();
    }

    @Override
    public void onEntityAdded(
        @Nonnull Ref<ECS_STORE> ref,
        @Nonnull AddReason reason,
        @Nonnull Store<ECS_STORE> store,
        @Nonnull CommandBuffer<ECS_STORE> commandBuffer
    ) {
        parent.onEntityAdded(ref, reason, store, commandBuffer);
    }

    @Override
    public void onEntityRemove(
        @Nonnull Ref<ECS_STORE> ref,
        @Nonnull RemoveReason reason,
        @Nonnull Store<ECS_STORE> store,
        @Nonnull CommandBuffer<ECS_STORE> commandBuffer
    ) {
        parent.onEntityRemove(ref, reason, store, commandBuffer);
    }

    @Override
    public Query<ECS_STORE> getQuery() {
        return this.query;
    }

    public IRegistry<ECS_STORE> getRegistry() {
        return this.parent.getRegistry();
    }
}
