package com.craftmorkovsk.soil;

import com.craftmorkovsk.config.MorkovskConfig;
import com.craftmorkovsk.core.FarmingHooks;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.RegistryObject;

/** Shared soil queries used by crops, fertilizer items, and irrigation. */
public final class SoilAPI {

    private SoilAPI() {}

    /** Tier of the soil block at this position (the soil itself, not the crop above). */
    public static SoilTier tierAt(BlockGetter level, BlockPos soilPos) {
        BlockState state = level.getBlockState(soilPos);
        if (state.getBlock() instanceof MorkovskSoilBlock soil) return soil.tier();
        if (state.getBlock() instanceof FarmBlock) return SoilTier.NORMAL;
        if (state.is(Blocks.DIRT) || state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.DIRT_PATH))
            return SoilTier.EXHAUSTED;
        return SoilTier.NORMAL;
    }

    public static boolean isFarmlandLike(BlockState state) {
        return state.getBlock() instanceof FarmBlock;
    }

    /** Hydrated if moist, wet-tier, irrigated, or under rain. */
    public static boolean isHydrated(Level level, BlockPos soilPos) {
        BlockState state = level.getBlockState(soilPos);
        if (state.getBlock() instanceof MorkovskSoilBlock soil && soil.tier() == SoilTier.WET) return true;
        if (state.hasProperty(FarmBlock.MOISTURE) && state.getValue(FarmBlock.MOISTURE) > 0) return true;
        if (FarmingHooks.isIrrigated(level, soilPos)) return true;
        return level.canSeeSky(soilPos.above()) && level.isRainingAt(soilPos.above());
    }

    /** Degrades the soil one tier with probability from config (fertilized soil resists). */
    public static void degrade(Level level, BlockPos soilPos, RandomSource random) {
        BlockState state = level.getBlockState(soilPos);
        if (!(state.getBlock() instanceof MorkovskSoilBlock soil)) return;
        SoilTier tier = soil.tier();
        if (tier == SoilTier.EXHAUSTED) return;
        float chance = (float) (MorkovskConfig.SOIL_DEGRADE_CHANCE.get() * (1.0f - tier.degradeResist));
        if (random.nextFloat() >= chance) return;
        SoilTier next = tier.degraded();
        level.setBlock(soilPos, blockFor(next, state), 3);
    }

    /** Replaces this soil with the given tier's block, preserving moisture. */
    public static void setTier(Level level, BlockPos soilPos, SoilTier tier) {
        BlockState state = level.getBlockState(soilPos);
        if (!(state.getBlock() instanceof FarmBlock) && !(state.getBlock() instanceof MorkovskSoilBlock)
                && !state.is(Blocks.DIRT) && !state.is(Blocks.GRASS_BLOCK)) return;
        BlockState next = blockFor(tier, state);
        level.setBlock(soilPos, next, 3);
    }

    private static BlockState blockFor(SoilTier tier, BlockState previous) {
        RegistryObject<MorkovskSoilBlock> reg = SoilModule.BLOCKS_BY_TIER.get(tier);
        BlockState next = reg.get().defaultBlockState();
        if (previous.hasProperty(FarmBlock.MOISTURE) && next.hasProperty(FarmBlock.MOISTURE))
            next = next.setValue(FarmBlock.MOISTURE, previous.getValue(FarmBlock.MOISTURE));
        return next;
    }
}
