package com.craftmorkovsk.animal.client;

import com.craftmorkovsk.animal.DuckEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/** Simple box duck: body + head/beak + flapping wings + two legs.
 *  Texture bands: u[0..32) body, u[32..44) head, u[44..52) beak/legs, u[52..64) wings. */
@OnlyIn(Dist.CLIENT)
public class DuckModel extends EntityModel<DuckEntity> {

    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart wingL;
    private final ModelPart wingR;
    private final ModelPart legL;
    private final ModelPart legR;

    public DuckModel(ModelPart root) {
        this.root = root;
        this.head = root.getChild("head");
        this.wingL = root.getChild("wing_l");
        this.wingR = root.getChild("wing_r");
        this.legL = root.getChild("leg_l");
        this.legR = root.getChild("leg_r");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        root.addOrReplaceChild("body",
                CubeListBuilder.create().texOffs(0, 0)
                        .addBox(-3.0F, -3.0F, -5.0F, 6.0F, 6.0F, 10.0F),
                PartPose.offset(0.0F, 16.0F, 0.0F));

        PartDefinition head = root.addOrReplaceChild("head",
                CubeListBuilder.create().texOffs(32, 0)
                        .addBox(-2.0F, -2.0F, -3.0F, 4.0F, 4.0F, 5.0F),
                PartPose.offset(0.0F, 14.0F, -5.0F));
        head.addOrReplaceChild("beak",
                CubeListBuilder.create().texOffs(44, 0)
                        .addBox(-1.5F, -0.5F, -5.0F, 3.0F, 1.0F, 3.0F),
                PartPose.offset(0.0F, 0.5F, -3.0F));

        root.addOrReplaceChild("wing_l",
                CubeListBuilder.create().texOffs(52, 0)
                        .addBox(-1.0F, -1.5F, -2.0F, 1.0F, 4.0F, 7.0F),
                PartPose.offset(-3.0F, 16.0F, -2.0F));
        root.addOrReplaceChild("wing_r",
                CubeListBuilder.create().texOffs(52, 0).mirror()
                        .addBox(0.0F, -1.5F, -2.0F, 1.0F, 4.0F, 7.0F),
                PartPose.offset(3.0F, 16.0F, -2.0F));

        root.addOrReplaceChild("leg_l",
                CubeListBuilder.create().texOffs(44, 10)
                        .addBox(-1.0F, 0.0F, -1.0F, 2.0F, 3.0F, 2.0F),
                PartPose.offset(-1.5F, 21.0F, 0.0F));
        root.addOrReplaceChild("leg_r",
                CubeListBuilder.create().texOffs(44, 10)
                        .addBox(-1.0F, 0.0F, -1.0F, 2.0F, 3.0F, 2.0F),
                PartPose.offset(1.5F, 21.0F, 0.0F));

        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(DuckEntity entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        this.head.xRot = headPitch * (Mth.PI / 180.0F);
        this.head.yRot = netHeadYaw * (Mth.PI / 180.0F);
        this.wingL.zRot = Mth.cos(limbSwing * 0.8F) * limbSwingAmount * 0.6F;
        this.wingR.zRot = -this.wingL.zRot;
        this.legL.xRot = Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
        this.legR.xRot = Mth.cos(limbSwing * 0.6662F + Mth.PI) * 1.4F * limbSwingAmount;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight,
                               int packedOverlay, float red, float green, float blue, float alpha) {
        this.root.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
