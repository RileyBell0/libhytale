package dev.twunk.ticking.component.system;

import com.hypixel.fastutil.ints.Int2ObjectConcurrentHashMap;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import dev.twunk.ticking.component.IRegisteredComponent;
import dev.twunk.ticking.response.TickResponse;
import java.util.ArrayList;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TickState implements IRegisteredComponent {
    // serializing/deserializing your vars
    @Nonnull
    public static final BuilderCodec<TickState> CODEC = BuilderCodec.builder(
            TickState.class,
            TickState::new)
            .build();

    @SuppressWarnings("null")
    @Nonnull
    public static ComponentType<ChunkStore, TickState> COMPONENT_TYPE;

    @Nonnull
    private final Int2ObjectConcurrentHashMap<TickResponse> tickingState = new Int2ObjectConcurrentHashMap<>();

    /**
     * Map from SystemID to locations where the item is stored (memory only,
     * not stored to disk)
     */
    @Nonnull
    private final Int2ObjectConcurrentHashMap<ArrayList<Ref<ChunkStore>>> memoryLocation = new Int2ObjectConcurrentHashMap<>();

    /**
     * Returns the previously set state (if one was already there)
     */
    @Nullable
    public TickResponse setTickingInfo(@Nonnull SmartTickSystem system, @Nonnull TickResponse state) {
        return this.tickingState.put(system.id, state);
    }

    @Nullable
    public TickResponse getTickingInfo(@Nonnull SmartTickSystem system) {
        return this.tickingState.get(system.id);
    }

    /**
     * Internal function for me. No touchy.
     *
     * Stores a ref to the place i currently keep track of our ticking state for our
     * object
     *
     * useful so when the block that contains <THIS> is removed, i can do cleanup
     * and *not* tick it anymore
     */
    @Nullable
    public ArrayList<Ref<ChunkStore>> _setMemoryLocation(
            @Nonnull SmartTickSystem system,
            @Nonnull ArrayList<Ref<ChunkStore>> state) {
        return memoryLocation.put(system.id, state);
    }

    /**
     * Track a ref to where (in SmartTickSystem) i currently keep the ref to
     * <THIS>
     *
     * useful so when the block that contains <THIS> is removed, i can do cleanup
     * and *not* tick it anymore
     */
    @Nullable
    public ArrayList<Ref<ChunkStore>> _getMemoryLocation(
            @Nonnull SmartTickSystem system) {
        return memoryLocation.get(system.id);
    }

    /**
     * Track a ref to where (in SmartTickSystem) i currently keep the ref to
     * <THIS>
     *
     * useful so when the block that contains <THIS> is removed, i can do cleanup
     * and *not* tick it anymore
     */
    @Nullable
    public ArrayList<Ref<ChunkStore>> _dumpMemoryLocation(
            @Nonnull SmartTickSystem system) {
        return memoryLocation.remove(system.id);
    }

    @Nonnull
    public TickState clone() {
        return new TickState();
    }

    @Nonnull
    public static ComponentType<ChunkStore, TickState> getComponentType() {
        return (ComponentType<ChunkStore, TickState>) IRegisteredComponent.getComponentType(TickState.class);
    }
}
