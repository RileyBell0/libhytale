package dev.twunk.hytale.interfaces;

import com.hypixel.hytale.component.SystemGroup;
import com.hypixel.hytale.component.dependency.Dependency;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.universe.world.WorldProvider;
import dev.twunk.hytale.interfaces.config.IEventConfig;
import dev.twunk.lib.LibHytaleException;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Set;
import javax.annotation.Nullable;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.NamingStrategy;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.dynamic.scaffold.subclass.ConstructorStrategy;
import net.bytebuddy.implementation.MethodCall;
import net.bytebuddy.utility.RandomString;

/**
 * Helper interface that means you can easily register sub systems by default
 *
 * Only useful for sub systems that extend actual hytale systems.
 *
 * No touchy.
 */
public interface IEventDriver<
    ECS_TYPE extends WorldProvider
> extends IRegistryProvider<ECS_TYPE>, IEventConfig<ECS_TYPE> {
    static final @Nullable HytaleLogger.Api console = HytaleLogger.forEnclosingClass().atInfo();

    /**
     * It will ALWAYS return a new class, one that didn't exist before. don't abuse this method. it's prefixed with
     * two underscores for a good reason - making you question yourself before deciding to use this...
     */
    @SuppressWarnings("null")
    public static <T> Class<? extends T> __duplicateClass(Class<T> subSystemClass, Class<?>... constructorArgTypes) {
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
                        protected String name(TypeDescription superClass) {
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
                "RILEY, you called a constructor that doesnt exist for:" +
                    "\n- CALLER class: " +
                    subSystemClass.getName() +
                    "\n- ARG TYPES:       " +
                    constructorArgTypes +
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
    public static <T> Constructor<? extends T> __dupeClassAndGetConstructor(
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
                        protected String name(TypeDescription superClass) {
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
                "RILEY, you called a constructor that doesnt exist for:" +
                    "\n- CALLER class: " +
                    subSystemClass.getName() +
                    "\n- ARG TYPES:       " +
                    constructorArgTypes +
                    "\n- EXCEPTION:    " +
                    e +
                    "\n" +
                    e.getCause()
            );
        }
    }

    public static <T> Constructor<T> __getConstructor(Class<T> clazz, Class<?>... args) {
        // get the classes for the objects
        ArrayList<Class<?>> classes = new ArrayList<>();
        for (var arg : args) {
            classes.add(arg.getClass());
        }

        try {
            var constructor = clazz.getDeclaredConstructor(classes.toArray(Class<?>[]::new));
            if (constructor == null) {
                throw new LibHytaleException("ERROR: shouldn't have been null but was asfhao8wh23r");
            }
            return constructor;
        } catch (IllegalArgumentException | NoSuchMethodException | SecurityException e) {
            e.printStackTrace();
            throw new LibHytaleException(e);
        }
    }

    public static <T> T __construct(Constructor<T> constructor, Object... args) {
        try {
            var res = constructor.newInstance(args);
            if (res == null) {
                throw new LibHytaleException("ERROR: shouldn't have been null but was asfhao8wh23r");
            }
            return res;
        } catch (
            InstantiationException
            | IllegalAccessException
            | IllegalArgumentException
            | InvocationTargetException
            | SecurityException e
        ) {
            e.printStackTrace();
            throw new LibHytaleException(e);
        }
    }

    public abstract void onRegister(JavaPlugin plugin);

    public abstract boolean addDependency(Dependency<ECS_TYPE> dependency);
    public abstract void setDependencies(Set<Dependency<ECS_TYPE>> dependencies);
    public abstract void setGroup(@Nullable SystemGroup<ECS_TYPE> group);
}
