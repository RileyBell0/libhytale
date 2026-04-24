package dev.twunk.hytale.ref;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.WorldProvider;
import java.util.ArrayList;
import javax.annotation.Nullable;

/**
 * Implements the SAME methods as Ref<ECS_TYPE> BUT also will include a bunch
 * more methods from Utils so that you can just take a ref and access the stuff
 * you want without having to go through a billion processes
 */
public class TrackedRef<ECS_TYPE extends WorldProvider> extends AnyRef<ECS_TYPE> {

    /** World that the entity is in (lazily evaluated) */
    @Nullable
    protected World world;

    private ArrayList<TrackedRef<ECS_TYPE>> holder;

    ///////////////////////////////////////////////////////////////////////////
    // \/======================\/-  Methods  -\/==========================\/ //
    ///////////////////////////////////////////////////////////////////////////

    public TrackedRef(Ref<ECS_TYPE> ref, ArrayList<TrackedRef<ECS_TYPE>> holder) {
        super(ref);
        this.holder = holder;
    }

    public void drop() {
        this.holder.remove(this);
    }

    @Override
    public String toString() {
        return "TrackedRef{" + super.toString() + "}";
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj instanceof TrackedRef) {
            return (TrackedRef<?>) obj == this;
        }
        return false;
    }
}
