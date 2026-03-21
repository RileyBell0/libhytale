Idea is: its basically a subsystem

it's going to be a THING that hooks your class into the event system
so it's just going to call methods on your class

NOTABLY, it's not hooking into the SYSTEM stuff in hytale, but into the interactions etc

so, ideally, i want to define one where you pass in the "ID" that you want for the interaction, so you're going to have to define the ID, and it's going to be relative to your class name

so if you define your submodule "asdf" on your class "dev.twunk.MySubModule", then the submodule's ID is going to be "dev.twunk.MySubModule.__module__asdf" i guess? yeah seems legit