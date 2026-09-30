package com.craftmorkovsk.progression.client;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public final class HandbookClientHooks {

    private HandbookClientHooks() {}

    public static void openHandbook() {
        Minecraft.getInstance().setScreen(new HandbookScreen());
    }
}
