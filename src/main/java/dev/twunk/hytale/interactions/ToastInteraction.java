// package dev.twunk.hytale.interactions;
// import com.hypixel.hytale.codec.builder.BuilderCodec;
// import com.hypixel.hytale.component.CommandBuffer;
// import com.hypixel.hytale.math.vector.Vector3i;
// import com.hypixel.hytale.protocol.InteractionType;
// import com.hypixel.hytale.server.core.entity.InteractionContext;
// import com.hypixel.hytale.server.core.entity.entities.Player;
// import com.hypixel.hytale.server.core.inventory.ItemStack;
// import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
// import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
// import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction;
// import com.hypixel.hytale.server.core.modules.interaction.interaction.config.client.SimpleBlockInteraction;
// import com.hypixel.hytale.server.core.plugin.registry.CodecMapRegistry.Assets;
// import com.hypixel.hytale.server.core.universe.world.World;
// import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
// import dev.twunk.hytale.plugin.ModPlugin;
// import dev.twunk.hytale.utils.ItemUtils;
// import dev.twunk.hytale.utils.message.Chat;
// import javax.annotation.Nonnull;
// import javax.annotation.Nullable;
// public class ToastInteraction extends SimpleInstantInteraction {
//     @Nonnull
//     public static final BuilderCodec<ToastInteraction> CODEC = BuilderCodec.builder(
//         ToastInteraction.class,
//         ToastInteraction::new,
//         SimpleBlockInteraction.CODEC
//     ).build();
//     @Override
//     protected void interactWithBlock(
//         @Nonnull World world,
//         @Nonnull CommandBuffer<EntityStore> commandBuffer,
//         @Nonnull InteractionType interactionType,
//         @Nonnull InteractionContext interactionContext,
//         @Nullable ItemStack heldItem,
//         @Nonnull Vector3i pos,
//         @Nonnull CooldownHandler cooldownHandler
//     ) {
//         var player = commandBuffer.getComponent(interactionContext.getOwningEntity(), Player.getComponentType());
//         if (player == null) {
//             return;
//         }
//         var playerRef = player.getReference();
//         if (playerRef == null) {
//             return;
//         }
//         // var primaryMessage = Message.raw("THIS WORKS!!!");
//         // var secondaryMessage = Message.raw("This is the secondary message").color("#228B22");
//         // Toast.build("THIS WORKS!!!", secondaryMessage)
//         //     .setItem((ItemWithAllMetadata) new ItemStack("Weapon_Sword_Mithril", 1).toPacket())
//         //     .send(player);
//         // Chat.sendMessage(player, "RAW: ", primaryMessage);
//         // Chat.log(Message.raw("UNIVERSE: ").color("#494949"), primaryMessage);
//         // Chat.log(world, Message.raw("WORLD:    ").color("#494949"), primaryMessage);
//         // Chat.log(player, Message.raw("PLAYER:   ").color("#494949"), primaryMessage);
//         Chat.log(
//             "Spawned item (interact): ",
//             ItemUtils.spawn(playerRef, commandBuffer, pos.clone().add(0, 1, 0), new ItemStack("Soil_Grass", 1))
//         );
//         this.simulateInteractWithBlock(interactionType, interactionContext, heldItem, world, pos);
//     }
//     @Override
//     protected void simulateInteractWithBlock(
//         @Nonnull InteractionType interactionType,
//         @Nonnull InteractionContext interactionContext,
//         @Nullable ItemStack heldItem,
//         @Nonnull World world,
//         @Nonnull Vector3i pos
//     ) {}
//     @Nonnull
//     @SuppressWarnings("null")
//     public final String getId() {
//         return this.getClass().getName();
//     }
//     @Nonnull
//     public final BuilderCodec<ToastInteraction> getCodec() {
//         return ToastInteraction.CODEC;
//     }
//     protected Assets<Interaction, ?> registerToPlugin(final @Nonnull ModPlugin plugin) {
//         return plugin.getCodecRegistry(Interaction.CODEC).register(this.getId(), this.getClass(), this.getCodec());
//     }
// }
