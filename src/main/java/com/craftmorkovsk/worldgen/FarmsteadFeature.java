package com.craftmorkovsk.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;

/** 7x5 wood-and-stone hut with a planted morkov plot beside it. */
public class FarmsteadFeature extends SimpleStructureFeature {

    private static final BlockState PLANK = Blocks.OAK_PLANKS.defaultBlockState();
    private static final BlockState LOG = Blocks.OAK_LOG.defaultBlockState();
    private static final BlockState STONE = Blocks.COBBLESTONE.defaultBlockState();
    private static final BlockState GLASS = Blocks.GLASS_PANE.defaultBlockState();
    private static final BlockState AIR = Blocks.AIR.defaultBlockState();
    private static final BlockState STAIR = Blocks.OAK_STAIRS.defaultBlockState();
    private static final BlockState SLAB = Blocks.OAK_SLAB.defaultBlockState();

    @Override
    protected void build(WorldGenLevel level, BlockPos origin, RandomSource random) {
        int ox = origin.getX();
        int oy = origin.getY();
        int oz = origin.getZ();

        // Foundation: stone under the hut footprint, plank floor on top.
        fill(level, ox - 3, oy - 1, oz - 2, ox + 3, oy - 1, oz + 2, STONE);
        fill(level, ox - 3, oy, oz - 2, ox + 3, oy, oz + 2, PLANK);

        // Carve interior + doorway so terrain never fills the hut.
        fill(level, ox - 2, oy + 1, oz - 1, ox + 2, oy + 3, oz + 1, AIR);
        fill(level, ox, oy + 1, oz + 2, ox, oy + 2, oz + 2, AIR);

        // Walls: two layers, cobble base ring then planks, log corners.
        for (int y = oy + 1; y <= oy + 2; y++) {
            BlockState wall = y == oy + 1 ? STONE : PLANK;
            fill(level, ox - 3, y, oz - 2, ox + 3, y, oz - 2, wall);
            fill(level, ox - 3, y, oz + 2, ox + 3, y, oz + 2, wall);
            fill(level, ox - 3, y, oz - 1, ox - 3, y, oz + 1, wall);
            fill(level, ox + 3, y, oz - 1, ox + 3, y, oz + 1, wall);
        }
        for (int[] corner : new int[][]{{-3, -2}, {3, -2}, {-3, 2}, {3, 2}}) {
            fill(level, ox + corner[0], oy + 1, oz + corner[1],
                    ox + corner[0], oy + 3, oz + corner[1], LOG);
        }
        // Windows on the long walls, door gap on the south face.
        set(level, ox - 1, oy + 2, oz - 2, GLASS);
        set(level, ox + 1, oy + 2, oz - 2, GLASS);
        set(level, ox, oy + 1, oz + 2, AIR);
        set(level, ox, oy + 2, oz + 2, AIR);

        // Gabled roof: stair rows stepping inward, slab ridge.
        BlockState northStair = STAIR.setValue(StairBlock.FACING, Direction.SOUTH);
        BlockState southStair = STAIR.setValue(StairBlock.FACING, Direction.NORTH);
        fill(level, ox - 3, oy + 3, oz - 2, ox + 3, oy + 3, oz - 2, northStair);
        fill(level, ox - 3, oy + 3, oz + 2, ox + 3, oy + 3, oz + 2, southStair);
        fill(level, ox - 2, oy + 4, oz - 1, ox + 2, oy + 4, oz - 1, northStair);
        fill(level, ox - 2, oy + 4, oz + 1, ox + 2, oy + 4, oz + 1, southStair);
        fill(level, ox - 3, oy + 5, oz, ox + 3, oy + 5, oz, SLAB);

        // Interior dressing: composter + hay bale.
        set(level, ox - 2, oy + 1, oz - 1, Blocks.COMPOSTER.defaultBlockState());
        set(level, ox + 2, oy + 1, oz - 1, Blocks.HAY_BLOCK.defaultBlockState());

        // Farm plot east of the hut: 3x5 rich farmland, central water canal, crops.
        fill(level, ox + 4, oy + 1, oz - 2, ox + 6, oy + 3, oz + 2, AIR);
        for (int z = -2; z <= 2; z++) {
            for (int x = 4; x <= 6; x++) {
                if (x == 5) {
                    set(level, ox + x, oy - 1, oz + z, Blocks.DIRT.defaultBlockState());
                    set(level, ox + x, oy, oz + z, Blocks.WATER.defaultBlockState());
                } else {
                    set(level, ox + x, oy - 1, oz + z, Blocks.DIRT.defaultBlockState());
                    set(level, ox + x, oy, oz + z, farmland());
                    set(level, ox + x, oy + 1, oz + z, morkovCrop(random));
                }
            }
        }

        // Ground the footprint edges if the terrain drops away.
        for (int x = -3; x <= 3; x++) {
            for (int z = -2; z <= 2; z++) {
                if (x == -3 || x == 3 || z == -2 || z == 2) {
                    support(level, ox + x, oy - 1, oz + z, 5);
                }
            }
        }
    }
}
