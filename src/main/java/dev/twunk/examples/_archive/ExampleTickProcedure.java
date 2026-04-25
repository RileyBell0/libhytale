package dev.twunk.examples._archive;

import com.hypixel.hytale.server.core.asset.type.blocktick.BlockTickStrategy;
import com.hypixel.hytale.server.core.asset.type.blocktick.config.TickProcedure;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import dev.twunk.hytale.codec.auto.Serializable;
import javax.annotation.Nullable;

@Serializable(inherits = TickProcedure.class, codecField = "BASE_CODEC")
public class ExampleTickProcedure extends TickProcedure {

    @Override
    public BlockTickStrategy onTick(
        final @Nullable World arg0,
        final @Nullable WorldChunk arg1,
        final int arg2,
        final int arg3,
        final int arg4,
        final int arg5
    ) {
        return BlockTickStrategy.CONTINUE;
    }
}
