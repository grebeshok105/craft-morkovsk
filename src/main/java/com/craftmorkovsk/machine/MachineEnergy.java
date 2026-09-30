package com.craftmorkovsk.machine;

import com.craftmorkovsk.energy.EnergyNetwork;
import com.craftmorkovsk.energy.MorkovskEnergyStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

/** FE intake for powered machines: draws from adjacent energy-capable block entities and
 *  any cable network they touch (batteries/generators on the other end). Pull-based so a
 *  machine also charges when its neighbor only exposes extraction, not pushing. */
public final class MachineEnergy {

    /** Max FE a machine draws from the network per tick. */
    public static final int DRAW_PER_TICK = 200;

    private MachineEnergy() {}

    /** Call at the top of a machine's server tick; tops up its internal buffer. */
    public static void draw(Level level, BlockPos pos, MorkovskEnergyStorage energy) {
        int space = energy.getMaxEnergyStored() - energy.getEnergyStored();
        if (space <= 0) return;
        int got = EnergyNetwork.pull(level, pos, Math.min(space, DRAW_PER_TICK), pos);
        if (got > 0) energy.receiveEnergy(got, false);
    }
}
