package dev.twunk.hytale.utils;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.universe.world.WorldProvider;
import dev.twunk.annotations.AutoCodec;
import java.lang.reflect.Field;
import java.util.function.Supplier;
import javax.annotation.Nonnull;

public final class AutoCodecGenerator {

    private static final HytaleLogger.Api console = HytaleLogger.forEnclosingClass().atInfo();

    @Nonnull
    private static final String normaliseFieldName(Field field) {
        // first letter must be capitalised in codec key
        var name = field.getName();
        name = name.substring(0, 1).toUpperCase() + name.substring(1);

        return name;
    }

    @Nonnull
    public static final <ECS_STORE extends WorldProvider, T extends Component<ECS_STORE>> BuilderCodec<T> build(
        Class<T> clazz,
        Supplier<T> supplier
    ) {
        return process(clazz, supplier).build();
    }

    @Nonnull
    public static final <ECS_STORE extends WorldProvider, T extends Component<ECS_STORE>> BuilderCodec.Builder<
        T
    > process(Class<T> clazz, Supplier<T> supplier) {
        var fields = clazz.getDeclaredFields();

        var builder = BuilderCodec.builder(clazz, supplier);
        for (var field : fields) {
            if (!field.isAnnotationPresent(AutoCodec.class)) {
                console.log("No annotation present on field " + field);
                continue;
            }
            var fieldClass = field.getType();

            // First: in case the type is something like `SimpleItemContainer`, we'll check if there's an
            //        existing codec on the class itself for the type of the var we're handling that we
            //        can use
            try {
                var codecField = fieldClass.getField("CODEC");
                var codec = codecField.get(fieldClass);

                if (codec != null && BuilderCodec.class.isAssignableFrom(codec.getClass())) {
                    field.setAccessible(true);
                    builder = appendCodec(builder, field, (BuilderCodec) codec);
                }
            } catch (Exception e) {}

            // Otherwise, we'll do some manual checking
            if (fieldClass.equals(Boolean.TYPE)) {
                builder = appendBoolean(builder, field);
            }
        }

        return builder;
    }

    private static final <ECS_STORE extends WorldProvider, T extends Component<ECS_STORE>> BuilderCodec.Builder<
        T
    > appendCodec(@Nonnull BuilderCodec.Builder<T> builder, @Nonnull Field field, @Nonnull BuilderCodec codec) {
        final var name = normaliseFieldName(field);

        field.setAccessible(true);
        return builder
            .append(
                (KeyedCodec<Object>) new KeyedCodec(name, codec),
                (self, val) -> {
                    if (val != null) {
                        try {
                            field.set(self, val);
                            return;
                        } catch (IllegalArgumentException e) {
                            e.printStackTrace();
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    }
                },
                self -> {
                    try {
                        var val = field.get(self);
                        return val;
                    } catch (IllegalArgumentException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                    return null;
                }
            )
            .add();
    }

    private static final <ECS_STORE extends WorldProvider, T extends Component<ECS_STORE>> BuilderCodec.Builder<
        T
    > appendBoolean(@Nonnull BuilderCodec.Builder<T> builder, @Nonnull Field field) {
        final var name = normaliseFieldName(field);

        field.setAccessible(true);
        return builder
            .append(
                new KeyedCodec<>(name, Codec.BOOLEAN),
                (self, val) -> {
                    if (val != null) {
                        try {
                            field.set(self, (boolean) val);
                        } catch (IllegalArgumentException e) {
                            e.printStackTrace();
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    }
                },
                self -> {
                    try {
                        return (Boolean) field.get(self);
                    } catch (IllegalArgumentException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                    return null;
                }
            )
            .add();
    }
}
