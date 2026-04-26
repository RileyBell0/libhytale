package dev.twunk.hytale.component;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.server.core.universe.world.WorldProvider;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.util.UUIDUtil;
import java.util.UUID;
import javax.annotation.Nonnull;
import org.bson.UuidRepresentation;
import org.bson.internal.UuidHelper;

final class Chunk extends UUIDComponent<ChunkStore> {

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static final BuilderCodec<Chunk> CODEC = BuilderCodec.builder(Chunk.class, Chunk::new)
        .append(
            new KeyedCodec("UUID", Codec.UUID_BINARY),
            (o, i) -> o.uuid = i == null ? UUIDUtil.generateVersion3UUID() : i,
            o -> o.uuid
        )
        .addValidator(Validators.nonNull())
        .add()
        .build();

    public Chunk() {
        super();
    }

    // combination of world, chunk, local index
    public Chunk(UUID coords) {
        super(coords);
    }

    // combination of world, chunk, local index
    public Chunk(UUID worldId, long chunkCoords, int blockCoords) {
        super(UUIDComponent.uuidFromBlockCoords(worldId, chunkCoords, blockCoords));
    }
}

final class Entity extends UUIDComponent<ChunkStore> {

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static final BuilderCodec<Chunk> CODEC = BuilderCodec.builder(Chunk.class, Chunk::new)
        .append(
            new KeyedCodec("UUID", Codec.UUID_BINARY),
            (o, i) -> o.uuid = i == null ? UUIDUtil.generateVersion3UUID() : i,
            o -> o.uuid
        )
        .addValidator(Validators.nonNull())
        .add()
        .build();

    public Entity() {
        super();
    }

    // combination of world, chunk, local index
    public Entity(UUID uuid) {
        super(uuid);
    }

    public static Entity generateVersion3UUID() {
        return new Entity(UUIDUtil.generateVersion3UUID());
    }

    @SuppressWarnings("null")
    public static Entity randomUUID() {
        return new Entity(UUID.randomUUID());
    }
}

public abstract class UUIDComponent<ECS_TYPE extends WorldProvider> implements Component<ECS_TYPE> {

    protected UUID uuid;

    protected UUIDComponent(UUID uuid) {
        this.uuid = uuid;
    }

    @SuppressWarnings("null")
    protected UUIDComponent() {}

    public UUID getUuid() {
        return this.uuid;
    }

    public Component<ECS_TYPE> clone() {
        return this;
    }

    // combination of world, chunk, local index
    public static final UUID uuidFromBlockCoords(UUID worldId, long chunkCoords, int blockCoords) {
        return combine(worldId, new UUID(chunkCoords, blockCoords));
    }

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
