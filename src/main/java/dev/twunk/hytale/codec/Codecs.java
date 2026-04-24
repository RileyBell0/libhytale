package dev.twunk.hytale.codec;

import com.hypixel.hytale.codec.Codec;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import javax.annotation.Nullable;

/**
 * Just storing some extra codecs i define in here
 */
public class Codecs {

    public static final Codec<Level> LEVEL_CODEC = new FromStringCodec<>(Object::toString, Level::parse);

    protected static final Map<Class<?>, Codec<?>> ALL_CODECS = new HashMap<>(Map.of(Level.class, LEVEL_CODEC));

    public static final <T> void regiserCodec(Class<T> clazz, Codec<T> codec) {
        ALL_CODECS.put(clazz, codec);
    }

    @Nullable
    public static final <T> Codec<T> tryGetCodec(Class<T> clazz) {
        @SuppressWarnings("unchecked")
        var codec = (Codec<T>) ALL_CODECS.get(clazz);

        return codec;
    }
}
