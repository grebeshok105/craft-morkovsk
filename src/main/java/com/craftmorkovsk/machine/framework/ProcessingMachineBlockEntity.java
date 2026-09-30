package com.craftmorkovsk.machine.framework;

import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/** Recipe-driven machine: one input slot (0), one output slot (1).
 *  Consumes {@link ProcessingRecipe}s whose {@code machine} field equals {@link #machineId()}. */
public abstract class ProcessingMachineBlockEntity extends MachineBlockEntity {

    private ProcessingRecipe activeRecipe;

    protected ProcessingMachineBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    /** The {@code "machine"} value this block entity processes, e.g. "grain_mill". */
    protected abstract String machineId();

    @Override
    protected void tickServer(Level level, BlockPos pos, BlockState state) {
        if (activeRecipe == null) {
            activeRecipe = findRecipe(level, items.getStackInSlot(0));
            progress = 0;
            if (activeRecipe == null) return;
        }
        maxProgress = activeRecipe.time();
        int cost = activeRecipe.energy();
        if (energy.getEnergyStored() < cost) return;
        if (!canFitOutput(activeRecipe.output())) {
            progress = 0;
            return;
        }
        energy.consumeInternal(cost);
        progress++;
        if (progress >= activeRecipe.time()) {
            items.extractItem(0, 1, false);
            items.insertItem(1, activeRecipe.output().copy(), false);
            progress = 0;
            activeRecipe = null;
            onCrafted(level, pos);
        }
        setChanged();
    }

    /** XP/xp-award hook for subclasses; default no-op. */
    protected void onCrafted(Level level, BlockPos pos) { }

    private boolean canFitOutput(ItemStack output) {
        ItemStack existing = items.getStackInSlot(1);
        if (existing.isEmpty()) return true;
        return ItemStack.isSameItemSameTags(existing, output)
                && existing.getCount() + output.getCount() <= existing.getMaxStackSize();
    }

    protected ProcessingRecipe findRecipe(Level level, ItemStack input) {
        if (input.isEmpty()) return null;
        RecipeManager manager = level.getRecipeManager();
        Container container = new SimpleContainer(input.copy());
        for (ProcessingRecipe r : manager.getAllRecipesFor(ProcessingRecipe.Type.INSTANCE)) {
            if (machineId().equals(r.machine()) && r.matches(container, level)) return r;
        }
        return null;
    }
}
