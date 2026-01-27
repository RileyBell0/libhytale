package com.example.plugin.structs;

import javax.annotation.Nonnull;

import com.example.plugin.interfaces.ModdedComponent;
import com.example.plugin.interfaces.ModdedServerPlugin;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;

// TODO - all a component like this really needs is a name, id, struct and associated codec

/**
 * Doesn't tick, doesn't do anything, just
 * - exists
 * - has data/state
 * - saves said data/state
 */
public class DataComponent implements ModdedComponent {
    ////////////////////////////////
    // Properties (the actual data YOU define)
    ////////////////////////////////
    private Boolean exampleField = true;

    ////////////////////////////////
    // Configuration
    ////////////////////////////////
    @Nonnull
    @SuppressWarnings("null")
    public static String Id = DataComponent.class.getName();
    private static ComponentType<ChunkStore, DataComponent> componentType;

    ////////////////////////////////
    // SERIALIZATION (saving/loading for next time you log into your server)
    ////////////////////////////////

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
    public static final BuilderCodec<DataComponent> CODEC = BuilderCodec.builder(
            DataComponent.class,
            DataComponent::new
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
                    (data) -> data.exampleField)
            .add()
            // FINALLY call .build once you're done
            .build();

    ////////////////////////////////
    // CONSTRUCTORS (for cloning etc)
    ////////////////////////////////

    // Example constructor
    public DataComponent() {
    }

    // Example constructor
    public DataComponent(Boolean exampleField) {
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
    public DataComponent(DataComponent self) {
        this.exampleField = self.exampleField;
    }

    /**
     * This function should create a deep clone of your class. Make sure to CLONE
     * all fields in `this` in your constructor above, we really don't want to
     * accidentally have the clone own and mutate your data!!
     */
    public DataComponent clone() {
        return new DataComponent(this);
    }

    ////////////////////////////////
    // BOILERPLATE
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
    public static ComponentType<ChunkStore, DataComponent> registerToPlugin(ModdedServerPlugin plugin) {
        var component = plugin.getChunkStoreRegistry().registerComponent(
                DataComponent.class,
                DataComponent.Id,
                DataComponent.CODEC);

        plugin.addToRegister(DataComponent.Id, component);

        // also keep me a copy of the component type after registering
        DataComponent.componentType = component;

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
    public static ComponentType<ChunkStore, DataComponent> getComponentType() {
        // As long as you don't break the contract of "actually initialize the block"
        // then this will never fail
        if (DataComponent.componentType == null) {
            throw new RuntimeException("Called " + DataComponent.class.getName()
                    + ".getComponentType() before plugin was setup.\nHint: make sure to call `"
                    + DataComponent.class.getName()
                    + ".registerFor(this);` within your plugin's `setup` function ");
        }

        return DataComponent.componentType;
    }
}