package com.craftmorkovsk.energy;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.capabilities.ForgeCapabilities;

import java.util.EnumMap;
import java.util.Map;

/** Energy cable. Connects to other cables and to any block exposing the FE capability. */
public class CableBlock extends Block implements SimpleWaterloggedBlock {

    public static final Map<Direction, BooleanProperty> CONNECTIONS = new EnumMap<>(Direction.class);
    static {
        CONNECTIONS.put(Direction.NORTH, BlockStateProperties.NORTH);
        CONNECTIONS.put(Direction.EAST, BlockStateProperties.EAST);
        CONNECTIONS.put(Direction.SOUTH, BlockStateProperties.SOUTH);
        CONNECTIONS.put(Direction.WEST, BlockStateProperties.WEST);
        CONNECTIONS.put(Direction.UP, BlockStateProperties.UP);
        CONNECTIONS.put(Direction.DOWN, BlockStateProperties.DOWN);
    }
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    public CableBlock(Properties properties) {
        super(properties);
        BlockState state = defaultBlockState().setValue(WATERLOGGED, false);
        for (BooleanProperty p : CONNECTIONS.values()) state = state.setValue(p, false);
        registerDefaultState(state);
    }

    public static boolean canConnectTo(LevelAccessor level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof CableBlock) return true;
        return level.getBlockEntity(pos) != null
                && level.getBlockEntity(pos).getCapability(ForgeCapabilities.ENERGY).isPresent();
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return withConnections(super.getStateForPlacement(ctx), ctx.getLevel(), ctx.getClickedPos());
    }

    private BlockState withConnections(BlockState state, LevelAccessor level, BlockPos pos) {
        state = state.setValue(WATERLOGGED, level.getFluidState(pos).getType() == Fluids.WATER);
        for (Map.Entry<Direction, BooleanProperty> e : CONNECTIONS.entrySet()) {
            state = state.setValue(e.getValue(), canConnectTo(level, pos.relative(e.getKey())));
        }
        return state;
    }

    @Override
    public BlockState updateShape(BlockState state, Direction dir, BlockState neighbor,
                                  LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (state.getValue(WATERLOGGED)) level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        return state.setValue(CONNECTIONS.get(dir), canConnectTo(level, neighborPos));
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(WATERLOGGED);
        CONNECTIONS.values().forEach(builder::add);
    }
}
