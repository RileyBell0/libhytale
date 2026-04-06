package dev.twunk.hytale.refs;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.WorldProvider;
import dev.twunk.hytale.utils.ComponentUtils;
import javax.annotation.Nullable;

/**
 * Implements the SAME methods as Ref<ECS_STORE> BUT also will include a bunch
 * more methods from Utils so that you can just take a ref and access the stuff
 * you want without having to go through a billion processes
 */
public class AnyRef<ECS_STORE extends WorldProvider> extends Ref<ECS_STORE> {

    @Nullable
    protected World world;

    public AnyRef(Ref<ECS_STORE> ref) {
        super(ref.getStore());
    }

    @Nullable
    public <T extends Component<ECS_STORE>> T getComponent(@Nullable ComponentType<ECS_STORE, T> componentType) {
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
