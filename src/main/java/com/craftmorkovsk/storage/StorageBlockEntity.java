package com.craftmorkovsk.storage;

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
import net.minecraft.world.inventory.ContainerLevelAccess;
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

/** Chest-like storage block entity: an {@link ItemStackHandler} sized and filtered by
 *  its {@link StorageKind}, exposed as the item capability (hopper-friendly) and to
 *  {@link StorageMenu}. Menu creation is a small switch on the kind. */
public class StorageBlockEntity extends BlockEntity implements MenuProvider {

    private final StorageKind kind;
    private final ItemStackHandler items;
    private LazyOptional<IItemHandler> itemCap;

    public StorageBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, StorageKind kind) {
        super(type, pos, state);
        this.kind = kind;
        this.items = createItemHandler();
    }

    private ItemStackHandler createItemHandler() {
        if (kind == StorageKind.SILO) return new SiloItemHandler(this);
        return new ItemStackHandler(kind.slots) {
            @Override
            protected void onContentsChanged(int slot) {
                setChanged();
            }

            @Override
            public boolean isItemValid(int slot, @NotNull ItemStack stack) {
                return kind.filter.test(stack);
            }

            @Override
            public int getSlotLimit(int slot) {
                return kind.slotLimit;
            }
        };
    }

    public StorageKind kind() { return kind; }
    public ItemStackHandler getItems() { return items; }

    /** Drops all stored items into the world (called by the block's onRemove). */
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
    public AbstractContainerMenu createMenu(int windowId, Inventory playerInventory, Player player) {
        return new StorageMenu(StorageModule.menuType(kind), kind, windowId, playerInventory,
                items, ContainerLevelAccess.create(level, worldPosition));
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) return itemHandler().cast();
        return super.getCapability(cap, side);
    }

    protected LazyOptional<IItemHandler> itemHandler() {
        if (itemCap == null) itemCap = LazyOptional.of(() -> items);
        return itemCap;
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        if (itemCap != null) itemCap.invalidate();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Items", items.serializeNBT());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        items.deserializeNBT(tag.getCompound("Items"));
    }
}
