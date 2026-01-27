package com.example.plugin;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.example.plugin.interfaces.ModdedServerPlugin;
import com.example.plugin.structs.DataComponent;
import com.example.plugin.structs.ExampleBlock;
import com.hypixel.hytale.assetstore.map.BlockTypeAssetMap;
import com.hypixel.hytale.builtin.blocktick.system.ChunkBlockTickSystem;
import com.hypixel.hytale.builtin.blocktick.system.MergeWaitingBlocksSystem;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.event.EventPriority;
import com.hypixel.hytale.server.core.asset.type.blocktick.BlockTickManager;
import com.hypixel.hytale.server.core.asset.type.blocktick.IBlockTickProvider;
import com.hypixel.hytale.server.core.asset.type.blocktick.config.TickProcedure;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.ChunkColumn;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.section.BlockSection;
import com.hypixel.hytale.server.core.universe.world.events.ChunkPreLoadProcessEvent;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;

public class RileysPlugin extends ModdedServerPlugin {
    protected static RileysPlugin instance;
    // private static HytaleLogger.Api console =
    // HytaleLogger.forEnclosingClass().atInfo();

    public static RileysPlugin get() {
        return instance;
    }

    public RileysPlugin(@Nonnull JavaPluginInit init) {
        super(init);
    }

    @Override
    protected void setup() {
        instance = this;
        super.setup();

        ExampleBlock.registerToPlugin(this);
    }

    protected void start() {
        this.getChunkStoreRegistry().registerSystem(new TickingInitializer());
        this.getChunkStoreRegistry().registerSystem(new TickingSystem());
    }

}