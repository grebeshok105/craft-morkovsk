package com.craftmorkovsk.data;

import net.minecraft.network.chat.Component;

/** Farmer progression ranks. totalXp thresholds are cumulative. */
public enum FarmingLevel {
    BEGINNER(0, "beginner_farmer", 0),
    FIELD_WORKER(1, "field_worker", 100),
    EXPERIENCED(2, "experienced_farmer", 350),
    AGRICULTURAL_SPECIALIST(3, "agricultural_specialist", 800),
    MORKOVSK_MASTER(4, "morkovsk_master", 1600);

    public final int level;
    public final String id;
    public final int requiredXp;

    FarmingLevel(int level, String id, int requiredXp) {
        this.level = level;
        this.id = id;
        this.requiredXp = requiredXp;
    }

    public static FarmingLevel of(int level) {
        for (FarmingLevel l : values()) if (l.level == level) return l;
        return BEGINNER;
    }

    public static FarmingLevel forXp(int xp) {
        FarmingLevel result = BEGINNER;
        for (FarmingLevel l : values()) if (xp >= l.requiredXp) result = l;
        return result;
    }

    public static int maxLevel() {
        return MORKOVSK_MASTER.level;
    }

    public Component displayName() {
        return Component.translatable("farming.craftmorkovsk.level." + id);
    }
}
