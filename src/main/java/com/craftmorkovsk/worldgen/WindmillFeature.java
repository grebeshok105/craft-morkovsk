package com.craftmorkovsk.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

/** Windmill-ish barn tower: cobble base, plank upper walls, wool blade rotor. */
public class WindmillFeature extends SimpleStructureFeature {

    private static final BlockState STONE = Blocks.COBBLESTONE.defaultBlockState();
    private static final BlockState PLANK = Blocks.OAK_PLANKS.defaultBlockState();
    private static final BlockState LOG = Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState();
    private static final BlockState WOOL = Blocks.WHITE_WOOL.defaultBlockState();
    private static final BlockState FENCE = Blocks.SPRUCE_FENCE.defaultBlockState();
    private static final BlockState SLAB = Blocks.SPRUCE_SLAB.defaultBlockState();
    private static final BlockState AIR = Blocks.AIR.defaultBlockState();
    private static final BlockState DOOR = Blocks.SPRUCE_DOOR.defaultBlockState();

    @Override
    protected void build(WorldGenLevel level, BlockPos origin, RandomSource random) {
        int ox = origin.getX();
        int oy = origin.getY();
        int oz = origin.getZ();

        // 3x3 footprint, 7 blocks of wall: cobble base 3 high, planks above.
        fill(level, ox - 1, oy - 1, oz - 1, ox + 1, oy - 1, oz + 1, STONE);
        fill(level, ox - 1, oy, oz - 1, ox + 1, oy, oz + 1, STONE);
        for (int y = oy + 1; y <= oy + 6; y++) {
            BlockState wall = y <= oy + 3 ? STONE : PLANK;
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    if (x == -1 || x == 1 || z == -1 || z == 1) {
                        set(level, ox + x, y, oz + z, wall);
                    } else {
                        set(level, ox + x, y, oz + z, AIR);
                    }
                }
            }
        }
        // Corner posts full height.
        for (int[] corner : new int[][]{{-1, -1}, {1, -1}, {-1, 1}, {1, 1}}) {
            fill(level, ox + corner[0], oy, oz + corner[1],
                    ox + corner[0], oy + 6, oz + corner[1], LOG);
        }
        // Door on the south face.
        set(level, ox, oy + 1, oz + 1, DOOR.setValue(DoorBlock.FACING, Direction.SOUTH)
                .setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER));
        set(level, ox, oy + 2, oz + 1, DOOR.setValue(DoorBlock.FACING, Direction.SOUTH)
                .setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER));

        // Roof cap.
        fill(level, ox - 1, oy + 7, oz - 1, ox + 1, oy + 7, oz + 1, SLAB);
        set(level, ox, oy + 8, oz, FENCE);

        // Rotor on the north face: axle fence + wool cross one block out.
        set(level, ox, oy + 5, oz - 2, FENCE);
        int bladeZ = oz - 3;
        set(level, ox, oy + 5, bladeZ, WOOL);
        set(level, ox - 1, oy + 5, bladeZ, WOOL);
        set(level, ox + 1, oy + 5, bladeZ, WOOL);
        set(level, ox, oy + 4, bladeZ, WOOL);
        set(level, ox, oy + 6, bladeZ, WOOL);
        // Diagonal tips for a rough pinwheel look.
        set(level, ox - 1, oy + 6, bladeZ, WOOL);
        set(level, ox + 1, oy + 4, bladeZ, WOOL);

        // Hay bales parked beside the tower.
        set(level, ox + 2, oy, oz + 1, Blocks.HAY_BLOCK.defaultBlockState());
        set(level, ox + 2, oy, oz, Blocks.HAY_BLOCK.defaultBlockState());

        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                if (x == -1 || x == 1 || z == -1 || z == 1) {
                    support(level, ox + x, oy - 1, oz + z, 5);
                }
            }
        }
    }
}
