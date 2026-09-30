package com.craftmorkovsk.machine;

import com.craftmorkovsk.CraftMorkovsk;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

/** Client wiring for the machine package: one shared screen class for all machine menus.
 *  Kept inside this package so {@code MorkovskClientInit} stays untouched. */
@Mod.EventBusSubscriber(modid = CraftMorkovsk.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class MachineClientInit {

    private MachineClientInit() {}

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MenuScreens.register(MachineModule.MILL_MENU.get(), ProcessorScreen::new);
            MenuScreens.register(MachineModule.OIL_PRESS_MENU.get(), ProcessorScreen::new);
            MenuScreens.register(MachineModule.SEPARATOR_MENU.get(), SeparatorScreen::new);
            MenuScreens.register(MachineModule.DRYER_MENU.get(), ProcessorScreen::new);
            MenuScreens.register(MachineModule.FERMENTER_MENU.get(), ProcessorScreen::new);
            MenuScreens.register(MachineModule.SEED_EXTRACTOR_MENU.get(), ProcessorScreen::new);
            MenuScreens.register(MachineModule.AUTO_HARVESTER_MENU.get(), HarvesterScreen::new);
            MenuScreens.register(MachineModule.FERTILIZER_MIXER_MENU.get(), MixerScreen::new);
            MenuScreens.register(MachineModule.PACKING_STATION_MENU.get(), ProcessorScreen::new);
        });
    }
}
