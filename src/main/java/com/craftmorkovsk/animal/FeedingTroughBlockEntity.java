package com.craftmorkovsk.animal;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/** Feeding trough storage: 9 slots accepting any species' food tag. Periodically
 *  scans a 6-block radius for unfed farm animals of the matching species, consumes
 *  one food item per animal and refreshes its fed window (+ breeding urge). */
public class FeedingTroughBlockEntity extends BlockEntity {

    public static final int SLOTS = 9;
    public static final double SCAN_RADIUS = 6.0D;
    public static final int SCAN_INTERVAL = 40;
    /** Re-feed when the animal's fed window has less than this many ticks left. */
    public static final long REFRESH_MARGIN = 6000L;

    private final ItemStackHandler inventory = new ItemStackHandler(SLOTS) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return isAnimalFood(stack);
        }
    };
    private final LazyOptional<IItemHandler> handlerCap = LazyOptional.of(() -> inventory);

    public FeedingTroughBlockEntity(BlockPos pos, BlockState state) {
        super(AnimalModule.FEEDING_TROUGH_BE.get(), pos, state);
    }

    public static boolean isAnimalFood(ItemStack stack) {
        return stack.is(AnimalModule.DUCK_FOOD)
                || stack.is(AnimalModule.TURKEY_FOOD)
                || stack.is(AnimalModule.GOAT_FOOD);
    }

    /** Player-facing insert: returns the leftover stack after filling the trough. */
    public ItemStack insertFood(ItemStack stack) {
        ItemStack remainder = stack;
        for (int slot = 0; slot < inventory.getSlots() && !remainder.isEmpty(); slot++) {
            remainder = inventory.insertItem(slot, remainder, false);
        }
        return remainder;
    }

    public ItemStackHandler getInventory() {
        return inventory;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, FeedingTroughBlockEntity trough) {
        if (level.getGameTime() % SCAN_INTERVAL != 0) return;
        long now = level.getGameTime();
        List<FarmAnimalEntity> nearby = level.getEntitiesOfClass(FarmAnimalEntity.class,
                new AABB(pos).inflate(SCAN_RADIUS), FarmAnimalEntity::isAlive);
        for (FarmAnimalEntity animal : nearby) {
            if (animal.isBaby()) continue;
            if (animal.getFedUntil() >= now + REFRESH_MARGIN) continue;
            int slot = trough.findFoodSlot(animal);
            if (slot < 0) continue;
            trough.inventory.extractItem(slot, 1, false);
            animal.feed(now + FarmAnimalEntity.FED_DURATION);
            animal.setInLove(null);
            level.playSound(null, pos, SoundEvents.GENERIC_EAT, SoundSource.BLOCKS, 0.6F,
                    0.9F + level.random.nextFloat() * 0.2F);
        }
    }

    private int findFoodSlot(FarmAnimalEntity animal) {
        for (int slot = 0; slot < inventory.getSlots(); slot++) {
            if (inventory.getStackInSlot(slot).is(animal.foodTag())) {
                return slot;
            }
        }
        return -1;
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return handlerCap.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        handlerCap.invalidate();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Inventory", inventory.serializeNBT());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("Inventory")) {
            inventory.deserializeNBT(tag.getCompound("Inventory"));
        }
    }
}
