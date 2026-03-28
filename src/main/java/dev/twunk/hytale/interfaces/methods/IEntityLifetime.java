package dev.twunk.hytale.interfaces.methods;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.system.RefSystem;
import com.hypixel.hytale.server.core.universe.world.WorldProvider;
import dev.twunk.hytale.interfaces.subsystem.IEntityLifetimeSystem;
import dev.twunk.hytale.subsystem.base.EntityLifetimeSubSystem;
import javax.annotation.Nonnull;

/**
 * Methods for my subsytem version of `RefSystem<ECS_Store>`
 *
 * My code
 * @see IEntityLifetimeSystem        - Additional requirements that an implementor of IEntityLifetime must satisfy
 *                                     in order to register a subsystem to run itself
 * @see EntityLifetimeSubSystem      - The base subsystem that "runs" something with "IEntityLifetime"
 *
 * Hytale's code
 * @see RefSystem - Hytale's underlying system that provides the `onEntityAdded` and `onEntityRemove` events
 */
public interface IEntityLifetime<ECS_STORE extends WorldProvider> {
    /**
     * Event for when an entity is added/loaded into the world
     *
     * @param ref           The entity that is being adeded/loaded
     * @param reason        WHY the entity was added (SPAWN|LOAD)
     * @param store         The store that the entity is held within
     * @param commandBuffer Useful for interacting with the world at large (buffered,
     *                      most of its methods should be ignored tbh unless you're GETTING
     *                      data). Generally you just want to use `commandBuffer.run((store)->{...})`
     *                      and run your MUTATION methods using the `store` within the lambda
     *                      you pass to `commandBuffer.run(...)`
     */
    public void onEntityAdded(
        final @Nonnull Ref<ECS_STORE> ref,
        final @Nonnull AddReason reason,
        final @Nonnull Store<ECS_STORE> store,
        final @Nonnull CommandBuffer<ECS_STORE> commandBuffer
    );

    /**
     * Event for when an entity is removed/unloaded from the world
     *
     * @param ref           The entity that is being removed/unloaded
     * @param reason        WHY the entity was added (REMOVE|UNLOAD)
     * @param store         The store that the entity is/was held within
     * @param commandBuffer Useful for interacting with the world at large (buffered,
     *                      most of its methods should be ignored tbh unless you're GETTING
     *                      data). Generally you just want to use `commandBuffer.run((store)->{...})`
     *                      and run your MUTATION methods using the `store` within the lambda
     *                      you pass to `commandBuffer.run(...)`
     */
    public void onEntityRemove(
        final @Nonnull Ref<ECS_STORE> ref,
        final @Nonnull RemoveReason reason,
        final @Nonnull Store<ECS_STORE> store,
        final @Nonnull CommandBuffer<ECS_STORE> commandBuffer
    );
}
