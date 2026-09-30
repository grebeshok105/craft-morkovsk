package com.craftmorkovsk.machine;

import com.craftmorkovsk.energy.MorkovskEnergyStorage;
import com.craftmorkovsk.item.QualityProduceItem;
import com.craftmorkovsk.machine.framework.MachineBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.ItemStackHandler;

/** Packing station: compresses 9 identical produce items into a {@link ProduceCrateItem}
 *  that remembers what was packed (item id, count, quality). */
public class PackingStationBlockEntity extends MachineBlockEntity {

    public static final int SLOT_INPUT = 0;
    public static final int SLOT_OUTPUT = 1;
    public static final int PACK_COUNT = 9;
    public static final int PROCESS_TICKS = 100;

    public PackingStationBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

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
    protected int getDefaultProcessTime() { return PROCESS_TICKS; }

    private boolean isPackable(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof QualityProduceItem
                && stack.getCount() >= PACK_COUNT;
    }

    private ItemStack makeCrate(ItemStack produce) {
        ItemStack crate = new ItemStack(MachineModule.PRODUCE_CRATE.get());
        ProduceCrateItem.pack(crate, produce, PACK_COUNT);
        return crate;
    }

    @Override
    protected void tickServer(Level level, BlockPos pos, BlockState state) {
        MachineEnergy.draw(level, pos, energy);
        ItemStack input = items.getStackInSlot(SLOT_INPUT);
        if (!isPackable(input)) {
            if (progress != 0) { progress = 0; setChanged(); }
            return;
        }
        ItemStack crate = makeCrate(input);
        if (!canFitOutput(crate)) {
            if (progress != 0) { progress = 0; setChanged(); }
            return;
        }
        maxProgress = PROCESS_TICKS;
        if (energy.getEnergyStored() < getEnergyPerTick()) return;
        energy.consumeInternal(getEnergyPerTick());
        progress++;
        if (progress >= PROCESS_TICKS) {
            items.extractItem(SLOT_INPUT, PACK_COUNT, false);
            items.insertItem(SLOT_OUTPUT, crate, false);
            progress = 0;
        }
        setChanged();
    }

    private boolean canFitOutput(ItemStack output) {
        ItemStack existing = items.getStackInSlot(SLOT_OUTPUT);
        if (existing.isEmpty()) return true;
        return ItemStack.isSameItemSameTags(existing, output)
                && existing.getCount() + output.getCount() <= existing.getMaxStackSize();
    }

    @Override
    public AbstractContainerMenu createMenu(int windowId, Inventory playerInventory, Player player) {
        return new ProcessorMenu(MachineModule.PACKING_STATION_MENU.get(), windowId, playerInventory,
                items, data, ContainerLevelAccess.create(level, worldPosition));
    }
}
