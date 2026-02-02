package dev.twunk.ticking.component;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import dev.twunk.ticking.response.TickResponse;
import java.util.HashMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

// "BlockType": {
//   "TickProcedure": {
//     "Type": "InherentTickProcedure"
//   },
//   ...
// }
// ^^ the above is an option. I dont like it but it's an option

public interface ITickingComponent extends Component<ChunkStore> {
    public static final HashMap<Class<? extends Component<ChunkStore>>, ComponentType<ChunkStore, ? extends Component<ChunkStore>>> registeredComponents = new HashMap<Class<? extends Component<ChunkStore>>, ComponentType<ChunkStore, ? extends Component<ChunkStore>>>();

    /**
     * Ticking a block? Just need some damn code to run in game while testing? put
     * it in here!
     *
     * NOTE: once you're done testing, move this across into a System to handle
     * this the way we're actually meant to be handling this
     *
     * remember: components don't tick, systems do, and systems "just so happen"
     * to be able to filter themselves down to entites with your component
     *
     * instead of "i want my component to tick", nah, you want a SYSTEM that
     * queries just your component to tick
     */
    @Nullable
    public default TickResponse onTick(
            @Nonnull World world,
            @Nonnull WorldChunk wc,
            @Nonnull CommandBuffer<ChunkStore> commandBuffer,
            int worldX,
            int worldY,
            int worldZ,
            int blockId) {
        HytaleLogger.forEnclosingClass().atInfo()
                .log("Ticked block at (" + worldX + ", " + worldY + ", " + worldZ + " )");
        return null;
    }

    /**
     *
     * Gets the type of the component. WOO
     */
    @Nonnull
    public default ComponentType<ChunkStore, ? extends ITickingComponent> getComponentType() {
        return getComponentType(this.getClass());
    }

    /**
     * WARNING: Only ever call this AFTER your plugin's setup function (e.g.
     * plugin's
     * start function, or really anywhere else in the code)
     *
     * This is designed to be easy to use throughout the code, so we assume it to
     * always succeed
     * and it WILL always succeed as long as you register your component before
     * calling it
     */
    @Nonnull
    @SuppressWarnings("unchecked")
    public static <T extends Component<ChunkStore>> ComponentType<ChunkStore, T> getComponentType(
            Class<T> componentClass) {
        var componentType = registeredComponents.get(componentClass);
        if (componentType == null) {
            throw new RuntimeException(
                    "Called getComponentType on class " + componentClass + " before initialising said class");
        }

        // casting is safe as long as i haven't stuffed something up
        return (ComponentType<ChunkStore, T>) componentType;
    }

    /**
     * Registers the component type with the static map that stores
     * all that goodness for us. ITickingComponent gets to know
     * about EVERYTHING above it WOOOO
     */
    public static <T extends Component<ChunkStore>> void registerComponentType(
            Class<T> myClass,
            ComponentType<ChunkStore, T> componentType) {
        registeredComponents.put(myClass, componentType);
    }
}
