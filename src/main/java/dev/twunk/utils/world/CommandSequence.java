package dev.twunk.utils.world;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import java.util.LinkedList;
import java.util.Queue;
import java.util.function.Consumer;
import javax.annotation.Nonnull;

public class CommandSequence implements Consumer<Store<ChunkStore>> {

    @Nonnull
    private final CommandBuffer<ChunkStore> commandBuffer;

    @Nonnull
    private Queue<Consumer<Store<ChunkStore>>> sequence = new LinkedList<>();

    /**
     * Runs the commands you give it in order, with NO GUARANTEE that there won't be other
     * stuff running inbetween your commands
     *
     * in fact, the very idea is that this SUPPORTS other stuff running inbetween your commands
     *
     * We only ever queue your next command, then the second that's finished we queue the
     * command after that
     *
     * that way, if your first command starts running some stuff, we just add your next command AFTER that stuff has run
     */
    private CommandSequence(
        @Nonnull final CommandBuffer<ChunkStore> commandBuffer,
        @Nonnull final Consumer<Store<ChunkStore>> firstCommand
    ) {
        this.commandBuffer = commandBuffer;
        this.sequence.add(firstCommand);
        this.commandBuffer.run(this);
    }

    public static final CommandSequence start(
        @Nonnull final CommandBuffer<ChunkStore> commandBuffer,
        @Nonnull final Consumer<Store<ChunkStore>> firstCommand
    ) {
        return new CommandSequence(commandBuffer, firstCommand);
    }

    public CommandSequence then(@Nonnull Consumer<Store<ChunkStore>> nextInSequence) {
        this.sequence.add(nextInSequence);

        return this;
    }

    @Override
    public void accept(Store<ChunkStore> store) {
        // do the func
        this.sequence.poll().accept(store);

        // then RECURSE (later) until we're empty
        if (this.sequence.size() != 0) {
            this.commandBuffer.run(this);
        }
    }
}
