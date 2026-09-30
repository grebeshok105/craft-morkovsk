package com.craftmorkovsk.animal.client;

import com.craftmorkovsk.CraftMorkovsk;
import com.craftmorkovsk.animal.AnimalModule;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Client wiring owned by the animal package (per contract — MorkovskClientInit untouched). */
@Mod.EventBusSubscriber(modid = CraftMorkovsk.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD,
        value = Dist.CLIENT)
public final class AnimalClientInit {

    private AnimalClientInit() {}

    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(AnimalModelLayers.DUCK, DuckModel::createBodyLayer);
        event.registerLayerDefinition(AnimalModelLayers.TURKEY, TurkeyModel::createBodyLayer);
        event.registerLayerDefinition(AnimalModelLayers.FARM_GOAT, FarmGoatModel::createBodyLayer);
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(AnimalModule.DUCK.get(), DuckRenderer::new);
        event.registerEntityRenderer(AnimalModule.TURKEY.get(), TurkeyRenderer::new);
        event.registerEntityRenderer(AnimalModule.FARM_GOAT.get(), FarmGoatRenderer::new);
        event.registerEntityRenderer(AnimalModule.DUCK_EGG_PROJECTILE.get(),
                context -> new ThrownItemRenderer<>(context));
        event.registerEntityRenderer(AnimalModule.TURKEY_EGG_PROJECTILE.get(),
                context -> new ThrownItemRenderer<>(context));
    }
}
