package com.craftmorkovsk.core;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;

import java.util.ArrayList;
import java.util.List;

/** Cross-module decoupling points. Owning modules (greenhouse, irrigation) register
 *  implementations during their Module#init(); the core crop/soil code consults them.
 *  Defaults are inert so the game works with or without those modules present. */
public final class FarmingHooks {

    /** Returns true when the position is enclosed by a valid greenhouse structure. */
    @FunctionalInterface
    public interface GreenhouseChecker {
        boolean isInsideGreenhouse(LevelReader level, BlockPos pos);
    }

    /** Returns true when irrigation equipment actively hydrates this position. */
    @FunctionalInterface
    public interface IrrigationChecker {
        boolean isIrrigated(LevelReader level, BlockPos pos);
    }

    private static final List<GreenhouseChecker> GREENHOUSE = new ArrayList<>();
    private static final List<IrrigationChecker> IRRIGATION = new ArrayList<>();

    private FarmingHooks() {}

    public static void registerGreenhouse(GreenhouseChecker checker) {
        GREENHOUSE.add(checker);
    }

    public static void registerIrrigation(IrrigationChecker checker) {
        IRRIGATION.add(checker);
    }

    public static boolean isInGreenhouse(LevelReader level, BlockPos pos) {
        for (GreenhouseChecker c : GREENHOUSE) if (c.isInsideGreenhouse(level, pos)) return true;
        return false;
    }

    public static boolean isIrrigated(LevelReader level, BlockPos pos) {
        for (IrrigationChecker c : IRRIGATION) if (c.isIrrigated(level, pos)) return true;
        return false;
    }
}
