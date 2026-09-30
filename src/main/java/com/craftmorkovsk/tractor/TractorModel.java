package com.craftmorkovsk.tractor;

import com.craftmorkovsk.CraftMorkovsk;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/** Simple box-model tractor: yellow chassis + hood, green grille, dark wheels and
 *  exhaust pipe, brown seat. Texture is a 128x64 palette; every cube maps onto a
 *  single flat-colored region (see tractor.png generator notes in the resource). */
@OnlyIn(Dist.CLIENT)
public class TractorModel extends EntityModel<TractorEntity> {

    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(new ResourceLocation(CraftMorkovsk.MOD_ID, "tractor"), "main");

    private final ModelPart root;

    public TractorModel(ModelPart root) {
        this.root = root;
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot().addOrReplaceChild("root", CubeListBuilder.create(),
                PartPose.offset(0.0f, 24.0f, 0.0f));

        // Yellow body (texture region y 0..31).
        root.addOrReplaceChild("chassis", CubeListBuilder.create().texOffs(0, 0)
                        .addBox(-9.0f, -14.0f, -10.0f, 18, 6, 20), PartPose.ZERO);
        root.addOrReplaceChild("hood", CubeListBuilder.create().texOffs(78, 0)
                        .addBox(-7.0f, -20.0f, -10.0f, 14, 6, 14), PartPose.ZERO);
        root.addOrReplaceChild("fender_l", CubeListBuilder.create().texOffs(78, 0)
                        .addBox(-15.0f, -15.0f, -3.0f, 7, 2, 12), PartPose.ZERO);
        root.addOrReplaceChild("fender_r", CubeListBuilder.create().texOffs(78, 0)
                        .addBox(8.0f, -15.0f, -3.0f, 7, 2, 12), PartPose.ZERO);

        // Green accents (region x 96..127, y 32..63).
        root.addOrReplaceChild("grille", CubeListBuilder.create().texOffs(96, 32)
                        .addBox(-6.0f, -19.0f, -11.0f, 12, 5, 2), PartPose.ZERO);

        // Brown seat (region x 0..31, y 32..63).
        root.addOrReplaceChild("seat", CubeListBuilder.create().texOffs(0, 32)
                        .addBox(-4.0f, -16.0f, 4.0f, 8, 3, 8), PartPose.ZERO);
        root.addOrReplaceChild("seat_back", CubeListBuilder.create().texOffs(0, 44)
                        .addBox(-4.0f, -22.0f, 10.0f, 8, 6, 2), PartPose.ZERO);

        // Dark wheels + exhaust (region x 32..95, y 32..63).
        root.addOrReplaceChild("exhaust", CubeListBuilder.create().texOffs(32, 32)
                        .addBox(5.0f, -27.0f, -8.0f, 3, 12, 3), PartPose.ZERO);
        root.addOrReplaceChild("wheel_rear_l", CubeListBuilder.create().texOffs(46, 32)
                        .addBox(-13.0f, -14.0f, -2.0f, 4, 14, 10), PartPose.ZERO);
        root.addOrReplaceChild("wheel_rear_r", CubeListBuilder.create().texOffs(46, 32)
                        .addBox(9.0f, -14.0f, -2.0f, 4, 14, 10), PartPose.ZERO);
        root.addOrReplaceChild("wheel_front_l", CubeListBuilder.create().texOffs(70, 44)
                        .addBox(-10.0f, -10.0f, -12.0f, 4, 10, 8), PartPose.ZERO);
        root.addOrReplaceChild("wheel_front_r", CubeListBuilder.create().texOffs(70, 44)
                        .addBox(6.0f, -10.0f, -12.0f, 4, 10, 8), PartPose.ZERO);

        return LayerDefinition.create(mesh, 128, 64);
    }

    @Override
    public void setupAnim(TractorEntity entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) { }

    @Override
    public void renderToBuffer(com.mojang.blaze3d.vertex.PoseStack poseStack,
                               com.mojang.blaze3d.vertex.VertexConsumer buffer,
                               int packedLight, int packedOverlay,
                               float red, float green, float blue, float alpha) {
        this.root.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
