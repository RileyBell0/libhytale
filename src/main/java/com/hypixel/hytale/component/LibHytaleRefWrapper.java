package com.hypixel.hytale.component;

import com.hypixel.hytale.server.core.universe.world.WorldProvider;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Implements the SAME methods as Ref<ECS_TYPE> BUT also will include a bunch
 * more methods from Utils so that you can just take a ref and access the stuff
 * you want without having to go through a billion processes
 */
public class LibHytaleRefWrapper<ECS_TYPE extends WorldProvider> extends Ref<ECS_TYPE> {

    @Nonnull
    protected final Ref<ECS_TYPE> _ref;

    public LibHytaleRefWrapper(@Nonnull Ref<ECS_TYPE> ref) {
        super(ref.getStore(), ref.getIndex());
        this._ref = ref;
    }

    protected Ref<ECS_TYPE> getInnerRef() {
        return this._ref;
    }

    // ////////////////////////////////////////////////////////////////////////
    // Had to sneak into hytale's package here to overwrite these
    // ///////////////`/////////////////////////////////////////////////////////

    @Override
    final void setIndex(int index) {
        this._ref.setIndex(index);
    }

    @Override
    final void invalidate() {
        this._ref.invalidate(null);
    }

    @Override
    final void invalidate(@Nullable Throwable invalidatedBy) {
        this._ref.invalidate(invalidatedBy);
    }

    @Nonnull
    @Override
    public final Store<ECS_TYPE> getStore() {
        return this._ref.getStore();
    }

    @Override
    public final int getIndex() {
        return this._ref.getIndex();
    }

    @Override
    public final int validate(@Nonnull Store<ECS_TYPE> store) {
        return this._ref.validate(store);
    }

    @Override
    public final void validate() {
        this._ref.validate();
    }

    @Override
    public final boolean isValid() {
        return this._ref.isValid();
    }
}
