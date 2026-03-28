package dev.twunk.hytale.utils;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.modules.block.BlockModule.BlockStateInfo;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import dev.twunk.hytale.utils.BlockUtils.Coords;
import javax.annotation.Nonnull;

/**
 * @deprecated TODO undeprecate this, need this to hook up and create subsystems for onBlockUpdate
 */
public abstract class TickProcedureUtils {

    private static final HytaleLogger.Api console = HytaleLogger.forEnclosingClass().atInfo();

    public static final boolean setTicking(final @Nonnull Ref<ChunkStore> ref) {
        return TickProcedureUtils.setTicking(ref, true);
    }

    public static final boolean setTicking(final @Nonnull Ref<ChunkStore> ref, final boolean ticking) {
        final var info = BlockUtils.Info.get(ref);
        if (info == null) {
            console.log("Info was null");
            return false;
        }

        return TickProcedureUtils.setTicking(info, ticking);
    }

    public static final boolean setTicking(final @Nonnull BlockStateInfo info) {
        return TickProcedureUtils.setTicking(info, true);
    }

    public static final boolean setTicking(final @Nonnull BlockStateInfo info, final boolean ticking) {
        final var worldChunk = ChunkUtils.WorldChunk_.get(info);
        if (worldChunk == null) {
            console.log("World chunk was null");
            return false;
        }

        final var coords = Coords.Local.get(info);
        return TickProcedureUtils.setTicking(worldChunk, coords, ticking);
    }

    public static final boolean setTicking(final @Nonnull WorldChunk worldChunk, final @Nonnull Vector3i coords) {
        return TickProcedureUtils.setTicking(worldChunk, coords, true);
    }

    public static final boolean setTicking(
        final @Nonnull WorldChunk worldChunk,
        final @Nonnull Vector3i coords,
        final boolean ticking
    ) {
        return worldChunk.setTicking(coords.x, coords.y, coords.z, ticking);
    }

    public static final boolean setTicking(
        final @Nonnull BlockChunk chunk,
        final @Nonnull BlockStateInfo info,
        final boolean ticking
    ) {
        final var coords = Coords.Local.get(info);
        return chunk.setTicking(coords.x, coords.y, coords.z, ticking);
    }

    public static final boolean setTicking(final @Nonnull BlockChunk chunk, final @Nonnull Vector3i coords) {
        return chunk.setTicking(coords.x, coords.y, coords.z, true);
    }

    public static final boolean setTicking(
        final @Nonnull BlockChunk chunk,
        final @Nonnull Vector3i coords,
        final boolean ticking
    ) {
        return chunk.setTicking(coords.x, coords.y, coords.z, ticking);
    }
}
