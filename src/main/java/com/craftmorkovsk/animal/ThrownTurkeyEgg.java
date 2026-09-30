package com.craftmorkovsk.animal;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;

/** Thrown turkey egg: same odds as the duck egg, hatching turkey poults. */
public class ThrownTurkeyEgg extends ThrowableItemProjectile {

    public ThrownTurkeyEgg(EntityType<? extends ThrownTurkeyEgg> type, Level level) {
        super(type, level);
    }

    public ThrownTurkeyEgg(Level level, LivingEntity shooter) {
        super(AnimalModule.TURKEY_EGG_PROJECTILE.get(), shooter, level);
    }

    public ThrownTurkeyEgg(Level level, double x, double y, double z) {
        super(AnimalModule.TURKEY_EGG_PROJECTILE.get(), x, y, z, level);
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!this.level().isClientSide) {
            if (this.random.nextInt(8) == 0) {
                int count = this.random.nextInt(32) == 0 ? 4 : 1;
                for (int i = 0; i < count; i++) {
                    TurkeyEntity turkey = AnimalModule.TURKEY.get().create(this.level());
                    if (turkey == null) continue;
                    turkey.setAge(-24000);
                    turkey.moveTo(this.getX(), this.getY(), this.getZ(), this.getYRot(), 0.0F);
                    this.level().addFreshEntity(turkey);
                }
            }
            this.level().broadcastEntityEvent(this, (byte) 3);
            this.discard();
        }
    }

    @Override
    protected Item getDefaultItem() {
        return AnimalModule.TURKEY_EGG.get();
    }
}
