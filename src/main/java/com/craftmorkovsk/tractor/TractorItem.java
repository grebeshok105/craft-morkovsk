package com.craftmorkovsk.tractor;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

/** Places a tractor entity on the clicked face, like a minecart item. */
public class TractorItem extends Item {

    public TractorItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        Level level = ctx.getLevel();
        BlockPos pos = ctx.getClickedPos().relative(ctx.getClickedFace());
        Player player = ctx.getPlayer();
        if (!level.isClientSide) {
            TractorEntity tractor = new TractorEntity(TractorModule.TRACTOR.get(), level);
            tractor.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
            if (player != null) tractor.setYRot(player.getYRot());
            level.addFreshEntity(tractor);
            if (player == null || !player.getAbilities().instabuild) ctx.getItemInHand().shrink(1);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
