package com.craftmorkovsk.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.Tags;

/** Greenhouse detection: a crop counts as covered when at least 3 glass blocks
 *  (blocks or panes, clear or stained) appear in the 3x3x6 volume above it.
 *  Bounded scan per call — safe to run from crop randomTick. */
public final class GreenhouseCheck {

    private static final int REQUIRED_GLASS = 3;
    private static final int SCAN_RADIUS = 1;
    private static final int SCAN_HEIGHT = 6;

    private GreenhouseCheck() {}

    public static boolean isInsideGreenhouse(LevelReader level, BlockPos pos) {
        int glass = 0;
        for (int dy = 1; dy <= SCAN_HEIGHT; dy++) {
            for (int dx = -SCAN_RADIUS; dx <= SCAN_RADIUS; dx++) {
                for (int dz = -SCAN_RADIUS; dz <= SCAN_RADIUS; dz++) {
                    BlockState state = level.getBlockState(pos.offset(dx, dy, dz));
                    if (state.is(Tags.Blocks.GLASS) || state.is(Tags.Blocks.GLASS_PANES)) {
                        if (++glass >= REQUIRED_GLASS) return true;
                    }
                }
            }
        }
        return false;
    }
}
