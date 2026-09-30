package com.craftmorkovsk.tractor;

import com.craftmorkovsk.CraftMorkovsk;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TractorRenderer extends EntityRenderer<TractorEntity> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation(CraftMorkovsk.MOD_ID, "textures/entity/tractor.png");

    private final TractorModel model;

    public TractorRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new TractorModel(context.bakeLayer(TractorModel.LAYER));
        this.shadowRadius = 0.9f;
    }

    @Override
    public ResourceLocation getTextureLocation(TractorEntity entity) {
        return TEXTURE;
    }

    @Override
    public void render(TractorEntity entity, float entityYaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0f - entityYaw));
        this.model.setupAnim(entity, 0.0f, 0.0f, entity.tickCount + partialTicks, 0.0f, 0.0f);
        VertexConsumer vertexConsumer = buffer.getBuffer(this.model.renderType(TEXTURE));
        this.model.renderToBuffer(poseStack, vertexConsumer, packedLight,
                OverlayTexture.NO_OVERLAY, 1.0f, 1.0f, 1.0f, 1.0f);
        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }
}
