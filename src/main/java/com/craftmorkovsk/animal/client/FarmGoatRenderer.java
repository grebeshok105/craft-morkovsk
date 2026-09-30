package com.craftmorkovsk.animal.client;

import com.craftmorkovsk.CraftMorkovsk;
import com.craftmorkovsk.animal.FarmGoatEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class FarmGoatRenderer extends MobRenderer<FarmGoatEntity, FarmGoatModel> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation(CraftMorkovsk.MOD_ID, "textures/entity/farm_goat.png");

    public FarmGoatRenderer(EntityRendererProvider.Context context) {
        super(context, new FarmGoatModel(context.bakeLayer(AnimalModelLayers.FARM_GOAT)), 0.6F);
    }

    @Override
    public ResourceLocation getTextureLocation(FarmGoatEntity entity) {
        return TEXTURE;
    }

    @Override
    protected void scale(FarmGoatEntity entity, PoseStack poseStack, float partialTickTime) {
        if (entity.isBaby()) {
            poseStack.scale(0.5F, 0.5F, 0.5F);
        }
    }
}
