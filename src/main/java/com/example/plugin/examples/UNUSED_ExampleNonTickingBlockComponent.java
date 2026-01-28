package com.example.plugin.examples;

import com.example.plugin.interfaces.ModPlugin;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import javax.annotation.Nonnull;

// later - all a component like this really needs is a name, id, struct and associated codec

/**
 * Doesn't tick, doesn't do anything, just
 * - exists
 * - has data/state
 * - saves said data/state
 */
public class UNUSED_ExampleNonTickingBlockComponent implements Component<ChunkStore> {

    private Boolean exampleField = true;

    // //////////////////////////////
    // SERIALIZATION (saving/loading for next time you log into your server)
    // //////////////////////////////
    /**
     * This is where you define how to serialise your object to disk and deserialise
     * it again
     *
     * AKA - if you want your block to still exist next time you log into your
     * server,
     * you'll need to configure this
     *
     * effectively, every single field you defined here on `this`, so anything you
     * can access with `this.<property_name>` (that isn't a function) should have
     * an entry here
     */
    @Nonnull
    public static final BuilderCodec<UNUSED_ExampleNonTickingBlockComponent> CODEC = BuilderCodec.builder(
        UNUSED_ExampleNonTickingBlockComponent.class,
        UNUSED_ExampleNonTickingBlockComponent::new
        // Optional third parameter: an existing codec you want to extend/inherit from
    )
        // use `.append` for each and every property you define that you want saved AND
        // call `.add` after
        .append(
            // The name to use when saving your field (must start with a capital letter)
            // NOTE: you can use anything for the name, i was going to use
            // ExampleFieldCanBeNamedAnythingHere for the example, but
            // i felt like that would look confusing. Really, its a case of
            // "chose your own name", you get to chose what name it gets stored
            // under. This will never come up and never matter, name them "Spaghetti1",
            // "Spaghetti2" etc and you'll be fine
            new KeyedCodec<Boolean>("ExampleField", Codec.BOOLEAN),
            // Setter (loading data from disk)
            // - data: the object of your class being constructed
            // - value: the value loaded from disk stored in "ExampleField"
            (data, value) -> data.exampleField = value,
            // Getter (storing your object to disk)
            // # Asks, how do I find "ExampleField" in your class??
            // - data: the object of your class (already constructed)
            data -> data.exampleField
        )
        .add()
        // FINALLY call .build once you're done
        .build();

    // //////////////////////////////
    // CONSTRUCTORS (for cloning etc)
    // //////////////////////////////

    // Example constructor
    public UNUSED_ExampleNonTickingBlockComponent() {}

    // Example constructor
    public UNUSED_ExampleNonTickingBlockComponent(Boolean exampleField) {
        this.exampleField = exampleField;
    }

    /**
     * IMPORTANT - Make sure to CLONE all fields from `self` onto `this`. You must
     * assume that the newly created object will own and mutate ALL data it
     * receives, so don't let it have a reference to any internal data you don't
     * want mangled by random other code
     *
     * effectively, this should be functional, immutable kinda vibes
     */
    public UNUSED_ExampleNonTickingBlockComponent(UNUSED_ExampleNonTickingBlockComponent self) {
        this.exampleField = self.exampleField;
    }

    /**
     * This function should create a deep clone of your class. Make sure to CLONE
     * all fields in `this` in your constructor above, we really don't want to
     * accidentally have the clone own and mutate your data!!
     */
    public Component<ChunkStore> clone() {
        return new UNUSED_ExampleNonTickingBlockComponent(this);
    }

    // //////////////////////////////
    // BOILERPLATE - there's a bunch of boilerplate to make life easy
    // //////////////////////////////

    @Nonnull
    public static final String ID = UNUSED_ExampleNonTickingBlockComponent.class.getName();

    private static ComponentType<ChunkStore, UNUSED_ExampleNonTickingBlockComponent> componentType;

    /**
     * Registers your component to the given plugin.
     *
     * Call `NameOfThisClass.registerToPlugin(this);` in your plugin's setup method
     */
    public static ComponentType<ChunkStore, UNUSED_ExampleNonTickingBlockComponent> registerToPlugin(ModPlugin plugin) {
        var component = plugin
            .getChunkStoreRegistry()
            .registerComponent(
                UNUSED_ExampleNonTickingBlockComponent.class,
                UNUSED_ExampleNonTickingBlockComponent.ID,
                UNUSED_ExampleNonTickingBlockComponent.CODEC
            );

        plugin.addToRegister(UNUSED_ExampleNonTickingBlockComponent.ID, component);

        // also keep me a copy of the component type after registering
        UNUSED_ExampleNonTickingBlockComponent.componentType = component;

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
    public static ComponentType<ChunkStore, UNUSED_ExampleNonTickingBlockComponent> getComponentType() {
        // As long as you don't break the contract of "actually initialize the block"
        // then this will never fail
        if (UNUSED_ExampleNonTickingBlockComponent.componentType == null) {
            throw new RuntimeException(
                "Called " +
                    UNUSED_ExampleNonTickingBlockComponent.class.getName() +
                    ".getComponentType() before plugin was setup.\nHint: make sure to call `" +
                    UNUSED_ExampleNonTickingBlockComponent.class.getName() +
                    ".registerFor(this);` within your plugin's `setup` function "
            );
        }

        return UNUSED_ExampleNonTickingBlockComponent.componentType;
    }
}
