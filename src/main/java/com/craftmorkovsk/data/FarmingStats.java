package com.craftmorkovsk.data;

import com.craftmorkovsk.config.MorkovskConfig;
import com.craftmorkovsk.network.MorkovskNet;
import com.craftmorkovsk.network.SyncFarmingPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.common.util.LazyOptional;

/** Static API all feature packages use to read/mutate player farming state.
 *  Always call server-side; state is capability-backed and synced to the client automatically. */
public final class FarmingStats {

    public enum XpReason {
        PLANTING(2), HARVESTING(5), BREEDING(8), PROCESSING(4), MACHINE_USE(3), SELLING(2), SPECIAL(10);
        public final int base;
        XpReason(int base) { this.base = base; }
    }

    private FarmingStats() {}

    public static LazyOptional<PlayerFarmingData> get(ServerPlayer player) {
        return player.getCapability(PlayerFarmingData.CAPABILITY);
    }

    public static int getLevel(ServerPlayer player) {
        return get(player).map(PlayerFarmingData::getLevel).orElse(0);
    }

    public static int getXp(ServerPlayer player) {
        return get(player).map(PlayerFarmingData::getXp).orElse(0);
    }

    public static long getMoney(ServerPlayer player) {
        return get(player).map(PlayerFarmingData::getMoney).orElse(0L);
    }

    /** Awards scaled XP, syncs, and announces level-ups. Returns the new level. */
    public static int awardXp(ServerPlayer player, XpReason reason, int times) {
        int amount = (int) Math.round(reason.base * times * MorkovskConfig.FARMING_XP_MULTIPLIER.get());
        if (amount <= 0) return getLevel(player);
        int[] holder = new int[2];
        get(player).ifPresent(data -> {
            int oldLevel = data.getLevel();
            data.addXp(amount);
            int newLevel = data.getLevel();
            holder[0] = newLevel;
            holder[1] = oldLevel;
        });
        sync(player);
        if (holder[0] > holder[1]) onLevelUp(player, holder[0]);
        return holder[0];
    }

    private static void onLevelUp(ServerPlayer player, int newLevel) {
        FarmingLevel rank = FarmingLevel.of(newLevel);
        player.displayClientMessage(Component.translatable(
                        "farming.craftmorkovsk.level_up", rank.displayName())
                .withStyle(ChatFormatting.GOLD), false);
        player.level().playSound(null, player.blockPosition(),
                SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.7f, 1.2f);
    }

    public static void addMoney(ServerPlayer player, long amount) {
        get(player).ifPresent(data -> data.addMoney(amount));
        sync(player);
    }

    public static boolean trySpend(ServerPlayer player, long amount) {
        boolean[] ok = {false};
        get(player).ifPresent(data -> ok[0] = data.trySpend(amount));
        if (ok[0]) sync(player);
        return ok[0];
    }

    public static void sync(ServerPlayer player) {
        get(player).ifPresent(data ->
                MorkovskNet.sendToPlayer(player, new SyncFarmingPacket(data.getXp(), data.getLevel(), data.getMoney())));
    }
}
