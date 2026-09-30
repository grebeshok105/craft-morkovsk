package com.craftmorkovsk.machine;

import com.craftmorkovsk.crop.CropCatalog;
import com.craftmorkovsk.crop.CropDef;
import com.craftmorkovsk.energy.MorkovskEnergyStorage;
import com.craftmorkovsk.machine.framework.MachineBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.registries.ForgeRegistries;

/** Seed extractor: one produce item -> 2x that crop's seeds. The produce -> seed mapping is
 *  looked up in {@link CropCatalog#BY_ID} at runtime, so no recipe JSON is needed. */
public class SeedExtractorBlockEntity extends MachineBlockEntity {

    public static final int SLOT_INPUT = 0;
    public static final int SLOT_OUTPUT = 1;
    public static final int PROCESS_TICKS = 160;
    public static final int SEEDS_PER_PRODUCE = 2;

    public SeedExtractorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
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

    /** Resolves the crop definition for a produce stack, or null when it isn't produce. */
    private CropDef defFor(ItemStack stack) {
        if (stack.isEmpty()) return null;
        ResourceLocation key = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (key == null || !"craftmorkovsk".equals(key.getNamespace())) return null;
        CropDef def = CropCatalog.BY_ID.get(key.getPath());
        return def != null && def.seeds != null ? def : null;
    }

    @Override
    protected void tickServer(Level level, BlockPos pos, BlockState state) {
        MachineEnergy.draw(level, pos, energy);
        CropDef def = defFor(items.getStackInSlot(SLOT_INPUT));
        if (def == null) {
            if (progress != 0) { progress = 0; setChanged(); }
            return;
        }
        ItemStack seeds = new ItemStack(def.seeds.get(), SEEDS_PER_PRODUCE);
        if (!canFitOutput(seeds)) {
            if (progress != 0) { progress = 0; setChanged(); }
            return;
        }
        maxProgress = PROCESS_TICKS;
        if (energy.getEnergyStored() < getEnergyPerTick()) return;
        energy.consumeInternal(getEnergyPerTick());
        progress++;
        if (progress >= PROCESS_TICKS) {
            items.extractItem(SLOT_INPUT, 1, false);
            items.insertItem(SLOT_OUTPUT, seeds, false);
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
        return new ProcessorMenu(MachineModule.SEED_EXTRACTOR_MENU.get(), windowId, playerInventory,
                items, data, ContainerLevelAccess.create(level, worldPosition));
    }
}
