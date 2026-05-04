package dev.twunk.examples.poison;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.SystemGroup;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageCause;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageModule;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageSystems;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.twunk.hytale.interfaces.event.IOnTick;
import dev.twunk.hytale.ref.AnyRef;
import javax.annotation.Nullable;

public class PoisonSystem implements IOnTick<EntityStore> {

    @Override
    public void onTick(float dt, AnyRef<EntityStore> ref, CommandBuffer<EntityStore> commandBuffer) {
        var poison = ref.getComponent(PoisonComponent.getComponentType()); // You could also just call PoisonComponent.getComponentType() instead of taking in the passed in variable here.
        if (poison == null) {
            return;
        }

        poison.addElapsedTime(dt);
        if (poison.getElapsedTime() >= poison.getTickInterval()) {
            poison.resetElapsedTime();
            Damage damage = new Damage(Damage.NULL_SOURCE, DamageCause.OUT_OF_WORLD, poison.getDamagePerTick());
            DamageSystems.executeDamage(ref, commandBuffer, damage);
            poison.decrementRemainingTicks();
        }
        if (poison.isExpired()) {
            commandBuffer.removeComponent(ref, PoisonComponent.getComponentType());
        }
    }

    @Nullable
    @Override
    public SystemGroup<EntityStore> getGroup() {
        return DamageModule.get().getGatherDamageGroup();
    }

    @Override
    public Query<EntityStore> getQuery() {
        return Query.and(PoisonComponent.getComponentType());
    }
}
