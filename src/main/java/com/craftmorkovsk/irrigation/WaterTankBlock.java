package com.craftmorkovsk.irrigation;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

/** Water tank block. Right-click with a water bucket to add, an empty bucket to
 *  take, anything else shows the stored amount (like BatteryBlock's charge). */
public class WaterTankBlock extends Block implements EntityBlock {

    private final java.util.function.Supplier<BlockEntityType<? extends WaterTankBlockEntity>> beType;

    public WaterTankBlock(Properties properties,
                          java.util.function.Supplier<BlockEntityType<? extends WaterTankBlockEntity>> beType) {
        super(properties);
        this.beType = beType;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return beType.get().create(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                  BlockEntityType<T> type) {
        return (l, pos, s, be) -> {
            if (be instanceof WaterTankBlockEntity tank) WaterTankBlockEntity.tick(l, pos, s, tank);
        };
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof WaterTankBlockEntity tank)) return InteractionResult.PASS;
        ItemStack held = player.getItemInHand(hand);
        if (!level.isClientSide) {
            if (held.is(Items.WATER_BUCKET)
                    && tank.receiveWater(WaterTankBlockEntity.BUCKET_UNITS, true) >= WaterTankBlockEntity.BUCKET_UNITS) {
                tank.receiveWater(WaterTankBlockEntity.BUCKET_UNITS, false);
                player.setItemInHand(hand, ItemUtils.createFilledResult(held, player,
                        new ItemStack(Items.BUCKET)));
                level.playSound(null, pos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 1f, 1f);
            } else if (held.is(Items.BUCKET)
                    && tank.extractWater(WaterTankBlockEntity.BUCKET_UNITS, true) >= WaterTankBlockEntity.BUCKET_UNITS) {
                tank.extractWater(WaterTankBlockEntity.BUCKET_UNITS, false);
                player.setItemInHand(hand, ItemUtils.createFilledResult(held, player,
                        new ItemStack(Items.WATER_BUCKET)));
                level.playSound(null, pos, SoundEvents.BUCKET_FILL, SoundSource.BLOCKS, 1f, 1f);
            } else if (!held.is(Items.WATER_BUCKET) && !held.is(Items.BUCKET)) {
                player.displayClientMessage(Component.translatable(
                        "block.craftmorkovsk.water_tank.level",
                        tank.getWaterStored(), tank.getWaterCapacity()), true);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
