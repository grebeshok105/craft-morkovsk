package com.craftmorkovsk.data;

import com.craftmorkovsk.config.MorkovskConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;

/** Crop quality tiers stored as item NBT ("Quality"). Drives price multipliers and naming. */
public enum Quality {
    NORMAL(0, "normal", 1.0, ChatFormatting.WHITE),
    GOOD(1, "good", 1.5, ChatFormatting.GREEN),
    EXCELLENT(2, "excellent", 2.25, ChatFormatting.AQUA),
    PERFECT(3, "perfect", 3.5, ChatFormatting.GOLD);

    public final int index;
    public final String id;
    public final double priceMultiplier;
    public final ChatFormatting color;

    Quality(int index, String id, double priceMultiplier, ChatFormatting color) {
        this.index = index;
        this.id = id;
        this.priceMultiplier = priceMultiplier;
        this.color = color;
    }

    public static Quality of(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains("Quality")) return NORMAL;
        int i = tag.getInt("Quality");
        return i >= 0 && i < values().length ? values()[i] : NORMAL;
    }

    public static void apply(ItemStack stack, Quality quality) {
        if (quality != NORMAL) stack.getOrCreateTag().putInt("Quality", quality.index);
    }

    /** Rolls a quality. soilBonus/levelLuck raise the roll; hydrated adds a nudge. */
    public static Quality roll(RandomSource random, int soilBonus, int farmingLevel, boolean hydrated) {
        int score = soilBonus * 10 + random.nextInt(60);
        double luck = MorkovskConfig.QUALITY_LUCK_PER_LEVEL.get() * farmingLevel * 100;
        if (hydrated) score += 10;
        score += (int) luck;
        if (score >= 70) return PERFECT;
        if (score >= 45) return EXCELLENT;
        if (score >= 22) return GOOD;
        return NORMAL;
    }

    public Component displayName() {
        return Component.translatable("quality.craftmorkovsk." + id).withStyle(color);
    }
}
