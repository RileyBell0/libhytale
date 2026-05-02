package com.hypixel.hytale.component;

import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.WorldProvider;
import dev.twunk.hytale.utils.ComponentUtils;
import javax.annotation.Nonnull;
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

    private Ref<ECS_TYPE> ref;

    public AnyRef(Ref<ECS_TYPE> ref) {
        this.ref = ref;
        super(ref.getStore(), ref.getIndex());
    }

    // ////////////////////////////////////////////////////////////////////////
    // Had to sneak into hytale's package here to overwrite these
    // ///////////////`/////////////////////////////////////////////////////////

    @Nonnull
    public static final <T extends WorldProvider> AnyRef<T> of(@Nonnull Ref<T> ref) {
        return new AnyRef<>(ref);
    }

    @Override
    final void setIndex(int index) {
        this.ref.setIndex(index);
    }

    @Override
    final void invalidate() {
        this.ref.invalidate(null);
    }

    @Override
    final void invalidate(@Nullable Throwable invalidatedBy) {
        this.ref.invalidate(invalidatedBy);
    }

    @Nonnull
    @Override
    public final Store<ECS_TYPE> getStore() {
        return this.ref.getStore();
    }

    @Override
    public final int getIndex() {
        return this.ref.getIndex();
    }

    @Override
    public final int validate(@Nonnull Store<ECS_TYPE> store) {
        return this.ref.validate(store);
    }

    @Override
    public final void validate() {
        this.ref.validate();
    }

    @Override
    public final boolean isValid() {
        return this.ref.isValid();
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

    @Nonnull
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
