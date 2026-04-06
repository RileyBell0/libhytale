package dev.twunk.lib.lifetime;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.World;
import java.util.ArrayList;
import javax.annotation.Nullable;

/**
 * Information about the block that's being ticked, cached between calls to
 * make calling ticks way easier
 *
 * it's IN MEMORY ONLY -> NOT EVER GOING TO BE STORED TO DISK. don't assume anything
 * here to be stable between launching ur server again, i really haven't looked closely
 * into this for that kinda stuff, don't trust it, i make no guarantees.
 *
 * without this, i'd have to (every tick)
 * - use the ref to our block to get its "BlockStateInfo" component
 * - use the "BlockStateInfo" to get the local coords of the block (within its
 * chunk)
 * - use the "BlockStateInfo" component to get the chunk the block is in
 * - use the chunk to convert the local coords of the block to global coords
 * - use the chunk to get a ref to the world itself
 * and THEN finally i could run a regular tick method for a component.
 *
 * e.g. code i used to use
 *
 * <pre>
 * {@code
 *  var ref = archetypeChunk.getReferenceTo(index);
 *
 *  var info = BlockUtils.getInfo(commandBuffer, ref);
 *  if (info == null) {
 *      return;
 *  }
 *  var worldChunk = BlockUtils.getWorldChunk(commandBuffer, info);
 *  if (worldChunk == null) {
 *      return;
 *  }
 *  var world = worldChunk.getWorld();
 *  if (world == null) {
 *      return;
 *  }
 *  var coords = BlockUtils.getGlobalCoords(worldChunk, info);
 * }
 * </pre>
 */
public class TrackedEntity<ECS_STORE> {

    private static long nextLifetimeId = 0;

    /**
     * this is a serial integer that is MEMORY ONLY -> it WILL NOT be the same
     * next time the entity is loaded off disk. do not save it to disk. Simply useful
     * for quick lookups on entities (adding/removing them). makes my caching WAAAY easier
     * and faster
     */
    public final long lifetimeId;

    /**
     * The world that your entity is in
     */
    public final World world;

    /**
     * The ref for your entity
     */
    public final Ref<ECS_STORE> ref;

    private ArrayList<TrackedEntity<ECS_STORE>> currentAreaRef;

    public TrackedEntity(World world, Ref<ECS_STORE> ref, ArrayList<TrackedEntity<ECS_STORE>> currentAreaRef) {
        this.lifetimeId = ++nextLifetimeId;
        this.world = world;
        this.ref = ref;
        this.currentAreaRef = currentAreaRef;
    }

    public void drop() {
        this.currentAreaRef.remove(this);
    }

    /**
     * We'll say they're the same if they have the same ref. should make removal easy
     */
    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj instanceof TrackedEntity) {
            return ((TrackedEntity<?>) obj).ref == this.ref;
        }
        return false;
    }
}
