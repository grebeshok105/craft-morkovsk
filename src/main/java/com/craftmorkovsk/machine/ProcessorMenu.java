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

/** One input slot + one output slot around the progress arrow. Shared by the mill,
 *  oil press, dryer, fermenter, seed extractor and packing station. */
public class ProcessorMenu extends MachineMenu {

    public ProcessorMenu(@Nullable MenuType<?> type, int windowId, Inventory playerInventory,
                         ItemStackHandler items, ContainerData data, ContainerLevelAccess access) {
        super(type, windowId, items, data, access);
        finishSlots(playerInventory);
    }

    /** Client-side ctor: reads the BlockPos NetworkHooks.openScreen wrote to the buffer. */
    public ProcessorMenu(@Nullable MenuType<?> type, int windowId, Inventory playerInventory,
                         FriendlyByteBuf buf) {
        this(type, windowId, playerInventory, buf.readBlockPos());
    }

    private ProcessorMenu(MenuType<?> type, int windowId, Inventory inv, BlockPos pos) {
        this(type, windowId, inv, machineAt(inv, pos));
    }

    private ProcessorMenu(MenuType<?> type, int windowId, Inventory inv, @Nullable MachineBlockEntity be) {
        this(type, windowId, inv,
                be != null ? be.getItems() : new ItemStackHandler(2),
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
        slot(0, 48, 35);  // input
        slot(1, 116, 35); // output
    }
}
