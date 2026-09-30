package com.craftmorkovsk.client;

import com.craftmorkovsk.data.FarmingLevel;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/** Client-side cache of the synced farming data (filled by SyncFarmingPacket). */
@OnlyIn(Dist.CLIENT)
public final class ClientFarmingData {
    private static int xp;
    private static int level;
    private static long money;

    private ClientFarmingData() {}

    public static void update(int newXp, int newLevel, long newMoney) {
        xp = newXp;
        level = newLevel;
        money = newMoney;
    }

    public static int xp() { return xp; }
    public static int level() { return level; }
    public static long money() { return money; }
    public static FarmingLevel rank() { return FarmingLevel.of(level); }
}
