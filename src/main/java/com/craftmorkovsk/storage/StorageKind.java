package com.craftmorkovsk.storage;

import com.craftmorkovsk.CraftMorkovsk;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemNameBlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Predicate;

/** Per-block storage behaviour: slot count, GUI rows, item filters and quirks.
 *  Shared by {@link StorageBlockEntity} (handler sizing/validation) and
 *  {@link StorageMenu} (grid layout + insertion hooks). */
public enum StorageKind {
    CROP_CRATE(27, 3, 64, false, stack -> true, false),
    LARGE_CRATE(54, 6, 64, false, stack -> true, false),
    SEED_VAULT(27, 3, 64, false, StorageKind::isSeedLike, true),
    SILO(18, 2, 256, true, StorageKind::isSiloStorable, true);

    /** Grain/seed/produce allowed in the silo (see data/craftmorkovsk/tags/items/silo_storable.json). */
    public static final TagKey<Item> SILO_STORABLE =
            ItemTags.create(new ResourceLocation(CraftMorkovsk.MOD_ID, "silo_storable"));

    public final int slots;
    public final int rows;
    /** Per-slot count cap. Values >64 are only meaningful when {@link #oversized} is set. */
    public final int slotLimit;
    /** True when slots may hold more than the item's own max stack size (silo bulk storage). */
    public final boolean oversized;
    public final Predicate<ItemStack> filter;
    /** Grants the "stockpiler" advancement when a player inserts an item through the menu. */
    public final boolean grantOnInsert;

    StorageKind(int slots, int rows, int slotLimit, boolean oversized,
                Predicate<ItemStack> filter, boolean grantOnInsert) {
        this.slots = slots;
        this.rows = rows;
        this.slotLimit = slotLimit;
        this.oversized = oversized;
        this.filter = filter;
        this.grantOnInsert = grantOnInsert;
    }

    /** Method ref so the enum constant doesn't forward-reference the field below. */
    static boolean isSiloStorable(ItemStack stack) {
        return stack.is(SILO_STORABLE);
    }

    /** Seed-vault filter: anything whose id ends in _seeds, plus any block-item seed
     *  (ItemNameBlockItem covers mod crops regardless of naming). */
    static boolean isSeedLike(ItemStack stack) {
        if (stack.getItem() instanceof ItemNameBlockItem) return true;
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());
        return id != null && id.getPath().endsWith("_seeds");
    }
}
