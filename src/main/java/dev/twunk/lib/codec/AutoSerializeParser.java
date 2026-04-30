/**
 * TODO this file is pretty uuh, repetitive, and hard to verify. needs a redo eventually
 */
package dev.twunk.lib.codec;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.codec.codecs.map.MapCodec;
import com.hypixel.hytale.component.ComponentType;
import dev.twunk.hytale.LibHytale;
import dev.twunk.hytale.LibHytaleException;
import dev.twunk.hytale.codec.Codecs;
import dev.twunk.hytale.codec.auto.Serializable;
import dev.twunk.hytale.codec.auto.Serialize;
import dev.twunk.hytale.interfaces.component.IContainerComponent;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import javax.lang.model.type.NullType;

/**
 * Really handly util, takes the annotations i supply for serialization and turns them into codecs.
 *
 * Hiding it away in here since if you're actually trying to find this code, you will, and
 * nobody else needs to be confused by it when they're learning programming (probably) and modding
 * in general.
 *
 * I figure you've got a sense of what you're doing, so, yeah, welcome, enjoy your stay,
 * and feel free to leave a PR or advice etc on how to make this less spaghetti and more, neat.
 */
public final class AutoSerializeParser {

    private static final String normaliseFieldName(Field field) {
        // first letter must be capitalised in codec key
        var name = field.getName();
        name = name.substring(0, 1).toUpperCase() + name.substring(1);

        return name;
    }

    public static final <T> BuilderCodec<T> build(Class<T> clazz) {
        return builder(clazz, () -> {
            try {
                return clazz.getConstructor().newInstance();
            } catch (
                InstantiationException
                | IllegalAccessException
                | IllegalArgumentException
                | InvocationTargetException
                | NoSuchMethodException
                | SecurityException e
            ) {
                e.printStackTrace();
                throw new LibHytaleException(
                    "ERROR: you passed a class without a default constructor to AutoCodecGenerator::build. I need a default constructor. Thats the entire point of this shorthand. CLASS IN QUESTION: " +
                        clazz
                );
            }
        }).build();
    }

    public static final <T> BuilderCodec<T> build(Class<T> clazz, Supplier<T> supplier) {
        return builder(clazz, supplier).build();
    }

    private static final <T> BuilderCodec.Builder<T> parseField(
        Class<T> clazz,
        BuilderCodec.Builder<T> builder,
        Field field
    ) {
        if (!field.isAnnotationPresent(Serialize.class)) {
            return builder;
        }
        var fieldClass = field.getType();

        // First: check if i've specifically defined a codec for that class
        try {
            var codec = Codecs.tryGetCodec(clazz);
            if (codec != null) {
                return appendRawCodec(builder, field, codec);
            }
        } catch (Exception _) {}

        // Next: in case the type is something like `SimpleItemContainer`, we'll check if there's an
        //        existing codec on the class itself for the type of the var we're handling that we
        //        can use
        try {
            var codecField = fieldClass.getField("CODEC");
            var codec = codecField.get(fieldClass);

            if (codec != null && Codec.class.isAssignableFrom(codec.getClass())) {
                field.setAccessible(true);
                return appendCodec(builder, field, (Codec<?>) codec);
            }
        } catch (Exception _) {}

        // Otherwise, we'll do some manual checking
        if (fieldClass.equals(Boolean.TYPE) || fieldClass.equals(boolean.class)) {
            return appendBoolean(builder, field);
        } else if (fieldClass.equals(String.class)) {
            return appendString(builder, field);
        } else if (fieldClass.equals(Short.class) || fieldClass.equals(short.class)) {
            return appendShort(builder, field);
        } else if (fieldClass.equals(ComponentType.class)) {
            return appendComponentType(builder, field);
        } else if (fieldClass.equals(Integer.class) || fieldClass.equals(int.class)) {
            return appendInt(builder, field);
        } else if (fieldClass.equals(Long.class) || fieldClass.equals(long.class)) {
            return appendLong(builder, field);
        } else if (List.class.isAssignableFrom(fieldClass)) {
            var genericType = field.getGenericType();
            if (!(genericType instanceof ParameterizedType)) {
                return builder;
            }

            ParameterizedType pType = (ParameterizedType) genericType;

            Type[] fieldArgTypes = pType.getActualTypeArguments();
            if (fieldArgTypes.length != 1) {
                return builder;
            }

            var arrayValueType = fieldArgTypes[0];
            if (!(arrayValueType instanceof Class)) {
                return builder;
            }
            Class<?> innerClass = (Class<?>) arrayValueType;

            return appendArray(builder, field, innerClass);
        } else if (Map.class.isAssignableFrom(fieldClass)) {
            var genericType = field.getGenericType();
            if (!(genericType instanceof ParameterizedType)) {
                return builder;
            }

            ParameterizedType pType = (ParameterizedType) genericType;

            Type[] fieldArgTypes = pType.getActualTypeArguments();
            if (fieldArgTypes.length != 2) {
                return builder;
            }

            var keyType = fieldArgTypes[0];
            var valueType = fieldArgTypes[1];
            if (!(keyType instanceof Class)) {
                return builder;
            }
            if (!(valueType instanceof Class)) {
                return builder;
            }

            Class<?> keyClass = (Class<?>) keyType;
            Class<?> valueClass = (Class<?>) valueType;

            if (keyClass == String.class) {
                return appendMap(builder, field, valueClass);
            } else {
                return appendMap(builder, field, keyClass, valueClass);
            }
        }

        return builder;
    }

    public static final <T> BuilderCodec.Builder<T> builder(Class<T> clazz, Supplier<T> supplier) {
        var fields = clazz.getDeclaredFields();
        var registeredComponent = clazz.getAnnotation(Serializable.class);
        var inherits = registeredComponent.inherits();
        var docString = registeredComponent.documentation();

        if (!inherits.equals(NullType.class) && !inherits.isAssignableFrom(clazz)) {
            throw new LibHytaleException(
                "ERROR: class of which this inherits a codec from MUST BE a superclass of the class you're in, fuck idk how to write this | " +
                    inherits +
                    " | " +
                    clazz
            );
        }

        @SuppressWarnings("unchecked")
        var asSuperT = (Class<? super T>) inherits;

        var temp = tryGetInheritedCodec(asSuperT, clazz);
        BuilderCodec<T> inheritedCodec = null;
        if (temp instanceof BuilderCodec c) {
            inheritedCodec = c;
        }

        BuilderCodec.Builder<T> builder;
        if (inheritedCodec == null) {
            builder = BuilderCodec.builder(clazz, supplier);
        } else {
            builder = BuilderCodec.builder(clazz, supplier, inheritedCodec);
        }

        if (!docString.isEmpty()) {
            builder = builder.documentation(docString);
        }

        for (var field : fields) {
            if (field == null) {
                continue;
            }

            builder = parseField(clazz, builder, field);
        }

        return builder;
    }

    private static final <T, U> BuilderCodec.Builder<T> appendArray(
        BuilderCodec.Builder<T> builder,
        Field field,
        Class<U> innerClass
    ) {
        final var annotation = field.getAnnotation(Serialize.class);
        var name = annotation.key();
        var required = annotation.required();
        if (name.isEmpty()) {
            name = normaliseFieldName(field);
        }
        var codec = Codecs.tryGetCodec(innerClass);
        if (codec == null) {
            codec = tryGetBuilderCodec(innerClass);
            if (codec == null) {
                throw new LibHytaleException("Failed to get codec for class " + innerClass);
            }
        }

        field.setAccessible(true);
        return builder
            .append(
                new KeyedCodec<U[]>(
                    name,
                    new ArrayCodec<>(codec, (int len) -> {
                        @SuppressWarnings("unchecked")
                        var asArr = (U[]) new Object[len];
                        return asArr;
                    }),
                    required
                ),
                (self, val) -> {
                    if (val == null) {
                        return;
                    }
                    try {
                        @SuppressWarnings("unchecked")
                        final List<U> actualField = (List<U>) field.get(self);
                        actualField.addAll(Arrays.asList(val));
                    } catch (IllegalArgumentException | IllegalAccessException e) {
                        e.printStackTrace();
                    }
                },
                self -> {
                    try {
                        @SuppressWarnings("unchecked")
                        final var actualField = (U[]) ((List<U>) field.get(self)).toArray();
                        return actualField;
                    } catch (IllegalArgumentException | IllegalAccessException e) {
                        e.printStackTrace();
                    }
                    return null;
                }
            )
            .add();
    }

    private static final <T, V> BuilderCodec.Builder<T> appendMap(
        BuilderCodec.Builder<T> builder,
        Field field,
        Class<V> valueClass
    ) {
        final var annotation = field.getAnnotation(Serialize.class);
        var name = annotation.key();
        var required = annotation.required();
        if (name.isEmpty()) {
            name = normaliseFieldName(field);
        }
        var valueCodec = Codecs.tryGetCodec(valueClass);
        if (valueCodec == null) {
            valueCodec = tryGetCodec(valueClass);
            if (valueCodec == null) {
                throw new LibHytaleException("Failed to get codec for class " + valueClass);
            }
        }
        field.setAccessible(true);

        return builder
            .append(
                new KeyedCodec<>(name, new MapCodec<>(valueCodec, HashMap::new, false), required),
                (self, val) -> {
                    if (val == null) {
                        return;
                    }
                    try {
                        field.set(self, val);
                    } catch (IllegalArgumentException | IllegalAccessException e) {
                        e.printStackTrace();
                    }
                },
                self -> {
                    try {
                        @SuppressWarnings("unchecked")
                        final var actualField = (Map<String, V>) field.get(self);
                        return actualField;
                    } catch (IllegalArgumentException | IllegalAccessException e) {
                        e.printStackTrace();
                    }
                    return null;
                }
            )
            .add();
    }

    private static final <T, K, V> BuilderCodec.Builder<T> appendMap(
        BuilderCodec.Builder<T> builder,
        Field field,
        Class<K> keyClass,
        Class<V> valueClass
    ) {
        final var annotation = field.getAnnotation(Serialize.class);
        var name = annotation.key();
        var required = annotation.required();
        if (name.isEmpty()) {
            name = normaliseFieldName(field);
        }
        var keyCodec = Codecs.tryGetFromStrCodec(keyClass);
        if (keyCodec == null) {
            throw new LibHytaleException("Failed to get codec for class " + keyClass);
        }
        var valueCodec = Codecs.tryGetCodec(valueClass);
        if (valueCodec == null) {
            valueCodec = tryGetCodec(valueClass);
            if (valueCodec == null) {
                throw new LibHytaleException("Failed to get codec for class " + valueClass);
            }
        }
        field.setAccessible(true);

        return builder
            .append(
                new KeyedCodec<>(name, new StringableKeyMapCodec<>(keyCodec, valueCodec, HashMap::new), required),
                (self, val) -> {
                    if (val == null) {
                        return;
                    }
                    try {
                        @SuppressWarnings("unchecked")
                        final var actualField = (Map<K, V>) field.get(self);
                        actualField.putAll(val);
                    } catch (IllegalArgumentException | IllegalAccessException e) {
                        e.printStackTrace();
                    }
                },
                self -> {
                    try {
                        @SuppressWarnings("unchecked")
                        final var actualField = (Map<K, V>) field.get(self);
                        return actualField;
                    } catch (IllegalArgumentException | IllegalAccessException e) {
                        e.printStackTrace();
                    }
                    return null;
                }
            )
            .add();
    }

    @Nullable
    public static final <T extends U, U> Codec<U> tryGetInheritedCodec(Class<U> clazz, Class<T> parent) {
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
            return tryGetBuilderCodec(clazz);
        }

        // Priority 2: Look in the field with the name the parent assumed CODEC might be in
        try {
            var fieldName = parentSerializable.codecField();
            var codecField = clazz.getField(fieldName);
            if (!BuilderCodec.class.isAssignableFrom(codecField.getType())) {
                return null;
            }

            // GOOD. We found a field which contains a codec
            Object codec = codecField.get(clazz);
            if (codec == null || !BuilderCodec.class.isAssignableFrom(codec.getClass())) {
                return null;
            }

            @SuppressWarnings("unchecked")
            var asBuilderCodec = (BuilderCodec<U>) codec;
            return asBuilderCodec;
        } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static final <T, U> BuilderCodec.Builder<T> appendCodec(
        BuilderCodec.Builder<T> builder,
        Field field,
        Codec<U> codec
    ) {
        final var annotation = field.getAnnotation(Serialize.class);
        var name = annotation.key();
        var required = annotation.required();
        if (name.isEmpty()) {
            name = normaliseFieldName(field);
        }

        field.setAccessible(true);

        @SuppressWarnings("unchecked")
        final var keyedCodec = (KeyedCodec<Object>) new KeyedCodec<>(name, codec, required);
        return builder
            .append(
                keyedCodec,
                (self, val) -> {
                    if (val == null) {
                        return;
                    }

                    try {
                        field.set(self, val);
                        return;
                    } catch (IllegalArgumentException | IllegalAccessException e) {
                        e.printStackTrace();
                    }
                },
                self -> {
                    try {
                        return field.get(self);
                    } catch (IllegalArgumentException | IllegalAccessException e) {
                        e.printStackTrace();
                    }

                    return null;
                }
            )
            .add();
    }

    private static final <T, U> BuilderCodec.Builder<T> appendRawCodec(
        BuilderCodec.Builder<T> builder,
        Field field,
        Codec<U> codec
    ) {
        final var annotation = field.getAnnotation(Serialize.class);
        var name = annotation.key();
        var required = annotation.required();
        if (name.isEmpty()) {
            name = normaliseFieldName(field);
        }

        field.setAccessible(true);
        @SuppressWarnings("unchecked")
        final var keyedCodec = (KeyedCodec<Object>) new KeyedCodec<>(name, codec, required);
        return builder
            .append(
                keyedCodec,
                (self, val) -> {
                    if (val == null) {
                        return;
                    }

                    try {
                        field.set(self, val);
                    } catch (IllegalArgumentException | IllegalAccessException e) {
                        e.printStackTrace();
                    }
                },
                self -> {
                    try {
                        return field.get(self);
                    } catch (IllegalArgumentException | IllegalAccessException e) {
                        e.printStackTrace();
                    }

                    return null;
                }
            )
            .add();
    }

    private static final <T> BuilderCodec.Builder<T> appendComponentType(BuilderCodec.Builder<T> builder, Field field) {
        final var annotation = field.getAnnotation(Serialize.class);
        var name = annotation.key();
        var required = annotation.required();
        if (name.isEmpty()) {
            name = normaliseFieldName(field);
        }
        var inChunkStore = annotation.inChunkStore();

        field.setAccessible(true);

        return builder
            .append(
                new KeyedCodec<>(name, Codec.STRING, required),
                (self, id) -> {
                    if (id == null) {
                        return;
                    }
                    final var potentialComponentType = inChunkStore
                        ? LibHytale.CHUNK_REGISTRY.getComponentType(id)
                        : LibHytale.ENTITY_REGISTRY.getComponentType(id);
                    if (potentialComponentType == null) {
                        // I'm deciding to crash gracefully here
                        return;
                    }

                    final var innerClass = potentialComponentType.getTypeClass();
                    if (!IContainerComponent.class.isAssignableFrom(innerClass)) {
                        return;
                    }
                    try {
                        field.set(self, potentialComponentType);
                    } catch (IllegalArgumentException | IllegalAccessException e) {
                        e.printStackTrace();
                    }
                },
                self -> {
                    try {
                        return ((ComponentType<?, ?>) field.get(self)).getTypeClass().getName();
                    } catch (IllegalArgumentException | IllegalAccessException e) {
                        e.printStackTrace();
                    }
                    return null;
                }
            )
            .add();
    }

    private static final <T> BuilderCodec.Builder<T> appendBoolean(BuilderCodec.Builder<T> builder, Field field) {
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
                            field.set(self, val);
                        } catch (IllegalArgumentException | IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    }
                },
                self -> {
                    try {
                        return field.getBoolean(self);
                    } catch (IllegalArgumentException | IllegalAccessException e) {
                        e.printStackTrace();
                    }
                    return null;
                }
            )
            .add();
    }

    private static final <T> BuilderCodec.Builder<T> appendString(BuilderCodec.Builder<T> builder, Field field) {
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
                            field.set(self, val);
                        } catch (IllegalArgumentException | IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    }
                },
                self -> {
                    try {
                        return (String) field.get(self);
                    } catch (IllegalArgumentException | IllegalAccessException e) {
                        e.printStackTrace();
                    }
                    return null;
                }
            )
            .add();
    }

    private static final <T> BuilderCodec.Builder<T> appendShort(BuilderCodec.Builder<T> builder, Field field) {
        final var annotation = field.getAnnotation(Serialize.class);
        var name = annotation.key();
        var required = annotation.required();
        var minVal = annotation.min();
        if (name.isEmpty()) {
            name = normaliseFieldName(field);
        }

        field.setAccessible(true);
        return builder
            .append(
                new KeyedCodec<>(name, Codec.SHORT, required),
                (self, val) -> {
                    if (val != null && val >= minVal) {
                        try {
                            field.set(self, val);
                        } catch (IllegalArgumentException | IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    }
                },
                self -> {
                    try {
                        return field.getShort(self);
                    } catch (IllegalArgumentException | IllegalAccessException e) {
                        e.printStackTrace();
                    }
                    return null;
                }
            )
            .add();
    }

    private static final <T> BuilderCodec.Builder<T> appendInt(BuilderCodec.Builder<T> builder, Field field) {
        final var annotation = field.getAnnotation(Serialize.class);
        var name = annotation.key();
        var required = annotation.required();
        var minVal = annotation.min();
        if (name.isEmpty()) {
            name = normaliseFieldName(field);
        }

        field.setAccessible(true);
        return builder
            .append(
                new KeyedCodec<>(name, Codec.INTEGER, required),
                (self, val) -> {
                    if (val != null && val >= minVal) {
                        try {
                            field.set(self, val);
                        } catch (IllegalArgumentException | IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    }
                },
                self -> {
                    try {
                        return field.getInt(self);
                    } catch (IllegalArgumentException | IllegalAccessException e) {
                        e.printStackTrace();
                    }
                    return null;
                }
            )
            .add();
    }

    private static final <T> BuilderCodec.Builder<T> appendLong(BuilderCodec.Builder<T> builder, Field field) {
        final var annotation = field.getAnnotation(Serialize.class);
        var name = annotation.key();
        var required = annotation.required();
        var minVal = annotation.min();
        if (name.isEmpty()) {
            name = normaliseFieldName(field);
        }

        field.setAccessible(true);
        return builder
            .append(
                new KeyedCodec<>(name, Codec.LONG, required),
                (self, val) -> {
                    if (val != null && val >= minVal) {
                        try {
                            field.set(self, val);
                        } catch (IllegalArgumentException | IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    }
                },
                self -> {
                    try {
                        return field.getLong(self);
                    } catch (IllegalArgumentException | IllegalAccessException e) {
                        e.printStackTrace();
                    }
                    return null;
                }
            )
            .add();
    }

    @Nullable
    public static final <T> Codec<T> tryGetCodec(Class<T> clazz) {
        try {
            var codecField = clazz.getField("CODEC");
            var codec = codecField.get(clazz);

            if (codec instanceof Codec<?> c) {
                return (Codec<T>) c;
            }
        } catch (Exception _) {
            // just trynna see if there is a codec for this class
            // and happy to just dip if there's an exception cause its really a "best effort" kinda sitch
        }

        if (!clazz.isAnnotationPresent(Serializable.class)) {
            return null;
        }

        final Supplier<T> supplier = () -> {
            try {
                return clazz.getConstructor().newInstance();
            } catch (
                InstantiationException
                | IllegalAccessException
                | IllegalArgumentException
                | InvocationTargetException
                | NoSuchMethodException
                | SecurityException e
            ) {
                e.printStackTrace();
                return null;
            }
        };
        return AutoSerializeParser.build(clazz, supplier);
    }

    @Nullable
    public static final <T> BuilderCodec<T> tryGetBuilderCodec(Class<T> clazz) {
        try {
            var codecField = clazz.getField("CODEC");
            if (clazz == codecField.getDeclaringClass()) {
                var codec = codecField.get(clazz);
                if (codec instanceof BuilderCodec<?> c) {
                    return (BuilderCodec<T>) c;
                }
            }
        } catch (Exception _) {
            // just trynna see if there is a codec for this class
            // and happy to just dip if there's an exception cause its really a "best effort" kinda sitch
        }

        if (!clazz.isAnnotationPresent(Serializable.class)) {
            return null;
        }

        final Supplier<T> supplier = () -> {
            try {
                return clazz.getConstructor().newInstance();
            } catch (
                InstantiationException
                | IllegalAccessException
                | IllegalArgumentException
                | InvocationTargetException
                | NoSuchMethodException
                | SecurityException e
            ) {
                e.printStackTrace();
                return null;
            }
        };

        return AutoSerializeParser.build(clazz, supplier);
    }
}
