package com.craftmorkovsk.machine;

import com.craftmorkovsk.energy.MorkovskEnergyStorage;
import com.craftmorkovsk.machine.framework.MachineBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.registries.ForgeRegistries;

/** Cream separator: milk bucket -> cream + butter + the empty bucket back.
 *  Cream and butter items are registered by the food package; they are resolved from the
 *  item registry at runtime so this machine simply idles while they are absent. */
public class SeparatorBlockEntity extends MachineBlockEntity {

    public static final int SLOT_INPUT = 0;
    public static final int SLOT_CREAM = 1;
    public static final int SLOT_BUTTER = 2;
    public static final int SLOT_BUCKET = 3;
    public static final int PROCESS_TICKS = 200;

    private static final ResourceLocation CREAM_ID = new ResourceLocation("craftmorkovsk", "cream");
    private static final ResourceLocation BUTTER_ID = new ResourceLocation("craftmorkovsk", "butter");

    public SeparatorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    protected ItemStackHandler createItemHandler() {
        return new ItemStackHandler(4) {
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

    private Item cream() { return ForgeRegistries.ITEMS.getValue(CREAM_ID); }
    private Item butter() { return ForgeRegistries.ITEMS.getValue(BUTTER_ID); }

    private boolean outputsReady() {
        if (!ForgeRegistries.ITEMS.containsKey(CREAM_ID)
                || !ForgeRegistries.ITEMS.containsKey(BUTTER_ID)) return false;
        return canFit(SLOT_CREAM, new ItemStack(cream()))
                && canFit(SLOT_BUTTER, new ItemStack(butter()))
                && canFit(SLOT_BUCKET, new ItemStack(Items.BUCKET));
    }

    private boolean canFit(int slot, ItemStack expected) {
        ItemStack existing = items.getStackInSlot(slot);
        if (existing.isEmpty()) return true;
        return ItemStack.isSameItemSameTags(existing, expected)
                && existing.getCount() + expected.getCount() <= existing.getMaxStackSize();
    }

    @Override
    protected void tickServer(Level level, BlockPos pos, BlockState state) {
        MachineEnergy.draw(level, pos, energy);
        ItemStack input = items.getStackInSlot(SLOT_INPUT);
        if (!input.is(Items.MILK_BUCKET) || !outputsReady()) {
            if (progress != 0) { progress = 0; setChanged(); }
            return;
        }
        maxProgress = PROCESS_TICKS;
        if (energy.getEnergyStored() < getEnergyPerTick()) return;
        energy.consumeInternal(getEnergyPerTick());
        progress++;
        if (progress >= PROCESS_TICKS) {
            items.extractItem(SLOT_INPUT, 1, false);
            items.insertItem(SLOT_CREAM, new ItemStack(cream()), false);
            items.insertItem(SLOT_BUTTER, new ItemStack(butter()), false);
            items.insertItem(SLOT_BUCKET, new ItemStack(Items.BUCKET), false);
            progress = 0;
        }
        setChanged();
    }

    @Override
    public AbstractContainerMenu createMenu(int windowId, Inventory playerInventory, Player player) {
        return new SeparatorMenu(MachineModule.SEPARATOR_MENU.get(), windowId, playerInventory,
                items, data, ContainerLevelAccess.create(level, worldPosition));
    }
}
