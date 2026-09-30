package com.craftmorkovsk.economy;

import com.craftmorkovsk.CraftMorkovsk;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

/** Client wiring for the economy package — menu screens only. */
@Mod.EventBusSubscriber(modid = CraftMorkovsk.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD,
        value = Dist.CLIENT)
public final class EconomyClientInit {

    private EconomyClientInit() {}

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MenuScreens.register(EconomyModule.SHIPPING_CRATE_MENU.get(), ShippingCrateScreen::new);
            MenuScreens.register(EconomyModule.SHOP_MENU.get(), ShopScreen::new);
        });
    }
}
