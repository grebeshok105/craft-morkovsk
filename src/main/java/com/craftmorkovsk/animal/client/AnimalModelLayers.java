package com.craftmorkovsk.animal.client;

import com.craftmorkovsk.CraftMorkovsk;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;

public final class AnimalModelLayers {

    public static final ModelLayerLocation DUCK =
            new ModelLayerLocation(new ResourceLocation(CraftMorkovsk.MOD_ID, "duck"), "main");
    public static final ModelLayerLocation TURKEY =
            new ModelLayerLocation(new ResourceLocation(CraftMorkovsk.MOD_ID, "turkey"), "main");
    public static final ModelLayerLocation FARM_GOAT =
            new ModelLayerLocation(new ResourceLocation(CraftMorkovsk.MOD_ID, "farm_goat"), "main");

    private AnimalModelLayers() {}
}
