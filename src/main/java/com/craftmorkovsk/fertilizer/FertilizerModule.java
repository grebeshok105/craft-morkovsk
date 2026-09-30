package com.craftmorkovsk.fertilizer;

import com.craftmorkovsk.registry.MorkovskRegistries;
import com.craftmorkovsk.registry.MorkovskTabs;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.registries.RegistryObject;

/** Fertilizer items (4 tiers) + the compost bin producing basic compost. */
public final class FertilizerModule {

    public static RegistryObject<Item> COMPOST;
    public static RegistryObject<Item> MANURE_FERTILIZER;
    public static RegistryObject<Item> MINERAL_FERTILIZER;
    public static RegistryObject<Item> MORKOVSK_FERTILIZER;
    public static RegistryObject<Block> COMPOST_BIN;

    private FertilizerModule() {}

    private static RegistryObject<Item> fert(String name, FertilizerItem.Potency potency) {
        RegistryObject<Item> item = MorkovskRegistries.ITEMS.register(name,
                () -> new FertilizerItem(new Item.Properties(), potency));
        MorkovskTabs.add(MorkovskTabs.ModTab.MISC, item::get);
        return item;
    }

    public static void init() {
        COMPOST = fert("compost", FertilizerItem.Potency.COMPOST);
        MANURE_FERTILIZER = fert("manure_fertilizer", FertilizerItem.Potency.MANURE);
        MINERAL_FERTILIZER = fert("mineral_fertilizer", FertilizerItem.Potency.MINERAL);
        MORKOVSK_FERTILIZER = fert("morkovsk_fertilizer", FertilizerItem.Potency.PREMIUM);

        COMPOST_BIN = MorkovskRegistries.BLOCKS.register("compost_bin",
                () -> new CompostBinBlock(BlockBehaviour.Properties.of().strength(0.8f)
                        .sound(SoundType.WOOD).randomTicks(), () -> COMPOST.get()));
        RegistryObject<Item> binItem = MorkovskRegistries.ITEMS.register("compost_bin",
                () -> new BlockItem(COMPOST_BIN.get(), new Item.Properties()));
        MorkovskTabs.add(MorkovskTabs.ModTab.MISC, binItem::get);
    }
}
