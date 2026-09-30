package com.craftmorkovsk.animal;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;

import java.util.function.Supplier;

/** Bird base: lays its egg item on a chicken-style timer, but only while fed
 *  (hand-fed or trough-fed). The fed window also doubles production rate. */
public abstract class FarmBirdEntity extends FarmAnimalEntity {

    public static final int EGG_BASE_TIME = 6000;

    private int eggTime = this.random.nextInt(EGG_BASE_TIME) + EGG_BASE_TIME;

    protected FarmBirdEntity(EntityType<? extends FarmBirdEntity> type, Level level) {
        super(type, level);
    }

    protected abstract Supplier<? extends ItemLike> eggProduct();

    @Override
    public void aiStep() {
        super.aiStep();
        if (!this.level().isClientSide && !this.isBaby() && this.isAlive() && this.isFed()) {
            this.eggTime -= productionBoost();
            if (this.eggTime <= 0) {
                this.playSound(produceSound(), 1.0F,
                        (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F);
                this.spawnAtLocation(new ItemStack(eggProduct().get()));
                this.eggTime = this.random.nextInt(EGG_BASE_TIME) + EGG_BASE_TIME;
            }
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("EggTime", this.eggTime);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("EggTime")) this.eggTime = tag.getInt("EggTime");
    }
}
