package com.craftmorkovsk.irrigation;

import com.craftmorkovsk.config.MorkovskConfig;
import com.craftmorkovsk.energy.EnergyNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Set;

/** Water pump: every cycle, if it sits next to a water source block or a tank
 *  that still has water, spends a little FE and pushes water into the connected
 *  pipe network (sprinklers, channels, tanks fill up). */
public class WaterPumpBlockEntity extends BlockEntity {

    private static final int PERIOD = 20;
    private static final int UNITS_PER_CYCLE = 20;

    private int counter;

    public WaterPumpBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, WaterPumpBlockEntity be) {
        if (level.isClientSide) return;
        if (++be.counter < PERIOD) return;
        be.counter = 0;
        be.pumpOnce(level, pos);
    }

    private void pumpOnce(Level level, BlockPos pos) {
        boolean infiniteSource = false;
        WaterTankBlockEntity sourceTank = null;
        for (Direction d : Direction.values()) {
            BlockPos n = pos.relative(d);
            if (level.getFluidState(n).is(FluidTags.WATER) && level.getFluidState(n).isSource()) {
                infiniteSource = true;
            }
            if (sourceTank == null
                    && level.getBlockEntity(n) instanceof WaterTankBlockEntity tank
                    && tank.getWaterStored() > 0) {
                sourceTank = tank;
            }
        }
        if (!infiniteSource && sourceTank == null) return;

        Set<IWaterStorage> endpoints = WaterNetwork.collect(level, pos, pos);
        if (endpoints.isEmpty()) return;

        int fe = Math.max(1, MorkovskConfig.MACHINE_ENERGY_PER_TICK.get());
        int drawn = EnergyNetwork.pull(level, pos, fe, pos);
        if (drawn < fe) {
            // Not powered: hand back whatever was drained so idle pumps are free.
            if (drawn > 0) EnergyNetwork.push(level, pos, drawn, pos);
            return;
        }

        int budget = UNITS_PER_CYCLE;
        if (!infiniteSource) budget = sourceTank.extractWater(budget, false);
        int leftover = WaterNetwork.push(budget, endpoints);
        if (!infiniteSource && leftover > 0) sourceTank.receiveWater(leftover, false);
    }
}
