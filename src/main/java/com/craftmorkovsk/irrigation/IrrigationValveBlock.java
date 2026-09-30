package com.craftmorkovsk.irrigation;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;

/** In-line valve. Open by default; right-click toggles. When closed the water
 *  network treats this position as a wall (WaterNetwork#conducts). */
public class IrrigationValveBlock extends WaterPipeBlock {

    public static final BooleanProperty OPEN = BlockStateProperties.OPEN;

    public IrrigationValveBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(OPEN, true));
    }

    @Override
    protected void addExtraProperties(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(OPEN);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return super.getStateForPlacement(ctx).setValue(OPEN, true);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        boolean open = !state.getValue(OPEN);
        if (!level.isClientSide) {
            level.setBlock(pos, state.setValue(OPEN, open), 3);
            level.playSound(null, pos, SoundEvents.LEVER_CLICK, SoundSource.BLOCKS, 0.5f, open ? 0.9f : 0.6f);
            player.displayClientMessage(Component.translatable(open
                    ? "block.craftmorkovsk.irrigation_valve.open"
                    : "block.craftmorkovsk.irrigation_valve.closed"), true);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
