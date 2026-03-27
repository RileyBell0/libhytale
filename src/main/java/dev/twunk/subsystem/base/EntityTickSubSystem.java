package dev.twunk.subsystem.base;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.ArchetypeTickingSystem;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.universe.world.WorldProvider;
import dev.twunk.interfaces.methods.IEntityTick;
import dev.twunk.subsystem.ISubSystem;
import dev.twunk.subsystem.base.interfaces.IEntityTickSystem;
import dev.twunk.subsystem.composite.interfaces.IRegistry;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Subsystem for calling `onEntityTick` on the parent system every tick
 *
 * GOAL: run code on entities every tick
 *
 * REQUIRES:
 * - N/A (this is a leaf)
 * PRODUCES:
 * - IEntityTickSystem runner
 *
 *
 * My code
 * @see IEntityTickSystem - Something that can be ticked by EntityTickSubSystem
 *                          (satisfies IEntityTick)
 * @see IEntityTick       - Underlying method for ticking an entity
 *
 * Hytale's code
 * @see EntityTickingSystem    - Baseline hytale system for ticking entities.
 *                               It's the underlying driver of IEntityTickSubSystem
 * @see ArchetypeTickingSystem - Underlying sort of baseline ticking system (that i know how to implement).
 *                               Runs ONCE per tick (global, not per matching entity, just runs a single
 *                               time per tick) and has an inbuilt query
 */
public class EntityTickSubSystem<ECS_STORE extends WorldProvider>
    extends EntityTickingSystem<ECS_STORE>
    implements ISubSystem<ECS_STORE>
{

    private final @Nonnull IEntityTickSystem<ECS_STORE> parent;
    private final @Nullable Query<ECS_STORE> query;

    /**
     * Hytale expects a new "class" for each system you register. Thus, to have these composable modules
     * of subsystems, each one must secretly create a new class each and every time you call it
     */
    @SuppressWarnings("unchecked")
    @Nonnull
    public static <ECS_STORE extends WorldProvider, T extends EntityTickSubSystem<ECS_STORE>> EntityTickSubSystem<
        ECS_STORE
    > newSubsystemFor(final @Nonnull IEntityTickSystem<ECS_STORE> parent) {
        return ISubSystem.__newSubSystem(EntityTickSubSystem.class, IEntityTickSystem.class, parent);
    }

    protected EntityTickSubSystem(@Nonnull IEntityTickSystem<ECS_STORE> parent) {
        this.parent = parent;
        this.query = parent.getQuery();
    }

    public void tick(
        final float dt,
        final int index,
        final @Nonnull ArchetypeChunk<ECS_STORE> archetypeChunk,
        final @Nonnull Store<ECS_STORE> store,
        final @Nonnull CommandBuffer<ECS_STORE> commandBuffer
    ) {
        parent.onEntityTick(dt, index, archetypeChunk, store, commandBuffer);
    }

    @Override
    public Query<ECS_STORE> getQuery() {
        return this.query;
    }

    @Override
    public IRegistry<ECS_STORE> getRegistry() {
        return this.parent.getRegistry();
    }
}
