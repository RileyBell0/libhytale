# LibHytale

A library for modding [Hytale](https://hytale.com/) server-side code.

## TODO

> [!NOTE] TODO
>
> ### Main TODO
>
> - 
>
> ### Docs
>
> - Add some side-by-side examples here
> - Add an example for the same poison system example they've got in the hytale docs
> - THEN add a side by side of what from the old system goes where, the boilerplate my stuff removes and the ease of use and understanding it can provide when ive got it documented properly
> - learn JavaDoc and get my shit fixed up
>
> ### Fixes
>
> - 
> - The team just added an `ItemContainerBlock` component, probably need to update my container utils to suit. might be able to extend that directly for containers
>
> ### Features 
>
> - Alter my inferECSType method to also
> - check the super type of a class that i receive for if IT implements component/interaction where i don't. Equally need to propgate this up all the way to Object
> - go through ALL interfaces defined on the class, not just the first one that extends component, as there's a chance someone might implement "Component" twice, once as a raw type or ECS_TYPE kinda thing, and another as an actual definition, meaning i can go until i find the first well defined component, or fallback to the first not-well-defined Component (e.g. raw type)
> - Test my Component.class inference system (inferECSType or whatever i called it) with instances of an object. the idea is: if you pass an instance of the object i'll register events TO it, if you pass the class i'll register events that will find the relevant component and call it on that
> - thus, i also need to make sure that i record which classes i've done this for already such that if you pass me several copies of an instance of a given system/object/whatever then i won't try and register the class again (e.g. imagine it extends component but its also a useful system? seems unlikely, but, its an edge case nonetheless so i must fix it eventually)
>
> ### Stretch goals
>
> - Add onBlockBreak, onBlockAdd (etc) event handlers as subsystems. The overall goal is to figure out an "onBlockUpdate" subsystem. this is a stretch goal because i so cbf rn
>
> ```java
> /**
> * TODO add a "timeout" for trash inventories, so when you CLOSE the inventory i go "ok yeah i get you, you
> * want to delete these items. Just gonna make SURE you're sure, by keeping them around for like, 30 seconds"
> *
> * limit it to only be the player that opened it that can see those inventories.
> * make it QUEUE all inventories for stuff that was deleted. if you open the trash it should pause all inventory deletions
> * and resume the countdown when you close the trash
> */
> ```

## The "Why"

Hytale's server side code (as of 31/03/2026) has no comments, making learning their systems a lengthy process for programmers entering into their ecosystem.

This library serves to provide **documented** and **tested** utilities to make your journey coding within Hytale far easier.

> [!NOTE] i take it back, this is alot more than that
>
> really its become a library about two things
>
> - codecs, making codecs easy
> - events. redefining systems as instead "observable" things you can listen to, kind of. you still need to define systems and such since its query based BUT doing so is a whole lot easier with you being able to define HOW and WHAT you want to listen to within your class definition itself by simply implementing the interface for the given event listener. WAY easier than before, all works through the same handler too, just gotta actually you know, finish this project and get it out there. ok. finish is a strong word, but, you know, get a 1.0.0 out that i can then fix bugs and docs and such later
>
> TODO: my release of it should include a few example mods, some staples we haven't had made yet to show how easy it is, and some dumb stuff, should be easy as long as i can come up with some ideas to throw at the systems

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

So, instead of all that junk now you can define

```java
// TODO allow lack of @Serializable tag BUT throw a Warning in the cases i mention in the next comment. Cause, yeah i can just make a default empty codec that's fine, just, please throw a warning so people add it, have it throw that warning by default and make a note that you CAN turn it off once you know what you're doing, but get in the habit of using it now and remove it for useless cases later
// importantly you can skip the @Serializable tag if you have a default constructor and no fields that aren't static. but if you DON'T have a default constructor OR you have fields that aren't static i'll at least throw a warning
// @Serializable
public class Example extends IOnAddRemove<ChunkStore>, IOnBlockTick {

    public void onEntityAdded(Ref<ChunkStore> ref, AddReason reason, CommandBuffer<ChunkStore> commandBuffer) {
        // your code here
    }

    public void onBlockTick(BlockRef blockRef, CommandBuffer<ChunkStore> commandBuffer) {
        // e.g. spawning an item every tick
        // ItemUtils.spawn(blockRef, commandBuffer, blockRef.getGlobalCoords(), new ItemStack("Soil_Grass", 1));
    }

    public void onEntityRemove(Ref<ChunkStore> ref, RemoveReason reason, CommandBuffer<ChunkStore> commandBuffer) {
        // your other code here
    }

    // TODO have my component also be allowed to extend ISystem so that it can provide the getQuery, getGroup etc etc. meaning, yeah... idk... code required ahead and a slight rework of all the event drivers so that they can take an ISystem in for config reasons, or rather, suppliers of that stuff? fuck idk, yeah, maybe just methods on them for "setGroup" and shit before you build, so maybe i make a builder for them? yeah a builder for each one would be neat, then i can within my OWN code be quite strict about it but then again, fuck it, its my own code? who cares? its literally gonna be used like twice in my codebase and if someone runs into it i just have to have some decent comments. i'll just, yeah fuck it. i'll hardcode it in maybe idk.
}
```

and BOOM, it works! yeah this is pretty neat.

Then in your plugin you run

```java
this.register(Example.class);
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

> [!NOTE] wow, that ^^ reads like ai, i think its just the quotes haha, ive got to get more sleep. future riley: remember to rewrite all ohyeah
>
> // TODO
>
> future riley come back here and fix up the readme, it all talks about subsystems too but i've moved it to events which, highkey, is STILL subsystems, just with a different (and optional) interface. plus, this in no way stops us doing anythign else or anyting extra alongside our current code, it literally just makes it possible to also OPT IN to this system and this library's way of doing things where that makes sense

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

---

## ignore this, im just dumping code

```java
public final int getIndexInArchetypeChunk(
    ArchetypeChunk<ECS_TYPE> archetypeChunk,
    com.hypixel.hytale.component.Ref<ECS_TYPE> ref
) {
    for (var i = 0; i < archetypeChunk.size(); i++) {
        if (ref.equals(archetypeChunk.getReferenceTo(i))) {
            return i;
        }
    }
    for (var i = 0; i < archetypeChunk.size(); i++) {
        System.out.println(i + ") " + archetypeChunk.getReferenceTo(i));
    }
    throw new RuntimeException("reiwriwjeroijwaorijawioj " + ref);
}
```

---

```java

final var listener = commandBuffer.ensureAndGetComponent(
    ref,
    (ComponentType<ECS_TYPE, ?>) listenerComponentType
);
@SuppressWarnings("unchecked")
final var trackedRef = (TrackedRef<ECS_TYPE, T>) new TrackedRef<>(
    ref,
    associatedCache,
    (IOnScheduledTick) listener
);
area.add(trackedRef); // we'll put chuck our cache into the right ticking group (ready to go)
```

```java
public static final <
    ECS_TYPE extends WorldProvider,
    T extends IOnScheduledTick<ECS_TYPE> & IEventQuery<ECS_TYPE>
> OnScheduledTick<ECS_TYPE> newDriverFor(String id, T listener, IRegistry<ECS_TYPE> registry) {
    return new OnScheduledTick__Listener<>(id, listener, listener.getQuery(), registry);
}

public static final <ECS_TYPE extends WorldProvider> OnScheduledTick<ECS_TYPE> newDriverFor(
    String id,
    IOnScheduledTick<ECS_TYPE> listener,
    Query<ECS_TYPE> query,
    IRegistry<ECS_TYPE> registry
) {
    return new OnScheduledTick__Listener<>(id, listener, query, registry);
}

public static final <ECS_TYPE extends WorldProvider, T extends Component<ECS_TYPE>> OnScheduledTick<
    ECS_TYPE
> newDriverFor(String id, ComponentType<ECS_TYPE, T> componentType, IRegistry<ECS_TYPE> registry) {
    return new OnScheduledTick__Component<>(id, componentType, registry);
}
```

and a method i was looking into for making unique identifiers (ish) for each block

problem is that world IDs are the size of a UUID so i can't just, combine them into one uuid without losing accuracy but that's whatever

```java
final class Chunk extends UUIDComponent<ChunkStore> {

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static final BuilderCodec<Chunk> CODEC = BuilderCodec.builder(Chunk.class, Chunk::new)
        .append(new KeyedCodec("UUID", Codec.UUID_BINARY, true), (o, i) -> o.uuid = i, o -> o.uuid)
        .addValidator(Validators.nonNull())
        .add()
        .build();

    public Chunk() {
        super();
    }

    // combination of world, chunk, local index
    public Chunk(UUID coords) {
        super(coords);
    }

    // combination of world, chunk, local index
    public Chunk(UUID worldId, long chunkCoords, int blockCoords) {
        super(UUIDComponent.uuidFromBlockCoords(worldId, chunkCoords, blockCoords));
    }
}




 // get the UUID component if it exists
@Nullable
UUIDComponent<ECS_TYPE> uuidComponent = ComponentUtils.get(ref, this.uuidComponentType);

// if it DIDNT exist we'll make a new one and chuck that onto the entity
// note: we have to do seperate logic for blocks vs entities. Entities seem to have one by default
// but blocks don't.
if (uuidComponent == null) {
    return;
    if (ChunkStore.class.isAssignableFrom(ref.getStore().getClass())) {
        @SuppressWarnings("unchecked")
        final var asChunkRef = (Ref<ChunkStore>) ref;

        @SuppressWarnings("null")
        @Nonnull
        final var info = ComponentUtils.get(asChunkRef, BlockStateInfo.getComponentType());

        final var blockIndex = info.getIndex();

        @SuppressWarnings("null")
        @Nonnull
        final var chunkIndex = ChunkUtils.Coords.Index.get(info);

        final var worldId = asChunkRef.getStore().getExternalData().getWorld().getWorldConfig().getUuid();
        final UUID blockUuid = UUIDComponent.uuidFromBlockCoords(worldId, chunkIndex, blockIndex);

        uuidComponent = commandBuffer.ensureAndGetComponent(ref, this.uuidComponentType);
        uuidComponent.setUuid(blockUuid);
    } else {
        uuidComponent = commandBuffer.ensureAndGetComponent(ref, this.uuidComponentType);
    }
}
```

annoyingly since theres no way to get a block ref back from its component that i know of (except maybe persistent ref though havent had a chance to read up on it that much, seems to be entity locked iirc) ive got this whole weird stupid ass system. that checks at runtime if its a block or entity. hate it. but hey it works? probably? maybe i can instead just put a method onto the UUID component that will get you the ref? that way i CAN put whatever data i want on the uuid and sure it might be a bit weird at a standing data thing but as long as nobody tries to hardcode UUIDS and shit we'll be good, yeah i could do that, means its two different components technically but hey who says i NEED to define both fields? coud just, yeah im sure i can find some hacks

//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//
// TODO Blocks I believe operate under a different chunk store to
// the world they're in? not sure. should test...
//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//

// TODO multiple endpoints:
// - blockRef + store => store gets OTHER component on entity with ref blockRef
// - blockChunk -> gets the componnet on a block (doesn't require a ref!!, just the local coords of the block)

// TODO worldChunk.getIndex()  and      Chunk.Coords.Index.get_chunkCoords(worldChunk.getX(), worldChunk.getZ()) do the same thing

// TODO don't register this, instead register a new component each time it loads
HytalePlugin.register(plugin, ActivelyTickingComponent.class);

// TODO some day add some type inferrer logic that means we can search for Component.class's generic
// for a type that implements it, e.g. if i implement onTick<ChunkStore> and onUniverseTick<EntityStore> theoretically
// that should be legal, no reason to block it, so i should lean into it and MAKE it super legal so you can
// define both on the same one and i'll just figure out if i should have both entity and chunk systems for it or just entity or just chunk etc
// and then if ive got that the code would do it for each one, e.g. whenever i find X is assignable from <your class> i can just
// follow X down until i find the actual class that defines IOnAddRemove or whatever and just check that path for one that satisfies both.
//
// might be as easy as replacing Component.class with (Class other) -> Component.class.isAssignableFrom(other) && IOnAddRemove.isAssignableFrom(other)
// except, notably, i need to have two "modes" i think, yeah one that finds IOnAdddRemove and THEN one that finds component
// so more like an array for me to go down, use the first until you actually find the exact defintion of it, then use the second etc. if i wrote it recursively
// that would be really easy, meaning i dont do a lambda, i do [IOnAddRemove.class, Component.class] and yeah you find IOnAddRemove then keep going down JUST into that type via reflection