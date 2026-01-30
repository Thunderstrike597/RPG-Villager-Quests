package net.kenji.rpg_villager_quests.network.packets.server_side;

import net.kenji.rpg_villager_quests.quest_system.QuestStage;
import net.kenji.rpg_villager_quests.quest_system.quest_data.QuestData;
import net.kenji.rpg_villager_quests.quest_system.quest_data.QuestInstance;
import net.kenji.rpg_villager_quests.quest_system.stage_types.ObjectiveStage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class StageStartPacket {

    private final String questId;
    private final String stageId;
    private final UUID villagerUuid;

    public StageStartPacket(String questId, String stageId, UUID villagerUuid) {
        this.questId = questId;
        this.stageId = stageId;
        this.villagerUuid = villagerUuid;
    }

    // Encode: Write data to buffer
    public static void encode(StageStartPacket packet, FriendlyByteBuf buf) {
        buf.writeUtf(packet.questId);
        buf.writeUtf(packet.stageId);
        buf.writeUUID(packet.villagerUuid);
    }

    // Decode: Read data from buffer
    public static StageStartPacket decode(FriendlyByteBuf buf) {
        String questId = buf.readUtf();
        String stageId = buf.readUtf();
        UUID villagerUuid = buf.readUUID();
        return new StageStartPacket(questId, stageId, villagerUuid);
    }

    // Handle: Process the packet on the receiving side
    public static void handle(StageStartPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if(player != null) {
                QuestData questData = QuestData.get(player);
                QuestInstance questInstance = questData.getQuestInstance(packet.questId, packet.villagerUuid);
                QuestStage questStage = questInstance.getQuest().getStageById(packet.stageId);
                questInstance.setCurrentStage(packet.stageId);

                if(questStage instanceof ObjectiveStage objectiveStage){
                    if(objectiveStage.getObjective() != null){
                        objectiveStage.getObjective().onStartObjective(player, questInstance);
                    }
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
