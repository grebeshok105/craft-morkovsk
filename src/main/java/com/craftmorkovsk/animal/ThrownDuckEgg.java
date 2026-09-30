package com.craftmorkovsk.animal;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;

/** Thrown duck egg: vanilla-egg physics, hatches a duckling on impact
 *  (1/8 chance, 1/256 for a clutch of four). */
public class ThrownDuckEgg extends ThrowableItemProjectile {

    public ThrownDuckEgg(EntityType<? extends ThrownDuckEgg> type, Level level) {
        super(type, level);
    }

    public ThrownDuckEgg(Level level, LivingEntity shooter) {
        super(AnimalModule.DUCK_EGG_PROJECTILE.get(), shooter, level);
    }

    public ThrownDuckEgg(Level level, double x, double y, double z) {
        super(AnimalModule.DUCK_EGG_PROJECTILE.get(), x, y, z, level);
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!this.level().isClientSide) {
            if (this.random.nextInt(8) == 0) {
                int count = this.random.nextInt(32) == 0 ? 4 : 1;
                for (int i = 0; i < count; i++) {
                    DuckEntity duck = AnimalModule.DUCK.get().create(this.level());
                    if (duck == null) continue;
                    duck.setAge(-24000);
                    duck.moveTo(this.getX(), this.getY(), this.getZ(), this.getYRot(), 0.0F);
                    this.level().addFreshEntity(duck);
                }
            }
            this.level().broadcastEntityEvent(this, (byte) 3);
            this.discard();
        }
    }

    @Override
    protected Item getDefaultItem() {
        return AnimalModule.DUCK_EGG.get();
    }
}
