package com.craftmorkovsk.crop;

import com.craftmorkovsk.config.MorkovskConfig;
import com.craftmorkovsk.core.FarmingHooks;
import com.craftmorkovsk.data.FarmingStats;
import com.craftmorkovsk.data.Quality;
import com.craftmorkovsk.soil.SoilAPI;
import com.craftmorkovsk.soil.SoilTier;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/** Data-driven crop. 8 growth stages; soil, hydration, water need, greenhouse and weather
 *  shape speed; harvest applies soil degradation and the quality roll to produce. */
public class MorkovskCropBlock extends CropBlock {

    private final CropDef def;
    private final Supplier<Item> produceItem;
    private final Supplier<Item> seedItem;

    public MorkovskCropBlock(Properties properties, CropDef def,
                             Supplier<Item> produceItem, Supplier<Item> seedItem) {
        super(properties);
        this.def = def;
        this.produceItem = produceItem;
        this.seedItem = seedItem;
    }

    public CropDef def() { return def; }

    @Override
    protected ItemLike getBaseSeedId() {
        return seedItem.get();
    }

    @Override
    protected boolean mayPlaceOn(BlockState ground, BlockGetter level, BlockPos pos) {
        return SoilAPI.isFarmlandLike(ground) && def.acceptsSoil(SoilAPI.tierAt(level, pos));
    }

    @Override
    public boolean canSurvive(BlockState state, net.minecraft.world.level.LevelReader level, BlockPos pos) {
        if (!super.canSurvive(state, level, pos)) return false;
        if (def.requiresHeadroom) return level.getBlockState(pos.above()).isAir();
        return true;
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (isMaxAge(state)) return;
        BlockPos soilPos = pos.below();
        boolean greenhouse = FarmingHooks.isInGreenhouse(level, pos);
        if (!greenhouse && level.getRawBrightness(pos, 0) < 8) return;

        SoilTier tier = SoilAPI.tierAt(level, soilPos);
        boolean hydrated = SoilAPI.isHydrated(level, soilPos);
        float chance = 0.12f * def.growthMult * tier.growthMult
                * MorkovskConfig.CROP_GROWTH_MULTIPLIER.get().floatValue();
        if (!hydrated) chance *= def.waterNeed.dryPenalty;
        if (greenhouse) chance *= 1.6f;
        if (level.isThundering() && level.canSeeSky(pos)) chance *= 1.25f;
        if (level.isRaining() && !level.canSeeSky(pos) && !greenhouse) chance *= 0.7f;

        if (random.nextFloat() < Math.min(chance, 1f)) {
            level.setBlock(pos, getStateForAge(getAge(state) + 1), 2);
        }
    }

    @Override
    public int getBonemealAgeIncrease(Level level) {
        return def.bonemealStages + level.random.nextInt(2);
    }

    @Override
    public boolean isValidBonemealTarget(net.minecraft.world.level.LevelReader level, BlockPos pos,
                                         BlockState state, boolean isClient) {
        return !isMaxAge(state);
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        ServerLevel level = params.getLevel();
        List<ItemStack> drops = new ArrayList<>();
        if (!isMaxAge(state)) {
            drops.add(new ItemStack(seedItem.get()));
            return drops;
        }
        int yield = def.yieldMin + level.random.nextInt(Math.max(1, def.yieldMax - def.yieldMin + 1));
        ItemStack produce = new ItemStack(produceItem.get(), yield);

        BlockPos soilPos = BlockPos.containing(params.getParameter(LootContextParams.ORIGIN)).below();
        Entity breaker = params.getOptionalParameter(LootContextParams.THIS_ENTITY);
        int farmingLevel = breaker instanceof ServerPlayer sp ? FarmingStats.getLevel(sp) : 0;
        SoilTier tier = SoilAPI.tierAt(level, soilPos);
        boolean hydrated = SoilAPI.isHydrated(level, soilPos);
        Quality.apply(produce, Quality.roll(level.random, tier.qualityBonus, farmingLevel, hydrated));
        drops.add(produce);
        drops.add(new ItemStack(seedItem.get(), 1 + level.random.nextInt(def.seedDropMax + 1)));
        return drops;
    }
}
