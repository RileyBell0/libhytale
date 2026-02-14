package dev.twunk.utils;

import dev.twunk.plugin.ModPlugin;
import dev.twunk.subsystem.composite._EntityScheduledTickStateComponent;
import dev.twunk.utils.tick.TickSchedulerComponent;

public abstract class TwunkLib {

    private static boolean hasRegisteredTickComponents = false;

    // Called automatically when you load a mod plugin
    public static void init(ModPlugin plugin) {
        registerTickComponents(plugin);
    }

    public static void registerTickComponents(ModPlugin plugin) {
        if (hasRegisteredTickComponents) {
            return;
        }

        // per-system ticking
        TickSchedulerComponent.COMPONENT_TYPE = plugin.registerComponent(TickSchedulerComponent.CODEC);

        // component-based ticking
        _EntityScheduledTickStateComponent.Active.COMPONENT_TYPE = plugin.registerComponent(
            _EntityScheduledTickStateComponent.Active.CODEC
        );
        _EntityScheduledTickStateComponent.Stopped.COMPONENT_TYPE = plugin.registerComponent(
            _EntityScheduledTickStateComponent.Stopped.CODEC
        );
        _EntityScheduledTickStateComponent.Unknown.COMPONENT_TYPE = plugin.registerComponent(
            _EntityScheduledTickStateComponent.Unknown.CODEC
        );
        _EntityScheduledTickStateComponent.Sleeping.COMPONENT_TYPE = plugin.registerComponent(
            _EntityScheduledTickStateComponent.Sleeping.CODEC
        );
        hasRegisteredTickComponents = true;
    }
}
