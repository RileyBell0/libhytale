package dev.twunk.hytale;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.Nullable;
import org.eclipse.jdt.annotation.NonNull;

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

    public static int logindent = 0;

    public static String prefix() {
        var res = "";
        for (var i = 0; i < logindent / 2; i++) {
            res += "   | ";
        }
        return res;
    }

    public static void logit(String msg) {
        System.out.println(prefix() + msg);
    }

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

    private static final AnalysisReturn analyzeClass(Class<?> componentClass, int index, Class<?> clazz) {
        if (clazz.getName() != "dev.twunk.plugin.tests.TestBlockRefComponent") {
            return new AnalysisReturn();
        }
        var finalRes = new AnalysisReturn();
        logit("Analysing " + clazz + " to (" + componentClass + ")");

        var superClass = componentClass.getGenericSuperclass();

        if (superClass != null) {
            logit("- " + clazz + " has superclass " + superClass);
            finalRes.append(analyzeType(componentClass, index, superClass));
        } else {
            logit("- No superclass");
        }
        for (var val : clazz.getGenericInterfaces()) {
            if (val == null) {
                continue;
            }
            logit("- Type param: " + val);

            var interfaceRes = analyzeType(componentClass, index, val);
            logit("- Returned: " + interfaceRes);
            finalRes.append(interfaceRes);
        }

        // finally, need to analyse results
        logit(finalRes.toString());
        return finalRes;
    }

    private static final AnalysisReturn analyzeType(Class<?> componentClass, int index, Type type) {
        logindent += 2;
        logit("TYPE: " + type);
        // figure out the underlying class of the type
        Class<?> clazz = getClassFromType(type);
        if (clazz == null) {
            logindent -= 2;
            return new AnalysisReturn();
        }

        // found it - ground case
        if (clazz == componentClass) {
            var res = new AnalysisReturn();
            var val = (ParameterizedType) type;
            res.current.add(val.getActualTypeArguments()[index]);
            logit("FOUND CLASS!!!  " + clazz);

            logindent -= 2;
            return res;
        }

        // recursively search through each interface defined on our class
        var subSearchRes = new AnalysisReturn();
        logit("Analysing interfaces on " + clazz);
        for (var val : clazz.getGenericInterfaces()) {
            if (val == null) {
                continue;
            }

            logit("  Analysing " + val);
            var asdf = analyzeType(componentClass, index, val);
            logit("  RETURNED " + asdf);
            subSearchRes.append(asdf);
        }

        // also recursively search up to the next superclass (and its interfaces etc etc)
        var superClass = clazz.getGenericSuperclass();
        if (superClass != null) {
            subSearchRes.append(analyzeType(componentClass, index, superClass));
        }

        // we'll keep track of all the promising generics we've lost along the way
        var res = new AnalysisReturn();
        res.lost.addAll(subSearchRes.lost);
        if (!(type instanceof ParameterizedType)) {
            res.lost.addAll(subSearchRes.current);
            logindent -= 2;
            return res;
        }

        // Final step: loop through "current' in search and compare against us, if we have a matching type we'll add OURS to current, otherwise we'll add it to lost
        var ourClassTypes = clazz.getTypeParameters();
        var ourResolvedTypeArguments = ((ParameterizedType) type).getActualTypeArguments();
        // subClassType is CLOSER to component than we are, so we don't want to keep it unless it's already a resolved type
        for (var subClassGeneric : subSearchRes.current) {
            // and we'll sus the current ones and figure out what we plan to do with them
            if (!(subClassGeneric instanceof TypeVariable)) {
                // if it's NOT a type variable, this means something concrete was received, meaning
                // we can stop looking down this path and save this
                //
                // We keep looking in general since we might find something MORE specific, really we could propell this
                // up the tree but i'm not really sure how this works so i'll go for a full approach first then cut back
                // where i can after
                res.finalised.add(subClassGeneric);
                continue;
            }

            // if our parent type isn't parameterized we can't keep following any of the current generic types up the tree, so we end here
            if (!(type instanceof ParameterizedType)) {
                res.finalised.add(subClassGeneric);
                continue;
            }

            // next up, we'll look backwards at OUR type arguments to figure out if this comes from one of ours, if so, we'll add OUR type argument

            // so, we have type X and we want to figure out which one it is

            // remember:
            // genericA<A1> genericB<B1>
            // becomes
            // genericA<Concrete>
            // genericB<A1>
            // so our next in heirarchy SHOULD be a type from our type arguments

            // in the case that we weren't lucky enough to have the type hard-coded as a given ECSStore for us
            // we'll look backwards until we can either determine what one to use, or give up
            // Join 1x way up the tree towards component
            Type foundGeneric = null;
            for (var i = 0; i < ourClassTypes.length; i++) {
                var ourClassGeneric = ourClassTypes[i];

                if (ourClassGeneric != subClassGeneric) {
                    continue;
                }

                // if we found a mathcing generic from the subclass type variable
                // we can use that index to figure out which actual generic
                // would have been passed where
                //
                // e.g.
                // if class definitions are
                // - MyClass<A1, A2, A3> extends SubClass<A3, A2>
                // - SubClass<B1, B2> extends Component<B2>
                // - Component<T>
                // this goes
                //
                // Hey when we looked into subclass we found it still extends component
                // then when we looked at component we saw it had the type param B2
                // So then now we're back in SubClass we want to figure out which one of our type args was used to extend Component,
                // - e.g. we have at this stage the info SubClass<?, ?> extends Component<B2>
                // then we loop through to go hey was C the first type arg or the second?
                // SubClass<B1, ?> extends Component<B2>
                // ok it wasnt that one, our class generic (B!) was not equal to sub class generic (B2)
                // then we find out
                // SubClass<B1, B2> extends Component<B2>
                // and we go
                // ok is B2 == B2? woah ok sick so we KNOW that our SubClass must have passed its type param "B2" in index 1
                // to the Component
                //
                // then we can go ok what was ACTUALLY passed to our SubClass generics themselves
                // and we find that our class definition for the actual types are as follows
                // - MyClass<String, ChunkStore, EntityStore>
                // - SubClass<A3, A2>
                // - Component<B2>
                // thus we can simply go ok so since I component received b2, i'll see that i join on B2 in SubClass
                // and return whatever subclass got instead, so subclass got A2 for its B2 param (A2 received in index 1)
                // so we use that
                //
                // the failure case would be if component received D3 instead of B2, cause then we go up the tree saying "hey does anyone see D3?" and nope nobody has so our inference fails
                foundGeneric = ourResolvedTypeArguments[i];
                break;
            }

            if (foundGeneric == null) {
                res.lost.add(subClassGeneric);

                System.out.println("DID NOT FIND GENERIC AAAH " + subClassGeneric);
            } else {
                res.current.add(foundGeneric);
            }
        }

        logindent -= 2;
        return res;
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
        var prediction = analyzeClass(Component.class, 0, subClass);

        var allRes = new ArrayList<>();
        allRes.addAll(prediction.current);
        allRes.addAll(prediction.finalised);
        allRes.addAll(prediction.lost);
        // if (!(type instanceof ParameterizedType)) {
        //     var t = subSearchRes.current.toArray(Type[]::new)[0];
        //     if (t instanceof TypeVariable) {
        //         for (var bound : ((TypeVariable<?>) t).getBounds()) {
        //             res.lost.add(bound);
        //         }
        //     }
        //     res.lost.addAll(subSearchRes.current);
        //     logindent -= 2;
        //     return res;
        // }
        // if (prediction == null || prediction instanceof TypeVariable) {
        //     return null;
        // } else {
        //     Class<?> a = getClassFromType(prediction);
        //     if (a == null) {
        //         throw new RuntimeException("THIS SHOULD NOT HAPPEN");
        //     } else {
        //         return a;
        //     }
        // }

        return ChunkStore.class;
    }
}

class AnalysisReturn {

    public Set<Type> finalised = new HashSet<>();
    public Set<Type> current = new HashSet<>();
    public Set<Type> lost = new HashSet<>();

    public AnalysisReturn append(AnalysisReturn other) {
        this.current.addAll(other.current);
        this.lost.addAll(other.lost);
        this.finalised.addAll(other.finalised);
        return this;
    }

    @Override
    public String toString() {
        var res = "AnalysisReturn" + this.hashCode() + " { ";
        res += "current: [";
        for (var val : this.current) {
            res += val + ", ";
        }
        res += "], ";
        res += "finalised: [";
        for (var val : this.finalised) {
            res += val + ", ";
        }
        res += "], ";
        res += "lost: [ ";
        for (var val : this.lost) {
            res += val + ", ";
        }
        res += "]";
        res += "}";
        return res;
    }
}
