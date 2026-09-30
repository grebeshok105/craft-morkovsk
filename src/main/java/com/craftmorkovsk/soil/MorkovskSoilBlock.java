package com.craftmorkovsk.soil;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.common.FarmlandWaterManager;

/** Soil block carrying a {@link SoilTier}. Keeps vanilla farmland moisture mechanics;
 *  hydration also accepts irrigation hooks; exhausted soil dehydrates instead of dirt. */
public class MorkovskSoilBlock extends FarmBlock {

    private final SoilTier tier;

    public MorkovskSoilBlock(Properties properties, SoilTier tier) {
        super(properties);
        this.tier = tier;
    }

    public SoilTier tier() { return tier; }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        int moisture = state.getValue(MOISTURE);
        if (tier == SoilTier.WET || com.craftmorkovsk.core.FarmingHooks.isIrrigated(level, pos)) {
            if (moisture < MAX_MOISTURE) level.setBlock(pos, state.setValue(MOISTURE, MAX_MOISTURE), 2);
            return;
        }
        if (!isNearWater(level, pos) && !level.isRainingAt(pos.above())) {
            if (moisture > 0) {
                level.setBlock(pos, state.setValue(MOISTURE, moisture - 1), 2);
            } else if (!hasCrop(level, pos)) {
                turnToDirt(null, state, level, pos);
            }
        } else if (moisture < MAX_MOISTURE) {
            level.setBlock(pos, state.setValue(MOISTURE, MAX_MOISTURE), 2);
        }
    }

    private static boolean isNearWater(LevelReader level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (FarmlandWaterManager.hasBlockWaterTicket(level, pos)) return true;
        for (BlockPos p : BlockPos.betweenClosed(pos.offset(-4, 0, -4), pos.offset(4, 1, 4))) {
            FluidState fluid = level.getFluidState(p);
            if (fluid.is(FluidTags.WATER)) return true;
            if (state.canBeHydrated(level, pos, fluid, p)) return true;
        }
        return false;
    }

    private static boolean hasCrop(BlockGetter level, BlockPos pos) {
        return level.getBlockState(pos.above()).is(BlockTags.MAINTAINS_FARMLAND);
    }

    @Override
    public boolean isFertile(BlockState state, BlockGetter level, BlockPos pos) {
        return tier.rank >= SoilTier.RICH.rank;
    }
}
