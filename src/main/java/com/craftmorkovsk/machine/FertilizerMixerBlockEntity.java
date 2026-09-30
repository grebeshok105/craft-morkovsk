package com.craftmorkovsk.machine;

import com.craftmorkovsk.energy.MorkovskEnergyStorage;
import com.craftmorkovsk.fertilizer.FertilizerModule;
import com.craftmorkovsk.machine.framework.MachineBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.ItemStackHandler;

import java.util.List;
import java.util.function.Supplier;

/** Fertilizer mixer: blends two fertilizer inputs into an upgraded product.
 *  Two consumed inputs can't be expressed by the 1-in/1-out ProcessingRecipe JSON type,
 *  so the mix table lives here:
 *    compost + mineral_fertilizer  -> morkovsk_fertilizer
 *    manure_fertilizer + compost   -> compost x2 */
public class FertilizerMixerBlockEntity extends MachineBlockEntity {

    public static final int SLOT_A = 0;
    public static final int SLOT_B = 1;
    public static final int SLOT_OUT = 2;
    public static final int PROCESS_TICKS = 160;

    private record Mix(Supplier<Item> a, Supplier<Item> b, Supplier<ItemStack> result) {
        boolean matches(Item i, Item j) {
            return (a.get() == i && b.get() == j) || (a.get() == j && b.get() == i);
        }
    }

    private static final List<Mix> MIXES = List.of(
            new Mix(() -> FertilizerModule.COMPOST.get(), () -> FertilizerModule.MINERAL_FERTILIZER.get(),
                    () -> new ItemStack(FertilizerModule.MORKOVSK_FERTILIZER.get())),
            new Mix(() -> FertilizerModule.MANURE_FERTILIZER.get(), () -> FertilizerModule.COMPOST.get(),
                    () -> new ItemStack(FertilizerModule.COMPOST.get(), 2)));

    public FertilizerMixerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    protected ItemStackHandler createItemHandler() {
        return new ItemStackHandler(3) {
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

    private Mix activeMix() {
        Item a = items.getStackInSlot(SLOT_A).getItem();
        Item b = items.getStackInSlot(SLOT_B).getItem();
        if (a == net.minecraft.world.item.Items.AIR || b == net.minecraft.world.item.Items.AIR) return null;
        for (Mix mix : MIXES) if (mix.matches(a, b)) return mix;
        return null;
    }

    @Override
    protected void tickServer(Level level, BlockPos pos, BlockState state) {
        MachineEnergy.draw(level, pos, energy);
        Mix mix = activeMix();
        if (mix == null) {
            if (progress != 0) { progress = 0; setChanged(); }
            return;
        }
        ItemStack result = mix.result().get();
        if (!canFitOutput(result)) {
            if (progress != 0) { progress = 0; setChanged(); }
            return;
        }
        maxProgress = PROCESS_TICKS;
        if (energy.getEnergyStored() < getEnergyPerTick()) return;
        energy.consumeInternal(getEnergyPerTick());
        progress++;
        if (progress >= PROCESS_TICKS) {
            items.extractItem(SLOT_A, 1, false);
            items.extractItem(SLOT_B, 1, false);
            items.insertItem(SLOT_OUT, result, false);
            progress = 0;
        }
        setChanged();
    }

    private boolean canFitOutput(ItemStack output) {
        ItemStack existing = items.getStackInSlot(SLOT_OUT);
        if (existing.isEmpty()) return true;
        return ItemStack.isSameItemSameTags(existing, output)
                && existing.getCount() + output.getCount() <= existing.getMaxStackSize();
    }

    @Override
    public AbstractContainerMenu createMenu(int windowId, Inventory playerInventory, Player player) {
        return new MixerMenu(MachineModule.FERTILIZER_MIXER_MENU.get(), windowId, playerInventory,
                items, data, ContainerLevelAccess.create(level, worldPosition));
    }
}
