package com.craftmorkovsk.energy;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** Battery: buffers FE. Generators charge it through the network; it discharges back in. */
public class BatteryBlockEntity extends BlockEntity {

    private final MorkovskEnergyStorage energy =
            new MorkovskEnergyStorage(50000, 500, 500, this::setChanged);
    private LazyOptional<MorkovskEnergyStorage> cap = LazyOptional.of(() -> energy);

    public BatteryBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public MorkovskEnergyStorage energy() { return energy; }

    public static void tick(Level level, BlockPos pos, BlockState state, BatteryBlockEntity be) {
        if (level.isClientSide) return;
        int stored = be.energy.getEnergyStored();
        if (stored <= 0) return;
        int want = Math.min(stored, 250);
        int leftover = EnergyNetwork.push(level, pos, want, pos);
        be.energy.extractEnergy(want - leftover, false);
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> c, @Nullable Direction side) {
        if (c == ForgeCapabilities.ENERGY) return cap.cast();
        return super.getCapability(c, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        cap.invalidate();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("Energy", energy.getEnergyStored());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        energy.setEnergy(tag.getInt("Energy"));
    }
}
