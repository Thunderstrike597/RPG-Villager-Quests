package net.kenji.rpg_villager_quests.client.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.kenji.rpg_villager_quests.RpgVillagerQuests;
import net.kenji.rpg_villager_quests.events.QuestVillagerEvents;
import net.kenji.rpg_villager_quests.manager.VillagerQuestManager;
import net.minecraft.client.model.VillagerModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.npc.Villager;

public class QuestVillagerClothingLayer
        extends RenderLayer<Villager, VillagerModel<Villager>> {

    private static final ResourceLocation QUEST_OUTFIT =
            ResourceLocation.fromNamespaceAndPath(RpgVillagerQuests.MODID,
                    "textures/entity/villager/quest_villager.png");

    public QuestVillagerClothingLayer(
            RenderLayerParent<Villager, VillagerModel<Villager>> parent) {
        super(parent);
    }

    @Override
    public void render(
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            Villager villager,
            float limbSwing,
            float limbSwingAmount,
            float partialTicks,
            float ageInTicks,
            float netHeadYaw,
            float headPitch) {

        if (villager.getPersistentData().getBoolean(QuestVillagerEvents.IS_QUEST_VILLAGER_TAG) || villager.getPersistentData().contains(QuestVillagerEvents.QUEST_VILLAGER_TAG)) {

            VertexConsumer consumer =
                    buffer.getBuffer(RenderType.entityCutoutNoCull(QUEST_OUTFIT));

            this.getParentModel().renderToBuffer(
                    poseStack,
                    consumer,
                    packedLight,
                    OverlayTexture.NO_OVERLAY,
                    1f, 1f, 1f, 1f
            );
        }
    }
}