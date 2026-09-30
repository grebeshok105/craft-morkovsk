package com.craftmorkovsk.irrigation;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;

/** Discovers the pipe graph around a point and moves water units through it.
 *  Mirrors EnergyNetwork: the graph is recomputed per operation (networks are
 *  small) and callers rate-limit. Pipes and open valves conduct; everything else
 *  with an {@link IWaterStorage} block entity is an endpoint. */
public final class WaterNetwork {

    private static final int MAX_PIPES = 512;

    private WaterNetwork() {}

    /** True when water can flow through this position (pipe or open valve). */
    public static boolean conducts(BlockState state) {
        if (state.getBlock() instanceof IrrigationValveBlock)
            return state.getValue(IrrigationValveBlock.OPEN);
        return state.getBlock() instanceof WaterPipeBlock;
    }

    /** All water-holding endpoints reachable from start through conducting pipes. */
    public static Set<IWaterStorage> collect(Level level, BlockPos start, BlockPos self) {
        Set<IWaterStorage> found = new HashSet<>();
        Set<BlockPos> visited = new HashSet<>();
        Deque<BlockPos> frontier = new ArrayDeque<>();
        visited.add(start);
        for (Direction d : Direction.values()) {
            BlockPos n = start.relative(d);
            if (n.equals(self)) continue;
            if (conducts(level.getBlockState(n))) frontier.add(n);
            else addEndpoint(level, n, self, found);
        }
        while (!frontier.isEmpty() && visited.size() < MAX_PIPES) {
            BlockPos cur = frontier.poll();
            if (!visited.add(cur)) continue;
            for (Direction d : Direction.values()) {
                BlockPos n = cur.relative(d);
                if (visited.contains(n) || n.equals(self)) continue;
                if (conducts(level.getBlockState(n))) frontier.add(n);
                else addEndpoint(level, n, self, found);
            }
        }
        return found;
    }

    private static void addEndpoint(Level level, BlockPos pos, BlockPos self, Set<IWaterStorage> out) {
        if (pos.equals(self)) return;
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof IWaterStorage storage) out.add(storage);
    }

    /** Pushes water into the network around pos; returns the leftover amount. */
    public static int push(Level level, BlockPos pos, int amount, BlockPos self) {
        return push(amount, collect(level, pos, self));
    }

    /** Pushes water into a precomputed endpoint set; returns the leftover. */
    public static int push(int amount, Set<IWaterStorage> endpoints) {
        int remaining = amount;
        for (IWaterStorage storage : endpoints) {
            if (remaining <= 0) break;
            remaining -= storage.receiveWater(remaining, false);
        }
        return remaining;
    }
}
