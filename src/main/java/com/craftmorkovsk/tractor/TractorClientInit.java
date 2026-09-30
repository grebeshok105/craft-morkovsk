package com.craftmorkovsk.tractor;

import com.craftmorkovsk.CraftMorkovsk;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

/** Tractor package's own client wiring — entity renderer, model layer and the menu
 *  screen. Never touched by the dedicated server (subscriber is Dist.CLIENT-gated). */
@Mod.EventBusSubscriber(modid = CraftMorkovsk.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class TractorClientInit {

    private TractorClientInit() {}

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() ->
                MenuScreens.register(TractorModule.TRACTOR_MENU.get(), TractorScreen::new));
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(TractorModule.TRACTOR.get(), TractorRenderer::new);
    }

    @SubscribeEvent
    public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(TractorModel.LAYER, TractorModel::createBodyLayer);
    }
}
