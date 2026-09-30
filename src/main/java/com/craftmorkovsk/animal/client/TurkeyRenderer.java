package com.craftmorkovsk.animal.client;

import com.craftmorkovsk.CraftMorkovsk;
import com.craftmorkovsk.animal.TurkeyEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TurkeyRenderer extends MobRenderer<TurkeyEntity, TurkeyModel> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation(CraftMorkovsk.MOD_ID, "textures/entity/turkey.png");

    public TurkeyRenderer(EntityRendererProvider.Context context) {
        super(context, new TurkeyModel(context.bakeLayer(AnimalModelLayers.TURKEY)), 0.4F);
    }

    @Override
    public ResourceLocation getTextureLocation(TurkeyEntity entity) {
        return TEXTURE;
    }

    @Override
    protected void scale(TurkeyEntity entity, PoseStack poseStack, float partialTickTime) {
        if (entity.isBaby()) {
            poseStack.scale(0.5F, 0.5F, 0.5F);
        }
    }
}
