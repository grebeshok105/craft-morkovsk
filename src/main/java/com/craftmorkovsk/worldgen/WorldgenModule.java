package com.craftmorkovsk.worldgen;

import com.craftmorkovsk.CraftMorkovsk;
import com.craftmorkovsk.core.FarmingHooks;
import com.craftmorkovsk.crop.MorkovskCropBlock;
import com.craftmorkovsk.data.Award;
import com.craftmorkovsk.registry.MorkovskRegistries;
import com.craftmorkovsk.registry.MorkovskTabs;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.RegistryObject;

/** World generation: wild crops, small farm structures, greenhouse detection.
 *  init() registers the wild_crop plant, four Feature<?>s (placement lives in
 *  datapack JSON + forge biome modifiers) and the FarmingHooks greenhouse checker. */
public final class WorldgenModule {

    public static RegistryObject<WildCropBlock> WILD_CROP;

    public static RegistryObject<WildCropPatchFeature> WILD_CROP_PATCH;
    public static RegistryObject<FarmsteadFeature> FARMSTEAD;
    public static RegistryObject<GreenhouseStructureFeature> GREENHOUSE;
    public static RegistryObject<WindmillFeature> WINDMILL;

    private WorldgenModule() {}

    public static void init() {
        WILD_CROP = MorkovskRegistries.BLOCKS.register("wild_crop",
                () -> new WildCropBlock(BlockBehaviour.Properties.of()
                        .noCollission()
                        .instabreak()
                        .sound(SoundType.CROP)));
        RegistryObject<Item> wildCropItem = MorkovskRegistries.ITEMS.register("wild_crop",
                () -> new BlockItem(WILD_CROP.get(), new Item.Properties()));
        MorkovskTabs.add(MorkovskTabs.ModTab.MISC, wildCropItem::get);

        WILD_CROP_PATCH = MorkovskRegistries.FEATURES.register("wild_crop_patch",
                WildCropPatchFeature::new);
        FARMSTEAD = MorkovskRegistries.FEATURES.register("farmstead", FarmsteadFeature::new);
        GREENHOUSE = MorkovskRegistries.FEATURES.register("greenhouse", GreenhouseStructureFeature::new);
        WINDMILL = MorkovskRegistries.FEATURES.register("windmill", WindmillFeature::new);

        FarmingHooks.registerGreenhouse(GreenhouseCheck::isInsideGreenhouse);
    }

    @Mod.EventBusSubscriber(modid = CraftMorkovsk.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static final class Events {

        /** green_thumb: fully-grown morkovsk crop broken under a glass roof. */
        @SubscribeEvent
        public static void onCropBreak(BlockEvent.BreakEvent event) {
            if (!(event.getState().getBlock() instanceof MorkovskCropBlock crop)) return;
            if (!crop.isMaxAge(event.getState())) return;
            if (!(event.getPlayer() instanceof ServerPlayer player)) return;
            if (!(event.getLevel() instanceof Level level)) return;
            if (FarmingHooks.isInGreenhouse(level, event.getPos())) {
                Award.grant(player, "green_thumb");
            }
        }

        /** wild_harvest: any wild_crop break. */
        @SubscribeEvent
        public static void onWildCropBreak(BlockEvent.BreakEvent event) {
            if (!(event.getState().getBlock() instanceof WildCropBlock)) return;
            if (!(event.getPlayer() instanceof ServerPlayer player)) return;
            Award.grant(player, "wild_harvest");
        }
    }
}
