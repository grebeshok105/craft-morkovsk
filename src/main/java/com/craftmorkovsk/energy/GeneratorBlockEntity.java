package com.craftmorkovsk.energy;

import com.craftmorkovsk.config.MorkovskConfig;
import com.craftmorkovsk.machine.framework.MachineBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.items.ItemStackHandler;

/** Small generator: burns solid fuel, pushes FE into the surrounding cable network. */
public class GeneratorBlockEntity extends MachineBlockEntity {

    private int burnTime;
    private int burnTimeTotal = 1;

    public GeneratorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    protected ItemStackHandler createItemHandler() {
        return new ItemStackHandler(1) {
            @Override
            protected void onContentsChanged(int slot) { setChanged(); }
        };
    }

    @Override
    protected MorkovskEnergyStorage createEnergyStorage() {
        return new MorkovskEnergyStorage(10000, 0, 500, this::setChanged);
    }

    @Override
    protected int getDefaultProcessTime() { return 1600; }

    protected int fePerTick() { return MorkovskConfig.GENERATOR_FE_PER_TICK.get(); }

    /** Subclasses (biomass) restrict what counts as fuel. */
    protected boolean isFuel(ItemStack stack) {
        return ForgeHooks.getBurnTime(stack, RecipeType.SMELTING) > 0;
    }

    protected int burnTimeOf(ItemStack stack) {
        return ForgeHooks.getBurnTime(stack, RecipeType.SMELTING);
    }

    @Override
    protected void tickServer(Level level, BlockPos pos, BlockState state) {
        boolean dirty = false;
        if (burnTime <= 0) {
            ItemStack fuel = items.getStackInSlot(0);
            if (isFuel(fuel) && energy.getEnergyStored() < energy.getMaxEnergyStored()) {
                burnTimeTotal = Math.max(1, burnTimeOf(fuel));
                burnTime = burnTimeTotal;
                items.extractItem(0, 1, false);
                dirty = true;
            }
        }
        if (burnTime > 0) {
            burnTime--;
            energy.generateInternal(fePerTick());
            int want = Math.min(energy.getEnergyStored(), fePerTick() * 2);
            int leftover = EnergyNetwork.push(level, pos, want, pos);
            energy.extractEnergy(want - leftover, false);
            dirty = true;
        }
        // GUI: progress bar = fuel remaining
        progress = burnTime;
        maxProgress = burnTimeTotal;
        if (dirty) setChanged();
    }

    @Override
    protected void tickClient(Level level, BlockPos pos, BlockState state) {
        if (burnTime > 0 && level.random.nextInt(24) == 0) {
            level.playLocalSound(pos, SoundEvents.FURNACE_FIRE_CRACKLE,
                    SoundSource.BLOCKS, 0.4f, 1.0f, false);
        }
    }

    @Override
    public boolean isWorking() { return burnTime > 0; }

    @Override
    public AbstractContainerMenu createMenu(int windowId, Inventory playerInventory, Player player) {
        return new FuelMachineMenu(EnergyModule.GENERATOR_MENU.get(), windowId, playerInventory,
                items, data, ContainerLevelAccess.create(level, worldPosition));
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("BurnTime", burnTime);
        tag.putInt("BurnTimeTotal", burnTimeTotal);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        burnTime = tag.getInt("BurnTime");
        burnTimeTotal = tag.getInt("BurnTimeTotal");
    }
}
