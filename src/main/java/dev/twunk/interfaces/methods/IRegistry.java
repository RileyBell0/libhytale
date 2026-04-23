package dev.twunk.interfaces.methods;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.system.ISystem;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.universe.world.WorldProvider;
import javax.annotation.Nullable;

/**
 * Hytale seperates the components and systems i can register out by two types
 * - ChunkStore
 * - EntityStore
 *
 * Notably, that makes it quite annoying for defining common utilities like any
 * subsystem for entities (onEntityAdded, onEntityRemove, onTick).
 *
 * Given this, i'd love to not have to define mutliple copies of the exact same thing.
 *
 * So, since my subsystems simply run a parent class instance that it receives,
 * something that implements IRegistry can be used for EITHER (in an entity store case
 * it'll return the `EntityStore` versions of a plugin's register methods, in a chunk store
 * case it'll do the same but for chunks etc)
 */
public interface IRegistry<ECS_TYPE extends WorldProvider> {
    @Nullable
    public <T extends Component<ECS_TYPE>> ComponentType<ECS_TYPE, T> getComponentType(Class<T> componentClass);

    @Nullable
    public ComponentType<ECS_TYPE, ? extends Component<ECS_TYPE>> getComponentType(String componentClass);

    public <T extends Component<ECS_TYPE>> void registerComponentType(
        ComponentType<ECS_TYPE, T> componentType,
        Class<T> myClass,
        String id
    );

    public <T extends Component<ECS_TYPE>> ComponentType<ECS_TYPE, T> registerComponent(
        JavaPlugin plugin,
        BuilderCodec<T> codec
    );

    public <T extends Component<ECS_TYPE>> ComponentType<ECS_TYPE, T> registerComponent(
        JavaPlugin plugin,
        Class<T> clazz
    );

    public void registerSystem(JavaPlugin plugin, ISystem<ECS_TYPE> system);

    public <T extends IQuery<ECS_TYPE>> void bindEventListeners(JavaPlugin plugin, T listener);

    // this one is interesting, should be the same as the above method basically except calling the newUninitialised method instead
    // without the query as thats just gonna be Query.and(componentType);
    public <T extends Component<ECS_TYPE>> void bindEventListeners(JavaPlugin plugin, Class<T> componentClass);
}
