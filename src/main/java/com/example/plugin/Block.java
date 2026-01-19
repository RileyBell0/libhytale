package com.example.plugin;

import java.util.concurrent.CompletableFuture;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;

public class Block {

    public static CompletableFuture<Ref<ChunkStore>> get(World world, int x, int y, int z) {
        var worldChunk = world.getChunkAsync(ChunkUtil.indexChunkFromBlock(x, z));
        var blockRef = worldChunk.thenApply((chunk) -> chunk.getBlockComponentEntity(x, y, z));

        return blockRef;
    }

    public static CompletableFuture<Ref<ChunkStore>[]> getTouching(World world, int x, int y, int z) {
        var future = new CompletableFuture<Ref<ChunkStore>[]>();

        @SuppressWarnings("unchecked")
        Ref<ChunkStore>[] items = new Ref[6];

        var block0 = Block.get(world, x, y, z + 1);
        var block1 = Block.get(world, x, y, z - 1);
        var block2 = Block.get(world, x, y + 1, z);
        var block3 = Block.get(world, x, y - 1, z);
        var block4 = Block.get(world, x + 1, y, z);
        var block5 = Block.get(world, x - 1, y, z);
        CompletableFuture.allOf(block0, block1, block2, block3, block4, block5).thenRun(() -> {
            items[0] = block0.join();
            items[1] = block1.join();
            items[2] = block2.join();
            items[3] = block3.join();
            items[4] = block4.join();
            items[5] = block5.join();

            future.complete(items);
        });

        return future;
    }
}
