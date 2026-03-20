package dev.twunk.subsystem;

import com.hypixel.hytale.component.system.ISystem;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import dev.twunk.interfaces.methods.IQuery;
import dev.twunk.plugin.ModPlugin;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Modifier;
import javax.annotation.Nonnull;
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
public interface ISubSystem extends ISystem<ChunkStore> {
    public static final HytaleLogger.Api console = HytaleLogger.forEnclosingClass().atInfo();

    public default void registerTo(ModPlugin plugin) {
        plugin.getChunkStoreRegistry().registerSystem(this);
    }

    public static <T extends ISubSystem, Parent extends IQuery> T __newSubSystem(
        @Nonnull final Class<T> subSystemClass,
        @Nonnull final Class<Parent> parentInterface,
        @Nonnull final Parent parent
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
                .withParameters(parentInterface)
                .intercept(MethodCall.invoke(subSystemClass.getDeclaredConstructor(parentInterface)).withArgument(0))
                // Build the class
                .make()
                .load(subSystemClass.getClassLoader(), ClassLoadingStrategy.UsingLookup.of(MethodHandles.lookup()))
                .getLoaded()
                // call the constructor
                .getDeclaredConstructor(parentInterface)
                .newInstance(parent);
        } catch (Exception e) {
            throw new RuntimeException(
                "RILEY, you called a constructor that doesnt exist for:" +
                    "\n- CALLER class: " +
                    subSystemClass.getName() +
                    "\n- PARENT:       " +
                    parent +
                    "\n- EXCEPTION:    " +
                    e +
                    "\n" +
                    e.getCause()
            );
        }
    }
}
