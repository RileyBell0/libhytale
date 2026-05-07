package dev.twunk.hytale;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.universe.world.WorldProvider;
import dev.twunk.hytale.interfaces.component.OnRegister;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;
import org.checkerframework.checker.nullness.compatqual.NullableType;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;

public class InitComponentType {

    private static final HytaleLogger console = HytaleLogger.forEnclosingClass();

    /// Warning: might throw an error if you set your code up wrong and
    /// are intentionally trying to break my code. so, yeah. don't do that, and you'll
    /// be fine. If you're reading this, you're probably in too deep into the code hahaha,
    /// or you're being quite helpful improving/validating stuff which is much appreciated!
    ///
    /// shoot me a message or submit a PR or really anything if you have feedback,
    /// requests, ideas/improvements, etc
    public static <
            ECS_TYPE extends WorldProvider,
            T extends Component<ECS_TYPE>
            > void trySetAnnotatedComponentType(
            Class<T> clazz,
            Class<ECS_TYPE> storeClass,
            ComponentType<ECS_TYPE, T> componentType
    ) {
        final var field = InitComponentType._getComponentTypeField(clazz, storeClass);
        if (field != null) {
            InitComponentType._setComponentType(field, componentType);
        }
    }

    @Nullable
    private static <ECS_TYPE extends WorldProvider> Field _getComponentTypeField(
            Class<? extends Component<ECS_TYPE>> clazz,
            Class<ECS_TYPE> storeClass
    ) {
        var fields = clazz.getDeclaredFields();
        for (final var field : fields) {
            // Make sure my annotation saying "yeah just set this field for me later" is on the field
            if (!field.isAnnotationPresent(OnRegister.class)) {
                continue;
            }

            // filter: only touch static fields
            if (!Modifier.isStatic(field.getModifiers())) {
                continue;
            }

            // filter: must be of type `ComponentType`
            if (!ComponentType.class.equals(field.getType())) {
                continue;
            }

            // filter: must have type parameters (known due to it being ComponentType, but just being safe)
            var generic = field.getGenericType();
            if (!(generic instanceof ParameterizedType pType)) {
                continue;
            }

            // filter: parameters must be defined AND there must only be 2 of them (again, known due to it being ComponentType, but just being safe)
            var args = pType.getActualTypeArguments();
            if (args.length != 2) {
                continue;
            }
            var storeGeneric = args[0];
            var typeGeneric = args[1];

            // parse the class of the store being used (must be well-defined, or we skip)
            if (!(storeGeneric instanceof Class)) {
                continue;
            }
            if (!storeClass.isAssignableFrom((Class<?>) storeGeneric)) {
                continue;
            }

            // parse the class of the type under the ComponentType<Store, T> and make sure it IS of our type T
            if (!(typeGeneric instanceof Class<?> typeClass)) {
                continue;
            }
            if (!typeClass.equals(clazz)) {
                // this guards against us defining other fields such as
                // private static ComponentType<ChunkStore, superclass/subclass/unrelatedclass> ____
                // as we don't want to modify other fields
                continue;
            }

            // filter: must NOT be final (so I can set it)
            if (Modifier.isFinal(field.getModifiers())) {
                continue;
            }

            // neat, now we have a ComponentType<ChunkStore, T>, just have to make SURE they're OK with us setting this

            // filter: force the field to let us in, if we can't change that, let's just give up
            if (!field.trySetAccessible()) {
                continue;
            }

            // filter: make sure the field is NOT considered nullable (as we're going to be relying on it being registered correctly at any point in time)
            if (
                    field.isAnnotationPresent(Nullable.class) ||
                            field.isAnnotationPresent(NullableDecl.class) ||
                            field.isAnnotationPresent(NullableType.class)
            ) {
                continue;
            }

            // make sure its currently set to `null` (which would be confusing for a nonnull final field, which is
            // exactly the strange marker I plan to use to define that my library CAN modify this)
            try {
                if (field.get(null) != null) {
                    continue;
                }
            } catch (IllegalArgumentException | IllegalAccessException _) {
                continue;
            }

            return field;
        }
        return null;
    }

    private static void _setComponentType(Field field, ComponentType<?, ?> componentType) {
        try {
            field.set(null, componentType);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            console
                    .atSevere()
                    .log(
                            "ERROR - failed to automatically set the component type while registering a component",
                            field,
                            componentType,
                            e
                    );
        }
    }
}
