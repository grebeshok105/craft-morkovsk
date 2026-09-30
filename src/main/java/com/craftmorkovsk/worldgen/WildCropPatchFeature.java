package com.craftmorkovsk.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/** Scatters a small patch of wild crops around the origin. Placement JSON decides
 *  how often patches are attempted; this only fans out individual plants. */
public class WildCropPatchFeature extends Feature<NoneFeatureConfiguration> {

    private static final int TRIES = 32;
    private static final int XZ_SPREAD = 5;
    private static final int Y_SPREAD = 3;

    public WildCropPatchFeature() {
        super(NoneFeatureConfiguration.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> ctx) {
        WorldGenLevel level = ctx.level();
        BlockPos origin = ctx.origin();
        RandomSource random = ctx.random();
        BlockState plant = WorldgenModule.WILD_CROP.get().defaultBlockState();
        boolean placed = false;

        for (int i = 0; i < TRIES; i++) {
            BlockPos pos = origin.offset(
                    random.nextInt(XZ_SPREAD * 2 + 1) - XZ_SPREAD,
                    random.nextInt(Y_SPREAD * 2 + 1) - Y_SPREAD,
                    random.nextInt(XZ_SPREAD * 2 + 1) - XZ_SPREAD);
            if (level.getBlockState(pos).isAir()
                    && plant.canSurvive(level, pos)
                    && level.setBlock(pos, plant, 2)) {
                placed = true;
            }
        }
        return placed;
    }
}
