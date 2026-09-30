package com.craftmorkovsk.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** S2C sync of farming XP/level/money into the client cache used by HUD and screens. */
public record SyncFarmingPacket(int xp, int level, long money) {

    public static void encode(SyncFarmingPacket pkt, FriendlyByteBuf buf) {
        buf.writeVarInt(pkt.xp);
        buf.writeVarInt(pkt.level);
        buf.writeVarLong(pkt.money);
    }

    public static SyncFarmingPacket decode(FriendlyByteBuf buf) {
        return new SyncFarmingPacket(buf.readVarInt(), buf.readVarInt(), buf.readVarLong());
    }

    public static void handle(SyncFarmingPacket pkt, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() ->
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                        () -> () -> com.craftmorkovsk.client.ClientFarmingData.update(pkt.xp, pkt.level, pkt.money)));
        ctx.get().setPacketHandled(true);
    }
}
