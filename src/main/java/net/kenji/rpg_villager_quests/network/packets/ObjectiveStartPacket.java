package net.kenji.rpg_villager_quests.network.packets;

import net.kenji.rpg_villager_quests.quest_system.QuestEffects;
import net.kenji.rpg_villager_quests.quest_system.QuestStage;
import net.kenji.rpg_villager_quests.quest_system.quest_data.QuestData;
import net.kenji.rpg_villager_quests.quest_system.quest_data.QuestInstance;
import net.kenji.rpg_villager_quests.quest_system.stage_types.DialogueStage;
import net.kenji.rpg_villager_quests.quest_system.stage_types.ObjectiveStage;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ObjectiveStartPacket {

    private final String questId;
    private final String stageId;
    public ObjectiveStartPacket(String questId, String stageId) {
        this.questId = questId;
        this.stageId = stageId;
    }

    // Encode: Write data to buffer
    public static void encode(ObjectiveStartPacket packet, FriendlyByteBuf buf) {
        buf.writeUtf(packet.questId);
        buf.writeUtf(packet.stageId);
    }

    // Decode: Read data from buffer
    public static ObjectiveStartPacket decode(FriendlyByteBuf buf) {
        String questId = buf.readUtf();
        String stageId = buf.readUtf();
        return new ObjectiveStartPacket(questId, stageId);
    }

    // Handle: Process the packet on the receiving side
    public static void handle(ObjectiveStartPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Player player = ctx.get().getSender();
            if(player != null) {
                QuestData questData = QuestData.get(player.getUUID());
                QuestInstance questInstance = questData.getQuestInstance(packet.questId);
                QuestStage questStage = questInstance.getQuest().getStageById(packet.stageId);

                if(questStage instanceof ObjectiveStage dialogueStage){
                    if(dialogueStage.getObjective() != null){
                        dialogueStage.getObjective().onStartObjective(player);
                    }
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
