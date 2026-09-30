package com.craftmorkovsk.crop;

import com.craftmorkovsk.soil.SoilTier;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.RegistryObject;

import java.util.EnumSet;
import java.util.Set;

/** Data-driven crop definition. Registration objects are wired in CropCatalog. */
public class CropDef {

    public final String id;
    public final WaterNeed waterNeed;
    public final float growthMult;
    public final int yieldMin;
    public final int yieldMax;
    public final int seedDropMax;
    public final Set<SoilTier> soils;
    public final int nutrition;
    public final float saturation;
    public final boolean requiresHeadroom;
    public final int bonemealStages;
    public final int price;
    public final int xpBonus;
    public final boolean secret;

    public RegistryObject<Block> block;
    public RegistryObject<Item> produce;
    public RegistryObject<Item> seeds;

    private CropDef(Builder b) {
        this.id = b.id;
        this.waterNeed = b.waterNeed;
        this.growthMult = b.growthMult;
        this.yieldMin = b.yieldMin;
        this.yieldMax = b.yieldMax;
        this.seedDropMax = b.seedDropMax;
        this.soils = b.soils;
        this.nutrition = b.nutrition;
        this.saturation = b.saturation;
        this.requiresHeadroom = b.requiresHeadroom;
        this.bonemealStages = b.bonemealStages;
        this.price = b.price;
        this.xpBonus = b.xpBonus;
        this.secret = b.secret;
    }

    public static Builder of(String id) { return new Builder(id); }

    public boolean acceptsSoil(SoilTier tier) {
        if (tier == SoilTier.EXHAUSTED) return false;
        return soils.isEmpty() || soils.contains(tier);
    }

    public static final class Builder {
        private final String id;
        private WaterNeed waterNeed = WaterNeed.MEDIUM;
        private float growthMult = 1.0f;
        private int yieldMin = 1;
        private int yieldMax = 3;
        private int seedDropMax = 2;
        private Set<SoilTier> soils = EnumSet.noneOf(SoilTier.class);
        private int nutrition = 0;
        private float saturation = 0.3f;
        private boolean requiresHeadroom = false;
        private int bonemealStages = 2;
        private int price = 0;
        private int xpBonus = 0;
        private boolean secret = false;

        private Builder(String id) { this.id = id; }

        public Builder water(WaterNeed need) { waterNeed = need; return this; }
        public Builder speed(float mult) { growthMult = mult; return this; }
        public Builder yieldRange(int min, int max) { yieldMin = min; yieldMax = max; return this; }
        public Builder seeds(int extraMax) { seedDropMax = extraMax; return this; }
        public Builder soils(SoilTier... tiers) { soils = EnumSet.copyOf(java.util.List.of(tiers)); return this; }
        public Builder food(int nutrition, float saturation) { this.nutrition = nutrition; this.saturation = saturation; return this; }
        public Builder headroom() { requiresHeadroom = true; return this; }
        public Builder bonemeal(int stages) { bonemealStages = stages; return this; }
        public Builder price(int price) { this.price = price; return this; }
        public Builder xp(int bonus) { xpBonus = bonus; return this; }
        public Builder secret() { secret = true; return this; }
        public CropDef build() { return new CropDef(this); }
    }
}
