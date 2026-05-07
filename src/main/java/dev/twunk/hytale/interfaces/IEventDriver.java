package dev.twunk.hytale.interfaces;

import com.hypixel.hytale.component.SystemGroup;
import com.hypixel.hytale.component.dependency.Dependency;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.universe.world.WorldProvider;
import dev.twunk.hytale.interfaces.config.IEventConfig;
import dev.twunk.lib.LibHytaleException;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.NamingStrategy;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.dynamic.scaffold.subclass.ConstructorStrategy;
import net.bytebuddy.implementation.MethodCall;
import net.bytebuddy.utility.RandomString;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

/// Helper interface that means you can easily register sub systems by default
///
/// Only useful for sub systems that extend actual hytale systems.
///
/// No touchy.
public interface IEventDriver<
        ECS_TYPE extends WorldProvider
        > extends IRegistryProvider<ECS_TYPE>, IEventConfig<ECS_TYPE> {
    @Nullable
    HytaleLogger.Api console = HytaleLogger.forEnclosingClass().atInfo();

    /**
     * It will ALWAYS return a new class, one that didn't exist before. don't abuse this method. it's prefixed with
     * two underscores for a good reason - making you question yourself before deciding to use this...
     */
    @SuppressWarnings("null")
    static <T> Class<? extends T> __duplicateClass(Class<T> subSystemClass, Class<?>... constructorArgTypes) {
        int[] indexes = new int[constructorArgTypes.length];
        for (var i = 0; i < constructorArgTypes.length; i++) {
            indexes[i] = i;
        }

        try {
            // Define constraints/link to parent class
            return new ByteBuddy()
                    .with(
                            new NamingStrategy.PrefixingRandom(subSystemClass.getName()) {
                                @Override
                                @Nonnull
                                protected String name(@Nonnull TypeDescription superClass) {
                                    return IEventDriver.class.getName() + "$" + new RandomString().nextString();
                                }
                            }
                    )
                    .subclass(subSystemClass, ConstructorStrategy.Default.NO_CONSTRUCTORS)
                    // Add in the (now public) constructor
                    .defineConstructor(Modifier.PUBLIC)
                    .withParameters(constructorArgTypes)
                    .intercept(
                            MethodCall.invoke(subSystemClass.getDeclaredConstructor(constructorArgTypes)).withArgument(indexes)
                    )
                    // Build the class
                    .make()
                    .load(subSystemClass.getClassLoader(), ClassLoadingStrategy.UsingLookup.of(MethodHandles.lookup()))
                    .getLoaded();
        } catch (Exception e) {
            throw new LibHytaleException(
                    "RILEY, you called a constructor that doesn't exist for:" +
                            "\n- CALLER class: " +
                            subSystemClass.getName() +
                            "\n- ARG TYPES:       " +
                            Arrays.toString(constructorArgTypes) +
                            "\n- EXCEPTION:    " +
                            e +
                            "\n" +
                            e.getCause()
            );
        }
    }

    /**
     * It will ALWAYS return a new class, one that didn't exist before. don't abuse this method. it's prefixed with
     * two underscores for a good reason - making you question yourself before deciding to use this...
     */
    @SuppressWarnings("null")
    static <T> Constructor<? extends T> __dupeClassAndGetConstructor(
            Class<T> subSystemClass,
            Class<?>... constructorArgTypes
    ) {
        int[] indexes = new int[constructorArgTypes.length];
        for (var i = 0; i < constructorArgTypes.length; i++) {
            indexes[i] = i;
        }

        try {
            // Define constraints/link to parent class
            return new ByteBuddy()
                    .with(
                            new NamingStrategy.PrefixingRandom(subSystemClass.getName()) {
                                @Override
                                @Nonnull
                                protected String name(@Nonnull TypeDescription superClass) {
                                    return IEventDriver.class.getName() + "$" + new RandomString().nextString();
                                }
                            }
                    )
                    .subclass(subSystemClass, ConstructorStrategy.Default.NO_CONSTRUCTORS)
                    // Add in the (now public) constructor
                    .defineConstructor(Modifier.PUBLIC)
                    .withParameters(constructorArgTypes)
                    .intercept(
                            MethodCall.invoke(subSystemClass.getDeclaredConstructor(constructorArgTypes)).withArgument(indexes)
                    )
                    // Build the class
                    .make()
                    .load(subSystemClass.getClassLoader(), ClassLoadingStrategy.UsingLookup.of(MethodHandles.lookup()))
                    .getLoaded()
                    .getDeclaredConstructor(constructorArgTypes);
        } catch (Exception e) {
            throw new LibHytaleException(
                    "RILEY, you called a constructor that doesn't exist for:" +
                            "\n- CALLER class: " +
                            subSystemClass.getName() +
                            "\n- ARG TYPES:       " +
                            Arrays.toString(constructorArgTypes) +
                            "\n- EXCEPTION:    " +
                            e +
                            "\n" +
                            e.getCause()
            );
        }
    }

    static <T> Constructor<T> __getConstructor(Class<T> clazz, Class<?>... args) {
        // get the classes for the objects
        ArrayList<Class<?>> classes = new ArrayList<>();
        Collections.addAll(classes, args);

        try {
            return clazz.getDeclaredConstructor(classes.toArray(Class<?>[]::new));
        } catch (IllegalArgumentException | NoSuchMethodException | SecurityException e) {
            HytaleLogger.forEnclosingClass().atSevere().log("Error: " + e);
            throw new LibHytaleException(e);
        }
    }

    static <T> T __construct(Constructor<T> constructor, Object... args) {
        try {
            return constructor.newInstance(args);
        } catch (
                InstantiationException
                | IllegalAccessException
                | IllegalArgumentException
                | InvocationTargetException
                | SecurityException e
        ) {
            HytaleLogger.forEnclosingClass().atSevere().log("Error: " + e);
            throw new LibHytaleException(e);
        }
    }

    void onRegister(JavaPlugin plugin);

    @SuppressWarnings("UnusedReturnValue")
    boolean addDependency(Dependency<ECS_TYPE> dependency);

    void setDependencies(Set<Dependency<ECS_TYPE>> dependencies);

    void setGroup(@Nullable SystemGroup<ECS_TYPE> group);
}
