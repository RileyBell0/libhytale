package dev.twunk.lib._archive;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.modules.block.BlockModule.BlockStateInfo;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import dev.twunk.hytale.utils.BlockUtils;
import dev.twunk.hytale.utils.BlockUtils.Coords;
import dev.twunk.hytale.utils.ChunkUtils;

public abstract class TickProcedureUtils {

    @SuppressWarnings("null")
    private static final HytaleLogger.Api console = HytaleLogger.forEnclosingClass().atInfo();

    public static final boolean setTicking(final Ref<ChunkStore> ref) {
        return TickProcedureUtils.setTicking(ref, true);
    }

    public static final boolean setTicking(final Ref<ChunkStore> ref, final boolean ticking) {
        final var info = BlockUtils.Info.get(ref);
        if (info == null) {
            console.log("Info was null");
            return false;
        }

        return TickProcedureUtils.setTicking(info, ticking);
    }

    public static final boolean setTicking(final BlockStateInfo info) {
        return TickProcedureUtils.setTicking(info, true);
    }

    public static final boolean setTicking(final BlockStateInfo info, final boolean ticking) {
        final var worldChunk = ChunkUtils.WorldChunks.get(info);
        if (worldChunk == null) {
            console.log("World chunk was null");
            return false;
        }

        final var coords = Coords.Local.get(info);
        return TickProcedureUtils.setTicking(worldChunk, coords, ticking);
    }

    public static final boolean setTicking(final WorldChunk worldChunk, final Vector3i coords) {
        return TickProcedureUtils.setTicking(worldChunk, coords, true);
    }

    public static final boolean setTicking(final WorldChunk worldChunk, final Vector3i coords, final boolean ticking) {
        return worldChunk.setTicking(coords.x, coords.y, coords.z, ticking);
    }

    public static final boolean setTicking(final BlockChunk chunk, final BlockStateInfo info, final boolean ticking) {
        final var coords = Coords.Local.get(info);
        return chunk.setTicking(coords.x, coords.y, coords.z, ticking);
    }

    public static final boolean setTicking(final BlockChunk chunk, final Vector3i coords) {
        return chunk.setTicking(coords.x, coords.y, coords.z, true);
    }

    public static final boolean setTicking(final BlockChunk chunk, final Vector3i coords, final boolean ticking) {
        return chunk.setTicking(coords.x, coords.y, coords.z, ticking);
    }
}
