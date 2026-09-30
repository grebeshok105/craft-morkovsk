package com.craftmorkovsk.worldgen;

import com.craftmorkovsk.crop.CropCatalog;
import com.craftmorkovsk.crop.CropDef;
import com.craftmorkovsk.soil.SoilModule;
import com.craftmorkovsk.soil.SoilTier;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/** Base for the mod's small programmatic farm buildings. The placed-feature JSON
 *  lands the origin on the surface; subclasses stamp their footprint there. */
public abstract class SimpleStructureFeature extends Feature<NoneFeatureConfiguration> {

    protected SimpleStructureFeature() {
        super(NoneFeatureConfiguration.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> ctx) {
        build(ctx.level(), ctx.origin(), ctx.random());
        return true;
    }

    /** Stamp the structure. origin = surface position at the centre of the footprint. */
    protected abstract void build(WorldGenLevel level, BlockPos origin, RandomSource random);

    protected static void set(WorldGenLevel level, int x, int y, int z, BlockState state) {
        set(level, new BlockPos(x, y, z), state);
    }

    protected static void set(WorldGenLevel level, BlockPos pos, BlockState state) {
        level.setBlock(pos, state, 3);
    }

    protected static void fill(WorldGenLevel level, int x0, int y0, int z0,
                               int x1, int y1, int z1, BlockState state) {
        for (BlockPos pos : BlockPos.betweenClosed(x0, y0, z0, x1, y1, z1)) {
            set(level, pos, state);
        }
    }

    /** Dirt column under an exposed floor edge so the building doesn't hover. */
    protected static void support(WorldGenLevel level, int x, int y, int z, int maxDepth) {
        for (int i = 1; i <= maxDepth; i++) {
            BlockPos pos = new BlockPos(x, y - i, z);
            if (!level.getBlockState(pos).isAir()) return;
            set(level, pos, Blocks.DIRT.defaultBlockState());
        }
    }

    protected static BlockState farmland() {
        return SoilModule.BLOCKS_BY_TIER.get(SoilTier.NORMAL).get().defaultBlockState();
    }

    /** A morkov crop at a mid-to-late growth stage for decorating farm plots. */
    protected static BlockState morkovCrop(RandomSource random) {
        CropDef def = CropCatalog.BY_ID.get("morkov");
        Block block = def.block.get();
        return ((CropBlock) block).getStateForAge(3 + random.nextInt(5));
    }
}
