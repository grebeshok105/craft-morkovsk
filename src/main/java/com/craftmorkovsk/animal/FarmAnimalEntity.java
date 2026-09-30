package com.craftmorkovsk.animal;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowParentGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;

import javax.annotation.Nullable;
import java.util.function.Supplier;

/** Shared base for the mod's farm animals: breeding via species food tag, a "fed"
 *  window (hand feeding or feeding trough) that gates/boons production, and a
 *  manure timer that runs whenever the animal is an adult. */
public abstract class FarmAnimalEntity extends Animal {

    public static final long FED_DURATION = 12000L;   // 10 min of being "fed"
    public static final int MANURE_BASE_TIME = 6000;  // ~5 min like the vanilla chicken egg timer

    /** Game time until which this animal counts as fed. */
    private long fedUntil = Long.MIN_VALUE;
    private int manureTime = this.random.nextInt(MANURE_BASE_TIME) + MANURE_BASE_TIME;

    protected FarmAnimalEntity(net.minecraft.world.entity.EntityType<? extends FarmAnimalEntity> type, Level level) {
        super(type, level);
    }

    /** Item tag accepted as food by this species. */
    public abstract TagKey<Item> foodTag();

    /** Item dropped on the manure timer (default {@code craftmorkovsk:manure}). */
    protected Supplier<? extends ItemLike> manureProduct() {
        return AnimalModule.MANURE::get;
    }

    /** Sound played when a product is dropped (egg / manure plop). */
    protected SoundEvent produceSound() {
        return SoundEvents.CHICKEN_EGG;
    }

    public boolean isFed() {
        return this.level().getGameTime() < this.fedUntil;
    }

    public long getFedUntil() {
        return this.fedUntil;
    }

    public void feed(long until) {
        this.fedUntil = Math.max(this.fedUntil, until);
    }

    /** Extra production ticks per game tick while fed (1 = 2x rate). */
    protected int productionBoost() {
        return isFed() ? 2 : 1;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new PanicGoal(this, 1.4D));
        this.goalSelector.addGoal(2, new BreedGoal(this, 1.0D));
        this.goalSelector.addGoal(3, new TemptGoal(this, 1.0D, Ingredient.of(foodTag()), false));
        this.goalSelector.addGoal(4, new FollowParentGoal(this, 1.1D));
        this.goalSelector.addGoal(5, new TroughEatGoal(this, 1.0D));
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(foodTag());
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        // Hand feeding doubles as real feeding: it also refreshes the fed window.
        if (this.isFood(player.getItemInHand(hand))) {
            this.feed(this.level().getGameTime() + FED_DURATION);
        }
        return super.mobInteract(player, hand);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!this.level().isClientSide && !this.isBaby() && this.isAlive()) {
            this.manureTime -= productionBoost();
            if (this.manureTime <= 0) {
                this.playSound(produceSound(), 1.0F,
                        (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F);
                this.spawnAtLocation(new ItemStack(manureProduct().get()));
                this.manureTime = this.random.nextInt(MANURE_BASE_TIME) + MANURE_BASE_TIME;
            }
        }
    }

    @Nullable
    @Override
    public abstract AgeableMob getBreedOffspring(net.minecraft.server.level.ServerLevel level, AgeableMob partner);

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putLong("FedUntil", this.fedUntil);
        tag.putInt("ManureTime", this.manureTime);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("FedUntil")) this.fedUntil = tag.getLong("FedUntil");
        if (tag.contains("ManureTime")) this.manureTime = tag.getInt("ManureTime");
    }
}
