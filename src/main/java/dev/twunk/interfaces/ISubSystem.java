package dev.twunk.interfaces;

import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.ISystem;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.universe.world.WorldProvider;
import dev.twunk.hytale.HytalePlugin;
import dev.twunk.interfaces.methods.IRegistry;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Modifier;
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
public interface ISubSystem<ECS_STORE extends WorldProvider> extends ISystem<ECS_STORE> {
    public static final @Nullable HytaleLogger.Api console = HytaleLogger.forEnclosingClass().atInfo();

    public default void registerTo(final HytalePlugin plugin) {
        this.getRegistry().registerSystem(plugin, this);
    }

    public abstract IRegistry<ECS_STORE> getRegistry();

    @SuppressWarnings("null")
    public static <ECS_STORE extends WorldProvider, T extends ISubSystem<ECS_STORE>, Listener> T __newSubSystem(
        final Class<T> subSystemClass,
        final Class<? extends Listener> eventListenerInterface,
        final Listener listener,
        final Query<ECS_STORE> query
    ) {
        try {
            // Define constraints/link to parent class
            return new ByteBuddy()
                .with(
                    new NamingStrategy.PrefixingRandom(subSystemClass.getName()) {
                        @Override
                        protected String name(TypeDescription superClass) {
                            return ISubSystem.class.getName() + "$" + new RandomString().nextString();
                        }
                    }
                )
                .subclass(subSystemClass, ConstructorStrategy.Default.NO_CONSTRUCTORS)
                // Add in the (now public) constructor
                .defineConstructor(Modifier.PUBLIC)
                .withParameters(eventListenerInterface)
                .intercept(
                    MethodCall.invoke(subSystemClass.getDeclaredConstructor(eventListenerInterface)).withArgument(0)
                )
                // Build the class
                .make()
                .load(subSystemClass.getClassLoader(), ClassLoadingStrategy.UsingLookup.of(MethodHandles.lookup()))
                .getLoaded()
                // call the constructor
                .getDeclaredConstructor(eventListenerInterface)
                .newInstance(listener);
        } catch (Exception e) {
            throw new RuntimeException(
                "RILEY, you called a constructor that doesnt exist for:" +
                    "\n- CALLER class: " +
                    subSystemClass.getName() +
                    "\n- PARENT:       " +
                    listener +
                    "\n- EXCEPTION:    " +
                    e +
                    "\n" +
                    e.getCause()
            );
        }
    }
}
