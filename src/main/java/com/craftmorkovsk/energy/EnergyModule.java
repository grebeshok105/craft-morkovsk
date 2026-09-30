package com.craftmorkovsk.energy;

import com.craftmorkovsk.machine.framework.FuelScreen;
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

/** Energy infrastructure: cables, small generator, biomass generator, battery.
 *  Machines built by other packages consume FE through {@link EnergyNetwork} via cables. */
public final class EnergyModule {

    public static RegistryObject<Block> ENERGY_CABLE;
    public static RegistryObject<Block> SMALL_GENERATOR;
    public static RegistryObject<Block> BIOMASS_GENERATOR;
    public static RegistryObject<Block> BATTERY;

    public static RegistryObject<BlockEntityType<GeneratorBlockEntity>> GENERATOR_BE;
    public static RegistryObject<BlockEntityType<BiomassGeneratorBlockEntity>> BIOMASS_BE;
    public static RegistryObject<BlockEntityType<BatteryBlockEntity>> BATTERY_BE;

    public static RegistryObject<MenuType<FuelMachineMenu>> GENERATOR_MENU;
    public static RegistryObject<MenuType<FuelMachineMenu>> BIOMASS_MENU;

    private EnergyModule() {}

    private static RegistryObject<Item> blockItem(RegistryObject<? extends Block> block, MorkovskTabs.ModTab tab) {
        RegistryObject<Item> item = MorkovskRegistries.ITEMS.register(block.getId().getPath(),
                () -> new BlockItem(block.get(), new Item.Properties()));
        MorkovskTabs.add(tab, item::get);
        return item;
    }

    public static void init() {
        ENERGY_CABLE = MorkovskRegistries.BLOCKS.register("energy_cable",
                () -> new CableBlock(BlockBehaviour.Properties.of().strength(0.5f).sound(SoundType.METAL)
                        .noOcclusion()));
        blockItem(ENERGY_CABLE, MorkovskTabs.ModTab.MACHINES);

        SMALL_GENERATOR = MorkovskRegistries.BLOCKS.register("small_generator",
                () -> new SmallGeneratorBlock(BlockBehaviour.Properties.of().strength(2.5f)
                        .sound(SoundType.METAL), () -> GENERATOR_BE.get()));
        blockItem(SMALL_GENERATOR, MorkovskTabs.ModTab.MACHINES);

        BIOMASS_GENERATOR = MorkovskRegistries.BLOCKS.register("biomass_generator",
                () -> new SmallGeneratorBlock(BlockBehaviour.Properties.of().strength(2.5f)
                        .sound(SoundType.METAL), () -> BIOMASS_BE.get()));
        blockItem(BIOMASS_GENERATOR, MorkovskTabs.ModTab.MACHINES);

        BATTERY = MorkovskRegistries.BLOCKS.register("battery",
                () -> new BatteryBlock(BlockBehaviour.Properties.of().strength(2.5f)
                        .sound(SoundType.METAL), () -> BATTERY_BE.get()));
        blockItem(BATTERY, MorkovskTabs.ModTab.MACHINES);

        GENERATOR_BE = MorkovskRegistries.BLOCK_ENTITIES.register("small_generator",
                () -> BlockEntityType.Builder.of(
                        (pos, state) -> new GeneratorBlockEntity(GENERATOR_BE.get(), pos, state),
                        SMALL_GENERATOR.get()).build(null));
        BIOMASS_BE = MorkovskRegistries.BLOCK_ENTITIES.register("biomass_generator",
                () -> BlockEntityType.Builder.of(
                        (pos, state) -> new BiomassGeneratorBlockEntity(BIOMASS_BE.get(), pos, state),
                        BIOMASS_GENERATOR.get()).build(null));
        BATTERY_BE = MorkovskRegistries.BLOCK_ENTITIES.register("battery",
                () -> BlockEntityType.Builder.of(
                        (pos, state) -> new BatteryBlockEntity(BATTERY_BE.get(), pos, state),
                        BATTERY.get()).build(null));

        GENERATOR_MENU = MorkovskRegistries.MENUS.register("small_generator",
                () -> IForgeMenuType.create((id, inv, buf) ->
                        new FuelMachineMenu(GENERATOR_MENU.get(), id, inv, buf)));
        BIOMASS_MENU = MorkovskRegistries.MENUS.register("biomass_generator",
                () -> IForgeMenuType.create((id, inv, buf) ->
                        new FuelMachineMenu(BIOMASS_MENU.get(), id, inv, buf)));
    }
}
