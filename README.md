# libhytale (WIP)

## PLAN for getting onUpdate sub system

> [!IMPORTANT]
> Next plan
> 
> well, plan for like, idk, i need to figure out when a block updates WHAT systems to call. seriously, is there a way to do that efficiently? can i hook into another system somehow? maybe i just have a component i throw on called like "onUpdateComponent" yeah, ok that works, then i just need some way to talk back to all systems that OH systems can register THEMSELVES on the component right? idk. nah that doesn't really work, or, ooh ok onEntityAdded: i listen for THAT and when an entity comes in, i go somewhere the tick procedure can tell and yeah
> 
> entityAdded -> some library component (registers self)
> 
> THEN
> 
> TickProcedure -> SAME library component -> runs through all systems we had before, now, congrats, they can run onUpdate!! WOOO!
> - remind systems to be KIND and assume other systems are running onUpdate too!!

> [!NOTE]
> OH and side note, ALSO need to generalise the idea of "I need to store some global data on an entity" e.g. scheduledTickSystem needs me to store on each entity that there's a system watching it (and equally i should remember to actually rmeove that when that list goes down to zero too lmao)

## preamble (this library is super pre-alpha)

If you found this, uuh, it's very much really a work in progress.

Worth reading around to see how i do things if i happen to have solved or found
areas of the code you're still figuring out (e.g. getting a block by coords)

Got most of my resources from reading the src code directly and from <https://hytalemodding.dev/en/docs>

highly reccomend <https://hytalemodding.dev/en/docs>, they're making TONS of changes and updates all the time, that's probably your best bet for learning this. Set an hour to read through their docs, then another day set aside another hour to read through their docs again.

The "[Entity Component System](https://hytalemodding.dev/en/docs/guides/ecs/entity-component-system)" docs are by FAR the most important ones for you to understand. I'm pretty new to modding so at least, seemed incredibly important for me.

Maybe come back in a few weeks or so when I've had the free time to finish more of this
Don't expect the install instructions to work, honestly, if my computer carks it even I don't know how to install it yet

What is this

- Better API for hytale modding

Better how?

- Turns out its hard to do easy things in their API currently. Alot of this may just be due to there being no comments in their code currently, though regardless, its quite hard to use
- Goal: make simple things easy to do (or really just easy to find)

Like what?

- Getting a block
- Scheduling ticks on matches in systems
- Easier getting world, world chunks, blocks, block info, etc

---

## Ignore everything below here

Why?

uuh, i forked someone else's code originally but completely threw all piece by piece except the gradle stuff

well, i say forked. More like downloaded their example i think? really not sure it was a while ago (and so much churn ago)

if you DO happen to stumble upon this and read through it for some reason, PRs are welcome, but honestly feedback or requests are far more welcome because, well, as mentioned, this is super super early development

oh, and importantly, I'm really very much in a churn stage of this project, still getting used to hytale's API itself and thus, everything i write here is subject to change (and currently is changing)

### Option 1: From Hytale Launcher

After installing the Hytale Launcher, you can find the server files in:

| OS      | Path                                                            |
|---------|-----------------------------------------------------------------|
| Windows | `%appdata%\Hytale\install\release\package\game\latest`          |
| Linux   | `$XDG_DATA_HOME/Hytale/install/release/package/game/latest`     |
| macOS   | `~/Application Support/Hytale/install/release/package/game/latest` |

Copy `HytaleServer.jar` from that directory into the `libs/` folder of this project.

### Option 2: Hytale Downloader CLI

For production servers, you can use the official **Hytale Downloader CLI** tool to download the latest server files. This requires OAuth2 authentication.

For more details, see the official [Hytale Server Manual](https://support.hytale.com/hc/en-us/articles/45326769420827-Hytale-Server-Manual).

## Building

1. Place `HytaleServer.jar` in the `libs/` directory
2. Build with Gradle:

```bash
./gradlew build
```

The compiled plugin JAR will be located at `build/libs/HelloPlugin-1.0-SNAPSHOT.jar`.

## Installation

Copy the built JAR file to your Hytale server's `plugins/` directory.

## Usage

In-game, use the command:

```
/hello
```

This will display a title message saying "Hello world!" to the player.

## Project Structure

```
src/main/java/dev.twunk/
├── HelloPlugin.java    # Main plugin class
└── HelloCommand.java   # Command implementation
```

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
