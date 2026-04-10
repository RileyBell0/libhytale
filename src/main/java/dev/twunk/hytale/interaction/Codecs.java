package dev.twunk.hytale.interaction;

import com.hypixel.hytale.codec.Codec;
import dev.twunk.lib.TransformCodec;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import javax.annotation.Nullable;

/**
 * Just storing some extra codecs i define in here
 */
public class Codecs {

    public static final Codec<Level> LEVEL_CODEC = new TransformCodec<Level>(
        val -> val.toString(),
        str -> Level.parse(str)
    );

    public static final HashMap<Class<?>, Codec<?>> allCodecs = new HashMap<>(Map.of(Level.class, LEVEL_CODEC));

    public static final <T> void regiserCodec(Class<T> clazz, Codec<T> codec) {
        allCodecs.put(clazz, codec);
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public static final <T> Codec<T> tryGetCodec(Class<T> clazz) {
        return (Codec<T>) allCodecs.get(clazz);
    }
}
