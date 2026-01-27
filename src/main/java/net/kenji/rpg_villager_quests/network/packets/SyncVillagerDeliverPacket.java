package net.kenji.rpg_villager_quests.network.packets;

import net.kenji.rpg_villager_quests.quest_system.QuestStage;
import net.kenji.rpg_villager_quests.quest_system.capability.QuestCapabilities;
import net.kenji.rpg_villager_quests.quest_system.objective_types.PackageDeliverObjective;
import net.kenji.rpg_villager_quests.quest_system.quest_data.QuestInstance;
import net.kenji.rpg_villager_quests.quest_system.stage_types.ObjectiveStage;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class SyncVillagerDeliverPacket {

    private final String questId;
    private final String stageId;
    private final UUID villagerUuid;
    private final int villagerId;
    public SyncVillagerDeliverPacket(String questId, String stageId, UUID villagerUuid, int villagerId) {
        this.questId = questId;
        this.stageId = stageId;
        this.villagerUuid = villagerUuid;
        this.villagerId = villagerId;
    }

    // Encode: Write data to buffer
    public static void encode(SyncVillagerDeliverPacket packet, FriendlyByteBuf buf) {
        buf.writeUtf(packet.questId);
        buf.writeUtf(packet.stageId);
        buf.writeUUID(packet.villagerUuid);
        buf.writeInt(packet.villagerId);
    }

    // Decode: Read data from buffer
    public static SyncVillagerDeliverPacket decode(FriendlyByteBuf buf) {
        String questId = buf.readUtf();
        String stageId = buf.readUtf();
        UUID villagerUuid = buf.readUUID();
        int villagerId = buf.readInt();
        return new SyncVillagerDeliverPacket(questId, stageId, villagerUuid, villagerId);
    }

    // Handle: Process the packet on the receiving side
    public static void handle(SyncVillagerDeliverPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if(ctx.get().getDirection().getReceptionSide().isClient()) {
                Player player = Minecraft.getInstance().player;
                if (player != null) {
                    player.getCapability(QuestCapabilities.PLAYER_QUESTS).ifPresent((questData) -> {
                        QuestInstance questInstance = questData.getQuestInstance(packet.questId);

                        if (questInstance != null) {

                            // Or find the specific stage by ID in the instance
                            QuestStage questStage = questInstance.getQuest().getStageById(packet.stageId);

                            if (questStage instanceof ObjectiveStage objectiveStage) {
                                if (objectiveStage.getObjective() instanceof PackageDeliverObjective packageDeliverObjective) {
                                    packageDeliverObjective.currentDeliverEntity = packet.villagerUuid;

                                    if (Minecraft.getInstance().level != null) {
                                        Entity entity = Minecraft.getInstance().level.getEntity(packet.villagerId);
                                        if (entity instanceof Villager villager) {
                                            villager.setGlowingTag(true);
                                            villager.getPersistentData().putUUID(PackageDeliverObjective.objectiveEntityTag, questInstance.getQuestVillager(player).getUUID());
                                        }
                                    }
                                }
                            }
                        }
                    });
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
