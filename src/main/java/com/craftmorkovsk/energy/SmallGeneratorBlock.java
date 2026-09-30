package com.craftmorkovsk.energy;

import com.craftmorkovsk.machine.framework.AbstractMachineBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class SmallGeneratorBlock extends AbstractMachineBlock {

    private final java.util.function.Supplier<BlockEntityType<? extends GeneratorBlockEntity>> beType;

    public SmallGeneratorBlock(Properties properties,
                               java.util.function.Supplier<BlockEntityType<? extends GeneratorBlockEntity>> beType) {
        super(properties);
        this.beType = beType;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return beType.get().create(pos, state);
    }
}
