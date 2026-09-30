package com.craftmorkovsk.animal.client;

import com.craftmorkovsk.animal.TurkeyEntity;
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

/** Turkey: bigger body, neck+head with snood, tail fan, wings, legs.
 *  Texture bands: u[0..32) body, u[32..40) head, u[40..46) beak/snood,
 *  u[46..52) legs, u[52..64) tail/wings. */
@OnlyIn(Dist.CLIENT)
public class TurkeyModel extends EntityModel<TurkeyEntity> {

    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart tail;
    private final ModelPart wingL;
    private final ModelPart wingR;
    private final ModelPart legL;
    private final ModelPart legR;

    public TurkeyModel(ModelPart root) {
        this.root = root;
        this.head = root.getChild("head");
        this.tail = root.getChild("tail");
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
                        .addBox(-4.0F, -4.0F, -6.0F, 8.0F, 8.0F, 12.0F),
                PartPose.offset(0.0F, 15.0F, 0.0F));

        PartDefinition head = root.addOrReplaceChild("head",
                CubeListBuilder.create().texOffs(32, 0)
                        .addBox(-1.5F, -5.0F, -2.0F, 3.0F, 6.0F, 4.0F),
                PartPose.offset(0.0F, 12.0F, -6.0F));
        head.addOrReplaceChild("beak",
                CubeListBuilder.create().texOffs(40, 0)
                        .addBox(-1.0F, -4.5F, -4.0F, 2.0F, 1.0F, 2.0F),
                PartPose.ZERO);
        head.addOrReplaceChild("snood",
                CubeListBuilder.create().texOffs(40, 8)
                        .addBox(-0.5F, -3.5F, -3.5F, 1.0F, 3.0F, 1.0F),
                PartPose.ZERO);

        root.addOrReplaceChild("tail",
                CubeListBuilder.create().texOffs(52, 0)
                        .addBox(-5.0F, -8.0F, 0.0F, 10.0F, 8.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, 14.0F, 6.0F, -0.35F, 0.0F, 0.0F));

        root.addOrReplaceChild("wing_l",
                CubeListBuilder.create().texOffs(52, 10)
                        .addBox(-1.0F, -2.0F, -3.0F, 1.0F, 6.0F, 9.0F),
                PartPose.offset(-4.0F, 15.0F, -2.0F));
        root.addOrReplaceChild("wing_r",
                CubeListBuilder.create().texOffs(52, 10).mirror()
                        .addBox(0.0F, -2.0F, -3.0F, 1.0F, 6.0F, 9.0F),
                PartPose.offset(4.0F, 15.0F, -2.0F));

        root.addOrReplaceChild("leg_l",
                CubeListBuilder.create().texOffs(46, 16)
                        .addBox(-1.0F, 0.0F, -1.0F, 2.0F, 4.0F, 2.0F),
                PartPose.offset(-2.0F, 20.0F, 0.0F));
        root.addOrReplaceChild("leg_r",
                CubeListBuilder.create().texOffs(46, 16)
                        .addBox(-1.0F, 0.0F, -1.0F, 2.0F, 4.0F, 2.0F),
                PartPose.offset(2.0F, 20.0F, 0.0F));

        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(TurkeyEntity entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        this.head.xRot = headPitch * (Mth.PI / 180.0F);
        this.head.yRot = netHeadYaw * (Mth.PI / 180.0F);
        this.tail.yRot = Mth.cos(ageInTicks * 0.05F) * 0.08F;
        this.wingL.zRot = Mth.cos(limbSwing * 0.8F) * limbSwingAmount * 0.5F;
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
