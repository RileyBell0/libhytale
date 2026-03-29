package dev.twunk.hytale.utils;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.logger.HytaleLogger;
import dev.twunk.annotations.Serializable;
import dev.twunk.annotations.Serialize;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.lang.model.type.NullType;

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
    public static final <T> BuilderCodec<T> build(@Nonnull Class<T> clazz, @Nonnull Supplier<T> supplier) {
        return builder(clazz, supplier).build();
    }

    @Nonnull
    public static final <T> BuilderCodec.Builder<T> builder(@Nonnull Class<T> clazz, @Nonnull Supplier<T> supplier) {
        var fields = clazz.getDeclaredFields();
        console.log(clazz.toString());
        var registeredComponent = clazz.getAnnotation(Serializable.class);
        var inherits = registeredComponent.inherits();

        if (!inherits.equals(NullType.class) && !inherits.isAssignableFrom(clazz)) {
            throw new RuntimeException(
                "ERROR: class of which this inherits a codec from MUST BE a superclass of the class you're in, fuck idk how to write this | " +
                    inherits +
                    " | " +
                    clazz
            );
        }

        var inheritedCodec = tryGetInheritedCodec((Class<? super T>) inherits, clazz);

        @Nonnull
        BuilderCodec.Builder<T> builder;
        if (inheritedCodec == null) {
            builder = BuilderCodec.builder(clazz, supplier);
        } else {
            builder = BuilderCodec.builder(clazz, supplier, inheritedCodec);
        }

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

    @Nullable
    public static final <T extends U, U> BuilderCodec<U> tryGetInheritedCodec(
        @Nonnull Class<U> clazz,
        @Nonnull Class<T> parent
    ) {
        if (clazz.equals(NullType.class)) {
            return null;
        }
        if (!parent.isAnnotationPresent(Serializable.class)) {
            return null;
        }

        var parentSerializable = parent.getAnnotation(Serializable.class);

        // Priority 1: if the class we're inherting a codec from has been annotated
        // with RegisteredComponent, we'll recurse
        if (clazz.isAnnotationPresent(Serializable.class)) {
            return tryGetCodec(clazz);
        }

        // Priority 2: Look in the field with the name the parent assumed CODEC might be in
        try {
            var fieldName = parentSerializable.codecField();
            var codecField = clazz.getField(fieldName);
            if (!BuilderCodec.class.isAssignableFrom(codecField.getType())) {
                return null;
            }

            // GOOD. We found a field which contains a codec
            Object codec;
            codec = codecField.get(clazz);
            if (codec == null || !BuilderCodec.class.isAssignableFrom(codec.getClass())) {
                return null;
            }

            return (BuilderCodec<U>) codec;
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return null;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return null;
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
            return null;
        } catch (SecurityException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Nonnull
    private static final <T, U> BuilderCodec.Builder<T> appendCodec(
        @Nonnull BuilderCodec.Builder<T> builder,
        @Nonnull Field field,
        @Nonnull BuilderCodec<U> codec
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

    @Nonnull
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

    @Nonnull
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

    @Nonnull
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

    public static final <T> BuilderCodec<T> tryGetCodec(Class<T> clazz) {
        if (!clazz.isAnnotationPresent(Serializable.class)) {
            return null;
        }

        final Supplier<T> supplier = () -> {
            try {
                return clazz.getConstructor().newInstance();
            } catch (InstantiationException e) {
                e.printStackTrace();
                return null;
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                return null;
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
                return null;
            } catch (InvocationTargetException e) {
                e.printStackTrace();
                return null;
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
                return null;
            } catch (SecurityException e) {
                e.printStackTrace();
                return null;
            }
        };
        return AutoCodecGenerator.build(clazz, supplier);
    }
}
