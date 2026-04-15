package dev.twunk.interfaces.component;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.server.core.universe.world.WorldProvider;
import dev.twunk.interfaces.methods.IOnTick;

public interface IOnTickComponent<ECS_STORE extends WorldProvider> extends IOnTick<ECS_STORE>, Component<ECS_STORE> {}
