/**
 * Understanding the below code
 *
 * ### Annotations
 * @Serializable auto-generates a Codec for you
 * @Serialize    adds the given field to the auto-generated codec
 * @OnRegister   stores the generated ComponentType in the static field when
 *               it's created during component registration
 *
 * ### Running `onTick`
 * When registering your component a unique EntityTickingSystem is spun up
 * for your component.
 *
 * The (simplified) implementation of said ticking system is as follows
 *
 * ```java
 * @Override
 * public void tick(
 *     float dt,
 *     int index,
 *     ArchetypeChunk<ECS_TYPE> archetypeChunk,
 *     Store<ECS_TYPE> store,
 *     CommandBuffer<ECS_TYPE> commandBuffer
 * ) {
 *     var ref = archetypeChunk.getReferenceTo(index);
 *     var component = ref.getStore().getComponent(ref, componentType);
 *
 *     component.onTick(dt, ref, commandBuffer);
 * }
 * ```
 */

package dev.twunk.examples;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.SystemGroup;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageCause;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageModule;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageSystems;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.twunk.hytale.OnRegister;
import dev.twunk.hytale.codec.auto.Serializable;
import dev.twunk.hytale.codec.auto.Serialize;
import dev.twunk.hytale.interfaces.config.IGroup;
import dev.twunk.hytale.interfaces.event.IOnTick;
import dev.twunk.hytale.ref.AnyRef;
import javax.annotation.Nullable;

// @Serializable - Auto-generate a Codec from fields marked with @Serialize
@Serializable
public class PoisonComponent implements Component<EntityStore>, IOnTick<EntityStore>, IGroup<EntityStore> {

    // \/==================\/-  Component Type  -\/===================\/ //

    // set during component registration
    @OnRegister
    @SuppressWarnings("null")
    private static ComponentType<EntityStore, PoisonComponent> componentType;

    public static ComponentType<EntityStore, PoisonComponent> getComponentType() {
        return PoisonComponent.componentType;
    }

    // \/==================\/-  Component State  -\/==================\/ //

    @Serialize
    private float damagePerTick = 5f;

    @Serialize
    private float tickInterval = 1.0f;

    @Serialize
    private int remainingTicks = 10;

    @Serialize
    private float elapsedTime = 0;

    @Nullable
    @Override
    public Component<EntityStore> clone() {
        var other = new PoisonComponent();

        other.damagePerTick = this.damagePerTick;
        other.tickInterval = this.tickInterval;
        other.remainingTicks = this.remainingTicks;
        other.elapsedTime = this.elapsedTime;

        return other;
    }

    // \/==================\/-  Event handler(s)  -\/=================\/ //

    @Override
    public void onTick(float dt, AnyRef<EntityStore> ref, CommandBuffer<EntityStore> commandBuffer) {
        this.elapsedTime += dt;

        if (this.elapsedTime >= this.tickInterval) {
            this.elapsedTime = 0;

            @SuppressWarnings({ "deprecation", "null" })
            Damage damage = new Damage(Damage.NULL_SOURCE, DamageCause.OUT_OF_WORLD, this.damagePerTick);
            DamageSystems.executeDamage(ref, commandBuffer, damage);

            this.remainingTicks--;
        }

        if (this.remainingTicks <= 0) {
            commandBuffer.removeComponent(ref, PoisonComponent.getComponentType());
        }
    }

    @Nullable
    @Override
    public SystemGroup<EntityStore> getGroup() {
        return DamageModule.get().getGatherDamageGroup();
    }
}
