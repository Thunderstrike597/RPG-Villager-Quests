package net.kenji.rpg_villager_quests.network;

import net.kenji.rpg_villager_quests.quest_system.QuestEffects;
import net.kenji.rpg_villager_quests.quest_system.QuestStage;
import net.kenji.rpg_villager_quests.quest_system.quest_data.QuestData;
import net.kenji.rpg_villager_quests.quest_system.quest_data.QuestInstance;
import net.kenji.rpg_villager_quests.quest_system.stage_types.DialogueStage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import org.jline.utils.Log;

import java.util.function.Supplier;

public class ChoicePacket {
    private final String questId;
    private final String stageId;
    private final int choiceIndex;
    public ChoicePacket(String questId, String stageId, int choiceIndex) {
        this.questId = questId;
        this.stageId = stageId;
        this.choiceIndex = choiceIndex;
    }

    // Encode: Write data to buffer
    public static void encode(ChoicePacket packet, FriendlyByteBuf buf) {
        buf.writeUtf(packet.questId);
        buf.writeUtf(packet.stageId);
        buf.writeInt(packet.choiceIndex);
    }

    // Decode: Read data from buffer
    public static ChoicePacket decode(FriendlyByteBuf buf) {
        QuestEffects questEffects = new QuestEffects();
        String questId = buf.readUtf();
        String stageId = buf.readUtf();
        int choiceIndex = buf.readInt();
        return new ChoicePacket(questId, stageId, choiceIndex);
    }

    // Handle: Process the packet on the receiving side
    public static void handle(ChoicePacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Player player = ctx.get().getSender();
            if(player != null) {
                QuestData questData = QuestData.get(player.getUUID());
                QuestInstance questInstance = questData.getQuestInstance(packet.questId);
                QuestStage questStage = questInstance.getQuest().getStageById(packet.stageId);

                if(questStage instanceof DialogueStage dialogueStage){
                    if(dialogueStage.choices != null){
                        Log.info("APPLYING REWARDS IN PACKET");

                        if(packet.choiceIndex < dialogueStage.choices.size()) {
                            dialogueStage.choices.get(packet.choiceIndex).applyRewards(player);
                        }
                    }
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}