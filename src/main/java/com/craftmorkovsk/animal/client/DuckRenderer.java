package com.craftmorkovsk.animal.client;

import com.craftmorkovsk.CraftMorkovsk;
import com.craftmorkovsk.animal.DuckEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class DuckRenderer extends MobRenderer<DuckEntity, DuckModel> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation(CraftMorkovsk.MOD_ID, "textures/entity/duck.png");

    public DuckRenderer(EntityRendererProvider.Context context) {
        super(context, new DuckModel(context.bakeLayer(AnimalModelLayers.DUCK)), 0.3F);
    }

    @Override
    public ResourceLocation getTextureLocation(DuckEntity entity) {
        return TEXTURE;
    }

    @Override
    protected void scale(DuckEntity entity, PoseStack poseStack, float partialTickTime) {
        if (entity.isBaby()) {
            poseStack.scale(0.5F, 0.5F, 0.5F);
        }
    }
}
