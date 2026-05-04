package dev.twunk.hytale.component;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.server.core.universe.world.WorldProvider;
import com.hypixel.hytale.server.core.util.UUIDUtil;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.bson.UuidRepresentation;
import org.bson.internal.UuidHelper;

public final class UUIDComponent<ECS_TYPE extends WorldProvider> implements Component<ECS_TYPE> {

    @SuppressWarnings({ "unchecked" })
    private static final Class<UUIDComponent<? extends WorldProvider>> UUID_CLASS = (Class<
        UUIDComponent<? extends WorldProvider>
    >) (Class<?>) UUIDComponent.class;

    public static final BuilderCodec<UUIDComponent<? extends WorldProvider>> CODEC = BuilderCodec.builder(
        UUID_CLASS,
        UUIDComponent::new
    )
        .append(new KeyedCodec<>("UUID", Codec.UUID_BINARY), (o, i) -> o.uuid = i, o -> o.uuid)
        .add()
        .afterDecode(v -> {
            if (v.uuid == null) {
                v.uuid = UUIDUtil.generateVersion3UUID();
            }
        })
        .build();

    @Nullable
    protected UUID uuid;

    @SuppressWarnings("null")
    public UUID getUuid() {
        return this.uuid;
    }

    public Component<ECS_TYPE> clone() {
        return this;
    }

    // combination of world, chunk, local index, neat idea but am not using it atm
    public static final UUID uuidFromBlockCoords(UUID worldId, long chunkCoords, int blockCoords) {
        return combine(worldId, new UUID(chunkCoords, blockCoords));
    }

    // neat idea but am not using it atm
    public static final UUID combine(UUID a, UUID b) {
        // Source - https://stackoverflow.com/a/5683621
        // Posted by pickypg, modified by community. See post 'Timeline' for change history
        // Retrieved 2026-04-26, License - CC BY-SA 4.0

        byte[] one = UuidHelper.encodeUuidToBinary(a, UuidRepresentation.STANDARD);
        byte[] two = UuidHelper.encodeUuidToBinary(b, UuidRepresentation.STANDARD);
        byte[] combined = new byte[one.length + two.length];

        System.arraycopy(one, 0, combined, 0, one.length);
        System.arraycopy(two, 0, combined, one.length, two.length);

        @SuppressWarnings("null")
        @Nonnull
        var uuid = UUID.nameUUIDFromBytes(combined);
        return uuid;
    }
}
