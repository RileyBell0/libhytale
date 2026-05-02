package dev.twunk.hytale.ref;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.WorldProvider;
import dev.twunk.hytale.utils.ComponentUtils;
import javax.annotation.Nullable;

/**
 * Implements the SAME methods as Ref<ECS_TYPE> BUT also will include a bunch
 * more methods from Utils so that you can just take a ref and access the stuff
 * you want without having to go through a billion processes
 */
public class AnyRef<ECS_TYPE extends WorldProvider> extends Ref<ECS_TYPE> {

    /** World that the entity is in (lazily evaluated) */
    @Nullable
    protected World world;

    // ////////////////////////////////////////////////////////////////////////
    // \/======================\/-  Methods  -\/==========================\/ //
    // ////////////////////////////////////////////////////////////////////////

    public AnyRef(Ref<ECS_TYPE> ref) {
        super(ref.getStore(), ref.getIndex());
    }

    @Nullable
    public <T extends Component<ECS_TYPE>> T getComponent(@Nullable ComponentType<ECS_TYPE, T> componentType) {
        if (componentType == null) {
            return null;
        }

        return ComponentUtils.get(this, componentType);
    }

    public final World getWorld() {
        if (this.world != null) {
            return this.world;
        }

        this.world = this.getStore().getExternalData().getWorld();

        return this.world;
    }

    @Override
    public String toString() {
        return "AnyRef{" + super.toString() + "}";
    }
}
