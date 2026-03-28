package dev.twunk.hytale.interfaces.component;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.server.OpenContainerInteraction;
import com.hypixel.hytale.server.core.universe.world.meta.state.ItemContainerState;
import dev.twunk.hytale.interfaces.IContainer;

// import dev.twunk.hytale.components.IContainer;

/**
 * Basically just a component that satisfies the main basic methods needed for
 * running a container (getting the container, the capacity, etc)
 *
 * My code
 * @see IContainer - My interface for methods I need containers to fulfil to
 *                   show them in GUI
 *
 * Hytale's code
 * @see ItemContainerState       - The "BlockState" (deprecated) that seems to
 *                                 store container information
 * @see OpenContainerInteraction - Their interaction that opens containers
 */
public interface IContainerComponent<ECS_TYPE> extends IContainer, Component<ECS_TYPE> {}
