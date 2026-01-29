package net.kenji.rpg_villager_quests.network.packets.server_side;

import net.kenji.rpg_villager_quests.quest_system.QuestEffects;
import net.kenji.rpg_villager_quests.quest_system.QuestStage;
import net.kenji.rpg_villager_quests.quest_system.quest_data.QuestData;
import net.kenji.rpg_villager_quests.quest_system.quest_data.QuestInstance;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class StageCompleteServerPacket {
    private final String questId;
    private final String stageId;
    private final QuestEffects effects;

    public StageCompleteServerPacket(String questId, String stageId, QuestEffects questEffects) {
        this.questId = questId;
        this.stageId = stageId;

        this.effects = questEffects;
    }

    // Encode: Write data to buffer
    public static void encode(StageCompleteServerPacket packet, FriendlyByteBuf buf) {
        buf.writeUtf(packet.questId);
        buf.writeUtf(packet.stageId);
        if(packet.effects != null){
            buf.writeBoolean(packet.effects.giveReward);
            buf.writeBoolean(packet.effects.endQuest);

            buf.writeItemStack(packet.effects.removeItem, true);
            buf.writeInt(packet.effects.itemCount);
        }
        else{
            buf.writeBoolean(true);
            buf.writeBoolean(false);

            buf.writeItemStack(ItemStack.EMPTY, true);
            buf.writeInt(0);
        }
    }

    // Decode: Read data from buffer
    public static StageCompleteServerPacket decode(FriendlyByteBuf buf) {
        QuestEffects questEffects = new QuestEffects();
        String questId = buf.readUtf();
        String stageId = buf.readUtf();
        questEffects.giveReward = buf.readBoolean();
        questEffects.endQuest = buf.readBoolean();
        questEffects.removeItem = buf.readItem();
        questEffects.itemCount = buf.readInt();
        return new StageCompleteServerPacket(questId, stageId, questEffects);
    }

    // Handle: Process the packet on the receiving side
    public static void handle(StageCompleteServerPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if(player != null) {
                QuestData questData = QuestData.get(player);
                QuestInstance questInstance = questData.getQuestInstance(packet.questId);
                QuestStage questStage = questInstance.getQuest().getStageById(packet.stageId);

                if(questStage != null && questStage.stageRewards != null) {
                    questStage.onComplete(packet.effects, player, questInstance);
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}