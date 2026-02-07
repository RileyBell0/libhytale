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
    private final Int2ObjectConcurrentHashMap<TickResponse> systemStates = new Int2ObjectConcurrentHashMap<>();

    /**
     * Map from SystemID to locations where the item is stored (memory only,
     * not stored to disk)
     */
    @Nonnull
    public final Int2ObjectConcurrentHashMap<ArrayList<Ref<ChunkStore>>> location = new Int2ObjectConcurrentHashMap<>();

    @Nullable
    public TickResponse setSystemState(SmartTickSystem system, @Nonnull TickResponse state) {
        return this.systemStates.put(system.id, state);
    }

    @Nullable
    public TickResponse getSystemState(SmartTickSystem system) {
        return this.systemStates.get(system.id);
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
