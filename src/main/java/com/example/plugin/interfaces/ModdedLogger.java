package com.example.plugin.interfaces;

import com.hypixel.hytale.logger.HytaleLogger;

// this is literally just here for easier logging
public interface ModdedLogger {
    static HytaleLogger.Api console = HytaleLogger.forEnclosingClass().atInfo();
}
