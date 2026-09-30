package com.craftmorkovsk.energy;

import net.minecraftforge.energy.IEnergyStorage;

/** Simple bounded FE storage with a change listener so owners can setChanged(). */
public class MorkovskEnergyStorage implements IEnergyStorage {

    private int energy;
    private final int capacity;
    private final int maxReceive;
    private final int maxExtract;
    private final Runnable onChange;

    public MorkovskEnergyStorage(int capacity, int maxReceive, int maxExtract, Runnable onChange) {
        this.capacity = capacity;
        this.maxReceive = maxReceive;
        this.maxExtract = maxExtract;
        this.onChange = onChange;
    }

    public static MorkovskEnergyStorage machine(int capacity, int ioRate, Runnable onChange) {
        return new MorkovskEnergyStorage(capacity, ioRate, 0, onChange);
    }

    public static MorkovskEnergyStorage battery(int capacity, int ioRate, Runnable onChange) {
        return new MorkovskEnergyStorage(capacity, ioRate, ioRate, onChange);
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        int accepted = Math.min(Math.min(this.maxReceive, maxReceive), capacity - energy);
        if (accepted > 0 && !simulate) {
            energy += accepted;
            changed();
        }
        return accepted;
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        int drained = Math.min(Math.min(this.maxExtract, maxExtract), energy);
        if (drained > 0 && !simulate) {
            energy -= drained;
            changed();
        }
        return drained;
    }

    /** Bypasses the maxExtract limit for internal machine consumption. */
    public int consumeInternal(int amount) {
        int drained = Math.min(amount, energy);
        if (drained > 0) {
            energy -= drained;
            changed();
        }
        return drained;
    }

    /** Bypasses the maxReceive limit for internal generation. */
    public int generateInternal(int amount) {
        int accepted = Math.min(amount, capacity - energy);
        if (accepted > 0) {
            energy += accepted;
            changed();
        }
        return accepted;
    }

    @Override
    public int getEnergyStored() { return energy; }

    @Override
    public int getMaxEnergyStored() { return capacity; }

    @Override
    public boolean canExtract() { return maxExtract > 0; }

    @Override
    public boolean canReceive() { return maxReceive > 0; }

    public void setEnergy(int value) {
        energy = Math.max(0, Math.min(capacity, value));
    }

    private void changed() {
        if (onChange != null) onChange.run();
    }
}
