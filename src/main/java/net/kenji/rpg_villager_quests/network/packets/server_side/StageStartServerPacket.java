package net.kenji.rpg_villager_quests.network.packets.server_side;

import net.kenji.rpg_villager_quests.quest_system.QuestStage;
import net.kenji.rpg_villager_quests.quest_system.quest_data.QuestData;
import net.kenji.rpg_villager_quests.quest_system.quest_data.QuestInstance;
import net.kenji.rpg_villager_quests.quest_system.stage_types.ObjectiveStage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class StageStartServerPacket {

    private final String questId;
    private final String stageId;
    public StageStartServerPacket(String questId, String stageId) {
        this.questId = questId;
        this.stageId = stageId;
    }

    // Encode: Write data to buffer
    public static void encode(StageStartServerPacket packet, FriendlyByteBuf buf) {
        buf.writeUtf(packet.questId);
        buf.writeUtf(packet.stageId);
    }

    // Decode: Read data from buffer
    public static StageStartServerPacket decode(FriendlyByteBuf buf) {
        String questId = buf.readUtf();
        String stageId = buf.readUtf();
        return new StageStartServerPacket(questId, stageId);
    }

    // Handle: Process the packet on the receiving side
    public static void handle(StageStartServerPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if(player != null) {
                QuestData questData = QuestData.get(player);
                QuestInstance questInstance = questData.getQuestInstance(packet.questId);
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
