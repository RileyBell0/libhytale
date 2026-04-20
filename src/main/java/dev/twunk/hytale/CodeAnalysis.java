package dev.twunk.hytale;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import javax.annotation.Nullable;

/**
 * I've got this filled with runtime exceptions atm since its just used in server startup
 *
 * so, conveniently as long as the server boots these will be fine, plus, the goal is to crash loudly
 * rather than fail silently atm
 *
 * also don't mind all the mentions to "Component" in here, just take that to mean the base `Class<T>` cause
 * yeah i cbf going through rn updating all the names of the vars, that's a step for when i'm doing
 * a more thorough passover at the end of this project
 */
public class CodeAnalysis {

    @Nullable
    public static final Class<?> getClassFromType(Type t) {
        if (t instanceof Class) {
            return (Class<?>) t;
        } else if (t instanceof ParameterizedType) {
            return (Class<?>) ((ParameterizedType) t).getRawType();
        } else {
            // pretty sure this should never come up
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public static final <T> Class<? extends T> getClassFromTypeIfSubtypeOfProvidedClass(Class<T> clazz, Type t) {
        Class<?> innerClass = getClassFromType(t);
        if (innerClass == null) {
            return null;
        }

        if (!clazz.isAssignableFrom(innerClass)) {
            return null;
        }

        return (Class<? extends T>) innerClass;
    }

    private static final <T> ArrayList<ParameterizedType> getAllTypeArgsThatExtendsProvidedClass(
        Type[] types,
        Class<T> subclass
    ) {
        var res = new ArrayList<ParameterizedType>();
        for (var val : types) {
            if (val == null) {
                // warning - not sure when this can happen or even if it can happen
                continue;
            }

            Class<?> classOfType = getClassFromType(val);
            if (classOfType == null) {
                continue;
            }

            if (!subclass.isAssignableFrom(classOfType)) {
                continue;
            }

            res.add((ParameterizedType) val);
        }

        return res;
    }

    /**
     * Gets all the steps inbetween subclass and class
     *
     * e.g. if class is A, and subclass is C this will return [C, B, A] where
     * - C extends B
     * - B extends A
     */
    private static final <T> ArrayList<Type> buildHeirarchyFromSubClassToClass(
        Class<T> clazz,
        Class<? extends T> subClass
    ) {
        ArrayList<Type> heirarchy = new ArrayList<>();
        var innerSubClass = subClass;

        // keep looking through its interfaces recursively (ish) going towards
        // the parent `clazz`
        while (innerSubClass != clazz && innerSubClass != null) {
            // Get the next type arg that gets us closer to clazz
            var innerType = getAllTypeArgsThatExtendsProvidedClass(innerSubClass.getGenericInterfaces(), clazz).get(0);
            if (innerType == null) {
                throw new RuntimeException("Failed to follow up the tree where <" + clazz + "> comes from");
            }

            // SAVE that type arg
            heirarchy.add(innerType);

            // move onto that type arg and we'll loop to keep going up the tree
            innerSubClass = getClassFromTypeIfSubtypeOfProvidedClass(clazz, innerType);
            if (innerSubClass == null) {
                throw new RuntimeException("Failed to get class of type " + innerType);
            }
        }

        return heirarchy;
    }

    /**
     * Heirarchy might have alot of bloat. We're trying to find the generic's actual
     * value
     *
     * e.g. assume our parent class is ClassA<T> and we're trynna figure out what value of T they used
     *
     * means our heirarchy might be (assuming our class is class F)
     *
     * - ClassF       extends ClassE<?>
     * - ClassE<?>    extends ClassD
     * - ClassD       extends ClassC<?>
     * - ClassC<?>    extends ClassB<?, ?>
     * - ClassB<?, ?> extends ClassA<T>
     * - ClassA<T>
     *
     * with a heirarchy like that we know that anything above D won't help us (e.g. F or E) so we'll just throw those out
     *
     * then we know for certain that the T in ClassA can be accurately determined (pass or fail) from
     * this heirarchy - and we haven't lost any information that could help
     *
     * The only exception to be aware of is that class B could define
     * - ClassB<?, ?> extends ClassA
     * which is legal despite it being a raw type, in which case our heirarchy though legit won't have the type information
     *
     * this is a known issue, and not a solvable one, its actually a feature believe it or not
     * and just one you need to be aware of when using classes that take generics - the parent
     * might not have actually defined them
     *
     * reflection is cool
     *
     * @param heirarchy the heirarchy generated by buildHeirarchyFromSubClassToClass
     * @return a flat straight line of how the class F extends A. In the order
     *         [A, B, C, D, E, F] except
     *         - the length will quite likely be different from the array you passed in
     *         - the length could actually be 0
     *         - if the length IS 0, know that from my current understanding this means a generic
     *           skipped somewhere along the way (they probably did
     *            `ClassB<?, ?> extends ClassA`
     *           instead of
     *            `ClassB<?, ?> extends ClassA<T>`)
     */
    private static final ArrayList<ParameterizedType> reverseAndTrimTypeHeirarchy(ArrayList<Type> heirarchy) {
        var reversed = new ArrayList<ParameterizedType>();
        for (var i = heirarchy.size() - 1; i >= 0; i--) {
            // there's no point keeping something that's not a parameterized type
            if (!(heirarchy.get(i) instanceof ParameterizedType)) {
                break;
            }
            reversed.add((ParameterizedType) heirarchy.get(i));
        }
        return reversed;
    }

    /**
     * Returned value is not constrained by T directly, its constrained by the generic
     * args T can accept, e.g. if T is
     * ClassA<? extends String>
     * then i might return the class String or any class that extends it
     *
     * if i return null, that means i don't know what the type is, i couldn't find it, something
     * failed and it very much could be my logic in here, i'm not in with the java compiler
     * crowd so i really don't know the specifics well enough here to make any bold or
     * concrete statements
     *
     * Plus, if this gives you null when you're not expecting it, if YOU can find what
     * class its used in the generic, odds are my code is wrong
     *
     * if you CAN'T find what class is used in the generic where it failed, my code probably
     * can't either
     */
    @Nullable
    public static final <T> Class<?> inferTypeReceivedByGenericInClassT(Class<T> clazz, Class<? extends T> subClass) {
        var interfaceTypes = subClass.getGenericInterfaces();
        for (var currType : interfaceTypes) {
            if (currType == null) {
                throw new RuntimeException("not sure why but curr type is null");
            }

            var interfaceClass = getClassFromTypeIfSubtypeOfProvidedClass(clazz, currType);
            if (interfaceClass == null) {
                continue;
            }

            // Get the heirarchy ABOVE this place
            ArrayList<Type> heirarchy = new ArrayList<>();
            heirarchy.add(currType);
            if (interfaceClass == clazz) {
                heirarchy.addAll(buildHeirarchyFromSubClassToClass(clazz, interfaceClass));
            }

            // before we process it, we'll check if the base class is a raw type
            var componentType = heirarchy.get(heirarchy.size() - 1);
            if (componentType instanceof Class && componentType == clazz) {
                return null;
            }

            // flip it and trim the bloated extra subclasses that give us no information
            var reversed = reverseAndTrimTypeHeirarchy(heirarchy);
            if (reversed.size() == 0) {
                throw new RuntimeException("not sure why but reversed size was 0");
            }

            // No matter what there SHOULD be a type in the first spot.
            var componentArgs = reversed.removeFirst().getActualTypeArguments();
            if (componentArgs.length == 0) {
                throw new RuntimeException(
                    "This shouldn't be possible, we must have a Component<> in the first slot by now due to prior guards"
                );
            }
            var typeVarInComponent = componentArgs[0];
            var typeVarReceivedByComponent = typeVarInComponent;
            if (!(typeVarInComponent instanceof TypeVariable)) {
                // incredibly, raw types of Component fallback to using ChunkStore somehow, idk how but i'll take it
                typeVarReceivedByComponent = typeVarInComponent;
            } else {
                // in the case that we weren't lucky enough to have the type hard-coded as a given ECSStore for us
                // we'll look backwards until we can either determine what one to use, or give up
                for (var nextInHeirarchy : reversed) {
                    // Join 1x way up the tree towards component
                    var typeParamsForBaseClass = ((Class<?>) nextInHeirarchy.getRawType()).getTypeParameters();
                    var typeArgsReceivedByOurImplementationOfBaseClass = nextInHeirarchy.getActualTypeArguments();
                    for (var j = 0; j < typeParamsForBaseClass.length; j++) {
                        var nameSeenByChild = typeParamsForBaseClass[j];

                        if (nameSeenByChild == typeVarReceivedByComponent) {
                            typeVarReceivedByComponent = typeArgsReceivedByOurImplementationOfBaseClass[j];
                            break;
                        }
                    }
                }
            }

            if (typeVarReceivedByComponent == null || typeVarReceivedByComponent instanceof TypeVariable) {
                return null;
            } else {
                Class<?> a = getClassFromType(typeVarReceivedByComponent);
                if (a == null) {
                    throw new RuntimeException("THIS SHOULD NOT HAPPEN");
                } else {
                    return a;
                }
            }
        }
        return null;
    }
}
