package com.craftmorkovsk.irrigation;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/** Water pump block. Pipes attach on any side; power arrives through energy cables. */
public class WaterPumpBlock extends Block implements EntityBlock {

    private final java.util.function.Supplier<BlockEntityType<? extends WaterPumpBlockEntity>> beType;

    public WaterPumpBlock(Properties properties,
                          java.util.function.Supplier<BlockEntityType<? extends WaterPumpBlockEntity>> beType) {
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
            if (be instanceof WaterPumpBlockEntity pump) WaterPumpBlockEntity.tick(l, pos, s, pump);
        };
    }
}
