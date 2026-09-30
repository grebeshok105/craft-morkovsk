package com.craftmorkovsk.animal.client;

import com.craftmorkovsk.animal.FarmGoatEntity;
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

/** Farm goat: body, head with two horns, four legs, stub tail.
 *  Texture bands: u[0..40) body, u[40..48) head, u[48..56) horns/legs accent, u[56..64) legs. */
@OnlyIn(Dist.CLIENT)
public class FarmGoatModel extends EntityModel<FarmGoatEntity> {

    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart legFL;
    private final ModelPart legFR;
    private final ModelPart legBL;
    private final ModelPart legBR;

    public FarmGoatModel(ModelPart root) {
        this.root = root;
        this.head = root.getChild("head");
        this.legFL = root.getChild("leg_fl");
        this.legFR = root.getChild("leg_fr");
        this.legBL = root.getChild("leg_bl");
        this.legBR = root.getChild("leg_br");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        root.addOrReplaceChild("body",
                CubeListBuilder.create().texOffs(0, 0)
                        .addBox(-4.0F, -4.0F, -7.0F, 8.0F, 8.0F, 14.0F),
                PartPose.offset(0.0F, 14.0F, 0.0F));
        root.addOrReplaceChild("tail",
                CubeListBuilder.create().texOffs(0, 24)
                        .addBox(-1.0F, -1.0F, 0.0F, 2.0F, 2.0F, 3.0F),
                PartPose.offsetAndRotation(0.0F, 11.0F, 7.0F, 0.6F, 0.0F, 0.0F));

        PartDefinition head = root.addOrReplaceChild("head",
                CubeListBuilder.create().texOffs(40, 0)
                        .addBox(-2.0F, -2.0F, -6.0F, 4.0F, 4.0F, 7.0F),
                PartPose.offset(0.0F, 11.0F, -8.0F));
        head.addOrReplaceChild("horn_l",
                CubeListBuilder.create().texOffs(48, 0)
                        .addBox(-3.0F, -5.0F, -3.0F, 2.0F, 4.0F, 2.0F),
                PartPose.ZERO);
        head.addOrReplaceChild("horn_r",
                CubeListBuilder.create().texOffs(48, 0).mirror()
                        .addBox(1.0F, -5.0F, -3.0F, 2.0F, 4.0F, 2.0F),
                PartPose.ZERO);

        root.addOrReplaceChild("leg_fl",
                CubeListBuilder.create().texOffs(56, 0)
                        .addBox(-1.5F, 0.0F, -1.5F, 3.0F, 6.0F, 3.0F),
                PartPose.offset(-2.5F, 18.0F, -4.5F));
        root.addOrReplaceChild("leg_fr",
                CubeListBuilder.create().texOffs(56, 0)
                        .addBox(-1.5F, 0.0F, -1.5F, 3.0F, 6.0F, 3.0F),
                PartPose.offset(2.5F, 18.0F, -4.5F));
        root.addOrReplaceChild("leg_bl",
                CubeListBuilder.create().texOffs(56, 0)
                        .addBox(-1.5F, 0.0F, -1.5F, 3.0F, 6.0F, 3.0F),
                PartPose.offset(-2.5F, 18.0F, 4.5F));
        root.addOrReplaceChild("leg_br",
                CubeListBuilder.create().texOffs(56, 0)
                        .addBox(-1.5F, 0.0F, -1.5F, 3.0F, 6.0F, 3.0F),
                PartPose.offset(2.5F, 18.0F, 4.5F));

        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(FarmGoatEntity entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        this.head.xRot = headPitch * (Mth.PI / 180.0F);
        this.head.yRot = netHeadYaw * (Mth.PI / 180.0F);
        float swing = Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
        float opposite = Mth.cos(limbSwing * 0.6662F + Mth.PI) * 1.4F * limbSwingAmount;
        this.legFL.xRot = swing;
        this.legBR.xRot = swing;
        this.legFR.xRot = opposite;
        this.legBL.xRot = opposite;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight,
                               int packedOverlay, float red, float green, float blue, float alpha) {
        this.root.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
