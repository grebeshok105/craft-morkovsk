package com.craftmorkovsk.economy;

import com.craftmorkovsk.data.Award;
import com.craftmorkovsk.data.FarmingStats;
import com.craftmorkovsk.energy.MorkovskEnergyStorage;
import com.craftmorkovsk.machine.framework.MachineBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.ItemStackHandler;

import java.util.UUID;

/** Shipping crate: sells its 27 slots every {@link #SELL_INTERVAL} ticks at the current
 *  daily prices. Earnings are credited to the last player who interacted (falling back
 *  to the nearest player within 8 blocks); if nobody can be resolved the payout waits
 *  in {@link #pendingPayout} until someone opens the crate or comes back online. */
public class ShippingCrateBlockEntity extends MachineBlockEntity {

    public static final int SELL_INTERVAL = 600; // 30 seconds

    private UUID lastInteracting;
    private long pendingPayout;
    private int pendingStacks;

    public ShippingCrateBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    protected ItemStackHandler createItemHandler() {
        return new ItemStackHandler(27) {
            @Override
            protected void onContentsChanged(int slot) { setChanged(); }
        };
    }

    @Override
    protected MorkovskEnergyStorage createEnergyStorage() {
        // Unpowered — the crate sells on a timer, not on FE.
        return new MorkovskEnergyStorage(0, 0, 0, this::setChanged);
    }

    @Override
    protected int getDefaultProcessTime() { return SELL_INTERVAL; }

    @Override
    protected void tickServer(Level level, BlockPos pos, BlockState state) {
        progress++;
        maxProgress = SELL_INTERVAL;
        if (progress >= SELL_INTERVAL) {
            progress = 0;
            sellContents(level);
            setChanged();
        }
        // Retry stashed payouts a few times a second — cheap when nothing is pending.
        if (pendingPayout > 0 && level.getGameTime() % 8 == 0) {
            tryPayout(level);
        }
    }

    private void sellContents(Level level) {
        long dayIndex = level.getDayTime() / 24000;
        long total = 0L;
        int stacks = 0;
        for (int i = 0; i < items.getSlots(); i++) {
            ItemStack stack = items.getStackInSlot(i);
            if (stack.isEmpty()) continue;
            total += PriceTable.stackSellPrice(stack, dayIndex);
            stacks++;
            items.setStackInSlot(i, ItemStack.EMPTY);
        }
        if (stacks == 0) return;
        pendingPayout += total;
        pendingStacks += stacks;
        tryPayout(level);
    }

    /** Pays out any stashed earnings once a creditable player can be resolved. */
    private void tryPayout(Level level) {
        if (pendingPayout <= 0 && pendingStacks <= 0) return;
        ServerPlayer player = resolvePlayer(level);
        if (player == null) return;
        long amount = pendingPayout;
        int stacks = pendingStacks;
        pendingPayout = 0;
        pendingStacks = 0;
        setChanged();
        if (amount > 0) {
            FarmingStats.addMoney(player, amount);
            player.displayClientMessage(
                    Component.translatable("economy.craftmorkovsk.sold", amount), true);
            Award.grant(player, "first_sale");
            if (FarmingStats.getMoney(player) >= 10000) {
                Award.grant(player, "rich_farmer");
            }
        }
        if (stacks > 0) {
            FarmingStats.awardXp(player, FarmingStats.XpReason.SELLING, stacks);
        }
    }

    private ServerPlayer resolvePlayer(Level level) {
        if (lastInteracting != null) {
            Player p = level.getPlayerByUUID(lastInteracting);
            if (p instanceof ServerPlayer sp) return sp;
        }
        Player nearest = level.getNearestPlayer(
                worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5,
                8.0, false);
        return nearest instanceof ServerPlayer sp ? sp : null;
    }

    @Override
    public boolean isWorking() { return progress > 0; }

    @Override
    public AbstractContainerMenu createMenu(int windowId, Inventory playerInventory, Player player) {
        lastInteracting = player.getUUID();
        if (!level.isClientSide) tryPayout(level);
        setChanged();
        return new ShippingCrateMenu(EconomyModule.SHIPPING_CRATE_MENU.get(), windowId,
                playerInventory, items, data, ContainerLevelAccess.create(level, worldPosition));
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (lastInteracting != null) tag.putUUID("LastInteracting", lastInteracting);
        tag.putLong("PendingPayout", pendingPayout);
        tag.putInt("PendingStacks", pendingStacks);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        lastInteracting = tag.hasUUID("LastInteracting") ? tag.getUUID("LastInteracting") : null;
        pendingPayout = tag.getLong("PendingPayout");
        pendingStacks = tag.getInt("PendingStacks");
    }
}
