package com.craftmorkovsk.tractor;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.Nullable;

/** Tractor cargo + attachment menu. Slot layout: 0 attachment, 1-9 cargo, 10+ player.
 *  Fuel/attachment are read on the client straight from the synced entity data. */
public class TractorMenu extends AbstractContainerMenu {

    private static final int ATTACHMENT_SLOT = 0;
    private static final int CARGO_SLOT_START = 1;
    private static final int CARGO_SLOT_END = 10;
    private static final int PLAYER_SLOTS_START = 10;

    @Nullable
    private final TractorEntity tractor;
    private final ItemStackHandler cargo;
    private final ItemStackHandler attachment;

    public TractorMenu(MenuType<?> type, int windowId, Inventory playerInventory, TractorEntity tractor) {
        super(type, windowId);
        this.tractor = tractor;
        this.cargo = tractor.getCargo();
        this.attachment = tractor.getAttachmentSlot();
        addTractorSlots();
        addPlayerSlots(playerInventory);
    }

    /** Client-side ctor: entity looked up by the id written by NetworkHooks.openScreen. */
    public TractorMenu(MenuType<?> type, int windowId, Inventory playerInventory, int entityId) {
        super(type, windowId);
        Entity entity = playerInventory.player.level().getEntity(entityId);
        this.tractor = entity instanceof TractorEntity t ? t : null;
        this.cargo = this.tractor != null ? this.tractor.getCargo() : new ItemStackHandler(9);
        this.attachment = this.tractor != null ? this.tractor.getAttachmentSlot() : new ItemStackHandler(1);
        addTractorSlots();
        addPlayerSlots(playerInventory);
    }

    private void addTractorSlots() {
        this.addSlot(new SlotItemHandler(this.attachment, 0, 30, 22) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.getItem() instanceof AttachmentItem;
            }

            @Override
            public int getMaxStackSize() {
                return 1;
            }
        });
        for (int i = 0; i < CARGO_SLOT_END - CARGO_SLOT_START; i++) {
            this.addSlot(new SlotItemHandler(this.cargo, i, 26 + i * 18, 46));
        }
    }

    private void addPlayerSlots(Inventory playerInventory) {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 142));
        }
    }

    @Nullable
    public TractorEntity tractor() {
        return this.tractor;
    }

    public int fuel() {
        return this.tractor != null ? this.tractor.getFuel() : 0;
    }

    public int maxFuel() {
        return this.tractor != null ? this.tractor.getMaxFuel() : 1;
    }

    public int attachmentId() {
        return this.tractor != null ? this.tractor.getAttachmentId() : TractorEntity.NO_ATTACHMENT;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (!slot.hasItem()) return result;
        ItemStack inSlot = slot.getItem();
        result = inSlot.copy();

        if (index < PLAYER_SLOTS_START) {
            if (!this.moveItemStackTo(inSlot, PLAYER_SLOTS_START, this.slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else {
            if (inSlot.getItem() instanceof AttachmentItem) {
                if (!this.moveItemStackTo(inSlot, ATTACHMENT_SLOT, ATTACHMENT_SLOT + 1, false)) {
                    if (!this.moveItemStackTo(inSlot, CARGO_SLOT_START, CARGO_SLOT_END, false)) {
                        return ItemStack.EMPTY;
                    }
                }
            } else if (!this.moveItemStackTo(inSlot, CARGO_SLOT_START, CARGO_SLOT_END, false)) {
                return ItemStack.EMPTY;
            }
        }

        if (inSlot.isEmpty()) slot.set(ItemStack.EMPTY);
        else slot.setChanged();
        return result;
    }

    @Override
    public boolean stillValid(Player player) {
        return this.tractor != null && this.tractor.isAlive()
                && (player.getVehicle() == this.tractor || player.distanceToSqr(this.tractor) <= 64.0);
    }
}
