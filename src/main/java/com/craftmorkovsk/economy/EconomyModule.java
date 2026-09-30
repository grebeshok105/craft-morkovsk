package com.craftmorkovsk.economy;

import com.craftmorkovsk.network.MorkovskNet;
import com.craftmorkovsk.registry.MorkovskRegistries;
import com.craftmorkovsk.registry.MorkovskTabs;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.RegistryObject;

/** Economy: the shipping crate (sell produce for morkoins) and the Morkovsk shop
 *  (buy seeds/fertilizer/parts with morkoins), plus machine/tractor parts and
 *  the soil test kit. Currency is the {@link com.craftmorkovsk.data.FarmingStats}
 *  money capability, not an item. */
public final class EconomyModule {

    public static RegistryObject<Block> SHIPPING_CRATE;
    public static RegistryObject<Block> MORKOVSK_SHOP;

    public static RegistryObject<BlockEntityType<ShippingCrateBlockEntity>> SHIPPING_CRATE_BE;

    public static RegistryObject<MenuType<ShippingCrateMenu>> SHIPPING_CRATE_MENU;
    public static RegistryObject<MenuType<ShopMenu>> SHOP_MENU;

    public static RegistryObject<Item> ENGINE_PART;
    public static RegistryObject<Item> GEARBOX;
    public static RegistryObject<Item> SPRINKLER_HEAD;
    public static RegistryObject<Item> TRACTOR_WHEEL;
    public static RegistryObject<Item> SOIL_TEST_KIT;

    private EconomyModule() {}

    private static RegistryObject<Item> blockItem(RegistryObject<? extends Block> block,
                                                MorkovskTabs.ModTab tab) {
        RegistryObject<Item> item = MorkovskRegistries.ITEMS.register(block.getId().getPath(),
                () -> new BlockItem(block.get(), new Item.Properties()));
        MorkovskTabs.add(tab, item::get);
        return item;
    }

    private static RegistryObject<Item> part(String name) {
        RegistryObject<Item> item = MorkovskRegistries.ITEMS.register(name,
                () -> new Item(new Item.Properties()));
        MorkovskTabs.add(MorkovskTabs.ModTab.MACHINES, item::get);
        return item;
    }

    public static void init() {
        SHIPPING_CRATE = MorkovskRegistries.BLOCKS.register("shipping_crate",
                () -> new ShippingCrateBlock(BlockBehaviour.Properties.of().strength(2.0f)
                        .sound(SoundType.WOOD), () -> SHIPPING_CRATE_BE.get()));
        blockItem(SHIPPING_CRATE, MorkovskTabs.ModTab.STORAGE);

        MORKOVSK_SHOP = MorkovskRegistries.BLOCKS.register("morkovsk_shop",
                () -> new MorkovskShopBlock(BlockBehaviour.Properties.of().strength(2.0f)
                        .sound(SoundType.WOOD)));
        blockItem(MORKOVSK_SHOP, MorkovskTabs.ModTab.STORAGE);

        SHIPPING_CRATE_BE = MorkovskRegistries.BLOCK_ENTITIES.register("shipping_crate",
                () -> BlockEntityType.Builder.of(
                        (pos, state) -> new ShippingCrateBlockEntity(SHIPPING_CRATE_BE.get(), pos, state),
                        SHIPPING_CRATE.get()).build(null));

        SHIPPING_CRATE_MENU = MorkovskRegistries.MENUS.register("shipping_crate",
                () -> IForgeMenuType.create((id, inv, buf) ->
                        new ShippingCrateMenu(SHIPPING_CRATE_MENU.get(), id, inv, buf)));
        SHOP_MENU = MorkovskRegistries.MENUS.register("morkovsk_shop",
                () -> IForgeMenuType.create((id, inv, buf) ->
                        new ShopMenu(SHOP_MENU.get(), id, inv, buf)));

        ENGINE_PART = part("engine_part");
        GEARBOX = part("gearbox");
        SPRINKLER_HEAD = part("sprinkler_head");
        TRACTOR_WHEEL = part("tractor_wheel");

        SOIL_TEST_KIT = MorkovskRegistries.ITEMS.register("soil_test_kit",
                () -> new SoilTestKitItem(new Item.Properties().stacksTo(1)));
        MorkovskTabs.add(MorkovskTabs.ModTab.MISC, SOIL_TEST_KIT::get);

        MorkovskNet.registerC2S(BuyPacket.class, BuyPacket::handle,
                BuyPacket::encode, BuyPacket::decode);
    }
}
