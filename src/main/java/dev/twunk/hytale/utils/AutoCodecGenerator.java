package dev.twunk.hytale.utils;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import dev.twunk.annotations.Serialize;
import java.lang.reflect.Field;
import java.util.function.Supplier;
import javax.annotation.Nonnull;

public final class AutoCodecGenerator {

    @Nonnull
    private static final String normaliseFieldName(Field field) {
        // first letter must be capitalised in codec key
        var name = field.getName();
        name = name.substring(0, 1).toUpperCase() + name.substring(1);

        return name;
    }

    @Nonnull
    public static final <T> BuilderCodec<T> build(Class<T> clazz, Supplier<T> supplier) {
        return process(clazz, supplier).build();
    }

    @Nonnull
    public static final <T> BuilderCodec.Builder<T> process(Class<T> clazz, Supplier<T> supplier) {
        var fields = clazz.getDeclaredFields();

        var builder = BuilderCodec.builder(clazz, supplier);
        for (var field : fields) {
            if (!field.isAnnotationPresent(Serialize.class)) {
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
            if (fieldClass.equals(Boolean.TYPE) || fieldClass.equals(boolean.class)) {
                builder = appendBoolean(builder, field);
            } else if (fieldClass.equals(String.class)) {
                builder = appendString(builder, field);
            } else if (fieldClass.equals(Short.class) || fieldClass.equals(short.class)) {
                builder = appendShort(builder, field);
            }
        }

        return builder;
    }

    private static final <T> BuilderCodec.Builder<T> appendCodec(
        @Nonnull BuilderCodec.Builder<T> builder,
        @Nonnull Field field,
        @Nonnull BuilderCodec codec
    ) {
        final var annotation = field.getAnnotation(Serialize.class);
        var name = annotation.key();
        var required = annotation.required();
        if (name.isEmpty()) {
            name = normaliseFieldName(field);
        }

        field.setAccessible(true);
        return builder
            .append(
                (KeyedCodec<Object>) new KeyedCodec(name, codec, required),
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

    private static final <T> BuilderCodec.Builder<T> appendBoolean(
        @Nonnull BuilderCodec.Builder<T> builder,
        @Nonnull Field field
    ) {
        final var annotation = field.getAnnotation(Serialize.class);
        var name = annotation.key();
        var required = annotation.required();
        if (name.isEmpty()) {
            name = normaliseFieldName(field);
        }

        field.setAccessible(true);
        return builder
            .append(
                new KeyedCodec<>(name, Codec.BOOLEAN, required),
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

    private static final <T> BuilderCodec.Builder<T> appendString(
        @Nonnull BuilderCodec.Builder<T> builder,
        @Nonnull Field field
    ) {
        final var annotation = field.getAnnotation(Serialize.class);
        var name = annotation.key();
        var required = annotation.required();
        if (name.isEmpty()) {
            name = normaliseFieldName(field);
        }

        field.setAccessible(true);
        return builder
            .append(
                new KeyedCodec<>(name, Codec.STRING, required),
                (self, val) -> {
                    if (val != null) {
                        try {
                            field.set(self, (String) val);
                        } catch (IllegalArgumentException e) {
                            e.printStackTrace();
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    }
                },
                self -> {
                    try {
                        return (String) field.get(self);
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

    private static final <T> BuilderCodec.Builder<T> appendShort(
        @Nonnull BuilderCodec.Builder<T> builder,
        @Nonnull Field field
    ) {
        final var annotation = field.getAnnotation(Serialize.class);
        var name = annotation.key();
        var required = annotation.required();
        if (name.isEmpty()) {
            name = normaliseFieldName(field);
        }

        field.setAccessible(true);
        return builder
            .append(
                new KeyedCodec<>(name, Codec.SHORT, required),
                (self, val) -> {
                    if (val != null) {
                        try {
                            field.set(self, (short) val);
                        } catch (IllegalArgumentException e) {
                            e.printStackTrace();
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    }
                },
                self -> {
                    try {
                        return (short) field.get(self);
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
