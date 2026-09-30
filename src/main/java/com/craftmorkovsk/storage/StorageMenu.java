package com.craftmorkovsk.storage;

import com.craftmorkovsk.data.Award;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.Nullable;

/** Chest-like storage menu: a 9-wide storage grid sized by {@link StorageKind} plus
 *  the player inventory, in the vanilla double-chest layout. Seed vault and silo
 *  additionally watch insertions to grant the "stockpiler" advancement. */
public class StorageMenu extends AbstractContainerMenu {

    public final StorageKind kind;
    private final IItemHandler items;
    private final ContainerLevelAccess access;
    private final Player player;
    private final int storageSlotCount;

    /** Server-side ctor — the block entity passes its handler and position. */
    public StorageMenu(MenuType<?> type, StorageKind kind, int windowId, Inventory inv,
                       IItemHandler items, ContainerLevelAccess access) {
        super(type, windowId);
        this.kind = kind;
        this.items = items;
        this.access = access == null ? ContainerLevelAccess.NULL : access;
        this.player = inv.player;
        this.storageSlotCount = items.getSlots();
        addStorageSlots();
        addPlayerSlots(inv);
    }

    /** Client-side ctor: reads the BlockPos NetworkHooks.openScreen wrote to the buffer. */
    public StorageMenu(MenuType<?> type, StorageKind kind, int windowId, Inventory inv,
                       FriendlyByteBuf buf) {
        this(type, kind, windowId, inv, buf.readBlockPos());
    }

    private StorageMenu(MenuType<?> type, StorageKind kind, int windowId, Inventory inv, BlockPos pos) {
        this(type, kind, windowId, inv, handlerAt(inv, pos, kind), accessAt(inv, pos));
    }

    private static IItemHandler handlerAt(Inventory inv, BlockPos pos, StorageKind kind) {
        BlockEntity be = inv.player.level().getBlockEntity(pos);
        return be instanceof StorageBlockEntity storage
                ? storage.getItems()
                : new ItemStackHandler(kind.slots);
    }

    private static ContainerLevelAccess accessAt(Inventory inv, BlockPos pos) {
        return pos != null ? ContainerLevelAccess.create(inv.player.level(), pos)
                : ContainerLevelAccess.NULL;
    }

    private void addStorageSlots() {
        for (int row = 0; row < kind.rows; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(storageSlot(row * 9 + col, 8 + col * 18, 18 + row * 18));
            }
        }
    }

    /** Storage slot factory — insertion-watching slots for advancement-granting kinds. */
    private Slot storageSlot(int index, int x, int y) {
        if (kind.grantOnInsert) return new InsertWatchSlot(items, index, x, y);
        return new SlotItemHandler(items, index, x, y);
    }

    /** Player inventory + hotbar below the storage grid (vanilla 9xN layout). */
    private void addPlayerSlots(Inventory inv) {
        int invTop = 103 + (kind.rows - 4) * 18;
        for (int row = 0; row < 3; row++)
            for (int col = 0; col < 9; col++)
                addSlot(new Slot(inv, col + row * 9 + 9, 8 + col * 18, invTop + row * 18));
        for (int col = 0; col < 9; col++)
            addSlot(new Slot(inv, col, 8 + col * 18, invTop + 58));
    }

    /** Total items currently stored — shown on the silo screen. */
    public int totalStored() {
        int total = 0;
        for (int i = 0; i < storageSlotCount; i++) total += items.getStackInSlot(i).getCount();
        return total;
    }

    public int capacity() {
        return storageSlotCount * kind.slotLimit;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (slot == null || !slot.hasItem()) return result;
        ItemStack inSlot = slot.getItem();
        result = inSlot.copy();
        if (index < storageSlotCount) {
            if (!moveItemStackTo(inSlot, storageSlotCount, slots.size(), true)) return ItemStack.EMPTY;
        } else {
            if (!moveItemStackTo(inSlot, 0, storageSlotCount, false)) return ItemStack.EMPTY;
        }
        if (inSlot.isEmpty()) slot.set(ItemStack.EMPTY);
        else slot.setChanged();
        return result;
    }

    @Override
    public boolean stillValid(Player player) {
        return access.evaluate((level, pos) -> player.distanceToSqr(
                pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= 64.0, true);
    }

    /** Slot that grants "stockpiler" the first time its contents grow (i.e. the player
     *  inserts something) — covers direct placement, cursor merge and shift-click. */
    private final class InsertWatchSlot extends SlotItemHandler {
        private ItemStack lastSeen = ItemStack.EMPTY;

        InsertWatchSlot(IItemHandler handler, int index, int x, int y) {
            super(handler, index, x, y);
        }

        @Override
        public void setChanged() {
            ItemStack now = getItem();
            boolean gained = !now.isEmpty() && (lastSeen.isEmpty()
                    || now.getCount() > lastSeen.getCount()
                    || !ItemStack.isSameItemSameTags(now, lastSeen));
            lastSeen = now.copy();
            super.setChanged();
            if (gained && player instanceof ServerPlayer serverPlayer) {
                Award.grant(serverPlayer, "stockpiler");
            }
        }
    }
}
