package com.craftmorkovsk.machine;

import com.craftmorkovsk.crop.MorkovskCropBlock;
import com.craftmorkovsk.energy.MorkovskEnergyStorage;
import com.craftmorkovsk.machine.framework.MachineBlockEntity;
import com.craftmorkovsk.soil.SoilAPI;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.items.ItemStackHandler;

import java.util.List;

/** Auto harvester: every HARVEST_TICKS of powered work it breaks one mature
 *  {@link MorkovskCropBlock} in the flat SCAN_RADIUS area on its own level, stores the
 *  drops in a 9-slot buffer, degrades the soil, and replants when seeds dropped. */
public class AutoHarvesterBlockEntity extends MachineBlockEntity {

    /** 5x5 flat scan area centered on the machine (radius 2). */
    public static final int SCAN_RADIUS = 2;
    /** Ticks of powered work per harvested crop. */
    public static final int HARVEST_TICKS = 200;
    /** How often the machine re-scans for a mature crop while idle. */
    public static final int SCAN_COOLDOWN = 20;
    public static final int BUFFER_SLOTS = 9;

    private BlockPos target;
    private int scanCooldown = 0;

    public AutoHarvesterBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    protected ItemStackHandler createItemHandler() {
        return new ItemStackHandler(BUFFER_SLOTS) {
            @Override
            protected void onContentsChanged(int slot) { setChanged(); }
        };
    }

    @Override
    protected MorkovskEnergyStorage createEnergyStorage() {
        return MorkovskEnergyStorage.machine(8000, 500, this::setChanged);
    }

    @Override
    protected int getDefaultProcessTime() { return HARVEST_TICKS; }

    @Override
    protected void tickServer(Level level, BlockPos pos, BlockState state) {
        MachineEnergy.draw(level, pos, energy);
        maxProgress = HARVEST_TICKS;

        if (target != null && !isMatureCrop(level, target)) {
            target = null;
            progress = 0;
        }
        if (target == null) {
            if (progress != 0) { progress = 0; setChanged(); }
            if (--scanCooldown <= 0) {
                scanCooldown = SCAN_COOLDOWN;
                target = findMatureCrop(level, pos);
            }
            return;
        }
        if (energy.getEnergyStored() < getEnergyPerTick()) return;
        energy.consumeInternal(getEnergyPerTick());
        progress++;
        if (progress >= HARVEST_TICKS) {
            harvest(level, target);
            target = null;
            progress = 0;
        }
        setChanged();
    }

    private boolean isMatureCrop(Level level, BlockPos cropPos) {
        BlockState s = level.getBlockState(cropPos);
        return s.getBlock() instanceof MorkovskCropBlock crop && crop.isMaxAge(s);
    }

    private BlockPos findMatureCrop(Level level, BlockPos center) {
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int dx = -SCAN_RADIUS; dx <= SCAN_RADIUS; dx++) {
            for (int dz = -SCAN_RADIUS; dz <= SCAN_RADIUS; dz++) {
                cursor.set(center.getX() + dx, center.getY(), center.getZ() + dz);
                if (isMatureCrop(level, cursor)) return cursor.immutable();
            }
        }
        return null;
    }

    /** Breaks the crop as if harvested: drops go to the buffer (overflow spills into the
     *  world), soil degrades, and the crop is replanted at age 0 when seeds dropped. */
    private void harvest(Level level, BlockPos cropPos) {
        BlockState cropState = level.getBlockState(cropPos);
        if (!(cropState.getBlock() instanceof MorkovskCropBlock crop) || !crop.isMaxAge(cropState)) return;
        if (!(level instanceof ServerLevel serverLevel)) return;

        List<ItemStack> drops = cropState.getDrops(new LootParams.Builder(serverLevel)
                .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(cropPos)));
        Item seedItem = crop.def().seeds.get();

        // Reserve one seed for replanting before the rest goes into the buffer.
        boolean replant = false;
        for (ItemStack drop : drops) {
            if (!replant && drop.is(seedItem)) {
                drop.shrink(1);
                replant = true;
            }
        }
        for (ItemStack drop : drops) {
            if (drop.isEmpty()) continue;
            ItemStack leftover = insertIntoBuffer(drop);
            if (!leftover.isEmpty()) {
                Containers.dropItemStack(level, cropPos.getX() + 0.5, cropPos.getY() + 0.5,
                        cropPos.getZ() + 0.5, leftover);
            }
        }

        SoilAPI.degrade(level, cropPos.below(), level.random);
        level.setBlock(cropPos, replant
                ? crop.getStateForAge(0) : Blocks.AIR.defaultBlockState(), 3);
    }

    private ItemStack insertIntoBuffer(ItemStack stack) {
        ItemStack remaining = stack.copy();
        for (int i = 0; i < items.getSlots() && !remaining.isEmpty(); i++) {
            remaining = items.insertItem(i, remaining, false);
        }
        return remaining;
    }

    @Override
    public AbstractContainerMenu createMenu(int windowId, Inventory playerInventory, Player player) {
        return new HarvesterMenu(MachineModule.AUTO_HARVESTER_MENU.get(), windowId, playerInventory,
                items, data, ContainerLevelAccess.create(level, worldPosition));
    }
}
