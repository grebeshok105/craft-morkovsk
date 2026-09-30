package com.craftmorkovsk.energy;

import com.craftmorkovsk.machine.framework.MachineBlockEntity;
import com.craftmorkovsk.machine.framework.MachineMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.ItemStackHandler;

/** One fuel slot + energy bar. Shared by the generator and biomass generator. */
public class FuelMachineMenu extends MachineMenu {

    public FuelMachineMenu(MenuType<?> type, int windowId, Inventory playerInventory,
                           ItemStackHandler items, ContainerData data, ContainerLevelAccess access) {
        super(type, windowId, items, data, access);
        finishSlots(playerInventory);
    }

    /** Client-side ctor: reads the BlockPos NetworkHooks.openScreen wrote to the buffer. */
    public FuelMachineMenu(MenuType<?> type, int windowId, Inventory playerInventory,
                           FriendlyByteBuf buf) {
        this(type, windowId, playerInventory, buf.readBlockPos());
    }

    private FuelMachineMenu(MenuType<?> type, int windowId, Inventory inv, BlockPos pos) {
        this(type, windowId, inv, machineAt(inv, pos));
    }

    private FuelMachineMenu(MenuType<?> type, int windowId, Inventory inv, MachineBlockEntity be) {
        this(type, windowId, inv,
                be != null ? be.getItems() : new ItemStackHandler(1),
                be != null ? be.getContainerData()
                        : new net.minecraft.world.inventory.SimpleContainerData(MachineBlockEntity.DATA_COUNT),
                be != null ? ContainerLevelAccess.create(inv.player.level(), be.getBlockPos())
                        : ContainerLevelAccess.NULL);
    }

    private static MachineBlockEntity machineAt(Inventory inv, BlockPos pos) {
        BlockEntity be = inv.player.level().getBlockEntity(pos);
        return be instanceof MachineBlockEntity machine ? machine : null;
    }

    @Override
    protected void addMachineSlots() {
        slot(0, 56, 36); // fuel input
    }
}
