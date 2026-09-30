package com.craftmorkovsk.storage;

import com.craftmorkovsk.CraftMorkovsk;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

/** Storage package's own client wiring: menu screens are registered here so the
 *  shared MorkovskClientInit never needs editing. */
@Mod.EventBusSubscriber(modid = CraftMorkovsk.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class StorageClientInit {

    private StorageClientInit() {}

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MenuScreens.register(StorageModule.CROP_CRATE_MENU.get(), StorageScreen::new);
            MenuScreens.register(StorageModule.LARGE_CRATE_MENU.get(), StorageScreen::new);
            MenuScreens.register(StorageModule.SEED_VAULT_MENU.get(), StorageScreen::new);
            MenuScreens.register(StorageModule.SILO_MENU.get(), StorageScreen::new);
            MenuScreens.register(StorageModule.REFRIGERATED_MENU.get(), RefrigeratedScreen::new);
        });
    }
}
