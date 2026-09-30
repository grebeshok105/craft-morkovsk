package com.craftmorkovsk.animal;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import javax.annotation.Nullable;

/** Feeding trough: right-click food to fill (or automate with hoppers). Nearby
 *  farm animals wander over and eat from it via {@link FeedingTroughBlockEntity}'s scan. */
public class FeedingTroughBlock extends BaseEntityBlock {

    public FeedingTroughBlock(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FeedingTroughBlockEntity(pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                  BlockEntityType<T> type) {
        return level.isClientSide ? null
                : createTickerHelper(type, AnimalModule.FEEDING_TROUGH_BE.get(),
                        FeedingTroughBlockEntity::serverTick);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        ItemStack held = player.getItemInHand(hand);
        if (held.isEmpty() || !FeedingTroughBlockEntity.isAnimalFood(held)) {
            return InteractionResult.PASS;
        }
        if (level.getBlockEntity(pos) instanceof FeedingTroughBlockEntity trough) {
            if (!level.isClientSide) {
                int before = held.getCount();
                ItemStack remainder = trough.insertFood(held);
                if (remainder.getCount() != before) {
                    if (!player.getAbilities().instabuild) {
                        player.setItemInHand(hand, remainder);
                    }
                    level.playSound(null, pos, SoundEvents.WOOD_PLACE, SoundSource.BLOCKS,
                            0.7F, 0.9F + level.random.nextFloat() * 0.2F);
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.PASS;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.is(newState.getBlock())) {
            if (level.getBlockEntity(pos) instanceof FeedingTroughBlockEntity trough) {
                for (int slot = 0; slot < FeedingTroughBlockEntity.SLOTS; slot++) {
                    ItemStack stack = trough.getInventory().getStackInSlot(slot);
                    if (!stack.isEmpty()) {
                        level.addFreshEntity(new ItemEntity(level,
                                pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, stack.copy()));
                    }
                }
            }
            super.onRemove(state, level, pos, newState, moved);
        }
    }
}
