package com.craftmorkovsk.irrigation;

import com.craftmorkovsk.CraftMorkovsk;
import com.craftmorkovsk.core.FarmingHooks;
import com.craftmorkovsk.registry.MorkovskRegistries;
import com.craftmorkovsk.registry.MorkovskTabs;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.RegistryObject;

/** Irrigation infrastructure: pipes, valve, channel, tank, pump, sprinklers.
 *  init() registers content and hooks WaterGrid into FarmingHooks so shared
 *  soil code sees sprinkler water via SoilAPI.isHydrated. */
public final class IrrigationModule {

    public static RegistryObject<Block> WATER_PIPE;
    public static RegistryObject<Block> WATER_CHANNEL;
    public static RegistryObject<Block> WATER_TANK;
    public static RegistryObject<Block> WATER_PUMP;
    public static RegistryObject<Block> SPRINKLER;
    public static RegistryObject<Block> ADVANCED_SPRINKLER;
    public static RegistryObject<Block> IRRIGATION_VALVE;

    public static RegistryObject<BlockEntityType<WaterChannelBlockEntity>> WATER_CHANNEL_BE;
    public static RegistryObject<BlockEntityType<WaterTankBlockEntity>> WATER_TANK_BE;
    public static RegistryObject<BlockEntityType<WaterPumpBlockEntity>> WATER_PUMP_BE;
    public static RegistryObject<BlockEntityType<SprinklerBlockEntity>> SPRINKLER_BE;

    private IrrigationModule() {}

    private static RegistryObject<Item> blockItem(RegistryObject<? extends Block> block, MorkovskTabs.ModTab tab) {
        RegistryObject<Item> item = MorkovskRegistries.ITEMS.register(block.getId().getPath(),
                () -> new BlockItem(block.get(), new Item.Properties()));
        MorkovskTabs.add(tab, item::get);
        return item;
    }

    private static BlockBehaviour.Properties metal(float hardness) {
        return BlockBehaviour.Properties.of().strength(hardness).sound(SoundType.METAL);
    }

    public static void init() {
        WATER_PIPE = MorkovskRegistries.BLOCKS.register("water_pipe",
                () -> new WaterPipeBlock(metal(0.5f).noOcclusion()));
        blockItem(WATER_PIPE, MorkovskTabs.ModTab.MACHINES);

        IRRIGATION_VALVE = MorkovskRegistries.BLOCKS.register("irrigation_valve",
                () -> new IrrigationValveBlock(metal(0.5f).noOcclusion()));
        blockItem(IRRIGATION_VALVE, MorkovskTabs.ModTab.MACHINES);

        WATER_CHANNEL = MorkovskRegistries.BLOCKS.register("water_channel",
                () -> new WaterChannelBlock(metal(1.0f).noOcclusion(), () -> WATER_CHANNEL_BE.get()));
        blockItem(WATER_CHANNEL, MorkovskTabs.ModTab.MACHINES);

        WATER_TANK = MorkovskRegistries.BLOCKS.register("water_tank",
                () -> new WaterTankBlock(metal(2.5f), () -> WATER_TANK_BE.get()));
        blockItem(WATER_TANK, MorkovskTabs.ModTab.MACHINES);

        WATER_PUMP = MorkovskRegistries.BLOCKS.register("water_pump",
                () -> new WaterPumpBlock(metal(2.5f), () -> WATER_PUMP_BE.get()));
        blockItem(WATER_PUMP, MorkovskTabs.ModTab.MACHINES);

        SPRINKLER = MorkovskRegistries.BLOCKS.register("sprinkler",
                () -> new SprinklerBlock(metal(1.0f).noOcclusion(), false, () -> SPRINKLER_BE.get()));
        blockItem(SPRINKLER, MorkovskTabs.ModTab.MACHINES);

        ADVANCED_SPRINKLER = MorkovskRegistries.BLOCKS.register("advanced_sprinkler",
                () -> new SprinklerBlock(metal(1.5f).noOcclusion(), true, () -> SPRINKLER_BE.get()));
        blockItem(ADVANCED_SPRINKLER, MorkovskTabs.ModTab.MACHINES);

        WATER_CHANNEL_BE = MorkovskRegistries.BLOCK_ENTITIES.register("water_channel",
                () -> BlockEntityType.Builder.of(
                        (pos, state) -> new WaterChannelBlockEntity(WATER_CHANNEL_BE.get(), pos, state),
                        WATER_CHANNEL.get()).build(null));
        WATER_TANK_BE = MorkovskRegistries.BLOCK_ENTITIES.register("water_tank",
                () -> BlockEntityType.Builder.of(
                        (pos, state) -> new WaterTankBlockEntity(WATER_TANK_BE.get(), pos, state),
                        WATER_TANK.get()).build(null));
        WATER_PUMP_BE = MorkovskRegistries.BLOCK_ENTITIES.register("water_pump",
                () -> BlockEntityType.Builder.of(
                        (pos, state) -> new WaterPumpBlockEntity(WATER_PUMP_BE.get(), pos, state),
                        WATER_PUMP.get()).build(null));
        SPRINKLER_BE = MorkovskRegistries.BLOCK_ENTITIES.register("sprinkler",
                () -> BlockEntityType.Builder.of(
                        (pos, state) -> new SprinklerBlockEntity(SPRINKLER_BE.get(), pos, state),
                        SPRINKLER.get(), ADVANCED_SPRINKLER.get()).build(null));

        FarmingHooks.registerIrrigation(WaterGrid::isIrrigated);
    }

    @Mod.EventBusSubscriber(modid = CraftMorkovsk.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static final class Events {
        @SubscribeEvent
        public static void onLevelUnload(LevelEvent.Unload event) {
            if (event.getLevel() instanceof Level level) WaterGrid.clear(level.dimension());
        }
    }
}
