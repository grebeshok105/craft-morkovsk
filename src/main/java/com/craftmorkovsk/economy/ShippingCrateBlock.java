package com.craftmorkovsk.economy;

import com.craftmorkovsk.machine.framework.AbstractMachineBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

/** Shipping crate: players dump produce in; the whole contents are sold periodically. */
public class ShippingCrateBlock extends AbstractMachineBlock {

    private final Supplier<BlockEntityType<? extends ShippingCrateBlockEntity>> beType;

    public ShippingCrateBlock(Properties properties,
                              Supplier<BlockEntityType<? extends ShippingCrateBlockEntity>> beType) {
        super(properties);
        this.beType = beType;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return beType.get().create(pos, state);
    }
}
