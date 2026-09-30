package com.craftmorkovsk.economy;

import com.craftmorkovsk.config.MorkovskConfig;
import com.craftmorkovsk.crop.CropCatalog;
import com.craftmorkovsk.crop.CropDef;
import com.craftmorkovsk.data.Quality;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/** Central pricing used by the shipping crate (server-side selling) and by client
 *  screens (estimated totals). The daily fluctuation factor is deterministic per
 *  (day, item) so client estimates match what the server will actually pay. */
public final class PriceTable {

    private static Map<Item, CropDef> produceIndex;
    private static Map<Item, CropDef> seedIndex;

    private PriceTable() {}

    private static synchronized void ensureIndex() {
        if (produceIndex != null) return;
        produceIndex = new HashMap<>();
        seedIndex = new HashMap<>();
        for (CropDef def : CropCatalog.ALL) {
            if (def.produce != null && def.produce.isPresent()) produceIndex.put(def.produce.get(), def);
            if (def.seeds != null && def.seeds.isPresent()) seedIndex.put(def.seeds.get(), def);
        }
    }

    /** Base per-unit sell price in morkoins before quality and fluctuation. */
    public static int basePrice(ItemStack stack) {
        ensureIndex();
        Item item = stack.getItem();
        CropDef produce = produceIndex.get(item);
        if (produce != null) return produce.price > 0 ? produce.price : fallback(stack);
        CropDef seeds = seedIndex.get(item);
        if (seeds != null) return Math.max(1, seeds.price / 4);
        return fallback(stack);
    }

    private static int fallback(ItemStack stack) {
        FoodProperties food = stack.getItem().getFoodProperties(stack, null);
        if (food != null && food.getNutrition() > 0) {
            return Math.max(1, food.getNutrition() * 2);
        }
        return MorkovskConfig.BASE_CROP_PRICE.get();
    }

    /** Deterministic per-day per-item price factor in 1 +/- PRICE_FLUCTUATION. */
    public static double dailyFactor(long dayIndex, Item item) {
        double amp = MorkovskConfig.PRICE_FLUCTUATION.get();
        if (amp <= 0.0) return 1.0;
        ResourceLocation key = ForgeRegistries.ITEMS.getKey(item);
        long seed = dayIndex * 0x9E3779B97F4A7C15L + (key == null ? 0L : key.hashCode());
        return 1.0 + (new Random(seed).nextDouble() * 2.0 - 1.0) * amp;
    }

    /** Sell price of one item of this stack's kind on the given day (quality applied). */
    public static int unitSellPrice(ItemStack stack, long dayIndex) {
        double price = basePrice(stack)
                * Quality.of(stack).priceMultiplier
                * dailyFactor(dayIndex, stack.getItem());
        return Math.max(1, (int) Math.round(price));
    }

    /** Sell price of the whole stack (count included). */
    public static long stackSellPrice(ItemStack stack, long dayIndex) {
        if (stack.isEmpty()) return 0L;
        return (long) unitSellPrice(stack, dayIndex) * stack.getCount();
    }

    /** Total value of all stacks in an item handler on the given day. */
    public static long estimate(IItemHandler items, long dayIndex) {
        long total = 0L;
        for (int i = 0; i < items.getSlots(); i++) {
            total += stackSellPrice(items.getStackInSlot(i), dayIndex);
        }
        return total;
    }
}
