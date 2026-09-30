package com.craftmorkovsk.animal;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.MoveToBlockGoal;
import net.minecraft.world.level.LevelReader;

/** Hungry (unfed) farm animals pathfind to the nearest feeding trough; the trough
 *  block entity then feeds them once they are inside its scan radius. */
public class TroughEatGoal extends MoveToBlockGoal {

    private final FarmAnimalEntity animal;

    public TroughEatGoal(FarmAnimalEntity animal, double speedModifier) {
        super(animal, speedModifier, 10, 3);
        this.animal = animal;
    }

    @Override
    public boolean canUse() {
        return !this.animal.isFed() && super.canUse();
    }

    @Override
    public boolean canContinueToUse() {
        return !this.animal.isFed() && super.canContinueToUse();
    }

    @Override
    protected boolean isValidTarget(LevelReader level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof FeedingTroughBlockEntity;
    }
}
