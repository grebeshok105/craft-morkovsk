package com.craftmorkovsk.machine;

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
import org.jetbrains.annotations.Nullable;

/** The auto harvester's 3x3 drop buffer. */
public class HarvesterMenu extends MachineMenu {

    public HarvesterMenu(@Nullable MenuType<?> type, int windowId, Inventory playerInventory,
                         ItemStackHandler items, ContainerData data, ContainerLevelAccess access) {
        super(type, windowId, items, data, access);
        finishSlots(playerInventory);
    }

    public HarvesterMenu(@Nullable MenuType<?> type, int windowId, Inventory playerInventory,
                         FriendlyByteBuf buf) {
        this(type, windowId, playerInventory, buf.readBlockPos());
    }

    private HarvesterMenu(MenuType<?> type, int windowId, Inventory inv, BlockPos pos) {
        this(type, windowId, inv, machineAt(inv, pos));
    }

    private HarvesterMenu(MenuType<?> type, int windowId, Inventory inv, @Nullable MachineBlockEntity be) {
        this(type, windowId, inv,
                be != null ? be.getItems() : new ItemStackHandler(AutoHarvesterBlockEntity.BUFFER_SLOTS),
                be != null ? be.getContainerData()
                        : new SimpleContainerData(MachineBlockEntity.DATA_COUNT),
                be != null ? ContainerLevelAccess.create(inv.player.level(), be.getBlockPos())
                        : ContainerLevelAccess.NULL);
    }

    private static MachineBlockEntity machineAt(Inventory inv, BlockPos pos) {
        BlockEntity be = inv.player.level().getBlockEntity(pos);
        return be instanceof MachineBlockEntity machine ? machine : null;
    }

    @Override
    protected void addMachineSlots() {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                slot(row * 3 + col, 104 + col * 18, 17 + row * 18);
            }
        }
    }
}
