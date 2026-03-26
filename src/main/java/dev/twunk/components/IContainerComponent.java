package dev.twunk.components;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.server.OpenContainerInteraction;
import com.hypixel.hytale.server.core.universe.world.meta.state.ItemContainerState;

// import dev.twunk.components.IContainer;

/**
 * My version of hytale's BlockState system that handles inventories
 *
 * i'm really not a fan of theirs (mainly because its deprecated) so here's a
 * version that will work long-term.
 *
 * They're not gonna deprecate components any time soon
 *
 * My code
 * @see IContainer  My interface for methods I need containers to fulfil to
 *                  show them in GUI
 *
 * Hytale's code
 * @see ItemContainerState        The "BlockState" (deprecated) that seems to
 *                                store container information
 * @see OpenContainerInteraction  Their interaction that opens containers
 */
public interface IContainerComponent<ECS_TYPE> extends IContainer, Component<ECS_TYPE> {}
