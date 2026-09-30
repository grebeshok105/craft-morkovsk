package com.craftmorkovsk.machine;

import com.craftmorkovsk.energy.MorkovskEnergyStorage;
import com.craftmorkovsk.machine.framework.ProcessingMachineBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.ItemStackHandler;

import java.util.function.Supplier;

/** Recipe-driven processor (input slot 0 -> output slot 1). Shared by the mill,
 *  oil press, dryer and fermenter; {@code machineId} binds the JSON recipe field. */
public class ProcessorBlockEntity extends ProcessingMachineBlockEntity {

    private final String machineId;
    private final Supplier<net.minecraft.world.inventory.MenuType<?>> menuType;

    public ProcessorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state,
                                String machineId, Supplier<net.minecraft.world.inventory.MenuType<?>> menuType) {
        super(type, pos, state);
        this.machineId = machineId;
        this.menuType = menuType;
    }

    @Override
    protected String machineId() { return machineId; }

    @Override
    protected ItemStackHandler createItemHandler() {
        return new ItemStackHandler(2) {
            @Override
            protected void onContentsChanged(int slot) { setChanged(); }
        };
    }

    @Override
    protected MorkovskEnergyStorage createEnergyStorage() {
        return MorkovskEnergyStorage.machine(4000, 500, this::setChanged);
    }

    @Override
    protected int getDefaultProcessTime() { return 200; }

    @Override
    protected void tickServer(Level level, BlockPos pos, BlockState state) {
        MachineEnergy.draw(level, pos, energy);
        super.tickServer(level, pos, state);
    }

    @Override
    public AbstractContainerMenu createMenu(int windowId, Inventory playerInventory, Player player) {
        return new ProcessorMenu(menuType.get(), windowId, playerInventory,
                items, data, ContainerLevelAccess.create(level, worldPosition));
    }
}
