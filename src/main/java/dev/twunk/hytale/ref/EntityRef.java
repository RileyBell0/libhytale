package dev.twunk.hytale.ref;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

/**
 * Implements the SAME methods as Ref<ChunkStore> BUT also will include a bunch
 * more methods from Utils so that you can just take a ref and access the stuff
 * you want without having to go through a billion processes
 * @see AnyRef
 * @see Ref (Ref<ChunkStore>)
 */
public final class EntityRef extends AnyRef<EntityStore> {

    public EntityRef(AnyRef<EntityStore> ref) {
        this(AnyRef.getInnerRef(ref));
    }

    public EntityRef(Ref<EntityStore> ref) {
        super(ref);
    }

    @Override
    public final String toString() {
        return "EntityRef{" + super.toString() + "}";
    }
}
