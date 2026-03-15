package dev.twunk.subsystem.base;

import static org.objectweb.asm.Opcodes.*;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.RefSystem;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import dev.twunk.subsystem.ISubSystem;
import dev.twunk.subsystem.base.interfaces.IEntityLifetimeSystem;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.objectweb.asm.*;

/**
 * Tiny Subsystem to simply tell our parent system when we added/removed entities
 * that match our parent's query
 *
 * GOAL: Need to know when entities load/unload (and optionally why they got added/removed)
 *
 * REQUIRES:
 * - N/A (this is a leaf)
 * PRODUCES:
 * - ILifetimeSystem runner
 */
public abstract class EntityLifetimeSubSystem extends RefSystem<ChunkStore> implements ISubSystem {

    private static final AtomicInteger ID = new AtomicInteger();

    /** warning: AI generated function, needs verification by someone that actually
     * understands this and wasn't coding this at 2am before their day job */
    private static EntityLifetimeSubSystem createNewClass(IEntityLifetimeSystem parent) {
        try {
            String className = "dev/twunk/subsystem/base/EntityLifetimeSubSystem$Generated" + ID.incrementAndGet();
            String superName = Type.getInternalName(EntityLifetimeSubSystem.class);

            ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);

            cw.visit(V17, ACC_PUBLIC, className, null, superName, null);

            /*
             * Constructor:
             *
             * public Generated(IEntityLifetimeSystem parent) {
             *     super(parent);
             * }
             */
            MethodVisitor mv = cw.visitMethod(
                ACC_PUBLIC,
                "<init>",
                "(Ldev/twunk/subsystem/base/interfaces/IEntityLifetimeSystem;)V",
                null,
                null
            );

            mv.visitCode();

            mv.visitVarInsn(ALOAD, 0);
            mv.visitVarInsn(ALOAD, 1);

            mv.visitMethodInsn(
                INVOKESPECIAL,
                superName,
                "<init>",
                "(Ldev/twunk/subsystem/base/interfaces/IEntityLifetimeSystem;)V",
                false
            );

            mv.visitInsn(RETURN);

            mv.visitMaxs(0, 0);
            mv.visitEnd();

            cw.visitEnd();

            byte[] bytes = cw.toByteArray();
            MethodHandles.Lookup lookup = MethodHandles.lookup();
            Class<?> clazz = lookup.defineClass(bytes);

            Constructor<?> ctor = clazz.getConstructor(IEntityLifetimeSystem.class);

            return (EntityLifetimeSubSystem) ctor.newInstance(parent);
        } catch (Throwable t) {
            throw new RuntimeException("Failed to generate EntityLifetimeSubSystem", t);
        }
    }

    private final @Nonnull IEntityLifetimeSystem parent;
    private final @Nullable Query<ChunkStore> query;

    public static <T extends EntityLifetimeSubSystem> EntityLifetimeSubSystem create(
        @Nonnull final IEntityLifetimeSystem parent
    ) {
        return createNewClass(parent);
    }

    EntityLifetimeSubSystem(@Nonnull final IEntityLifetimeSystem parent) {
        this.parent = parent;
        this.query = parent.getQuery();
    }

    @Override
    public void onEntityAdded(
        @Nonnull Ref<ChunkStore> ref,
        @Nonnull AddReason reason,
        @Nonnull Store<ChunkStore> store,
        @Nonnull CommandBuffer<ChunkStore> commandBuffer
    ) {
        parent.onEntityAdded(ref, reason, store, commandBuffer);
    }

    @Override
    public void onEntityRemove(
        @Nonnull Ref<ChunkStore> ref,
        @Nonnull RemoveReason reason,
        @Nonnull Store<ChunkStore> store,
        @Nonnull CommandBuffer<ChunkStore> commandBuffer
    ) {
        parent.onEntityRemove(ref, reason, store, commandBuffer);
    }

    @Override
    public Query<ChunkStore> getQuery() {
        return this.query;
    }
}
