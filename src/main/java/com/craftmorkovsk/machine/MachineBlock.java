package com.craftmorkovsk.machine;

import com.craftmorkovsk.machine.framework.AbstractMachineBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

/** Generic powered machine block: facing + working state + menu handled by the framework;
 *  the concrete BlockEntity comes from the supplied BlockEntityType. */
public class MachineBlock extends AbstractMachineBlock {

    private final Supplier<BlockEntityType<?>> beType;

    public MachineBlock(Properties properties, Supplier<BlockEntityType<?>> beType) {
        super(properties);
        this.beType = beType;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return beType.get().create(pos, state);
    }
}
