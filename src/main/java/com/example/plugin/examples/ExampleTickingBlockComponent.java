package com.example.plugin.examples;

import com.example.plugin.interfaces.ModPlugin;
import com.example.plugin.interfaces.TickingBlockComponent;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.asset.type.blocktick.BlockTickStrategy;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ExampleTickingBlockComponent implements TickingBlockComponent {

    private static final HytaleLogger.Api console = HytaleLogger.forEnclosingClass().atInfo();

    private int ticks = 0;

    @Nonnull
    public static final BuilderCodec<ExampleTickingBlockComponent> CODEC = BuilderCodec.builder(
        ExampleTickingBlockComponent.class,
        ExampleTickingBlockComponent::new
    )
        .append(
            new KeyedCodec<Integer>("Ticks", Codec.INTEGER),
            (data, value) -> data.ticks = value,
            data -> data.ticks
        )
        .add()
        .build();

    /**
     * Run actions every tick by
     * - implementing TickingBlockEntity
     * - adding self to TickingInitialiser
     */
    @Nonnull
    public BlockTickStrategy onTick(
        @Nonnull World world,
        @Nonnull WorldChunk wc,
        int worldX,
        int worldY,
        int worldZ,
        int blockId
    ) {
        console.log("Ticked block at (" + worldX + ", " + worldY + ", " + worldZ + " ) " + ++this.ticks + " times");

        return BlockTickStrategy.CONTINUE;
    }

    ////////////////////////////////
    // MY BOILERPLATE BELOW
    /////////////////////////////// s/

    @Nonnull
    public static String Id = ExampleTickingBlockComponent.class.getSimpleName();

    private static ComponentType<ChunkStore, ExampleTickingBlockComponent> componentType;

    public ExampleTickingBlockComponent() {}

    public ExampleTickingBlockComponent(@Nonnull ExampleTickingBlockComponent self) {
        this.ticks = self.ticks;
    }

    @Nullable
    public ExampleTickingBlockComponent clone() {
        return new ExampleTickingBlockComponent(this);
    }

    /**
     * Registers your component to the given plugin.
     *
     * Call `NameOfThisClass.register(this);` in your plugin's setup method
     */
    public static ComponentType<ChunkStore, ExampleTickingBlockComponent> registerToPlugin(@Nonnull ModPlugin plugin) {
        var component = plugin
            .getChunkStoreRegistry()
            .registerComponent(
                ExampleTickingBlockComponent.class,
                ExampleTickingBlockComponent.Id,
                ExampleTickingBlockComponent.CODEC
            );

        // also keep me a copy of the component type after registering
        ExampleTickingBlockComponent.componentType = component;

        plugin.addToRegister(ExampleTickingBlockComponent.Id, component);

        return component;
    }

    /**
     * Registers your component to the given plugin.
     *
     * Call `NameOfThisClass.register(this);` in your plugin's setup method
     */
    public static ComponentType<ChunkStore, ExampleTickingBlockComponent> registerToPluginWithNewSystems(
        @Nonnull ModPlugin plugin
    ) {
        @Nonnull
        var component = plugin
            .getChunkStoreRegistry()
            .registerComponent(
                ExampleTickingBlockComponent.class,
                ExampleTickingBlockComponent.Id,
                ExampleTickingBlockComponent.CODEC
            );

        plugin.addToRegisterWithNewSystems(ExampleTickingBlockComponent.Id, component);

        // also keep me a copy of the component type after registering
        ExampleTickingBlockComponent.componentType = component;

        return component;
    }

    /**
     * WARNING: Only ever call this AFTER your plugin's setup function (e.g. plugin's
     * start function, or really anywhere else in the code)
     *
     * This is designed to be easy to use throughout the code, so we assume it to always succeed
     * and it WILL always succeed as long as you register your component before calling it
     */
    @Nonnull
    public static ComponentType<ChunkStore, ExampleTickingBlockComponent> getComponentType() {
        // As long as you don't break the contract of "actually initialize the block"
        // then this will never fail
        if (ExampleTickingBlockComponent.componentType == null) {
            throw new RuntimeException(
                "Called " +
                    ExampleTickingBlockComponent.class.getName() +
                    ".getComponentType() before plugin was setup.\nHint: make sure to call `" +
                    ExampleTickingBlockComponent.class.getName() +
                    ".registerFor(this);` within your plugin's `setup` function "
            );
        }

        return ExampleTickingBlockComponent.componentType;
    }
}
