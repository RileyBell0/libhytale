package dev.twunk.lib.component;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import dev.twunk.hytale.codec.auto.Serializable;
import dev.twunk.hytale.codec.auto.Serialize;
import dev.twunk.hytale.interfaces.component.OnRegister;

@Serializable
public class TestComponent implements Component<ChunkStore> {

    @OnRegister
    @SuppressWarnings("null")
    private static ComponentType<ChunkStore, TestComponent> componentType;

    public static ComponentType<ChunkStore, TestComponent> getComponentType() {
        return TestComponent.componentType;
    }

    @Serialize
    private int value;

    public final TestComponent setValue(final int val) {
        this.value = val;
        return this;
    }

    public final int getValue() {
        return this.value;
    }

    @SuppressWarnings("MethodDoesntCallSuperMethod")
    public TestComponent clone() {
        return new TestComponent();
    }
}
