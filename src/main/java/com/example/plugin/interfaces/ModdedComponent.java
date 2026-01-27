package com.example.plugin.interfaces;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;

// You want a custom block?
// that's already done, they're just models
//
// oh, you want BEHAVIOUR? ok now we're talking
//
// start here. This does NOTHING except extend the specific interface you need
// for when you want to do stuff with blocks
public abstract interface ModdedComponent extends Component<ChunkStore> {
    static final HytaleLogger.Api console = HytaleLogger.forEnclosingClass().atInfo();
}
