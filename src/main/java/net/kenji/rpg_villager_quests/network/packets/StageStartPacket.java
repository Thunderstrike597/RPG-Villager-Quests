package net.kenji.rpg_villager_quests.network.packets;

import net.kenji.rpg_villager_quests.quest_system.QuestStage;
import net.kenji.rpg_villager_quests.quest_system.capability.QuestCapabilities;
import net.kenji.rpg_villager_quests.quest_system.quest_data.QuestInstance;
import net.kenji.rpg_villager_quests.quest_system.stage_types.ObjectiveStage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class StageStartPacket {

    private final String questId;
    private final String stageId;
    public StageStartPacket(String questId, String stageId) {
        this.questId = questId;
        this.stageId = stageId;
    }

    // Encode: Write data to buffer
    public static void encode(StageStartPacket packet, FriendlyByteBuf buf) {
        buf.writeUtf(packet.questId);
        buf.writeUtf(packet.stageId);
    }

    // Decode: Read data from buffer
    public static StageStartPacket decode(FriendlyByteBuf buf) {
        String questId = buf.readUtf();
        String stageId = buf.readUtf();
        return new StageStartPacket(questId, stageId);
    }

    // Handle: Process the packet on the receiving side
    public static void handle(StageStartPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Player player = ctx.get().getSender();
            if(player != null) {
                player.getCapability(QuestCapabilities.PLAYER_QUESTS).ifPresent((questData) -> {
                    QuestInstance questInstance = questData.getQuestInstance(packet.questId);
                    QuestStage questStage = questInstance.getQuest().getStageById(packet.stageId);
                    questInstance.setCurrentStage(packet.stageId);

                    if (questStage instanceof ObjectiveStage objectiveStage) {
                        if (objectiveStage.getObjective() != null) {
                            objectiveStage.getObjective().onStartObjective(player, questInstance);
                        }
                    }
                });
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
