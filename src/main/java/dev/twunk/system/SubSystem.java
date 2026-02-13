package dev.twunk.system;

import dev.twunk.plugin.ModPlugin;
import dev.twunk.system.easy.ISubSystem;
import java.util.ArrayList;
import javax.annotation.Nonnull;

public abstract class SubSystem implements ISubSystem {

    @Nonnull
    private final ArrayList<ISubSystem> subSystems = new ArrayList<>();

    public void addSubSystem(ISubSystem system) {
        this.subSystems.add(system);
    }

    @Override
    public void registerTo(ModPlugin plugin) {
        for (var system : subSystems) {
            system.registerTo(plugin);
        }
    }
}
