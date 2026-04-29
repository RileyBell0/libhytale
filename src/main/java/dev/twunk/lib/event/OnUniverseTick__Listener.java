package dev.twunk.lib.event;

import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.WorldProvider;
import dev.twunk.hytale.event.OnUniverseTick;
import dev.twunk.hytale.interfaces.event.IOnUniverseTick;
import dev.twunk.hytale.interfaces.methods.IRegistry;

public class OnUniverseTick__Listener<ECS_TYPE extends WorldProvider> extends OnUniverseTick<ECS_TYPE> {

    private final IOnUniverseTick<ECS_TYPE> listener;

    protected OnUniverseTick__Listener(IRegistry<ECS_TYPE> registry, IOnUniverseTick<ECS_TYPE> listener) {
        super(registry);
        this.listener = listener;
    }

    // ////////////////////////////////////////////////////////////////////////
    // \/======================\/-  Methods  -\/==========================\/ //
    // ////////////////////////////////////////////////////////////////////////

    @Override
    public final void tick(float dt, int index, Store<ECS_TYPE> store) {
        this.listener.onUniverseTick(dt, index, store);
    }
}
// [2026/04/29 12:30:33 SEVERE] [InteractionSystems$TickInteractionManagerSystem] Exception while ticking entity interactions! Removing!
// java.lang.ArrayIndexOutOfBoundsException: Index -2147483648 out of bounds for length 42
//         at com.hypixel.hytale.component.Store.removeEntity(Store.java:841)
//         at com.hypixel.hytale.component.CommandBuffer.lambda$removeEntity$0(CommandBuffer.java:293)
//         at com.hypixel.hytale.component.CommandBuffer.consume(CommandBuffer.java:534)
//         at com.hypixel.hytale.component.Store.removeEntity(Store.java:884)
//         at com.hypixel.hytale.component.Store.removeEntity(Store.java:818)
//         at com.hypixel.hytale.server.core.modules.block.BlockEntity.setBlockEntity(BlockEntity.java:27)
//         at com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk.setState(WorldChunk.java:537)
//         at com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk.setBlock(WorldChunk.java:352)
//         at com.hypixel.hytale.server.core.universe.world.accessor.BlockAccessor.breakBlock(BlockAccessor.java:78)
//         at com.hypixel.hytale.server.core.universe.world.accessor.BlockAccessor.breakBlock(BlockAccessor.java:86)
//         at com.hypixel.hytale.server.core.modules.interaction.BlockHarvestUtils.removeBlock(BlockHarvestUtils.java:960)
//         at com.hypixel.hytale.server.core.modules.interaction.BlockHarvestUtils.naturallyRemoveBlock(BlockHarvestUtils.java:831)
//         at com.hypixel.hytale.server.core.modules.interaction.BlockHarvestUtils.performBlockBreak(BlockHarvestUtils.java:692)
//         at com.hypixel.hytale.server.core.modules.interaction.BlockHarvestUtils.performBlockBreak(BlockHarvestUtils.java:597)
//         at com.hypixel.hytale.server.core.modules.interaction.BlockHarvestUtils.performBlockBreak(BlockHarvestUtils.java:533)
//         at com.hypixel.hytale.server.core.modules.interaction.interaction.config.client.BreakBlockInteraction.interactWithBlock(BreakBlockInteraction.java:149)
//         at com.hypixel.hytale.server.core.modules.interaction.interaction.config.client.SimpleBlockInteraction.tick0(SimpleBlockInteraction.java:142)
//         at com.hypixel.hytale.server.core.modules.interaction.interaction.config.client.BreakBlockInteraction.tick0(BreakBlockInteraction.java:81)
//         at com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction.tick(Interaction.java:406)
//         at com.hypixel.hytale.server.core.modules.interaction.interaction.operation.OperationsBuilder$LabelOperation.tick(OperationsBuilder.java:272)
//         at com.hypixel.hytale.server.core.entity.InteractionManager.serverTick(InteractionManager.java:991)
//         at com.hypixel.hytale.server.core.entity.InteractionManager.doTickChain(InteractionManager.java:833)
//         at com.hypixel.hytale.server.core.entity.InteractionManager.tickChain(InteractionManager.java:672)
//         at com.hypixel.hytale.server.core.entity.InteractionManager.syncStart(InteractionManager.java:1303)
//         at com.hypixel.hytale.server.core.entity.InteractionManager.tryConsumePacketQueue(InteractionManager.java:514)
//         at com.hypixel.hytale.server.core.entity.InteractionManager.tick(InteractionManager.java:422)
//         at com.hypixel.hytale.server.core.modules.interaction.system.InteractionSystems$TickInteractionManagerSystem.tick(InteractionSystems.java:76)
//         at com.hypixel.hytale.component.system.tick.EntityTickingSystem.doTick(EntityTickingSystem.java:92)
//         at com.hypixel.hytale.component.system.tick.EntityTickingSystem.tick(EntityTickingSystem.java:36)
//         at com.hypixel.hytale.component.Store.tick(Store.java:2030)
//         at com.hypixel.hytale.component.system.tick.ArchetypeTickingSystem.tick(ArchetypeTickingSystem.java:36)
//         at com.hypixel.hytale.component.Store.tickInternal(Store.java:1986)
//         at com.hypixel.hytale.component.Store.tick(Store.java:1956)
//         at com.hypixel.hytale.server.core.universe.world.World.tick(World.java:461)
//         at com.hypixel.hytale.server.core.util.thread.TickingThread.run(TickingThread.java:95)
//         at java.base/java.lang.Thread.run(Thread.java:1516)
