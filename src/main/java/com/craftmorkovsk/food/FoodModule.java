package com.craftmorkovsk.food;

import com.craftmorkovsk.CraftMorkovsk;
import com.craftmorkovsk.data.Award;
import com.craftmorkovsk.data.FarmingStats;
import com.craftmorkovsk.registry.MorkovskRegistries;
import com.craftmorkovsk.registry.MorkovskTabs;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** Cooked/processed food products (~26 items) plus eat-XP and the home_cook advancement.
 *  Machine-made items (raisins, pickles, cream, ...) are plain items here; the machines
 *  that produce them live in the machine package — we only own the items + recipes. */
public final class FoodModule {

    public static RegistryObject<Item> BREAD_MORKOVSK;
    public static RegistryObject<Item> CARROT_CAKE;
    public static RegistryObject<Item> PUMPKIN_PIE_MORKOVSK;
    public static RegistryObject<Item> CORN_BREAD;

    public static RegistryObject<Item> RAISINS;
    public static RegistryObject<Item> DRIED_TOMATO;
    public static RegistryObject<Item> DRIED_HERB;
    public static RegistryObject<Item> CREAM;
    public static RegistryObject<Item> BUTTER;
    public static RegistryObject<Item> PICKLES;
    public static RegistryObject<Item> SAUERKRAUT;
    public static RegistryObject<Item> GRAPE_JUICE;
    public static RegistryObject<Item> BERRY_JAM;

    public static RegistryObject<Item> VEGETABLE_SOUP;
    public static RegistryObject<Item> MORKOV_STEW;
    public static RegistryObject<Item> STUFFED_PEPPERS;
    public static RegistryObject<Item> CABBAGE_ROLLS;
    public static RegistryObject<Item> FARMER_SALAD;
    public static RegistryObject<Item> GRILLED_CORN;
    public static RegistryObject<Item> BAKED_POTATO_DISH;
    public static RegistryObject<Item> TOMATO_SAUCE;
    public static RegistryObject<Item> RATATOUILLE;

    public static RegistryObject<Item> MORKOV_JUICE;
    public static RegistryObject<Item> GOAT_CHEESE;

    public static RegistryObject<Item> SUPREME_SALAD;
    public static RegistryObject<Item> CARROT_OF_POWER;

    private FoodModule() {}

    private static FoodProperties.Builder props(int nutrition, float saturation) {
        return new FoodProperties.Builder().nutrition(nutrition).saturationMod(saturation);
    }

    private static RegistryObject<Item> food(String name, FoodProperties props) {
        RegistryObject<Item> item = MorkovskRegistries.ITEMS.register(name,
                () -> new FoodItem(new Item.Properties().food(props)));
        MorkovskTabs.add(MorkovskTabs.ModTab.FOOD, item::get);
        return item;
    }

    private static RegistryObject<Item> juice(String name, FoodProperties props) {
        RegistryObject<Item> item = MorkovskRegistries.ITEMS.register(name,
                () -> new JuiceItem(new Item.Properties().food(props)));
        MorkovskTabs.add(MorkovskTabs.ModTab.FOOD, item::get);
        return item;
    }

    public static void init() {
        // Baked goods (flour comes from the machine package's mill).
        BREAD_MORKOVSK = food("bread_morkovsk", props(6, 0.7f).build());
        CARROT_CAKE = food("carrot_cake", props(8, 0.8f).build());
        PUMPKIN_PIE_MORKOVSK = food("pumpkin_pie_morkovsk", props(8, 0.5f).build());
        CORN_BREAD = food("corn_bread", props(6, 0.6f).build());

        // Machine-made products: dryer / fermenter / separator outputs.
        RAISINS = food("raisins", props(3, 0.4f).fast().build());
        DRIED_TOMATO = food("dried_tomato", props(3, 0.4f).fast().build());
        DRIED_HERB = food("dried_herb", props(1, 0.2f).build());
        CREAM = food("cream", props(2, 0.4f).build());
        BUTTER = food("butter", props(3, 0.5f).build());
        PICKLES = food("pickles", props(4, 0.5f).build());
        SAUERKRAUT = food("sauerkraut", props(4, 0.5f).build());
        GRAPE_JUICE = juice("grape_juice", props(4, 0.4f).alwaysEat().build());
        BERRY_JAM = food("berry_jam", props(4, 0.5f).build());

        // Meals.
        VEGETABLE_SOUP = food("vegetable_soup", props(7, 0.8f).build());
        MORKOV_STEW = food("morkov_stew", props(9, 0.9f)
                .effect(new MobEffectInstance(MobEffects.REGENERATION, 100, 0), 1.0f).build());
        STUFFED_PEPPERS = food("stuffed_peppers", props(8, 0.8f).build());
        CABBAGE_ROLLS = food("cabbage_rolls", props(8, 0.8f).build());
        FARMER_SALAD = food("farmer_salad", props(6, 0.7f).build());
        GRILLED_CORN = food("grilled_corn", props(5, 0.6f).build());
        BAKED_POTATO_DISH = food("baked_potato_dish", props(8, 0.8f).build());
        TOMATO_SAUCE = food("tomato_sauce", props(3, 0.4f).build());
        RATATOUILLE = food("ratatouille", props(9, 0.9f).build());

        // Extras.
        MORKOV_JUICE = juice("morkov_juice", props(4, 0.5f).alwaysEat().build());
        GOAT_CHEESE = food("goat_cheese", props(5, 0.6f).build());

        // Legendary.
        SUPREME_SALAD = food("supreme_salad", props(20, 1.6f).alwaysEat()
                .effect(new MobEffectInstance(MobEffects.REGENERATION, 200, 1), 1.0f)
                .effect(new MobEffectInstance(MobEffects.ABSORPTION, 1200, 0), 1.0f)
                .build());
        CARROT_OF_POWER = food("carrot_of_power", props(10, 1.0f).alwaysEat()
                .effect(new MobEffectInstance(MobEffects.REGENERATION, 200, 1), 1.0f)
                .effect(new MobEffectInstance(MobEffects.ABSORPTION, 600, 1), 1.0f)
                .build());
    }

    @Mod.EventBusSubscriber(modid = CraftMorkovsk.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static final class Events {

        /** Distinct mod-food ids each player has crafted this session — feeds home_cook. */
        private static final Map<UUID, Set<ResourceLocation>> CRAFTED_FOODS = new HashMap<>();

        private Events() {}

        /** Eating a Craft Morkovsk food awards a tiny bit of processing XP. */
        @SubscribeEvent
        public static void onFoodEaten(LivingEntityUseItemEvent.Finish event) {
            if (!(event.getItem().getItem() instanceof FoodItem)) return;
            if (!(event.getEntity() instanceof ServerPlayer player)) return;
            FarmingStats.awardXp(player, FarmingStats.XpReason.PROCESSING, 1);
        }

        /** home_cook: craft any 3 distinct mod food items. */
        @SubscribeEvent
        public static void onFoodCrafted(PlayerEvent.ItemCraftedEvent event) {
            Item result = event.getCrafting().getItem();
            if (!(result instanceof FoodItem)) return;
            if (!(event.getEntity() instanceof ServerPlayer player)) return;
            Set<ResourceLocation> crafted = CRAFTED_FOODS.computeIfAbsent(
                    player.getUUID(), id -> new HashSet<>());
            crafted.add(ForgeRegistries.ITEMS.getKey(result));
            if (crafted.size() >= 3) Award.grant(player, "home_cook");
        }
    }
}
