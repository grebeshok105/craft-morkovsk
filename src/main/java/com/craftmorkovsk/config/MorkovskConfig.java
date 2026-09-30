package com.craftmorkovsk.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

/** Central balancing knobs, exposed as a COMMON Forge config (craftmorkovsk-common.toml). */
public final class MorkovskConfig {

    private static final ForgeConfigSpec.Builder B = new ForgeConfigSpec.Builder();

    public static final ForgeConfigSpec.DoubleValue CROP_GROWTH_MULTIPLIER = B
            .comment("Global multiplier applied to all mod crop growth speed")
            .defineInRange("crops.growthMultiplier", 1.0, 0.05, 20.0);
    public static final ForgeConfigSpec.DoubleValue FARMING_XP_MULTIPLIER = B
            .comment("Multiplier for all farming XP awards")
            .defineInRange("progression.xpMultiplier", 1.0, 0.1, 10.0);
    public static final ForgeConfigSpec.IntValue MACHINE_ENERGY_PER_TICK = B
            .comment("Default Forge Energy consumed by a running machine per tick")
            .defineInRange("machines.energyPerTick", 20, 0, 10000);
    public static final ForgeConfigSpec.IntValue GENERATOR_FE_PER_TICK = B
            .comment("FE produced per tick by the small generator while burning")
            .defineInRange("energy.generatorFePerTick", 40, 1, 10000);
    public static final ForgeConfigSpec.IntValue BIOMASS_FE_PER_TICK = B
            .comment("FE produced per tick by the biomass generator while burning")
            .defineInRange("energy.biomassFePerTick", 30, 1, 10000);
    public static final ForgeConfigSpec.IntValue IRRIGATION_SPRINKLER_RANGE = B
            .comment("Basic sprinkler hydration radius in blocks")
            .defineInRange("irrigation.sprinklerRange", 3, 1, 16);
    public static final ForgeConfigSpec.IntValue IRRIGATION_ADVANCED_SPRINKLER_RANGE = B
            .comment("Advanced sprinkler hydration radius in blocks")
            .defineInRange("irrigation.advancedSprinklerRange", 6, 1, 32);
    public static final ForgeConfigSpec.IntValue BASE_CROP_PRICE = B
            .comment("Fallback sell price (in morkoins) for a NORMAL quality crop with no dedicated price")
            .defineInRange("economy.baseCropPrice", 8, 1, 100000);
    public static final ForgeConfigSpec.DoubleValue PRICE_FLUCTUATION = B
            .comment("Daily random price fluctuation amplitude, 0.1 = +/-10%")
            .defineInRange("economy.priceFluctuation", 0.15, 0.0, 1.0);
    public static final ForgeConfigSpec.IntValue TRACTOR_FUEL_PER_TICK = B
            .comment("Fuel units the tractor burns per tick while driving")
            .defineInRange("tractor.fuelPerTick", 1, 0, 1000);
    public static final ForgeConfigSpec.IntValue TRACTOR_MAX_FUEL = B
            .comment("Tractor fuel tank capacity in fuel units")
            .defineInRange("tractor.maxFuel", 4000, 100, 1000000);
    public static final ForgeConfigSpec.DoubleValue QUALITY_LUCK_PER_LEVEL = B
            .comment("Extra probability per farming level of upgrading crop quality by one tier")
            .defineInRange("quality.luckPerLevel", 0.04, 0.0, 1.0);
    public static final ForgeConfigSpec.DoubleValue SOIL_DEGRADE_CHANCE = B
            .comment("Chance that harvesting a crop degrades its soil one tier (fertilized soil resists)")
            .defineInRange("soil.degradeChance", 0.25, 0.0, 1.0);
    public static final ForgeConfigSpec.IntValue THE_CARROT_GROWTH_TICKS = B
            .comment("Average random ticks per growth stage for THE CARROT (dramatic slowness)")
            .defineInRange("rare.theCarrotGrowthTicks", 4000, 100, 1000000);

    public static final ForgeConfigSpec SPEC = B.build();

    private MorkovskConfig() {}

    public static void register() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, SPEC, "craftmorkovsk-common.toml");
    }
}
