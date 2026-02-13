package dev.twunk.utils;

import dev.twunk.plugin.ModPlugin;
import dev.twunk.system.response.TickBroken;
import dev.twunk.system.response.TickContinue;
import dev.twunk.system.response.TickSleep;
import dev.twunk.system.response.TickStop;

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
        TickContinue.COMPONENT_TYPE = plugin.registerComponent(TickContinue.CODEC);
        TickStop.COMPONENT_TYPE = plugin.registerComponent(TickStop.CODEC);
        TickBroken.COMPONENT_TYPE = plugin.registerComponent(TickBroken.CODEC);
        TickSleep.COMPONENT_TYPE = plugin.registerComponent(TickSleep.CODEC);
        hasRegisteredTickComponents = true;
    }
}
