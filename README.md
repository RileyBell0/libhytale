# LibHytale

A library for modding [Hytale](https://hytale.com/) server-side code.

No AI was harmed in the making of this project.

## The "Why"

Hytale's server side code (as of 31/03/2026) has no comments, making learning their systems a lengthy process for programmers entering into their ecosystem.

This library serves to provide **documented** and **tested** utilities to make your journey coding within Hytale far easier.

> [NOTE]
> TODO: Add some side-by-side examples here

Hytale is designed around Entity Component Systems (ECS), meaning you've got Entities (anything in a world) that has Components (data). To then run code and respond to events on these you have Systems.

Without this library, if you want to define a system that
- Logs when an entity is added
- Spawn a grass block at its position each tick
- Logs when the entity is removed

you'd need
- 1x SYSTEM for entity add/remove events
- 1x SYSTEM for running ticks on the specific entities you're interested in (entities that have your custom component)
- 1x COMPONENT file for the component itself

then you'd have to register all three into the right stores, etc, etc.

It's a pain.

So, instead of all that junk now you can define (for simple cases such as this example)
```java
@Serializable
public class Example extends ILifetimeComponent, IBlockTickComponent {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    public void onEntityAdded(AnyRef<ChunkStore> ref, AddReason reason, CommandBuffer<ChunkStore> commandBuffer) {
        LOGGER.atInfo.log("Added new example entity");
    }

    public void onBlockTick(BlockRef blockRef, CommandBuffer<ChunkStore> commandBuffer) {
        ItemUtils.spawn(blockRef, commandBuffer, blockRef.getGlobalCoords(), new ItemStack("Soil_Grass", 1));
    }

    public void onEntityRemove(AnyRef<ChunkStore> ref, RemoveReason reason, CommandBuffer<ChunkStore> commandBuffer) {
        LOGGER.atInfo.log("Removed example entity");
    }
}
```

Then in your plugin you run
```
this.registerChunkComponent(Example.class);
```

In my opinion that seems, alot easier

## The "How"

For now, fork the following repo (or clone it or do whatever really) and just delete my test code. It's a good basis
https://github.com/RileyBell0/hytale-plugin

^^ It's where I test LibHytale. It has some examples of some use cases (usually of whatever part I'm working on) but should be pretty easy to follow or follow on from

## TODO

1. Add onBlockBreak, onBlockAdd (etc) event handlers as subsystems. The overall goal is to figure out an "onBlockUpdate" subsystem
2. Fix ScheduledTickSystem
3. The team just added an `ItemContainerBlock` component, probably need to update my container utils to suit. might be able to extend that directly for containers

## Extra

Got most of my resources from reading the src code directly and from <https://hytalemodding.dev/en/docs>

I highly recommend checking out <https://hytalemodding.dev/en/docs> for an overview on Hytale's systems and some examples
