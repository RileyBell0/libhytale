package dev.twunk.interfaces;

import com.hypixel.hytale.builtin.blocktick.system.ChunkBlockTickSystem;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.modules.block.BlockModule;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockChunk;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import dev.twunk.utils.BlockUtils;
import dev.twunk.utils.GameTime;
import it.unimi.dsi.fastutil.objects.ObjectHeapPriorityQueue;
import java.time.Instant;
import java.util.Comparator;
import javax.annotation.Nonnull;

public class GlobalTickScheduler extends ChunkBlockTickSystem.PreTick {
    public record TickRequest(@Nonnull Ref<ChunkStore> block,
            @Nonnull Instant requestedGameTime) {
    }

    public GlobalTickScheduler() {

    }

    @Nonnull
    @SuppressWarnings("null")
    private static final ComponentType<ChunkStore, BlockChunk> COMPONENT_TYPE = BlockChunk
            .getComponentType();

    private static final Comparator<TickRequest> TICK_REQUEST_COMPARATOR = Comparator
            .comparing(t -> t.requestedGameTime());
    private static final ObjectHeapPriorityQueue<TickRequest> SCHEDULED_TICKS = new ObjectHeapPriorityQueue<>(
            TICK_REQUEST_COMPARATOR);

    public static void scheduleTick(TickRequest req) {
        HytaleLogger.forEnclosingClass().atInfo().log("SCHEDULED TICK FOR " + req);
        SCHEDULED_TICKS.enqueue(req);
    }

    // we scope our ticking to block chunks so that not only we always get to tick,
    // but we actually get a command buffer.
    @Override
    public Query<ChunkStore> getQuery() {
        return Query.and(COMPONENT_TYPE);
    }

    // Re-enables blocks that are going to be ticking
    @Override
    public void tick(
            float dt,
            int index,
            @Nonnull ArchetypeChunk<ChunkStore> archetypeChunk,
            @Nonnull Store<ChunkStore> store,
            @Nonnull CommandBuffer<ChunkStore> commandBuffer) {
        Instant gameTime = GameTime.get(commandBuffer);
        var blockInfoComponentType = BlockModule.BlockStateInfo.getComponentType();
        if (blockInfoComponentType == null || COMPONENT_TYPE == null) {
            return;
        }

        // DEQUEUE all components that need ticking
        TickRequest request;

        while (!SCHEDULED_TICKS.isEmpty()
                && (request = SCHEDULED_TICKS.first()).requestedGameTime.isBefore(gameTime)) {
            HytaleLogger.forEnclosingClass().atInfo().log("Running scheduled tick on " + request);
            SCHEDULED_TICKS.dequeue();
            if (!request.block.isValid()) {
                continue;
            }

            BlockUtils.setTicking(commandBuffer, request.block);
        }
    }
}
