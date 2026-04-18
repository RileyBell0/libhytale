package dev.twunk.hytale;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.system.ISystem;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.plugin.registry.CodecMapRegistry.Assets;
import com.hypixel.hytale.server.core.universe.world.WorldProvider;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.twunk.annotations.Serializable;
import dev.twunk.interfaces.events.IOnAddRemove;
import dev.twunk.interfaces.events.IOnTick;
import dev.twunk.interfaces.methods.IQuery;
import dev.twunk.lib.AutoBuilderCodec;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import javax.annotation.Nullable;

// Simple wrapper around JavaPlugin to make behaviour less annoying...
public abstract class HytalePlugin extends JavaPlugin {

    private enum InferredECSType {
        Unknown,
        Chunk,
        Entity,
        SomeOtherTypeIDontKnow
    }

    @SuppressWarnings("null")
    private static final HytaleLogger console = HytaleLogger.forEnclosingClass();

    public HytalePlugin(final JavaPluginInit init) {
        super(init);
        console.atInfo().log("Initializing plugin " + this.getName());
        LibHytale.init(this);
    }

    @Override
    protected final void setup0() {
        console.atInfo().log("Setting up plugin " + this.getName());
        super.setup0();
    }

    /**
     * Register the given system to the plugin
     * @param system
     */
    public final void register(final ISystem<ChunkStore> system) {
        this.getChunkStoreRegistry().registerSystem(system);
    }

    /**
     * Register the given system to the plugin
     * @param system
     */
    public final void registerSystem(final ISystem<ChunkStore> system) {
        this.getChunkStoreRegistry().registerSystem(system);
    }

    /**
     * Register event listeners to the provided instance
     *
     * Requires a query on which to setup the event drivers themselves
     *
     * objectThatImplementsEventListenerMethodsThatICanCallFromSubSystems
     */
    public final void register(IQuery<?> instance) {}

    @Nullable
    public static final Class<?> getClassFromType(Type t) {
        if (t instanceof Class) {
            return (Class<?>) t;
        } else if (t instanceof ParameterizedType) {
            return (Class<?>) ((ParameterizedType) t).getRawType();
        } else {
            throw new RuntimeException("Pretty sure this state is impossible");
        }
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public static final Class<? extends Component<?>> getClassFromTypeIfExtendsComponent(Type t) {
        Class<?> innerClass = getClassFromType(t);

        if (!Component.class.isAssignableFrom(innerClass)) {
            return null;
        }

        return (Class<? extends Component<?>>) innerClass;
    }

    @Nullable
    private static final Type getFirstTypeArgThatExtendsComponent(Class<? extends Component<?>> clazz) {
        for (var innerType : clazz.getGenericInterfaces()) {
            if (innerType == null) {
                throw new RuntimeException("Surprise null val in getGenericInterfaces");
            }

            Class<?> innerClass = getClassFromTypeIfExtendsComponent(innerType);
            if (innerClass != null) {
                return innerType;
            }
        }
        throw new RuntimeException("Shouldn't happen if i got my logic right.askdfmawe");
    }

    private static final ArrayList<Type> buildHeirarchy(Class<? extends Component> componentClass) {
        ArrayList<Type> heirarchy = new ArrayList<>();
        var innerComponentClass = componentClass;

        // until we get to the lowest level of Component.class we're going to keep going
        while (innerComponentClass != Component.class && innerComponentClass != null) {
            // Get the next type arg that gets us closer to Component
            var innerType = getFirstTypeArgThatExtendsComponent((Class) innerComponentClass);
            if (innerType == null) {
                throw new RuntimeException("Failed to follow up the tree where <Component> comes from");
            }

            // SAVE that type arg
            heirarchy.add(innerType);

            // move onto that type arg and we'll loop to keep going up the tree
            innerComponentClass = getClassFromTypeIfExtendsComponent(innerType);
            if (innerComponentClass == null) {
                throw new RuntimeException(
                    "Failed to get class of type " + innerType + " | rileys debug code: iasdfoawef"
                );
            }
        }

        return heirarchy;
    }

    private static final ArrayList<ParameterizedType> reverseAndTrimTypeHeirarchy(ArrayList<Type> heirarchy) {
        var reversed = new ArrayList<ParameterizedType>();
        for (var i = heirarchy.size() - 1; i >= 0; i--) {
            if (!(heirarchy.get(i) instanceof ParameterizedType)) {
                break;
            }
            reversed.add((ParameterizedType) heirarchy.get(i));
        }
        return reversed;
    }

    @Nullable
    private static final InferredECSType inferECSTypeForComponent(Class<?> clazz) {
        var interfaceTypes = clazz.getGenericInterfaces();
        for (var currType : interfaceTypes) {
            if (currType == null) {
                throw new RuntimeException("not sure why but curr type is null");
            }

            var interfaceClass = (Class) getClassFromTypeIfExtendsComponent(currType);
            if (interfaceClass == null) {
                continue;
            }

            // Get the heirarchy ABOVE this place
            ArrayList<Type> heirarchy = new ArrayList<>();
            heirarchy.add(currType);
            if (interfaceClass == Component.class) {
                heirarchy.addAll(buildHeirarchy(interfaceClass));
            }

            // before we process it, we'll check if the base class is a raw type
            var componentType = heirarchy.get(heirarchy.size() - 1);
            if (componentType instanceof Class && componentType == Component.class) {
                return InferredECSType.Unknown;
            }

            // flip it and trim the bloated extra subclasses that give us no information
            var reversed = reverseAndTrimTypeHeirarchy(heirarchy);
            if (reversed.size() == 0) {
                throw new RuntimeException("not sure why but reversed size was 0");
            }

            // No matter what there SHOULD be a type in the first spot.
            var componentArgs = reversed.removeFirst().getActualTypeArguments();
            if (componentArgs.length == 0) {
                throw new RuntimeException(
                    "This shouldn't be possible, we must have a Component<> in the first slot by now due to prior guards"
                );
            }
            var typeVarInComponent = componentArgs[0];
            var typeVarReceivedByComponent = typeVarInComponent;
            if (!(typeVarInComponent instanceof TypeVariable)) {
                // incredibly, raw types of Component fallback to using ChunkStore somehow, idk how but i'll take it
                typeVarReceivedByComponent = typeVarInComponent;
            } else {
                // in the case that we weren't lucky enough to have the type hard-coded as a given ECSStore for us
                // we'll look backwards until we can either determine what one to use, or give up
                for (var nextInHeirarchy : reversed) {
                    // Join 1x way up the tree towards component
                    var typeParamsForBaseClass = ((Class<?>) nextInHeirarchy.getRawType()).getTypeParameters();
                    var typeArgsReceivedByOurImplementationOfBaseClass = nextInHeirarchy.getActualTypeArguments();
                    for (var j = 0; j < typeParamsForBaseClass.length; j++) {
                        var nameSeenByChild = typeParamsForBaseClass[j];

                        if (nameSeenByChild == typeVarReceivedByComponent) {
                            typeVarReceivedByComponent = typeArgsReceivedByOurImplementationOfBaseClass[j];
                            break;
                        }
                    }
                }
            }

            if (typeVarReceivedByComponent == null || typeVarReceivedByComponent instanceof TypeVariable) {
                return InferredECSType.Unknown;
            } else {
                Class<?> a = getClassFromType(typeVarReceivedByComponent);
                if (a == null) {
                    throw new RuntimeException("THIS SHOULD NOT HAPPEN");
                } else if (a == ChunkStore.class) {
                    return InferredECSType.Chunk;
                } else if (a == EntityStore.class) {
                    return InferredECSType.Entity;
                } else {
                    return InferredECSType.SomeOtherTypeIDontKnow;
                }
            }
        }
        return InferredECSType.Unknown;
    }

    /**
     * Register event listeners for components of the given type. Note: this will
     * setup systems to call the methods defined ON your component of type T
     *
     * classOfYourComponentThatImplementsEventListenerMethodsThatICanCall
     */
    public final <ECS_TYPE extends WorldProvider, T> void register(Class<T> clazz) {
        if (Interaction.class.isAssignableFrom(clazz)) {
            @SuppressWarnings("unchecked")
            var asInteraction = (Class<? extends Interaction>) clazz;

            this.registerInteraction(asInteraction);
        }

        if (Component.class.isAssignableFrom(clazz)) {
            registerComponent((Class) clazz);
        }
    }

    public final <ECS_TYPE extends WorldProvider, T extends Component<?>> void registerComponent(Class<T> clazz) {
        var inferred = inferECSTypeForComponent(clazz);
        if (inferred == null) {
            console
                .atSevere()
                .log(
                    " ->| FAILED TO INFER ECS type of " + inferred + " for " + clazz.getSimpleName() + "(" + clazz + ")"
                );
            console.atSevere().log("  >| COMPONENT WAS NOT ADDED TO ANY REGISTRY");
            return;
        }

        switch (inferred) {
            case InferredECSType.Entity:
                console
                    .atInfo()
                    .log(
                        " ->| INFERRED ECS type  " +
                            String.format("%-8s", "<" + inferred + ">") +
                            "  for " +
                            clazz.getSimpleName() +
                            " (" +
                            clazz +
                            ")"
                    );
                LibHytale.ENTITY_REGISTRY.bindEventListeners(this, (Class) clazz);
                break;
            case InferredECSType.Chunk:
                console
                    .atInfo()
                    .log(
                        " ->| INFERRED ECS type  " +
                            String.format("%-8s", "<" + inferred + ">") +
                            "  for " +
                            clazz.getSimpleName() +
                            " (" +
                            clazz +
                            ")"
                    );
                LibHytale.CHUNK_REGISTRY.bindEventListeners(this, (Class) clazz);
                break;
            case InferredECSType.Unknown:
                console
                    .atWarning()
                    .log(" ->| INFERRED ECS type  <Common> for " + clazz.getSimpleName() + " (" + clazz + ")");
                LibHytale.ENTITY_REGISTRY.bindEventListeners(this, (Class) clazz);
                LibHytale.CHUNK_REGISTRY.bindEventListeners(this, (Class) clazz);
                break;
            default:
                console
                    .atSevere()
                    .log(
                        " ->| FAILED TO INFER ECS type of " +
                            inferred +
                            " for " +
                            clazz.getSimpleName() +
                            "(" +
                            clazz +
                            ")"
                    );
                console.atSevere().log("  >| COMPONENT WAS NOT ADDED TO ANY REGISTRY");
                break;
        }
    }

    protected final <ECS_TYPE extends WorldProvider, T extends Component<ECS_TYPE>> void initCommonSystemsFor(
        Class<T> clazz,
        ComponentType<ECS_TYPE, T> componentType
    ) {
        if (!clazz.isAnnotationPresent(Serializable.class)) {
            return;
        }
        // need to make a hashmap for annotations

        if (Component.class.isAssignableFrom(clazz)) {
            if (IOnTick.class.isAssignableFrom(clazz)) {
                // var config = getSystemConfig(clazz, ITickComponent.class);
                // new AutoBlockTickSystem(componentType).registerTo(this);
            }

            if (IOnAddRemove.class.isAssignableFrom(clazz)) {
                // var config = getSystemConfig(clazz, IOnAddRemoveComponent.class);
                // new AutoBlockLifetimeSystem(componentType).registerTo(this);
            }
        }
    }

    public <T extends Component<ChunkStore>> ComponentType<ChunkStore, T> registerChunkComponent(
        BuilderCodec<T> codec
    ) {
        return LibHytale.CHUNK_REGISTRY.registerComponent(this, codec);
    }

    public <T extends Component<ChunkStore>> ComponentType<ChunkStore, T> registerChunkComponent(Class<T> clazz) {
        return LibHytale.CHUNK_REGISTRY.registerComponent(this, clazz);
    }

    public <T extends Component<EntityStore>> ComponentType<EntityStore, T> registerEntityComponent(
        BuilderCodec<T> codec
    ) {
        return LibHytale.ENTITY_REGISTRY.registerComponent(this, codec);
    }

    public <T extends Component<EntityStore>> ComponentType<EntityStore, T> registerEntityComponent(Class<T> clazz) {
        return LibHytale.ENTITY_REGISTRY.registerComponent(this, clazz);
    }

    /**
     * Register the specified component via codec. Does NOT setup
     * system/initialiser.
     * Useful especially for non-ticking components
     *
     * If you want that to be auto-registered, call `registerTickingComponent`
     * instead
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public <T extends Component> void registerCommonComponent(final Class<T> clazz) {
        final var defaultId = clazz.getName();
        final BuilderCodec<T> codec = AutoBuilderCodec.tryGetCodec(clazz);
        if (codec == null || !BuilderCodec.class.isAssignableFrom(codec.getClass())) {
            throw new RuntimeException("Failed to get codec for class " + clazz);
        }

        console
            .atInfo()
            .log("Adding component for class " + clazz.getSimpleName() + " (" + clazz + ", " + defaultId + ")");
        if (defaultId == null) {
            throw new RuntimeException("Failed to get classname while registering component with codec " + codec);
        }

        // Store our component in the global register
        final var chunkComponent = this.getChunkStoreRegistry().registerComponent(clazz, defaultId, codec);
        LibHytale.registerChunkComponentType(chunkComponent, clazz, defaultId);

        final var entityComponent = this.getEntityStoreRegistry().registerComponent(clazz, defaultId, codec);
        LibHytale.registerEntityComponentType(entityComponent, clazz, defaultId);

        this.initCommonSystemsFor(clazz, chunkComponent);
        this.initCommonSystemsFor(clazz, entityComponent);
    }

    public <T extends Interaction> Assets<Interaction, ?> registerInteraction(final Class<T> clazz) {
        final var defaultId = clazz.getName();
        if (defaultId == null) {
            throw new RuntimeException("Failed to get classname while registering interaction with class " + clazz);
        }

        return registerInteraction(clazz, defaultId);
    }

    public <T extends Interaction> Assets<Interaction, ?> registerInteraction(final Class<T> clazz, final String id) {
        final BuilderCodec<T> codec = AutoBuilderCodec.tryGetCodec(clazz);
        if (codec == null || !BuilderCodec.class.isAssignableFrom(codec.getClass())) {
            throw new RuntimeException("Failed to get codec for class " + clazz);
        }

        return registerInteraction(codec, id);
    }

    public <T extends Interaction> Assets<Interaction, ?> registerInteraction(final BuilderCodec<T> codec) {
        final Class<T> myClass = codec.getInnerClass();
        final var defaultId = myClass.getName();
        if (defaultId == null) {
            throw new RuntimeException("Failed to get classname while registering interaction with codec " + codec);
        }

        return registerInteraction(codec, defaultId);
    }

    public <T extends Interaction> Assets<Interaction, ?> registerInteraction(
        final BuilderCodec<T> codec,
        final String id
    ) {
        final Class<T> myClass = codec.getInnerClass();

        console
            .atInfo()
            .log("Adding Interaction for class " + myClass.getSimpleName() + " (" + myClass + ", " + id + ")");

        return this.getCodecRegistry(Interaction.CODEC).register(id, myClass, codec);
    }
}
