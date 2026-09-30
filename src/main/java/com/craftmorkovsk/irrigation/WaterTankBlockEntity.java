package com.craftmorkovsk.irrigation;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/** Water tank: buffers up to 8000 water units. Filled by pumps through the pipe
 *  network, by rain when it can see the sky, or by hand with a water bucket.
 *  Pumps and channels can draw from it. */
public class WaterTankBlockEntity extends BlockEntity implements IWaterStorage {

    public static final int CAPACITY = 8000;
    /** A vanilla bucket of water, in our units (1 unit ~ 100 mB). */
    public static final int BUCKET_UNITS = 10;
    private static final int RAIN_PERIOD = 100;
    private static final int RAIN_UNITS = 5;

    private int water;
    private int counter;

    public WaterTankBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, WaterTankBlockEntity be) {
        if (level.isClientSide) return;
        if (++be.counter < RAIN_PERIOD) return;
        be.counter = 0;
        if (be.water < CAPACITY && level.isRaining() && level.canSeeSky(pos.above())) {
            be.water = Math.min(CAPACITY, be.water + RAIN_UNITS);
            be.setChanged();
        }
    }

    @Override
    public int receiveWater(int amount, boolean simulate) {
        int accepted = Math.min(amount, CAPACITY - water);
        if (accepted > 0 && !simulate) {
            water += accepted;
            setChanged();
        }
        return accepted;
    }

    @Override
    public int extractWater(int amount, boolean simulate) {
        int drained = Math.min(amount, water);
        if (drained > 0 && !simulate) {
            water -= drained;
            setChanged();
        }
        return drained;
    }

    @Override
    public int getWaterStored() { return water; }

    @Override
    public int getWaterCapacity() { return CAPACITY; }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("Water", water);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        water = Math.min(CAPACITY, tag.getInt("Water"));
    }
}
