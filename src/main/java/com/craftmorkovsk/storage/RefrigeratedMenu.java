package com.craftmorkovsk.storage;

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

/** Fridge menu: 27-slot storage grid plus energy/progress sync via the machine
 *  framework. The grid is shifted right ({@link #GRID_X}) so the screen's left
 *  energy bar stays clear of the slot cells; the screen is wider accordingly. */
public class RefrigeratedMenu extends MachineMenu {

    public static final int GRID_X = 26;

    public RefrigeratedMenu(MenuType<?> type, int windowId, Inventory inv,
                            ItemStackHandler items, ContainerData data, ContainerLevelAccess access) {
        super(type, windowId, items, data, access);
        addMachineSlots();
        addPlayerSlots(inv, GRID_X, 84);
    }

    /** Client-side ctor: reads the BlockPos NetworkHooks.openScreen wrote to the buffer. */
    public RefrigeratedMenu(MenuType<?> type, int windowId, Inventory inv, FriendlyByteBuf buf) {
        this(type, windowId, inv, buf.readBlockPos());
    }

    private RefrigeratedMenu(MenuType<?> type, int windowId, Inventory inv, BlockPos pos) {
        this(type, windowId, inv, fridgeAt(inv, pos));
    }

    private RefrigeratedMenu(MenuType<?> type, int windowId, Inventory inv, RefrigeratedChestBlockEntity be) {
        this(type, windowId, inv,
                be != null ? be.getItems() : new ItemStackHandler(27),
                be != null ? be.getContainerData() : new SimpleContainerData(MachineBlockEntity.DATA_COUNT),
                be != null ? ContainerLevelAccess.create(inv.player.level(), be.getBlockPos())
                        : ContainerLevelAccess.NULL);
    }

    private static RefrigeratedChestBlockEntity fridgeAt(Inventory inv, BlockPos pos) {
        BlockEntity be = inv.player.level().getBlockEntity(pos);
        return be instanceof RefrigeratedChestBlockEntity fridge ? fridge : null;
    }

    @Override
    protected void addMachineSlots() {
        for (int row = 0; row < 3; row++)
            for (int col = 0; col < 9; col++)
                slot(row * 9 + col, GRID_X + col * 18, 18 + row * 18);
    }

    /** Powered flag synced from the block entity (rides the progress data slot). */
    public boolean powered() {
        return progress() > 0;
    }
}
