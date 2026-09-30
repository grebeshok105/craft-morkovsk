package com.craftmorkovsk.machine.framework;

import com.craftmorkovsk.energy.MorkovskEnergyStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** Base block entity for powered machines: item slots + FE buffer + progress + sync + caps.
 *  Subclasses implement {@link #tickServer(Level, BlockPos, BlockState)} for their logic and
 *  {@link #createMenu(int, Inventory, Player)} for their menu. */
public abstract class MachineBlockEntity extends BlockEntity implements MenuProvider {

    public static final int DATA_ENERGY = 0;
    public static final int DATA_MAX_ENERGY = 1;
    public static final int DATA_PROGRESS = 2;
    public static final int DATA_MAX_PROGRESS = 3;
    public static final int DATA_COUNT = 4;

    protected final ItemStackHandler items;
    protected final MorkovskEnergyStorage energy;
    protected int progress;
    protected int maxProgress;

    protected final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case DATA_ENERGY -> energy.getEnergyStored();
                case DATA_MAX_ENERGY -> energy.getMaxEnergyStored();
                case DATA_PROGRESS -> progress;
                case DATA_MAX_PROGRESS -> maxProgress;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case DATA_ENERGY -> energy.setEnergy(value);
                case DATA_PROGRESS -> progress = value;
                case DATA_MAX_PROGRESS -> maxProgress = value;
                default -> { }
            }
        }

        @Override
        public int getCount() {
            return DATA_COUNT;
        }
    };

    private LazyOptional<IItemHandler> itemCap;
    private LazyOptional<net.minecraftforge.energy.IEnergyStorage> energyCapHolder;

    protected MachineBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        this.items = createItemHandler();
        this.energy = createEnergyStorage();
        this.maxProgress = getDefaultProcessTime();
    }

    /** Item slots exposed to automation and the menu. */
    protected abstract ItemStackHandler createItemHandler();

    /** FE buffer. Machines use {@code MorkovskEnergyStorage.machine(cap, io, this::setChanged)}. */
    protected abstract MorkovskEnergyStorage createEnergyStorage();

    /** Default ticks per operation; shown as the progress bar maximum. */
    protected abstract int getDefaultProcessTime();

    /** FE consumed per tick while working; gate {@link #tickServer} on the buffer having it. */
    protected int getEnergyPerTick() {
        return com.craftmorkovsk.config.MorkovskConfig.MACHINE_ENERGY_PER_TICK.get();
    }

    /** Server tick. Consume energy via {@code energy.consumeInternal(n)}; bump {@code progress}. */
    protected abstract void tickServer(Level level, BlockPos pos, BlockState state);

    /** Client tick — override for visual/audio state. Default no-op. */
    protected void tickClient(Level level, BlockPos pos, BlockState state) { }

    public static <T extends BlockEntity> void tick(Level level, BlockPos pos, BlockState state, T be) {
        if (!(be instanceof MachineBlockEntity machine)) return;
        if (level.isClientSide) machine.tickClient(level, pos, state);
        else machine.tickServer(level, pos, state);
    }

    /** True while the machine is doing work — drives the lit/active blockstate + GUI arrow. */
    public boolean isWorking() {
        return progress > 0;
    }

    /** Drops this machine's stored items into the world (called by the block's onRemove). */
    public void dropContents(Level level, BlockPos pos) {
        SimpleContainer container = new SimpleContainer(items.getSlots());
        for (int i = 0; i < items.getSlots(); i++) container.setItem(i, items.getStackInSlot(i));
        Containers.dropContents(level, pos, container);
    }

    @Override
    public Component getDisplayName() {
        return getBlockState().getBlock().getName();
    }

    @Nullable
    @Override
    public abstract AbstractContainerMenu createMenu(int windowId, Inventory playerInventory, Player player);

    public ItemStackHandler getItems() { return items; }
    public MorkovskEnergyStorage getEnergy() { return energy; }
    public ContainerData getContainerData() { return data; }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) return itemHandler().cast();
        if (cap == ForgeCapabilities.ENERGY) return energyHandler().cast();
        return super.getCapability(cap, side);
    }

    protected LazyOptional<IItemHandler> itemHandler() {
        if (itemCap == null) itemCap = LazyOptional.of(() -> items);
        return itemCap;
    }

    protected LazyOptional<net.minecraftforge.energy.IEnergyStorage> energyHandler() {
        if (energyCapHolder == null) energyCapHolder = LazyOptional.of(() -> energy);
        return energyCapHolder;
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        if (itemCap != null) itemCap.invalidate();
        if (energyCapHolder != null) energyCapHolder.invalidate();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Items", items.serializeNBT());
        tag.putInt("Energy", energy.getEnergyStored());
        tag.putInt("Progress", progress);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        items.deserializeNBT(tag.getCompound("Items"));
        energy.setEnergy(tag.getInt("Energy"));
        progress = tag.getInt("Progress");
    }
}
