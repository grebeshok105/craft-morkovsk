package com.craftmorkovsk.crop;

import com.craftmorkovsk.CraftMorkovsk;
import com.craftmorkovsk.data.Award;
import com.craftmorkovsk.data.FarmingStats;
import com.craftmorkovsk.soil.SoilAPI;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Crop registration + harvest/planting side effects (XP, soil degradation, rare-crop drama). */
public final class CropModule {

    private CropModule() {}

    public static void init() {
        CropCatalog.init();
    }

    @Mod.EventBusSubscriber(modid = CraftMorkovsk.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static final class Events {

        @SubscribeEvent
        public static void onHarvest(BlockEvent.BreakEvent event) {
            if (!(event.getState().getBlock() instanceof MorkovskCropBlock crop)) return;
            if (!(event.getPlayer() instanceof ServerPlayer player)) return;
            if (!(event.getLevel() instanceof Level level)) return;
            BlockPos pos = event.getPos();
            if (!crop.isMaxAge(event.getState())) return;

            CropDef def = crop.def();
            FarmingStats.awardXp(player, FarmingStats.XpReason.HARVESTING, 1 + def.xpBonus);
            SoilAPI.degrade(level, pos.below(), level.random);
            Award.grant(player, "root");
            Award.grant(player, "first_harvest");
            if (level.getBlockState(pos.below()).getBlock() instanceof com.craftmorkovsk.soil.MorkovskSoilBlock)
                Award.grant(player, "dirt_professional");

            switch (def.id) {
                case "golden_carrot_plant" -> Award.grant(player, "golden_harvest");
                case "giant_morkov" -> Award.grant(player, "giant_morkov");
                case "morkovsk_supreme" -> Award.grant(player, "morkovsk_supreme");
                case "the_carrot" -> {
                    Award.grant(player, "the_carrot");
                    FarmingStats.addMoney(player, 10000);
                    FarmingStats.awardXp(player, FarmingStats.XpReason.SPECIAL, 10);
                    player.displayClientMessage(net.minecraft.network.chat.Component
                                    .translatable("event.craftmorkovsk.the_carrot")
                                    .withStyle(net.minecraft.ChatFormatting.GOLD, net.minecraft.ChatFormatting.BOLD),
                            false);
                }
                default -> { }
            }
        }

        @SubscribeEvent
        public static void onPlant(BlockEvent.EntityPlaceEvent event) {
            if (!(event.getPlacedBlock().getBlock() instanceof MorkovskCropBlock)) return;
            if (!(event.getEntity() instanceof ServerPlayer player)) return;
            FarmingStats.awardXp(player, FarmingStats.XpReason.PLANTING, 1);
        }
    }
}
