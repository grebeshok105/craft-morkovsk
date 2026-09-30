package com.craftmorkovsk.progression.client;

import com.craftmorkovsk.CraftMorkovsk;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Client-only wiring for the progression package (HUD overlay). Owns its own
 *  MOD-bus subscriber so shared MorkovskClientInit stays untouched. */
@Mod.EventBusSubscriber(modid = CraftMorkovsk.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ProgressionClientInit {

    private ProgressionClientInit() {}

    @SubscribeEvent
    public static void onRegisterGuiOverlays(RegisterGuiOverlaysEvent event) {
        event.registerAboveAll("morkovsk_hud", new MorkovskHudOverlay());
    }
}
