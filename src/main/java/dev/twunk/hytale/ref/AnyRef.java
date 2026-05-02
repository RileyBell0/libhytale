package dev.twunk.hytale.ref;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.LibHytaleRefWrapper;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.WorldProvider;
import dev.twunk.hytale.utils.ComponentUtils;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AnyRef<ECS_TYPE extends WorldProvider> extends LibHytaleRefWrapper<ECS_TYPE> {

    /** World that the entity is in (lazily evaluated) */
    @Nullable
    protected World world;

    public AnyRef(Ref<ECS_TYPE> ref) {
        super(ref);
    }

    public static <T extends WorldProvider> AnyRef<T> of(Ref<T> ref) {
        return new AnyRef<>(ref);
    }

    protected static <T extends WorldProvider> Ref<T> getInnerRef(AnyRef<T> ref) {
        return ref._ref;
    }

    // ////////////////////////////////////////////////////////////////////////
    // \/======================\/-  Methods  -\/==========================\/ //
    // ///////////////`/////////////////////////////////////////////////////////

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
    @Nonnull
    public String toString() {
        return "AnyRef{" + super.toString() + "}";
    }
}
