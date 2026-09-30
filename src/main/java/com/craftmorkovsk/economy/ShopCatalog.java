package com.craftmorkovsk.economy;

import com.craftmorkovsk.crop.CropCatalog;
import com.craftmorkovsk.crop.CropDef;
import com.craftmorkovsk.fertilizer.FertilizerModule;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/** The Morkovsk shop's hardcoded offer list: every crop's seeds, all fertilizers,
 *  machine/tractor parts, and utility items. Order is fixed so offer indices stay
 *  stable between the client screen and the server's BuyPacket validation. */
public final class ShopCatalog {

    /** Minimum farming level required to see/buy secret seeds. */
    public static final int SECRET_LEVEL = 3;

    /** A purchasable offer. stack is supplied lazily so registration order doesn't matter. */
    public record ShopOffer(Supplier<ItemStack> stack, int price, boolean secret) {}

    private static final List<ShopOffer> OFFERS = new ArrayList<>();
    private static boolean built;

    private ShopCatalog() {}

    public static synchronized List<ShopOffer> offers() {
        if (!built) build();
        return OFFERS;
    }

    public static ShopOffer offer(int index) {
        List<ShopOffer> all = offers();
        return index >= 0 && index < all.size() ? all.get(index) : null;
    }

    private static void build() {
        built = true;
        // Seeds: price = def.price / 2 rounded up (min 2), rares priced steeply by hand.
        Map<String, Integer> rarePrices = Map.of(
                "golden_carrot_plant", 400,
                "giant_morkov", 150,
                "morkovsk_supreme", 900,
                "the_carrot", 9999);
        for (CropDef def : CropCatalog.ALL) {
            int price = rarePrices.getOrDefault(def.id, Math.max(2, (def.price + 1) / 2));
            OFFERS.add(new ShopOffer(() -> new ItemStack(def.seeds.get()), price, def.secret));
        }
        // Fertilizers.
        OFFERS.add(new ShopOffer(() -> new ItemStack(FertilizerModule.COMPOST.get()), 5, false));
        OFFERS.add(new ShopOffer(() -> new ItemStack(FertilizerModule.MANURE_FERTILIZER.get()), 8, false));
        OFFERS.add(new ShopOffer(() -> new ItemStack(FertilizerModule.MINERAL_FERTILIZER.get()), 12, false));
        OFFERS.add(new ShopOffer(() -> new ItemStack(FertilizerModule.MORKOVSK_FERTILIZER.get()), 40, false));
        // Machine/tractor parts (also craftable — see recipes/*_part*.json).
        OFFERS.add(new ShopOffer(() -> new ItemStack(EconomyModule.ENGINE_PART.get()), 250, false));
        OFFERS.add(new ShopOffer(() -> new ItemStack(EconomyModule.GEARBOX.get()), 120, false));
        OFFERS.add(new ShopOffer(() -> new ItemStack(EconomyModule.SPRINKLER_HEAD.get()), 80, false));
        OFFERS.add(new ShopOffer(() -> new ItemStack(EconomyModule.TRACTOR_WHEEL.get()), 60, false));
        // Utility.
        OFFERS.add(new ShopOffer(() -> new ItemStack(EconomyModule.SOIL_TEST_KIT.get()), 150, false));
    }
}
