package dev.twunk.subsystem.base;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.ArchetypeTickingSystem;
import com.hypixel.hytale.server.core.universe.world.WorldProvider;
import dev.twunk.subsystem.ISubSystem;
import dev.twunk.subsystem.base.interfaces.IGlobalTickSystem;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Subsystem for calling `onSystemTick` on the parent system every tick
 *
 * GOAL: run code ONCE per tick globally. not per element, just, run this once per tick
 *
 * REQUIRES:
 * - N/A (this is a leaf)
 * PRODUCES:
 * - IGlobalTickSystem runner
 */
public class GlobalTickSubSystem<ECS_STORE extends WorldProvider>
    extends ArchetypeTickingSystem<ECS_STORE>
    implements ISubSystem<ECS_STORE>
{

    private final @Nonnull IGlobalTickSystem<ECS_STORE> parent;
    private final @Nullable Query<ECS_STORE> query;

    /**
     * Hytale expects a new "class" for each system you register. Thus, to have these composable modules
     * of subsystems, each one must secretly create a new class each and every time you call it
     */
    public static <ECS_STORE extends WorldProvider, T extends GlobalTickSubSystem<ECS_STORE>> GlobalTickSubSystem<
        ECS_STORE
    > newSubsystemFor(@Nonnull final IGlobalTickSystem<ECS_STORE> parent) {
        return ISubSystem.__newSubSystem(GlobalTickSubSystem.class, IGlobalTickSystem.class, parent);
    }

    protected GlobalTickSubSystem(@Nonnull IGlobalTickSystem<ECS_STORE> parent) {
        this.parent = parent;
        this.query = parent.getQuery();
    }

    /**
     * tick method that gets called by the `store`
     * this is pretty much just a shim to get into my code, as i don't want to touch
     * theirs wherever possible
     */
    @Override
    public void tick(
        float dt,
        @Nonnull ArchetypeChunk<ECS_STORE> archetypeChunk,
        @Nonnull Store<ECS_STORE> store,
        @Nonnull CommandBuffer<ECS_STORE> commandBuffer
    ) {
        parent.onSystemTick(dt, archetypeChunk, store, commandBuffer);
    }

    @Override
    @Nullable
    public Query<ECS_STORE> getQuery() {
        return this.query;
    }
}
