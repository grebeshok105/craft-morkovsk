package com.craftmorkovsk.energy;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

/** Battery block — right-click shows charge. Energy flows in/out through the cable network. */
public class BatteryBlock extends Block implements EntityBlock {

    private final java.util.function.Supplier<BlockEntityType<? extends BatteryBlockEntity>> beType;

    public BatteryBlock(Properties properties,
                        java.util.function.Supplier<BlockEntityType<? extends BatteryBlockEntity>> beType) {
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
            if (be instanceof BatteryBlockEntity battery) BatteryBlockEntity.tick(l, pos, s, battery);
        };
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        if (level.getBlockEntity(pos) instanceof BatteryBlockEntity battery) {
            if (level.isClientSide) {
                player.displayClientMessage(Component.translatable(
                        "block.craftmorkovsk.battery.charge",
                        battery.energy().getEnergyStored(), battery.energy().getMaxEnergyStored()), true);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.PASS;
    }
}
