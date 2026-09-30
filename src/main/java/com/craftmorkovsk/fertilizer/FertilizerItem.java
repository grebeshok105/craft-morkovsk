package com.craftmorkovsk.fertilizer;

import com.craftmorkovsk.soil.SoilAPI;
import com.craftmorkovsk.soil.SoilTier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

/** Fertilizer tiers. Each potency maps soil to a target tier and feeds the quality roll. */
public class FertilizerItem extends Item {

    public enum Potency {
        COMPOST(SoilTier.COMPOST),
        MANURE(SoilTier.COMPOST),
        MINERAL(SoilTier.FERTILIZED),
        PREMIUM(SoilTier.FERTILIZED);

        public final SoilTier target;
        Potency(SoilTier target) { this.target = target; }
    }

    private final Potency potency;

    public FertilizerItem(Properties properties, Potency potency) {
        super(properties);
        this.potency = potency;
    }

    public Potency potency() { return potency; }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        Level level = ctx.getLevel();
        var pos = ctx.getClickedPos();
        SoilTier current = SoilAPI.tierAt(level, pos);
        if (current.rank >= potency.target.rank) return InteractionResult.PASS;
        if (!SoilAPI.isFarmlandLike(level.getBlockState(pos))
                && !(level.getBlockState(pos).getBlock() instanceof com.craftmorkovsk.soil.MorkovskSoilBlock)
                && !level.getBlockState(pos).is(net.minecraft.world.level.block.Blocks.DIRT)
                && !level.getBlockState(pos).is(net.minecraft.world.level.block.Blocks.GRASS_BLOCK))
            return InteractionResult.PASS;
        if (!level.isClientSide) {
            SoilAPI.setTier(level, pos, potency.target);
            level.playSound(null, pos, SoundEvents.HOE_TILL, SoundSource.BLOCKS, 0.8f, 0.9f);
            ctx.getItemInHand().shrink(1);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
