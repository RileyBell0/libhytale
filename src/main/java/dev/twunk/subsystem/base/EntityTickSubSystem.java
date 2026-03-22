package dev.twunk.subsystem.base;

import com.hypixel.hytale.builtin.blocktick.system.ChunkBlockTickSystem;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.universe.world.WorldProvider;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import dev.twunk.subsystem.ISubSystem;
import dev.twunk.subsystem.base.interfaces.IEntityTickSystem;
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
    public static <ECS_STORE extends WorldProvider, T extends EntityTickSubSystem<ECS_STORE>> EntityTickSubSystem<
        ECS_STORE
    > newSubsystemFor(@Nonnull final IEntityTickSystem<ECS_STORE> parent) {
        return ISubSystem.__newSubSystem(EntityTickSubSystem.class, IEntityTickSystem.class, parent);
    }

    protected EntityTickSubSystem(@Nonnull IEntityTickSystem<ECS_STORE> parent) {
        this.parent = parent;
        this.query = parent.getQuery();
    }

    public void tick(
        float dt,
        int index,
        @Nonnull ArchetypeChunk<ECS_STORE> archetypeChunk,
        @Nonnull Store<ECS_STORE> store,
        @Nonnull CommandBuffer<ECS_STORE> commandBuffer
    ) {
        parent.onEntityTick(dt, index, archetypeChunk, store, commandBuffer);
    }

    @Override
    public Query<ECS_STORE> getQuery() {
        return this.query;
    }
}
