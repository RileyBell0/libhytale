package dev.twunk.hytale.interfaces.event;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.system.RefSystem;
import com.hypixel.hytale.server.core.universe.world.WorldProvider;
import dev.twunk.hytale.event.OnAddRemove;
import dev.twunk.hytale.ref.AnyRef;

/**
 * Methods for my subsytem version of `RefSystem<ECS_Store>`
 *
 * My code
 * @see OnAddRemove      - The base subsystem that "runs" something with "IEntityLifetime"
 *
 * Hytale's code
 * @see RefSystem - Hytale's underlying system that provides the `onEntityAdded` and `onEntityRemove` events
 */
public interface IOnAddRemove<ECS_TYPE extends WorldProvider> {
    /**
     * Event for when an entity is added/loaded into the world
     *
     * @param ref           The entity that is being adeded/loaded
     * @param reason        WHY the entity was added (SPAWN|LOAD)
     * @param commandBuffer Useful for interacting with the world at large (buffered,
     *                      most of its methods should be ignored tbh unless you're GETTING
     *                      data). Generally you just want to use `commandBuffer.run((store)->{...})`
     *                      and run your MUTATION methods using the `store` within the lambda
     *                      you pass to `commandBuffer.run(...)`
     */
    public default void onAdd(AnyRef<ECS_TYPE> ref, AddReason reason, CommandBuffer<ECS_TYPE> commandBuffer) {}

    /**
     * Event for when an entity is removed/unloaded from the world
     *
     * @param ref           The entity that is being removed/unloaded
     * @param reason        WHY the entity was added (REMOVE|UNLOAD)
     * @param commandBuffer Useful for interacting with the world at large (buffered,
     *                      most of its methods should be ignored tbh unless you're GETTING
     *                      data). Generally you just want to use `commandBuffer.run((store)->{...})`
     *                      and run your MUTATION methods using the `store` within the lambda
     *                      you pass to `commandBuffer.run(...)`
     */
    public default void onRemove(AnyRef<ECS_TYPE> ref, RemoveReason reason, CommandBuffer<ECS_TYPE> commandBuffer) {}
}
