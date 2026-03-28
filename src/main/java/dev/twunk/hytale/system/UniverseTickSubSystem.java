package dev.twunk.hytale.system;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.ArchetypeTickingSystem;
import com.hypixel.hytale.server.core.universe.world.WorldProvider;
import dev.twunk.interfaces.IRegistry;
import dev.twunk.interfaces.ISubSystem;
import dev.twunk.interfaces.subsystem.IUniverseTickSystem;
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
 *
 * My code
 * @see IUniverseTickSystem - Something that this subsystem can call and run.
 *
 * Hytale's code
 * @see ArchetypeTickingSystem - I use this to run the subsystem. Only way i currently know
 *                               of for getting a commandBuffer in a global tick
 */
public class UniverseTickSubSystem<ECS_STORE extends WorldProvider>
    extends ArchetypeTickingSystem<ECS_STORE>
    implements ISubSystem<ECS_STORE>
{

    private final @Nonnull IUniverseTickSystem<ECS_STORE> parent;
    private final @Nullable Query<ECS_STORE> query;

    /**
     * Hytale expects a new "class" for each system you register. Thus, to have these composable modules
     * of subsystems, each one must secretly create a new class each and every time you call it
     */
    @SuppressWarnings("unchecked")
    @Nonnull
    public static <ECS_STORE extends WorldProvider, T extends UniverseTickSubSystem<ECS_STORE>> UniverseTickSubSystem<
        ECS_STORE
    > newSubsystemFor(final @Nonnull IUniverseTickSystem<ECS_STORE> parent) {
        return ISubSystem.__newSubSystem(UniverseTickSubSystem.class, IUniverseTickSystem.class, parent);
    }

    protected UniverseTickSubSystem(final @Nonnull IUniverseTickSystem<ECS_STORE> parent) {
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
        final float dt,
        final @Nonnull ArchetypeChunk<ECS_STORE> archetypeChunk,
        final @Nonnull Store<ECS_STORE> store,
        final @Nonnull CommandBuffer<ECS_STORE> commandBuffer
    ) {
        parent.onSystemTick(dt, archetypeChunk, store, commandBuffer);
    }

    @Override
    @Nullable
    public Query<ECS_STORE> getQuery() {
        return this.query;
    }

    @Override
    public IRegistry<ECS_STORE> getRegistry() {
        return parent.getRegistry();
    }
}
