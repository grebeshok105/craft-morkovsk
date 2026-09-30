package com.craftmorkovsk.worldgen;

import com.craftmorkovsk.crop.CropCatalog;
import com.craftmorkovsk.crop.CropDef;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;

import java.util.ArrayList;
import java.util.List;

/** Decorative wild plant found in plains/forests. Breaking it yields 1-2 seed
 *  stacks picked from the common (non-secret, non-rare) crop roster. */
public class WildCropBlock extends BushBlock {

    /** Common starter crops whose seeds a wild plant can drop. */
    private static final String[] WILD_SEED_IDS = {
            "morkov", "carrot_red", "potato", "onion", "lettuce",
            "wheat_spelt", "wheat_rye", "sunflower", "cabbage", "cucumber", "garlic"
    };

    public WildCropBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected boolean mayPlaceOn(BlockState ground, BlockGetter level, BlockPos pos) {
        return ground.is(BlockTags.DIRT) || ground.getBlock() instanceof FarmBlock;
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        ServerLevel level = params.getLevel();
        RandomSource random = level.random;
        List<CropDef> pool = wildSeedPool();
        List<ItemStack> drops = new ArrayList<>();
        int count = 1 + random.nextInt(2);
        for (int i = 0; i < count; i++) {
            CropDef def = pool.get(random.nextInt(pool.size()));
            if (def.seeds != null) drops.add(new ItemStack(def.seeds.get()));
        }
        return drops;
    }

    private static List<CropDef> wildSeedPool() {
        List<CropDef> pool = new ArrayList<>();
        for (String id : WILD_SEED_IDS) {
            CropDef def = CropCatalog.BY_ID.get(id);
            if (def != null && !def.secret) pool.add(def);
        }
        return pool;
    }
}
