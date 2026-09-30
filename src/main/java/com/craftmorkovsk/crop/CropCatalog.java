package com.craftmorkovsk.crop;

import com.craftmorkovsk.registry.MorkovskRegistries;
import com.craftmorkovsk.registry.MorkovskTabs;
import com.craftmorkovsk.soil.SoilTier;
import com.craftmorkovsk.item.QualityProduceItem;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemNameBlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** The full crop roster. Every definition becomes a crop block + seeds + produce item. */
public final class CropCatalog {

    public static final List<CropDef> ALL = new ArrayList<>();
    public static final Map<String, CropDef> BY_ID = new LinkedHashMap<>();

    private CropCatalog() {}

    private static CropDef add(CropDef def) {
        ALL.add(def);
        BY_ID.put(def.id, def);
        return def;
    }

    static {
        // Carrot varieties — the point of the whole mod.
        add(CropDef.of("morkov").water(WaterNeed.MEDIUM).speed(1.1f).yieldRange(2, 4).food(3, 0.5f).price(9).build());
        add(CropDef.of("carrot_red").water(WaterNeed.MEDIUM).yieldRange(2, 4).food(3, 0.45f).price(8).build());
        add(CropDef.of("carrot_purple").water(WaterNeed.MEDIUM).speed(0.9f).yieldRange(2, 4).food(4, 0.55f).price(14).xp(1).build());
        // Staples.
        add(CropDef.of("potato").water(WaterNeed.MEDIUM).yieldRange(2, 5).food(1, 0.3f).price(7).build());
        add(CropDef.of("tomato").water(WaterNeed.HIGH).speed(0.9f).yieldRange(2, 4).food(3, 0.4f)
                .soils(SoilTier.NORMAL, SoilTier.RICH, SoilTier.FERTILIZED, SoilTier.COMPOST).price(12).build());
        add(CropDef.of("corn").water(WaterNeed.HIGH).speed(0.7f).yieldRange(2, 4).headroom().food(2, 0.35f).price(15).xp(1).build());
        add(CropDef.of("onion").water(WaterNeed.MEDIUM).yieldRange(2, 4).food(2, 0.3f).price(10).build());
        add(CropDef.of("garlic").water(WaterNeed.LOW).speed(0.8f).yieldRange(2, 5).food(1, 0.2f)
                .soils(SoilTier.NORMAL, SoilTier.COMPOST, SoilTier.FERTILIZED).price(12).build());
        add(CropDef.of("cabbage").water(WaterNeed.MEDIUM).speed(0.8f).yieldRange(1, 2).food(3, 0.4f).price(14).build());
        add(CropDef.of("lettuce").water(WaterNeed.HIGH).speed(1.2f).yieldRange(1, 3).food(2, 0.3f).price(9).build());
        add(CropDef.of("cucumber").water(WaterNeed.HIGH).speed(1.0f).yieldRange(2, 4).food(2, 0.3f).price(10).build());
        // Berries & fruit.
        add(CropDef.of("strawberry").water(WaterNeed.HIGH).speed(0.8f).yieldRange(2, 5).food(2, 0.4f).price(16).xp(1).build());
        add(CropDef.of("blueberry").water(WaterNeed.MEDIUM).speed(0.8f).yieldRange(2, 5).food(2, 0.4f)
                .soils(SoilTier.NORMAL, SoilTier.RICH, SoilTier.COMPOST).price(16).xp(1).build());
        add(CropDef.of("grape").water(WaterNeed.MEDIUM).speed(0.6f).yieldRange(2, 4).food(3, 0.4f).price(18).xp(2).build());
        // Grains & oil crops.
        add(CropDef.of("rice").water(WaterNeed.HIGH).speed(0.8f).yieldRange(2, 5)
                .soils(SoilTier.WET).price(11).build()); // rice demands wet soil — irrigation!
        add(CropDef.of("wheat_spelt").water(WaterNeed.LOW).speed(0.9f).yieldRange(2, 4).price(8).build());
        add(CropDef.of("wheat_rye").water(WaterNeed.LOW).speed(1.0f).yieldRange(2, 4).price(9).build());
        add(CropDef.of("pumpkin_morkovsk").water(WaterNeed.MEDIUM).speed(0.5f).yieldRange(1, 2).headroom().price(20).xp(2).build());
        add(CropDef.of("melon_morkovsk").water(WaterNeed.HIGH).speed(0.5f).yieldRange(3, 6).food(2, 0.3f).price(10).xp(1).build());
        add(CropDef.of("sunflower").water(WaterNeed.LOW).speed(0.9f).yieldRange(2, 4).price(9).build());
        // Peppers.
        add(CropDef.of("pepper_chili").water(WaterNeed.MEDIUM).speed(0.9f).yieldRange(2, 4).food(2, 0.3f).price(13).build());
        add(CropDef.of("pepper_bell").water(WaterNeed.MEDIUM).yieldRange(2, 4).food(3, 0.4f).price(11).build());
        // Herbs — non-food produce for recipes.
        add(CropDef.of("herb_basil").water(WaterNeed.MEDIUM).speed(1.2f).yieldRange(1, 3).price(8).build());
        add(CropDef.of("herb_mint").water(WaterNeed.MEDIUM).speed(1.2f).yieldRange(1, 3).price(8).build());
        add(CropDef.of("herb_thyme").water(WaterNeed.LOW).speed(1.2f).yieldRange(1, 3).price(8).build());
        // Rare & ridiculous.
        add(CropDef.of("golden_carrot_plant").water(WaterNeed.MEDIUM).speed(0.25f).yieldRange(1, 2)
                .soils(SoilTier.FERTILIZED, SoilTier.COMPOST).price(120).xp(8).build());
        add(CropDef.of("giant_morkov").water(WaterNeed.HIGH).speed(0.35f).yieldRange(3, 8).headroom()
                .food(6, 0.8f).price(45).xp(5).build());
        add(CropDef.of("morkovsk_supreme").water(WaterNeed.MEDIUM).speed(0.18f).yieldRange(1, 2)
                .soils(SoilTier.FERTILIZED).food(8, 1.2f).price(300).xp(15).build());
        add(CropDef.of("the_carrot").water(WaterNeed.MEDIUM).speed(0.02f).yieldRange(1, 1)
                .soils(SoilTier.FERTILIZED).food(10, 2.0f).price(5000).xp(50).secret().build());
    }

    private static Item.Properties foodProps(CropDef def) {
        Item.Properties props = new Item.Properties();
        if (def.nutrition > 0) {
            props.food(new FoodProperties.Builder()
                    .nutrition(def.nutrition)
                    .saturationMod(def.saturation)
                    .build());
        }
        return props;
    }

    private static void register(CropDef def) {
        def.produce = MorkovskRegistries.ITEMS.register(def.id,
                () -> new QualityProduceItem(foodProps(def)));
        def.block = MorkovskRegistries.BLOCKS.register(def.id + "_crop",
                () -> new MorkovskCropBlock(cropProps(), def,
                        () -> def.produce.get(), () -> def.seeds.get()));
        def.seeds = MorkovskRegistries.ITEMS.register(def.id + "_seeds",
                () -> new ItemNameBlockItem(def.block.get(), new Item.Properties()));
        MorkovskTabs.add(MorkovskTabs.ModTab.CROPS, () -> def.produce.get());
        MorkovskTabs.add(MorkovskTabs.ModTab.CROPS, () -> def.seeds.get());
    }

    private static BlockBehaviour.Properties cropProps() {
        return BlockBehaviour.Properties.of()
                .noCollission()
                .randomTicks()
                .instabreak()
                .sound(SoundType.CROP);
    }

    public static void init() {
        for (CropDef def : ALL) register(def);
    }
}
