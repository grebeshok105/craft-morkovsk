package com.craftmorkovsk.client;

import com.craftmorkovsk.CraftMorkovsk;
import com.craftmorkovsk.energy.EnergyModule;
import com.craftmorkovsk.machine.framework.FuelScreen;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

/** Client composition root. Menu screens, entity renderers and particle providers wire here.
 *  Sub-sessions append registrations inside the marked region only. */
@Mod.EventBusSubscriber(modid = CraftMorkovsk.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class MorkovskClientInit {

    private MorkovskClientInit() {}

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MenuScreens.register(EnergyModule.GENERATOR_MENU.get(), FuelScreen::new);
            MenuScreens.register(EnergyModule.BIOMASS_MENU.get(), FuelScreen::new);
            // ==== SCREEN WIRING — sub-sessions append one line per menu/screen ====
        });
        // ==== CLIENT SETUP WIRING — sub-sessions may append one-line calls ====
    }
}
