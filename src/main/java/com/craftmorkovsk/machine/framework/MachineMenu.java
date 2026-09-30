package com.craftmorkovsk.machine.framework;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.Nullable;

/** Base machine menu: machine slots + player inventory + ContainerData (energy/progress) sync.
 *  Subclasses pass the machine's ItemStackHandler and add their slots in
 *  {@link #addMachineSlots()}; call {@link #finishSlots(Inventory)} at the end of their ctor. */
public abstract class MachineMenu extends AbstractContainerMenu {

    protected final ItemStackHandler items;
    protected final ContainerData data;
    protected final ContainerLevelAccess access;
    private final int machineSlotCount;

    protected MachineMenu(@Nullable MenuType<?> type, int windowId,
                          ItemStackHandler items, ContainerData data, @Nullable ContainerLevelAccess access) {
        super(type, windowId);
        this.items = items;
        this.data = data;
        this.access = access == null ? ContainerLevelAccess.NULL : access;
        this.machineSlotCount = items.getSlots();
        addDataSlots(data);
    }

    /** Add the machine-specific slots via {@link #slot(int, int, int)}. */
    protected abstract void addMachineSlots();

    protected Slot slot(int index, int x, int y) {
        return addSlot(new SlotItemHandler(items, index, x, y));
    }

    /** Call at the end of the subclass constructor: adds machine + player slots. */
    protected void finishSlots(Inventory playerInventory) {
        addMachineSlots();
        addPlayerSlots(playerInventory, 8, 84);
    }

    protected void addPlayerSlots(Inventory playerInventory, int x, int y) {
        for (int row = 0; row < 3; row++)
            for (int col = 0; col < 9; col++)
                addSlot(new Slot(playerInventory, col + row * 9 + 9, x + col * 18, y + row * 18));
        for (int col = 0; col < 9; col++)
            addSlot(new Slot(playerInventory, col, x + col * 18, y + 58));
    }

    public int energy() { return data.get(MachineBlockEntity.DATA_ENERGY); }
    public int maxEnergy() { return data.get(MachineBlockEntity.DATA_MAX_ENERGY); }
    public int progress() { return data.get(MachineBlockEntity.DATA_PROGRESS); }
    public int maxProgress() { return data.get(MachineBlockEntity.DATA_MAX_PROGRESS); }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return result;
        ItemStack inSlot = slot.getItem();
        result = inSlot.copy();
        if (index < machineSlotCount) {
            if (!moveItemStackTo(inSlot, machineSlotCount, slots.size(), true)) return ItemStack.EMPTY;
        } else {
            if (!moveItemStackTo(inSlot, 0, machineSlotCount, false)) return ItemStack.EMPTY;
        }
        if (inSlot.isEmpty()) slot.set(ItemStack.EMPTY);
        else slot.setChanged();
        return result;
    }

    @Override
    public boolean stillValid(Player player) {
        return access.evaluate((level, pos) -> player.distanceToSqr(
                pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= 64.0, true);
    }
}
