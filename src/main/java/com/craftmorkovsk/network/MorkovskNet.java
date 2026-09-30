package com.craftmorkovsk.network;

import com.craftmorkovsk.CraftMorkovsk;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

/** Shared SimpleChannel. Feature packages register their packets inside their Module#init()
 *  through {@link #register}. Registration order is fixed by the module call order in
 *  {@link com.craftmorkovsk.MorkovskModules} — the same on client and server, so packet ids
 *  stay consistent. */
public final class MorkovskNet {

    private static final String VERSION = "1";
    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(CraftMorkovsk.MOD_ID, "main"),
            () -> VERSION, VERSION::equals, VERSION::equals);

    private static int nextId = 0;

    private MorkovskNet() {}

    /** Registers a packet handler. Call from module init only (never lazily). */
    public static <M> void register(Class<M> type,
                                    java.util.function.BiConsumer<M, java.util.function.Supplier<net.minecraftforge.network.NetworkEvent.Context>> handler,
                                    java.util.function.BiConsumer<M, net.minecraft.network.FriendlyByteBuf> encoder,
                                    java.util.function.Function<net.minecraft.network.FriendlyByteBuf, M> decoder,
                                    NetworkDirection direction) {
        CHANNEL.messageBuilder(type, nextId++, direction)
                .encoder(encoder)
                .decoder(decoder)
                .consumerNetworkThread(handler)
                .add();
    }

    /** S2C convenience: same as register() with PLAY_TO_CLIENT. */
    public static <M> void registerS2C(Class<M> type,
                                       java.util.function.BiConsumer<M, java.util.function.Supplier<net.minecraftforge.network.NetworkEvent.Context>> handler,
                                       java.util.function.BiConsumer<M, net.minecraft.network.FriendlyByteBuf> encoder,
                                       java.util.function.Function<net.minecraft.network.FriendlyByteBuf, M> decoder) {
        register(type, handler, encoder, decoder, NetworkDirection.PLAY_TO_CLIENT);
    }

    /** C2S convenience: same as register() with PLAY_TO_SERVER. */
    public static <M> void registerC2S(Class<M> type,
                                       java.util.function.BiConsumer<M, java.util.function.Supplier<net.minecraftforge.network.NetworkEvent.Context>> handler,
                                       java.util.function.BiConsumer<M, net.minecraft.network.FriendlyByteBuf> encoder,
                                       java.util.function.Function<net.minecraft.network.FriendlyByteBuf, M> decoder) {
        register(type, handler, encoder, decoder, NetworkDirection.PLAY_TO_SERVER);
    }

    public static void sendToPlayer(ServerPlayer player, Object msg) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), msg);
    }

    public static void sendToServer(Object msg) {
        CHANNEL.sendToServer(msg);
    }

    public static void sendToAllTracking(net.minecraft.world.entity.Entity entity, Object msg) {
        CHANNEL.send(PacketDistributor.TRACKING_ENTITY.with(() -> entity), msg);
    }

    public static void sendToAllTrackingAndSelf(net.minecraft.world.entity.Entity entity, Object msg) {
        CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> entity), msg);
    }
}
