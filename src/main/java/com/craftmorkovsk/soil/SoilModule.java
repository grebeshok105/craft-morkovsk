package com.craftmorkovsk.soil;

import com.craftmorkovsk.registry.MorkovskRegistries;
import com.craftmorkovsk.registry.MorkovskTabs;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.registries.RegistryObject;

import java.util.EnumMap;
import java.util.Map;

/** Registers all soil tiers. Vanilla farmland still works (treated as NORMAL by SoilAPI). */
public final class SoilModule {

    public static final Map<SoilTier, RegistryObject<MorkovskSoilBlock>> BLOCKS_BY_TIER = new EnumMap<>(SoilTier.class);

    private SoilModule() {}

    private static BlockBehaviour.Properties props() {
        return BlockBehaviour.Properties.of()
                .strength(0.6f)
                .sound(SoundType.GRAVEL)
                .randomTicks();
    }

    private static void soil(SoilTier tier) {
        RegistryObject<MorkovskSoilBlock> block = MorkovskRegistries.BLOCKS.register(
                tier.blockName, () -> new MorkovskSoilBlock(props(), tier));
        RegistryObject<Item> item = MorkovskRegistries.ITEMS.register(
                tier.blockName, () -> new BlockItem(block.get(), new Item.Properties()));
        BLOCKS_BY_TIER.put(tier, block);
        MorkovskTabs.add(MorkovskTabs.ModTab.MISC, item::get);
    }

    public static void init() {
        for (SoilTier tier : SoilTier.values()) soil(tier);
    }
}
