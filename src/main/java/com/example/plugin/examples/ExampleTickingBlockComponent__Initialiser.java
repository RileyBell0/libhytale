package com.example.plugin.examples;

import com.example.plugin.interfaces.TickingBlockComponent_Initialiser;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.server.core.modules.block.BlockModule;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;

public class ExampleTickingBlockComponent__Initialiser extends TickingBlockComponent_Initialiser {

    public ExampleTickingBlockComponent__Initialiser() {
        super();
    }

    @Override
    public Query<ChunkStore> getQuery() {
        return Query.and(
            BlockModule.BlockStateInfo.getComponentType(),
            ExampleTickingBlockComponent.getComponentType()
        );
    }
}
