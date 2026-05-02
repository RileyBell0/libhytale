package dev.twunk.lib.registry;

import dev.twunk.hytale.interfaces.event.IOnAddRemove;
import dev.twunk.hytale.interfaces.event.IOnBlockTick;
import dev.twunk.hytale.interfaces.event.IOnScheduledTick;
import dev.twunk.hytale.interfaces.event.IOnTick;
import dev.twunk.hytale.interfaces.event.IOnUniverseTick;
import dev.twunk.hytale.interfaces.event.IOnWorldTick;
import java.util.LinkedHashSet;
import java.util.SequencedSet;
import java.util.Set;

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
public abstract class EventOrderInferrer {

    @SuppressWarnings("null")
    public static final Set<Class<?>> EVENT_INTERFACES = Set.of(
        IOnWorldTick.class,
        IOnUniverseTick.class,
        IOnAddRemove.class,
        IOnBlockTick.class,
        IOnTick.class,
        IOnScheduledTick.class
    );

    public static final SequencedSet<Class<?>> analyze(Class<?> clazz, Set<Class<?>> knownEventInterfaces) {
        SequencedSet<Class<?>> res = new LinkedHashSet<>();

        // Add all the superclass info in first
        var superClass = clazz.getSuperclass();
        if (superClass != null) {
            for (var val : analyze(superClass, knownEventInterfaces)) {
                res.addLast(val);
            }
        }

        // then each interface on the current class
        for (var iClass : clazz.getInterfaces()) {
            if (iClass == null) {
                continue;
            }

            if (knownEventInterfaces.contains(iClass)) {
                res.addLast(iClass);
            } else {
                for (var val : analyze(iClass, knownEventInterfaces)) {
                    res.addLast(val);
                }
            }
        }

        return res;
    }
}
