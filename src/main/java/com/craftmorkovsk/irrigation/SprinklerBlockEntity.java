package com.craftmorkovsk.irrigation;

import com.craftmorkovsk.config.MorkovskConfig;
import com.craftmorkovsk.data.Award;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.UUID;

/** Sprinkler: buffers water pushed through the pipe network and, roughly every
 *  40 ticks, spends a gulp to hydrate farmland in its radius — registered in
 *  WaterGrid (SoilAPI.isHydrated via FarmingHooks), backed by a Forge farmland
 *  water ticket, and MOISTURE=7 pushed onto FarmBlocks directly. */
public class SprinklerBlockEntity extends BlockEntity implements IWaterStorage {

    private static final int PULSE_PERIOD = 40;
    private static final int BASIC_CAPACITY = 300;
    private static final int ADVANCED_CAPACITY = 600;
    private static final int BASIC_COST = 10;
    private static final int ADVANCED_COST = 20;

    private int water;
    private int counter;
    private UUID placer;
    private boolean advancementGranted;
    private final WaterTicket ticket = new WaterTicket();

    public SprinklerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public void setPlacer(UUID placer) { this.placer = placer; }

    private boolean advanced() {
        return getBlockState().getBlock() instanceof SprinklerBlock block && block.isAdvanced();
    }

    private int capacity() { return advanced() ? ADVANCED_CAPACITY : BASIC_CAPACITY; }

    private int pulseCost() { return advanced() ? ADVANCED_COST : BASIC_COST; }

    private int range() {
        return advanced()
                ? MorkovskConfig.IRRIGATION_ADVANCED_SPRINKLER_RANGE.get()
                : MorkovskConfig.IRRIGATION_SPRINKLER_RANGE.get();
    }

    public static void tick(Level level, BlockPos pos, BlockState state, SprinklerBlockEntity be) {
        if (level.isClientSide) return;
        if (++be.counter < PULSE_PERIOD) return;
        be.counter = 0;
        be.pulse(level, pos);
    }

    private void pulse(Level level, BlockPos pos) {
        if (water < pulseCost()) {
            WaterGrid.unmark(level, pos);
            ticket.invalidate();
            return;
        }
        water -= pulseCost();

        int range = range();
        LongSet area = new LongOpenHashSet();
        for (int dx = -range; dx <= range; dx++) {
            for (int dz = -range; dz <= range; dz++) {
                // Head-level and one below — sprinklers sit beside crops or above soil.
                area.add(pos.offset(dx, 0, dz).asLong());
                area.add(pos.offset(dx, -1, dz).asLong());
            }
        }
        WaterGrid.mark(level, pos, area);
        ticket.ensure(level, new AABB(pos).inflate(range, 1, range));

        int farmland = 0;
        LongIterator it = area.iterator();
        while (it.hasNext()) {
            BlockPos p = BlockPos.of(it.nextLong());
            BlockState soil = level.getBlockState(p);
            if (soil.hasProperty(FarmBlock.MOISTURE)) {
                farmland++;
                if (soil.getValue(FarmBlock.MOISTURE) < FarmBlock.MAX_MOISTURE) {
                    level.setBlock(p, soil.setValue(FarmBlock.MOISTURE, FarmBlock.MAX_MOISTURE), 2);
                }
            }
        }

        if (level instanceof ServerLevel server) {
            server.sendParticles(ParticleTypes.SPLASH,
                    pos.getX() + 0.5, pos.getY() + 0.9, pos.getZ() + 0.5,
                    10, range * 0.2, 0.25, range * 0.2, 0.05);
        }

        if (farmland > 0 && !advancementGranted && placer != null && level.getServer() != null) {
            ServerPlayer player = level.getServer().getPlayerList().getPlayer(placer);
            if (player != null) {
                Award.grant(player, "irrigation");
                advancementGranted = true;
            }
        }
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
        int accepted = Math.min(amount, capacity() - water);
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
    public int getWaterCapacity() { return capacity(); }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("Water", water);
        if (placer != null) tag.putUUID("Placer", placer);
        tag.putBoolean("AdvancementGranted", advancementGranted);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        water = Math.min(ADVANCED_CAPACITY, tag.getInt("Water"));
        if (tag.hasUUID("Placer")) placer = tag.getUUID("Placer");
        advancementGranted = tag.getBoolean("AdvancementGranted");
    }
}
