# LibHytale

A library for modding [Hytale](https://hytale.com/) server-side code.

No AI was harmed in the making of this project.

## The "Why"

Hytale's server side code (as of 31/03/2026) has no comments, making learning their systems a lengthy process for programmers entering into their ecosystem.

This library serves to provide **documented** and **tested** utilities to make your journey coding within Hytale far easier.

> [!NOTE]
> ## TODO
> 
> ### Docs
> - Add some side-by-side examples here
> - Add an example for the same poison system example they've got in the hytale docs
>
> ### Features / Fixes
> - Fix up the LogInteraction and such's codecs - need to support nested ish codecs you see, or rather probably just need to fix up logInteraction to actually use multiple classes
> - Add onBlockBreak, onBlockAdd (etc) event handlers as subsystems. The overall goal is to figure out an "onBlockUpdate" subsystem
> - Fix ScheduledTickSystem
> - The team just added an `ItemContainerBlock` component, probably need to update my container utils to suit. might be able to extend that directly for containers
> ```java
> /**
>  * TODO add a "timeout" for trash inventories, so when you CLOSE the inventory i go "ok yeah i get you, you
>  * want to delete these items. Just gonna make SURE you're sure, by keeping them around for like, 30 seconds"
>  *
>  * limit it to only be the player that opened it that can see those inventories.
>  * make it QUEUE all inventories for stuff that was deleted. if you open the trash it should pause all inventory deletions
>  * and resume the countdown when you close the trash
>  */
> ```
> ### Stretch goals
> - Fix up my definitions so they only NEED anything of type Object, because, well, theoretically i want my functions to just work and figure stuff out based on the props and interfaces something has, something that REALLY doesn't need annotations to work, so, may as well just make it work. Importantly, this means if you EXTEND component -> we're good, i'll register you as a component, if you DON'T extend component, i'll just not register you. Really i should ONLY throw exceptions or rather blocks of warnings for "you tried to register objects but nothing happened"

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

## The "Why else?"

uuh, this does alot. Check out the `Utils`...

There's
- utils for sending chat messages to
  - Universe
  - World
  - Player
- utils for sending notifications to
  - Universe
  - World
  - Player
- chunk utils
  - Getting its coordinates
  - Getting its index
  - Getting a component on it
  - Getting the world chunk
- Block utils
  - Get a block's ID, Tye, etc
  - Its Local INDEX / coordinates
  - Its global coordinates
  - Components on it
- Getting the current game time
- Working with tick procedures (i believe they're deprecated? not sure...)

Mainly there's subsystems. Subsystems are really the "killer app" of this library, that and the registration process i've got alongside the auto-codec-generation system

Basically, my goal was, if i've got system-like code - i want it all in the same file. It's WAY easier to debug/code stuff if you can see it all at the same time

Equally, I want to provide some common utils/examples of common systems that can be built upon with the idea being - if you extend your code from THIS library rather than the hytale source code, you'll have a much easier time in dealing with messages, chats, containers, interactions, systems/events etc

Plus, there's some annoying concepts in hytale's code such as thier `ChunkBlockTickSystem.Ticking` or whatever it's called that relies upon this idea `setTicking` for blocks. Great in theory, riddled with bugs in execution. Hoping to sidestep that whole process and bring everything back to components and systems. So if _deprecated and/or buggy code that's really really easy to mess up and easy to accidentally step on the toes of others_ sounds like, a nightmare to you too, this library is _probably_ for you.

If you have requests or suggestions that fit here LMK.

I come from the `rust` programming language side of things, so the idea of trusting that your code will run as you expect it to (barring other people specifically doing weird things to your blocks) is exactly the goal of this.


Plus, if i've learned anything in my career, it's that basic really usable utilities that let you MAKE abstractions easliy (rather than abstracting pretty much anything) are amazing. Code that just, does what it says on the tin, and is completely replaceable if need be

The goals/mission-statement of this library is basically
- serving as a standard library for hytale modding
- ^^ Specifically, i want to make sure that really common functionality can all go through here, with an abundance of interfaces etc to ensure cross compatibility between libraries/mods with BASIC stuff such as items, item transferring, blocks, containers, common components
- Anything implemented in here is a THIN wrapper that should basically be thought of as a fancy macro, meaning if you don't like what i did, great, do it yourself, it will still work just fine, you don't need to jump through any more hoops than what this library does in setting up the code you're replacing
- making their code FAR safer to use in ways that stop mod developers breaking code from other mods, or rather really encourages thinking about problems in ways that WON'T break other mods (e.g. avoiding `setTicking` like your life depends on it).

AND, since i come from rust, i want to do my absolute very best to do-away with the idea that "you can code something that will break at runtime really easliy doing stuff that works at compile time", e.g. i don't want my code to expose methods that would result in a "error, you shouldnt be calling this from a system" kinda thing.

Plus, i've seen us all reinventing the same wheel a ton of times, each in a different way usually due to a different scope of knowledge/understanding of their server SRC code. We really do keep doing the exact same thing, just with slight differences that can be really easily generalised

and, doing anything common (e.g. "Get me block X", "Delete block Y", "Where am i?") should be EASY and ACCESIBLE METHODS from the props you receive in any function, AND the helpers i've got for this should be really easily constructable no-matter where you are in your code. Hence, why the utils files are HUGE. I really insist on being able to run `BlockUtils.Ref.get()` with any parameters that give you
- access to block refs in a given world
- ANYTHING that can uniquely identify a block in that world (e.g. chunk coords + local block coords, global block coords, a WorldChunk with no chunk coords BUT you have the local block index/block coords etc etc)

## The "How"

For now, fork the following repo (or clone it or do whatever really) and just delete my test code. It's a good basis
https://github.com/RileyBell0/hytale-plugin

^^ It's where I test LibHytale. It has some examples of some use cases (usually of whatever part I'm working on) but should be pretty easy to follow or follow on from

## Extra

Got most of my resources from reading the src code directly and from <https://hytalemodding.dev/en/docs>

I highly recommend checking out <https://hytalemodding.dev/en/docs> for an overview on Hytale's systems and some examples
