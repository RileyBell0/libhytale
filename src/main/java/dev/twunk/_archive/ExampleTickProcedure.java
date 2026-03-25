package dev.twunk._archive;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.asset.type.blocktick.BlockTickStrategy;
import com.hypixel.hytale.server.core.asset.type.blocktick.config.TickProcedure;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import javax.annotation.Nullable;

public class ExampleTickProcedure extends TickProcedure {

    public static final BuilderCodec<ExampleTickProcedure> CODEC = BuilderCodec.builder(
        ExampleTickProcedure.class,
        ExampleTickProcedure::new,
        TickProcedure.BASE_CODEC
    ).build();

    public ExampleTickProcedure() {
        HytaleLogger.forEnclosingClass().atInfo().log("CONSTRUCTING INHERENT TICK PROCEDURE");
    }

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
