package com.craftmorkovsk.storage;

import com.craftmorkovsk.machine.framework.AbstractMachineBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

/** Fridge block: FACING/WORKING visuals, ticker and open-on-use come from the
 *  machine framework; WORKING mirrors the powered flag. */
public class RefrigeratedChestBlock extends AbstractMachineBlock {

    private final Supplier<BlockEntityType<? extends RefrigeratedChestBlockEntity>> beType;

    public RefrigeratedChestBlock(Properties properties,
                                  Supplier<BlockEntityType<? extends RefrigeratedChestBlockEntity>> beType) {
        super(properties);
        this.beType = beType;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return beType.get().create(pos, state);
    }
}
