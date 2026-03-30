package dev.twunk.hytale.refs;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

/**
 * Implements the SAME methods as Ref<ChunkStore> BUT also will include a bunch
 * more methods from Utils so that you can just take a ref and access the stuff
 * you want without having to go through a billion processes
 * @see AnyRef
 * @see Ref (Ref<ChunkStore>)
 */
public class EntityRef extends AnyRef<EntityStore> {

    public EntityRef(@Nonnull Ref<EntityStore> ref) {
        super(ref);
    }

    @Nonnull
    @Override
    public String toString() {
        return "EntityRef{" + super.toString() + "}";
    }
}
