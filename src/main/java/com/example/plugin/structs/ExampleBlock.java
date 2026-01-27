package com.example.plugin.structs;

import javax.annotation.Nonnull;

import com.example.plugin.interfaces.ModdedComponent;
import com.example.plugin.interfaces.ModdedServerPlugin;
import com.hypixel.hytale.builtin.blocktick.procedure.BasicChanceBlockGrowthProcedure;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.asset.type.blocktick.BlockTickStrategy;
import com.hypixel.hytale.server.core.asset.type.blocktick.config.TickProcedure;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;

public class ExampleBlock extends TickProcedure implements ModdedComponent {
    ////////////////////////////////
    // Tick! (example method just logs how many ticks it's been around every
    // 30 ticks. 30 ticks is 1 second by default)
    ////////////////////////////////

    private int ticks = 0;

    @Nonnull
    public static final BuilderCodec<ExampleBlock> CODEC = BuilderCodec
            .builder(ExampleBlock.class, ExampleBlock::new, TickProcedure.BASE_CODEC)
            .append(
                    new KeyedCodec<Integer>("Ticks", Codec.INTEGER),
                    (data, value) -> data.ticks = value,
                    (data) -> data.ticks)
            .add()
            .build();

    // Ticking a block or entity? put it in here!
    @Nonnull
    @Override
    public BlockTickStrategy onTick(
            @Nonnull World world, WorldChunk wc, int worldX, int worldY, int worldZ, int blockId) {
        this.ticks += 1;
        console.log("Ticked block at (" + worldX + ", " + worldY + ", " + worldZ + " ) " + this.ticks + " times");

        // if you want to sleep for a bit, return BlockTickStrategy.SLEEP
        // if (this.ticks > 30 * 10) {
        // // return BlockTickStrategy.SLEEP;
        // }

        return BlockTickStrategy.CONTINUE;
    }

    ////////////////////////////////
    // MY BOILERPLATE BELOW
    /////////////////////////////// s/

    @Nonnull
    @SuppressWarnings("null")
    public static String Id = ExampleBlock.class.getSimpleName();
    private static ComponentType<ChunkStore, ExampleBlock> componentType;

    ////////////////////////////////
    // CONSTRUCTORS (for cloning etc)
    ////////////////////////////////

    public ExampleBlock() {
    }

    public ExampleBlock(ExampleBlock self) {
        this.ticks = self.ticks;
    }

    public ExampleBlock clone() {
        return new ExampleBlock(this);
    }

    ////////////////////////////////
    // BOILERPLATE (same as BasicComponentExample)
    ////////////////////////////////

    /**
     * This function is here to make it easier to register your block, no more
     * remembering "what the fuck am i meant to type in?"
     *
     * just, call "CustomBlock.register(this)" in your plugin and DONE
     *
     * @param plugin
     * @return
     */
    public static ComponentType<ChunkStore, ExampleBlock> registerToPlugin(ModdedServerPlugin plugin) {
        var component = plugin.getChunkStoreRegistry().registerComponent(
                ExampleBlock.class,
                ExampleBlock.Id,
                ExampleBlock.CODEC);

        plugin.addToRegister(ExampleBlock.Id, component);

        TickProcedure.CODEC.register(ExampleBlock.Id, ExampleBlock.class,
                ExampleBlock.CODEC);

        // also keep me a copy of the component type after registering
        ExampleBlock.componentType = component;

        return component;
    }

    /**
     * This function should only ever be called AFTER your plugin has finished its
     * setup function,
     * as otherwise we'll not have set this field yet
     *
     * @return
     */
    @Nonnull
    public static ComponentType<ChunkStore, ExampleBlock> getComponentType() {
        // As long as you don't break the contract of "actually initialize the block"
        // then this will never fail
        if (ExampleBlock.componentType == null) {
            throw new RuntimeException("Called " + ExampleBlock.class.getName()
                    + ".getComponentType() before plugin was setup.\nHint: make sure to call `"
                    + ExampleBlock.class.getName()
                    + ".registerFor(this);` within your plugin's `setup` function ");
        }

        return ExampleBlock.componentType;
    }

}

// @Nonnull
// public static final BuilderCodec<TickingComponent> CODEC = BuilderCodec
// .builder(TickingComponent.class, TickingComponent::new)
// // use `.append` for each and every property you define that you want saved
// AND
// // call `.add` after
// //
// // If we want our block to remember the TOTAL time it's been alive for
// (across
// // sessions, logouts, server restarts etc) then we keep this codec and it'll
// // resume from where it left off next time it's loaded
// //
// // but, for example let's say your mod was a chunk loader sort of thing
// // that wanted to show only how long it's kept the chunk loaded for.
// // AND specifically how long it's been loaded for during a given period of
// // server uptime (niche i know but hey)
// //
// // In such a case, removing this .append.add stuff here would be ideal
// // as that would mean it DOESN'T save how many ticks it's been alive for,
// // giving you an automatic reset whenever it gets unloaded/reloaded
// .append(
// // The name to use when saving your field (must start with a capital letter)
// new KeyedCodec<Integer>("Ticks", Codec.INTEGER),

// // Setter (loading data from disk)
// // - data: the object of your class being constructed
// // - value: the value loaded from disk stored in "Ticks"
// (data, value) -> data.ticks = value,

// // Getter (storing your object to disk)
// // # Asks, how do I find "Ticks" in your class??
// // - data: the object of your class (already constructed)
// (data) -> data.ticks)
// .add()
// .build();
