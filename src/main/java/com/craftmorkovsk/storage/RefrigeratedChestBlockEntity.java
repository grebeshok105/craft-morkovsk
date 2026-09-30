package com.craftmorkovsk.storage;

import com.craftmorkovsk.energy.EnergyNetwork;
import com.craftmorkovsk.energy.MorkovskEnergyStorage;
import com.craftmorkovsk.machine.framework.MachineBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.ItemStackHandler;

/** Refrigerated chest: a 27-slot food store that runs on Forge Energy.
 *  Burns {@value #FE_PER_TICK} FE/tick from its buffer; the buffer is refilled from the
 *  cable network via {@link EnergyNetwork#pull} (and from anything pushing into its
 *  exposed ENERGY capability). While powered, produce extracted from it gains one
 *  quality tier — see {@link ChilledItemHandler}. */
public class RefrigeratedChestBlockEntity extends MachineBlockEntity {

    public static final int FE_PER_TICK = 5;
    private static final int ENERGY_CAPACITY = 4000;
    private static final int MAX_PULL_PER_TICK = 40;

    private boolean powered;

    public RefrigeratedChestBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    protected ItemStackHandler createItemHandler() {
        return new ChilledItemHandler(27, this::isPowered, this::setChanged);
    }

    @Override
    protected MorkovskEnergyStorage createEnergyStorage() {
        return MorkovskEnergyStorage.machine(ENERGY_CAPACITY, 200, this::setChanged);
    }

    @Override
    protected int getDefaultProcessTime() {
        return 0; // no processing arrow — "powered" rides the progress data slot instead
    }

    @Override
    protected void tickServer(Level level, BlockPos pos, BlockState state) {
        int room = energy.getMaxEnergyStored() - energy.getEnergyStored();
        if (room > 0) {
            energy.generateInternal(EnergyNetwork.pull(level, pos, Math.min(MAX_PULL_PER_TICK, room), pos));
        }
        powered = energy.consumeInternal(FE_PER_TICK) >= FE_PER_TICK;
        progress = powered ? 1 : 0; // synced to the menu for the screen's status readout
        setChanged();
    }

    /** True while the fridge is fed — gates the chilled-extraction upgrade. */
    public boolean isPowered() {
        return powered;
    }

    @Override
    public boolean isWorking() {
        return powered;
    }

    @Override
    public AbstractContainerMenu createMenu(int windowId, Inventory playerInventory, Player player) {
        return new RefrigeratedMenu(StorageModule.REFRIGERATED_MENU.get(), windowId, playerInventory,
                items, data, ContainerLevelAccess.create(level, worldPosition));
    }
}
