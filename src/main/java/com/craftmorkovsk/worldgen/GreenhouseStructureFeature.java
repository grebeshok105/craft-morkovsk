package com.craftmorkovsk.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/** 5x5 glass house packed with planted morkov — matches the greenhouse checker's
 *  scan so crops inside count as greenhouse-grown. */
public class GreenhouseStructureFeature extends SimpleStructureFeature {

    private static final BlockState GLASS = Blocks.GLASS.defaultBlockState();
    private static final BlockState PLANK = Blocks.OAK_PLANKS.defaultBlockState();
    private static final BlockState AIR = Blocks.AIR.defaultBlockState();

    @Override
    protected void build(WorldGenLevel level, BlockPos origin, RandomSource random) {
        int ox = origin.getX();
        int oy = origin.getY();
        int oz = origin.getZ();

        // Frame floor, then glass walls (two layers), full glass roof.
        fill(level, ox - 2, oy, oz - 2, ox + 2, oy, oz + 2, PLANK);
        for (int y = oy + 1; y <= oy + 2; y++) {
            fill(level, ox - 2, y, oz - 2, ox + 2, y, oz - 2, GLASS);
            fill(level, ox - 2, y, oz + 2, ox + 2, y, oz + 2, GLASS);
            fill(level, ox - 2, y, oz - 1, ox - 2, y, oz + 1, GLASS);
            fill(level, ox + 2, y, oz - 1, ox + 2, y, oz + 1, GLASS);
        }
        fill(level, ox - 2, oy + 3, oz - 2, ox + 2, oy + 3, oz + 2, GLASS);

        // Doorway on the south face.
        set(level, ox, oy + 1, oz + 2, AIR);
        set(level, ox, oy + 2, oz + 2, AIR);

        // Interior 3x3: sunken water centre, farmland + planted morkov elsewhere.
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                if (x == 0 && z == 0) {
                    set(level, ox, oy, oz, Blocks.WATER.defaultBlockState());
                } else {
                    set(level, ox + x, oy, oz + z, farmland());
                    set(level, ox + x, oy + 1, oz + z, morkovCrop(random));
                }
            }
        }

        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                if (x == -2 || x == 2 || z == -2 || z == 2) {
                    support(level, ox + x, oy, oz + z, 5);
                }
            }
        }
    }
}
