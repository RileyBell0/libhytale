package dev.twunk.hytale.codec;

import com.hypixel.hytale.codec.Codec;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import javax.annotation.Nullable;

/**
 * Just storing some extra codecs i define in here
 */
public class Codecs {

    public static final FromStringCodec<Level> LEVEL_CODEC = new FromStringCodec<>(Object::toString, Level::parse);
    public static final FromStringCodec<UUID> UUID_CODEC = new FromStringCodec<>(UUID::toString, UUID::fromString);

    protected static final Map<Class<?>, FromStringCodec<?>> ALL_CODECS = new HashMap<>(
        Map.of(Level.class, LEVEL_CODEC, UUID.class, UUID_CODEC)
    );

    public static final <T> void regiserCodec(Class<T> clazz, FromStringCodec<T> codec) {
        ALL_CODECS.put(clazz, codec);
    }

    @Nullable
    public static final <T> Codec<T> tryGetCodec(Class<T> clazz) {
        @SuppressWarnings("unchecked")
        var codec = (Codec<T>) ALL_CODECS.get(clazz);

        return codec;
    }

    @Nullable
    public static final <T> FromStringCodec<T> tryGetFromStrCodec(Class<T> clazz) {
        @SuppressWarnings("unchecked")
        var codec = (FromStringCodec<T>) ALL_CODECS.get(clazz);

        return codec;
    }
}
