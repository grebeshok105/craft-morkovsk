package com.craftmorkovsk.data;

import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.AutoRegisterCapability;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.util.INBTSerializable;

/** Per-player persistent farming state: XP, derived level, morkoins (money). */
@AutoRegisterCapability
public class PlayerFarmingData implements INBTSerializable<CompoundTag> {

    public static final Capability<PlayerFarmingData> CAPABILITY =
            CapabilityManager.get(new CapabilityToken<>() {});

    private int xp;
    private long money;

    public int getXp() { return xp; }
    public void setXp(int xp) { this.xp = Math.max(0, xp); }
    public int addXp(int amount) {
        this.xp += Math.max(0, amount);
        return this.xp;
    }

    public int getLevel() { return FarmingLevel.forXp(xp).level; }

    public long getMoney() { return money; }
    public void setMoney(long money) { this.money = Math.max(0, money); }
    public void addMoney(long amount) { this.money = Math.max(0, this.money + amount); }
    public boolean trySpend(long amount) {
        if (money < amount) return false;
        money -= amount;
        return true;
    }

    public void copyFrom(PlayerFarmingData other) {
        this.xp = other.xp;
        this.money = other.money;
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("FarmingXp", xp);
        tag.putLong("Money", money);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        xp = tag.getInt("FarmingXp");
        money = tag.getLong("Money");
    }
}
