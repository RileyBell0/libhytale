package dev.twunk.lib.component;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.server.core.universe.world.WorldProvider;
import dev.twunk.hytale.codec.auto.Serializable;

/**
 * It's literally just a marker, nothing more, and abstract since it's not intended to be used on its own. I need a different ComponentType for each
 * system with a different ID on each so yeah... this serves as a base for ByteBuddy to dupe for each system that needs it, and THEN
 * it only really serves as a flag
 */
@Serializable
public class ActivelyTickingComponent<ECS_TYPE extends WorldProvider> implements Component<ECS_TYPE> {

    @SuppressWarnings("MethodDoesntCallSuperMethod")
    @Override
    public Component<ECS_TYPE> clone() {
        return new ActivelyTickingComponent<>();
    }
}
