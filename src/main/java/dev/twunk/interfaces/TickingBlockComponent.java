package dev.twunk.interfaces;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.asset.type.blocktick.BlockTickStrategy;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import java.util.HashMap;
import javax.annotation.Nonnull;

public interface TickingBlockComponent extends Component<ChunkStore> {
    static final HytaleLogger.Api console = HytaleLogger.forEnclosingClass().atInfo();

    static HashMap<Class<? extends TickingBlockComponent>, ComponentType<ChunkStore, ? extends TickingBlockComponent>> registeredComponents = new HashMap<Class<? extends TickingBlockComponent>, ComponentType<ChunkStore, ? extends TickingBlockComponent>>();

    // Ticking a block? Just need some damn code to run in game while testing? put
    // it in here!
    @Nonnull
    public default BlockTickStrategy onTick(
            @Nonnull World world,
            @Nonnull WorldChunk wc,
            int worldX,
            int worldY,
            int worldZ,
            int blockId) {
        console.log("Ticked block at (" + worldX + ", " + worldY + ", " + worldZ + " )");
        return BlockTickStrategy.CONTINUE;
    }

    public default ComponentType<ChunkStore, ? extends TickingBlockComponent> getComponentType() {
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
    public static <T extends TickingBlockComponent> ComponentType<ChunkStore, T> getComponentType(
            Class<T> componentClass) {
        var componentType = registeredComponents.get(componentClass);
        if (componentType == null) {
            throw new RuntimeException(
                    "Called getComponentType on class " + componentClass + " before initialising said class");
        }

        // casting is safe as long as i haven't stuffed something up
        return (ComponentType<ChunkStore, T>) componentType;
    }

    public static <T extends TickingBlockComponent> void registerComponentType(
            Class<T> myClass,
            ComponentType<ChunkStore, T> componentType) {
        registeredComponents.put(myClass, componentType);
    }
}
