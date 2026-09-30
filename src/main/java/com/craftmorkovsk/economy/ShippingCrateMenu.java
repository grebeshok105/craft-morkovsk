package com.craftmorkovsk.economy;

import com.craftmorkovsk.machine.framework.MachineBlockEntity;
import com.craftmorkovsk.machine.framework.MachineMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.ItemStackHandler;

/** 27 sell slots in a 9x3 grid + player inventory. */
public class ShippingCrateMenu extends MachineMenu {

    public ShippingCrateMenu(MenuType<?> type, int windowId, Inventory playerInventory,
                             ItemStackHandler items, ContainerData data, ContainerLevelAccess access) {
        super(type, windowId, items, data, access);
        finishSlots(playerInventory);
    }

    /** Client-side ctor: reads the BlockPos NetworkHooks.openScreen wrote to the buffer. */
    public ShippingCrateMenu(MenuType<?> type, int windowId, Inventory playerInventory,
                             FriendlyByteBuf buf) {
        this(type, windowId, playerInventory, buf.readBlockPos());
    }

    private ShippingCrateMenu(MenuType<?> type, int windowId, Inventory inv, BlockPos pos) {
        this(type, windowId, inv, crateAt(inv, pos));
    }

    private ShippingCrateMenu(MenuType<?> type, int windowId, Inventory inv, MachineBlockEntity be) {
        this(type, windowId, inv,
                be != null ? be.getItems() : new ItemStackHandler(27),
                be != null ? be.getContainerData()
                        : new SimpleContainerData(MachineBlockEntity.DATA_COUNT),
                be != null ? ContainerLevelAccess.create(inv.player.level(), be.getBlockPos())
                        : ContainerLevelAccess.NULL);
    }

    private static MachineBlockEntity crateAt(Inventory inv, BlockPos pos) {
        BlockEntity be = inv.player.level().getBlockEntity(pos);
        return be instanceof MachineBlockEntity machine ? machine : null;
    }

    /** Client accessor for the sell slots (used by the screen for the estimate). */
    public ItemStackHandler handler() { return items; }

    @Override
    protected void addMachineSlots() {
        for (int row = 0; row < 3; row++)
            for (int col = 0; col < 9; col++)
                slot(row * 9 + col, 8 + col * 18, 18 + row * 18);
    }
}
