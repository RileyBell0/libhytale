package dev.twunk.hytale.codec;

import com.hypixel.hytale.codec.Codec;
import dev.twunk.hytale.component.UUIDComponent;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import javax.annotation.Nullable;

/**
 * Just storing some extra codecs I define in here
 */
public class Codecs {

    public static final FromStringCodec<Level> LEVEL_CODEC = new FromStringCodec<>(Object::toString, Level::parse);
    public static final FromStringCodec<UUID> UUID_CODEC = new FromStringCodec<>(UUID::toString, UUID::fromString);

    protected static final Map<Class<?>, Codec<?>> ALL_CODECS = new HashMap<>(
        Map.of(Level.class, LEVEL_CODEC, UUID.class, UUIDComponent.UUID_CODEC_WITH_DEFAULT)
    );

    public static <T> void registerCodec(Class<T> clazz, FromStringCodec<T> codec) {
        ALL_CODECS.put(clazz, codec);
    }

    @Nullable
    public static <T> Codec<T> tryGetCodec(Class<T> clazz) {
        try {
            @SuppressWarnings("unchecked")
            var codec = (Codec<T>) ALL_CODECS.get(clazz);

            return codec;
        } catch (ClassCastException | NullPointerException _) {
            return null;
        }
    }

    @Nullable
    public static <T> FromStringCodec<T> tryGetFromStrCodec(Class<T> clazz) {
        try {
            @SuppressWarnings("unchecked")
            var codec = (FromStringCodec<T>) ALL_CODECS.get(clazz);

            return codec;
        } catch (ClassCastException | NullPointerException _) {
            return null;
        }
    }
}
