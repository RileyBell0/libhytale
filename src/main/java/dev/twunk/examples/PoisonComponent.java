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

@Serializable // auto-generates a Codec for you
public class PoisonComponent implements Component<EntityStore>, IOnTick<EntityStore>, IGroup<EntityStore> {

    @OnRegister
    @SuppressWarnings("null")
    private static ComponentType<EntityStore, PoisonComponent> componentType = null;

    public static ComponentType<EntityStore, PoisonComponent> getComponentType() {
        return PoisonComponent.componentType;
    }

    // ////////////////////////////////////////////////////////////////////////
    // Component setup (previously - and currently - `PoisonComponent`)
    // ////////////////////////////////////////////////////////////////////////

    // Fields annotated with @Serialize will be added to the generated codec
    private @Serialize float damagePerTick = 5f;
    private @Serialize float tickInterval = 1.0f;
    private @Serialize int remainingTicks = 10;
    private @Serialize float elapsedTime = 0;

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

    // ////////////////////////////////////////////////////////////////////////
    // Event setup (previously in `PoisonSystem`)
    // ////////////////////////////////////////////////////////////////////////

    /**
     * Event handler: `IOnTick`
     * Run every tick for your actual component(s), ON your actual component(s)
     */
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

    /**
     * Defines the group for your Event Handler(s)
     */
    @Nullable
    @Override
    public SystemGroup<EntityStore> getGroup() {
        return DamageModule.get().getGatherDamageGroup();
    }
}
