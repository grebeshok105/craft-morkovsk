package com.craftmorkovsk.economy;

import com.craftmorkovsk.data.FarmingStats;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** C2S: buy the shop offer at this index. The server re-validates everything —
 *  the player must have a ShopMenu open, the offer must exist and be unlocked
 *  for their farming level, and the morkoins are spent before the item is given. */
public record BuyPacket(int offerIndex) {

    public static void encode(BuyPacket pkt, FriendlyByteBuf buf) {
        buf.writeVarInt(pkt.offerIndex);
    }

    public static BuyPacket decode(FriendlyByteBuf buf) {
        return new BuyPacket(buf.readVarInt());
    }

    public static void handle(BuyPacket pkt, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;
            if (!(player.containerMenu instanceof ShopMenu)) return;
            ShopCatalog.ShopOffer offer = ShopCatalog.offer(pkt.offerIndex);
            if (offer == null) return;
            if (offer.secret() && FarmingStats.getLevel(player) < ShopCatalog.SECRET_LEVEL) {
                player.displayClientMessage(Component.translatable(
                        "economy.craftmorkovsk.locked", ShopCatalog.SECRET_LEVEL), true);
                return;
            }
            if (!FarmingStats.trySpend(player, offer.price())) {
                player.displayClientMessage(
                        Component.translatable("economy.craftmorkovsk.cant_afford"), true);
                return;
            }
            ItemStack stack = offer.stack().get();
            if (!player.getInventory().add(stack)) {
                player.drop(stack, false);
            }
            player.displayClientMessage(Component.translatable(
                    "economy.craftmorkovsk.purchased", stack.getHoverName(), offer.price()), true);
            player.level().playSound(null, player.blockPosition(),
                    SoundEvents.VILLAGER_YES, SoundSource.PLAYERS, 0.6f, 1.2f);
        });
        ctx.get().setPacketHandled(true);
    }
}
