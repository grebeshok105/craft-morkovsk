package com.craftmorkovsk.irrigation;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/** Water channel: a ground trough holding a small water reserve. Fills from
 *  adjacent water source blocks, adjacent tanks, or water pushed through the
 *  pipe network. While it holds water it keeps the 4 horizontally adjacent
 *  soil positions hydrated, slowly seeping dry. */
public class WaterChannelBlockEntity extends BlockEntity implements IWaterStorage {

    public static final int CAPACITY = 200;
    private static final int FILL_PERIOD = 20;
    private static final int FILL_UNITS = 10;
    private static final int IRRIGATE_PERIOD = 40;
    private static final int SEEP_UNITS = 1;

    private int water;
    private int counter;
    private final WaterTicket ticket = new WaterTicket();

    public WaterChannelBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, WaterChannelBlockEntity be) {
        if (level.isClientSide) return;
        be.counter++;
        if (be.counter % FILL_PERIOD == 0) be.fillStep(level, pos);
        if (be.counter >= IRRIGATE_PERIOD) {
            be.counter = 0;
            be.irrigateStep(level, pos);
        }
    }

    /** Sips water from adjacent water sources and adjacent tanks. */
    private void fillStep(Level level, BlockPos pos) {
        if (water >= CAPACITY) return;
        for (Direction d : Direction.values()) {
            BlockPos n = pos.relative(d);
            if (level.getFluidState(n).is(FluidTags.WATER) && level.getFluidState(n).isSource()) {
                water = Math.min(CAPACITY, water + FILL_UNITS);
                setChanged();
            } else if (level.getBlockEntity(n) instanceof WaterTankBlockEntity tank) {
                int got = tank.extractWater(Math.min(FILL_UNITS, CAPACITY - water), false);
                if (got > 0) {
                    water += got;
                    setChanged();
                }
            }
            if (water >= CAPACITY) return;
        }
    }

    private void irrigateStep(Level level, BlockPos pos) {
        if (water <= 0) {
            WaterGrid.unmark(level, pos);
            ticket.invalidate();
            return;
        }
        LongSet area = new LongOpenHashSet(4);
        for (Direction d : Direction.Plane.HORIZONTAL) area.add(pos.relative(d).asLong());
        WaterGrid.mark(level, pos, area);
        ticket.ensure(level, new AABB(pos).inflate(1, 0.5, 1));
        water = Math.max(0, water - SEEP_UNITS);
        setChanged();
    }

    private void unload(Level level, BlockPos pos) {
        WaterGrid.unmark(level, pos);
        ticket.invalidate();
    }

    @Override
    public void setRemoved() {
        unload(level, worldPosition);
        super.setRemoved();
    }

    @Override
    public void onChunkUnloaded() {
        unload(level, worldPosition);
        super.onChunkUnloaded();
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
