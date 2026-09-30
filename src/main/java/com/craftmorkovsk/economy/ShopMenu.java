package com.craftmorkovsk.economy;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/** Shop menu: no real item slots of its own — offers are drawn by the screen and
 *  bought via {@link BuyPacket}. Only the player inventory is present so players
 *  can see what they're carrying while shopping. */
public class ShopMenu extends AbstractContainerMenu {

    public static final int PLAYER_INV_X = 39;
    public static final int PLAYER_INV_Y = 140;

    private final ContainerLevelAccess access;

    public ShopMenu(MenuType<?> type, int windowId, Inventory playerInventory,
                    ContainerLevelAccess access) {
        super(type, windowId);
        this.access = access == null ? ContainerLevelAccess.NULL : access;
        addPlayerSlots(playerInventory);
    }

    /** Client-side ctor: reads the BlockPos NetworkHooks.openScreen wrote to the buffer. */
    public ShopMenu(MenuType<?> type, int windowId, Inventory playerInventory, FriendlyByteBuf buf) {
        this(type, windowId, playerInventory,
                ContainerLevelAccess.create(playerInventory.player.level(), buf.readBlockPos()));
    }

    private void addPlayerSlots(Inventory inv) {
        for (int row = 0; row < 3; row++)
            for (int col = 0; col < 9; col++)
                addSlot(new Slot(inv, col + row * 9 + 9,
                        PLAYER_INV_X + col * 18, PLAYER_INV_Y + row * 18));
        for (int col = 0; col < 9; col++)
            addSlot(new Slot(inv, col, PLAYER_INV_X + col * 18, PLAYER_INV_Y + 58));
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        // Nothing to shift into — the shop holds no real slots.
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return access.evaluate((level, pos) -> player.distanceToSqr(
                pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= 64.0, true);
    }
}
