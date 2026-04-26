package dev.twunk.hytale.event.composite;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.server.core.universe.world.WorldProvider;

/**
 * It's literally just a marker, nothing more, and abstract since its not intended to be used on its own. I need a different ComponentType for each
 * system with a different ID on each so yeah... this serves as a base for bytebuddy to dupe for each system that needs it, and THEN
 * it only really serves as a flag
 */
public abstract class ActivelyTickingComponent<ECS_TYPE extends WorldProvider> implements Component<ECS_TYPE> {

    public abstract ActivelyTickingComponent<ECS_TYPE> clone();
}
