package com.craftmorkovsk.machine;

import com.craftmorkovsk.CraftMorkovsk;
import com.craftmorkovsk.data.Award;
import com.craftmorkovsk.data.FarmingStats;
import com.craftmorkovsk.registry.MorkovskRegistries;
import com.craftmorkovsk.registry.MorkovskTabs;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.RegistryObject;

/** The powered processing fleet: 9 FE-consuming machines plus the items only this
 *  package owns (flour, plant oil, produce crate). All blocks open a MachineMenu. */
public final class MachineModule {

    public static RegistryObject<Block> MILL;
    public static RegistryObject<Block> OIL_PRESS;
    public static RegistryObject<Block> SEPARATOR;
    public static RegistryObject<Block> DRYER;
    public static RegistryObject<Block> FERMENTER;
    public static RegistryObject<Block> SEED_EXTRACTOR;
    public static RegistryObject<Block> AUTO_HARVESTER;
    public static RegistryObject<Block> FERTILIZER_MIXER;
    public static RegistryObject<Block> PACKING_STATION;

    public static RegistryObject<BlockEntityType<ProcessorBlockEntity>> MILL_BE;
    public static RegistryObject<BlockEntityType<ProcessorBlockEntity>> OIL_PRESS_BE;
    public static RegistryObject<BlockEntityType<SeparatorBlockEntity>> SEPARATOR_BE;
    public static RegistryObject<BlockEntityType<ProcessorBlockEntity>> DRYER_BE;
    public static RegistryObject<BlockEntityType<ProcessorBlockEntity>> FERMENTER_BE;
    public static RegistryObject<BlockEntityType<SeedExtractorBlockEntity>> SEED_EXTRACTOR_BE;
    public static RegistryObject<BlockEntityType<AutoHarvesterBlockEntity>> AUTO_HARVESTER_BE;
    public static RegistryObject<BlockEntityType<FertilizerMixerBlockEntity>> FERTILIZER_MIXER_BE;
    public static RegistryObject<BlockEntityType<PackingStationBlockEntity>> PACKING_STATION_BE;

    public static RegistryObject<MenuType<ProcessorMenu>> MILL_MENU;
    public static RegistryObject<MenuType<ProcessorMenu>> OIL_PRESS_MENU;
    public static RegistryObject<MenuType<SeparatorMenu>> SEPARATOR_MENU;
    public static RegistryObject<MenuType<ProcessorMenu>> DRYER_MENU;
    public static RegistryObject<MenuType<ProcessorMenu>> FERMENTER_MENU;
    public static RegistryObject<MenuType<ProcessorMenu>> SEED_EXTRACTOR_MENU;
    public static RegistryObject<MenuType<HarvesterMenu>> AUTO_HARVESTER_MENU;
    public static RegistryObject<MenuType<MixerMenu>> FERTILIZER_MIXER_MENU;
    public static RegistryObject<MenuType<ProcessorMenu>> PACKING_STATION_MENU;

    public static RegistryObject<Item> FLOUR;
    public static RegistryObject<Item> PLANT_OIL;
    public static RegistryObject<Item> PRODUCE_CRATE;

    private MachineModule() {}

    private static RegistryObject<Block> machine(String name,
                                                 java.util.function.Supplier<BlockEntityType<?>> beType) {
        RegistryObject<Block> block = MorkovskRegistries.BLOCKS.register(name,
                () -> new MachineBlock(BlockBehaviour.Properties.of()
                        .strength(2.5f).sound(SoundType.METAL).requiresCorrectToolForDrops(),
                        beType));
        RegistryObject<Item> item = MorkovskRegistries.ITEMS.register(name,
                () -> new BlockItem(block.get(), new Item.Properties()));
        MorkovskTabs.add(MorkovskTabs.ModTab.MACHINES, item::get);
        return block;
    }

    private static RegistryObject<Item> simpleItem(String name, MorkovskTabs.ModTab tab) {
        RegistryObject<Item> item = MorkovskRegistries.ITEMS.register(name, () -> new Item(new Item.Properties()));
        MorkovskTabs.add(tab, item::get);
        return item;
    }

    public static void init() {
        MILL = machine("mill", () -> MILL_BE.get());
        OIL_PRESS = machine("oil_press", () -> OIL_PRESS_BE.get());
        SEPARATOR = machine("separator", () -> SEPARATOR_BE.get());
        DRYER = machine("dryer", () -> DRYER_BE.get());
        FERMENTER = machine("fermenter", () -> FERMENTER_BE.get());
        SEED_EXTRACTOR = machine("seed_extractor", () -> SEED_EXTRACTOR_BE.get());
        AUTO_HARVESTER = machine("auto_harvester", () -> AUTO_HARVESTER_BE.get());
        FERTILIZER_MIXER = machine("fertilizer_mixer", () -> FERTILIZER_MIXER_BE.get());
        PACKING_STATION = machine("packing_station", () -> PACKING_STATION_BE.get());

        MILL_BE = MorkovskRegistries.BLOCK_ENTITIES.register("mill",
                () -> BlockEntityType.Builder.of(
                        (pos, state) -> new ProcessorBlockEntity(MILL_BE.get(), pos, state,
                                "mill", () -> MILL_MENU.get()),
                        MILL.get()).build(null));
        OIL_PRESS_BE = MorkovskRegistries.BLOCK_ENTITIES.register("oil_press",
                () -> BlockEntityType.Builder.of(
                        (pos, state) -> new ProcessorBlockEntity(OIL_PRESS_BE.get(), pos, state,
                                "oil_press", () -> OIL_PRESS_MENU.get()),
                        OIL_PRESS.get()).build(null));
        SEPARATOR_BE = MorkovskRegistries.BLOCK_ENTITIES.register("separator",
                () -> BlockEntityType.Builder.of(
                        (pos, state) -> new SeparatorBlockEntity(SEPARATOR_BE.get(), pos, state),
                        SEPARATOR.get()).build(null));
        DRYER_BE = MorkovskRegistries.BLOCK_ENTITIES.register("dryer",
                () -> BlockEntityType.Builder.of(
                        (pos, state) -> new ProcessorBlockEntity(DRYER_BE.get(), pos, state,
                                "dryer", () -> DRYER_MENU.get()),
                        DRYER.get()).build(null));
        FERMENTER_BE = MorkovskRegistries.BLOCK_ENTITIES.register("fermenter",
                () -> BlockEntityType.Builder.of(
                        (pos, state) -> new ProcessorBlockEntity(FERMENTER_BE.get(), pos, state,
                                "fermenter", () -> FERMENTER_MENU.get()),
                        FERMENTER.get()).build(null));
        SEED_EXTRACTOR_BE = MorkovskRegistries.BLOCK_ENTITIES.register("seed_extractor",
                () -> BlockEntityType.Builder.of(
                        (pos, state) -> new SeedExtractorBlockEntity(SEED_EXTRACTOR_BE.get(), pos, state),
                        SEED_EXTRACTOR.get()).build(null));
        AUTO_HARVESTER_BE = MorkovskRegistries.BLOCK_ENTITIES.register("auto_harvester",
                () -> BlockEntityType.Builder.of(
                        (pos, state) -> new AutoHarvesterBlockEntity(AUTO_HARVESTER_BE.get(), pos, state),
                        AUTO_HARVESTER.get()).build(null));
        FERTILIZER_MIXER_BE = MorkovskRegistries.BLOCK_ENTITIES.register("fertilizer_mixer",
                () -> BlockEntityType.Builder.of(
                        (pos, state) -> new FertilizerMixerBlockEntity(FERTILIZER_MIXER_BE.get(), pos, state),
                        FERTILIZER_MIXER.get()).build(null));
        PACKING_STATION_BE = MorkovskRegistries.BLOCK_ENTITIES.register("packing_station",
                () -> BlockEntityType.Builder.of(
                        (pos, state) -> new PackingStationBlockEntity(PACKING_STATION_BE.get(), pos, state),
                        PACKING_STATION.get()).build(null));

        MILL_MENU = MorkovskRegistries.MENUS.register("mill",
                () -> IForgeMenuType.create((id, inv, buf) ->
                        new ProcessorMenu(MILL_MENU.get(), id, inv, buf)));
        OIL_PRESS_MENU = MorkovskRegistries.MENUS.register("oil_press",
                () -> IForgeMenuType.create((id, inv, buf) ->
                        new ProcessorMenu(OIL_PRESS_MENU.get(), id, inv, buf)));
        SEPARATOR_MENU = MorkovskRegistries.MENUS.register("separator",
                () -> IForgeMenuType.create((id, inv, buf) ->
                        new SeparatorMenu(SEPARATOR_MENU.get(), id, inv, buf)));
        DRYER_MENU = MorkovskRegistries.MENUS.register("dryer",
                () -> IForgeMenuType.create((id, inv, buf) ->
                        new ProcessorMenu(DRYER_MENU.get(), id, inv, buf)));
        FERMENTER_MENU = MorkovskRegistries.MENUS.register("fermenter",
                () -> IForgeMenuType.create((id, inv, buf) ->
                        new ProcessorMenu(FERMENTER_MENU.get(), id, inv, buf)));
        SEED_EXTRACTOR_MENU = MorkovskRegistries.MENUS.register("seed_extractor",
                () -> IForgeMenuType.create((id, inv, buf) ->
                        new ProcessorMenu(SEED_EXTRACTOR_MENU.get(), id, inv, buf)));
        AUTO_HARVESTER_MENU = MorkovskRegistries.MENUS.register("auto_harvester",
                () -> IForgeMenuType.create((id, inv, buf) ->
                        new HarvesterMenu(AUTO_HARVESTER_MENU.get(), id, inv, buf)));
        FERTILIZER_MIXER_MENU = MorkovskRegistries.MENUS.register("fertilizer_mixer",
                () -> IForgeMenuType.create((id, inv, buf) ->
                        new MixerMenu(FERTILIZER_MIXER_MENU.get(), id, inv, buf)));
        PACKING_STATION_MENU = MorkovskRegistries.MENUS.register("packing_station",
                () -> IForgeMenuType.create((id, inv, buf) ->
                        new ProcessorMenu(PACKING_STATION_MENU.get(), id, inv, buf)));

        FLOUR = simpleItem("flour", MorkovskTabs.ModTab.FOOD);
        PLANT_OIL = simpleItem("plant_oil", MorkovskTabs.ModTab.FOOD);
        PRODUCE_CRATE = MorkovskRegistries.ITEMS.register("produce_crate",
                () -> new ProduceCrateItem(new Item.Properties().stacksTo(16)));
        MorkovskTabs.add(MorkovskTabs.ModTab.STORAGE, PRODUCE_CRATE::get);
    }

    @Mod.EventBusSubscriber(modid = CraftMorkovsk.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static final class Events {

        /** First machine crafted -> machine_operator advancement + a little XP. */
        @SubscribeEvent
        public static void onCraft(PlayerEvent.ItemCraftedEvent event) {
            if (!(event.getEntity() instanceof ServerPlayer player)) return;
            if (!(event.getCrafting().getItem() instanceof BlockItem blockItem)) return;
            if (!(blockItem.getBlock() instanceof MachineBlock)) return;
            Award.grant(player, "machine_operator");
            FarmingStats.awardXp(player, FarmingStats.XpReason.MACHINE_USE, 1);
        }
    }
}
