package com.craftmorkovsk.storage;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

/** Silo storage: 18 bulk slots capped at 256 items each, accepting only
 *  {@code craftmorkovsk:silo_storable} items (produce, seeds, grains).
 *
 *  Custom NBT (de)serialization is required: vanilla ItemStack.save() writes Count
 *  as a byte, so counts above 127 would corrupt on chunk save. We write the slot,
 *  item id and count as ints ourselves. */
public class SiloItemHandler extends ItemStackHandler {

    public static final int SLOTS = 18;
    public static final int SLOT_CAP = 256;

    private final StorageBlockEntity owner;

    public SiloItemHandler(StorageBlockEntity owner) {
        super(SLOTS);
        this.owner = owner;
    }

    @Override
    protected void onContentsChanged(int slot) {
        owner.setChanged();
    }

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        return stack.is(StorageKind.SILO_STORABLE);
    }

    @Override
    public int getSlotLimit(int slot) {
        return SLOT_CAP;
    }

    @Override
    protected int getStackLimit(int slot, @NotNull ItemStack stack) {
        return SLOT_CAP;
    }

    @Override
    public CompoundTag serializeNBT() {
        ListTag list = new ListTag();
        for (int i = 0; i < getSlots(); i++) {
            ItemStack stack = getStackInSlot(i);
            if (stack.isEmpty()) continue;
            CompoundTag itemTag = new CompoundTag();
            itemTag.putInt("Slot", i);
            ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());
            if (id == null) continue;
            itemTag.putString("id", id.toString());
            itemTag.putInt("Count", stack.getCount());
            if (stack.hasTag()) itemTag.put("tag", stack.getTag().copy());
            list.add(itemTag);
        }
        CompoundTag nbt = new CompoundTag();
        nbt.put("Items", list);
        nbt.putInt("Size", getSlots());
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        ListTag list = nbt.getList("Items", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag itemTag = list.getCompound(i);
            int slot = itemTag.getInt("Slot");
            if (slot < 0 || slot >= getSlots()) continue;
            Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemTag.getString("id")));
            if (item == null || item == Items.AIR) continue;
            ItemStack stack = new ItemStack(item, itemTag.getInt("Count"));
            if (itemTag.contains("tag")) stack.setTag(itemTag.getCompound("tag").copy());
            setStackInSlot(slot, stack);
        }
        onLoad();
    }
}
