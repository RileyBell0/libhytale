package dev.twunk.subsystem;

import com.hypixel.hytale.component.system.ISystem;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import dev.twunk.common.IQuery;
import dev.twunk.plugin.ModPlugin;
import java.lang.invoke.MethodHandles;
import javax.annotation.Nonnull;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.dynamic.scaffold.subclass.ConstructorStrategy;

/**
 * Helper interface that means you can easily register sub systems by default
 *
 * Only useful for sub systems that extend actual hytale systems.
 *
 * No touchy.
 */
public interface ISubSystem extends ISystem<ChunkStore> {
    public default void registerTo(ModPlugin plugin) {
        plugin.getChunkStoreRegistry().registerSystem(this);
    }

    public static <T extends ISubSystem, Parent extends IQuery> T __newSubSystem(
        @Nonnull final Class<T> callerClass,
        @Nonnull final Parent parent
    ) {
        final Class<? extends T> clazz = new ByteBuddy()
            .subclass(callerClass, ConstructorStrategy.Default.IMITATE_SUPER_CLASS)
            .make()
            .load(callerClass.getClassLoader(), ClassLoadingStrategy.UsingLookup.of(MethodHandles.lookup()))
            .getLoaded();

        try {
            var constructor = clazz.getDeclaredConstructor(parent.getClass());
            return constructor.newInstance(parent);
        } catch (Exception e) {
            throw new RuntimeException(
                "RILEY, you called a constructor that doesnt exist for " + callerClass + " | " + parent + " | " + e
            );
        }
    }
}
