package com.craftmorkovsk.energy;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.energy.IEnergyStorage;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;

/** Discovers the cable graph around a point and pushes/pulls FE through it.
 *  Graph is recomputed per operation (networks here are small); callers rate-limit. */
public final class EnergyNetwork {

    private static final int MAX_CABLES = 512;

    private EnergyNetwork() {}

    /** All IEnergyStorage endpoints reachable from start through cables + direct neighbors. */
    public static Set<IEnergyStorage> collect(Level level, BlockPos start, BlockPos self) {
        Set<IEnergyStorage> found = new HashSet<>();
        Set<BlockPos> visited = new HashSet<>();
        Deque<BlockPos> frontier = new ArrayDeque<>();
        visited.add(start);
        for (Direction d : Direction.values()) {
            BlockPos n = start.relative(d);
            if (n.equals(self)) continue;
            if (level.getBlockState(n).getBlock() instanceof CableBlock) frontier.add(n);
            else addEndpoint(level, n, d.getOpposite(), found);
        }
        while (!frontier.isEmpty() && visited.size() < MAX_CABLES) {
            BlockPos cur = frontier.poll();
            if (!visited.add(cur)) continue;
            for (Direction d : Direction.values()) {
                BlockPos n = cur.relative(d);
                if (visited.contains(n) || n.equals(self)) continue;
                if (level.getBlockState(n).getBlock() instanceof CableBlock) frontier.add(n);
                else addEndpoint(level, n, d.getOpposite(), found);
            }
        }
        return found;
    }

    private static void addEndpoint(Level level, BlockPos pos, Direction side, Set<IEnergyStorage> out) {
        var be = level.getBlockEntity(pos);
        if (be == null) return;
        be.getCapability(ForgeCapabilities.ENERGY, side).ifPresent(out::add);
    }

    /** Pushes energy into the network around pos; returns leftover. */
    public static int push(Level level, BlockPos pos, int amount, BlockPos self) {
        int remaining = amount;
        for (IEnergyStorage es : collect(level, pos, self)) {
            if (remaining <= 0) break;
            remaining -= es.receiveEnergy(remaining, false);
        }
        return remaining;
    }

    /** Pulls energy from the network around pos; returns amount received. */
    public static int pull(Level level, BlockPos pos, int amount, BlockPos self) {
        int received = 0;
        for (IEnergyStorage es : collect(level, pos, self)) {
            if (received >= amount) break;
            received += es.extractEnergy(amount - received, false);
        }
        return received;
    }
}
