package com.craftmorkovsk.animal;

import com.craftmorkovsk.data.Award;
import com.craftmorkovsk.data.FarmingStats;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

/** Farm goat: wheat/morkov-fed quadruped. Milkable with an empty bucket on a
 *  cooldown (vanilla cow pattern), drops manure. */
public class FarmGoatEntity extends FarmAnimalEntity {

    public static final int MILK_COOLDOWN = 2400; // 2 min between milkings

    private int milkCooldown = 0;

    public FarmGoatEntity(EntityType<? extends FarmGoatEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 10.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.23D);
    }

    @Override
    public TagKey<Item> foodTag() {
        return AnimalModule.GOAT_FOOD;
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob partner) {
        return AnimalModule.FARM_GOAT.get().create(level);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!this.level().isClientSide && this.milkCooldown > 0) {
            this.milkCooldown = Math.max(0, this.milkCooldown - productionBoost());
        }
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        if (held.is(Items.BUCKET) && !this.isBaby() && this.milkCooldown <= 0) {
            player.playSound(SoundEvents.COW_MILK, 1.0F, 1.0F);
            ItemStack filled = ItemUtils.createFilledResult(held, player,
                    new ItemStack(AnimalModule.GOAT_MILK_BUCKET.get()));
            player.setItemInHand(hand, filled);
            if (!this.level().isClientSide) {
                this.milkCooldown = MILK_COOLDOWN;
                if (player instanceof ServerPlayer serverPlayer) {
                    FarmingStats.awardXp(serverPlayer, FarmingStats.XpReason.BREEDING, 1);
                    Award.grant(serverPlayer, "animal_keeper");
                }
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }
        return super.mobInteract(player, hand);
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.GOAT_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.GOAT_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.GOAT_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(SoundEvents.GOAT_STEP, 0.15F, 1.0F);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("MilkCooldown", this.milkCooldown);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("MilkCooldown")) this.milkCooldown = tag.getInt("MilkCooldown");
    }
}
