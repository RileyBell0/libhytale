package dev.twunk.utils;

import dev.twunk.interfaces.ModPlugin;
import dev.twunk.ticking.response.TickContinue;
import dev.twunk.ticking.response.TickSleep;
import dev.twunk.ticking.response.TickStop;

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
        plugin.registerComponent(TickContinue.CODEC);
        plugin.registerComponent(TickStop.CODEC);
        plugin.registerComponent(TickSleep.CODEC);
        hasRegisteredTickComponents = true;
    }
}
