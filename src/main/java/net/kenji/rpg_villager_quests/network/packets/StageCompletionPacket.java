package net.kenji.rpg_villager_quests.network.packets;

import net.kenji.rpg_villager_quests.quest_system.QuestEffects;
import net.kenji.rpg_villager_quests.quest_system.QuestStage;
import net.kenji.rpg_villager_quests.quest_system.capability.QuestCapabilities;
import net.kenji.rpg_villager_quests.quest_system.quest_data.QuestInstance;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class StageCompletionPacket {
    private final String questId;
    private final String stageId;
    private final QuestEffects effects;

    public StageCompletionPacket(String questId, String stageId, QuestEffects questEffects) {
        this.questId = questId;
        this.stageId = stageId;
        this.effects = questEffects;
    }

    // Encode: Write data to buffer
    public static void encode(StageCompletionPacket packet, FriendlyByteBuf buf) {
        buf.writeUtf(packet.questId);
        buf.writeUtf(packet.stageId);
        if(packet.effects != null){
            buf.writeBoolean(packet.effects.giveReward);
            buf.writeBoolean(packet.effects.endQuest);
            if(packet.effects.removeItem != null) {
                buf.writeItemStack(packet.effects.removeItem, true);
                buf.writeInt(packet.effects.itemCount);
            }
            else{
                buf.writeItemStack(ItemStack.EMPTY, true);
                buf.writeInt(0);
            }
        }
        else{
            buf.writeBoolean(true);
            buf.writeBoolean(false);
            buf.writeItemStack(ItemStack.EMPTY, true);
            buf.writeInt(0);
        }
    }

    // Decode: Read data from buffer
    public static StageCompletionPacket decode(FriendlyByteBuf buf) {
        QuestEffects questEffects = new QuestEffects();
        String questId = buf.readUtf();
        String stageId = buf.readUtf();
        questEffects.giveReward = buf.readBoolean();
        questEffects.endQuest = buf.readBoolean();
        questEffects.removeItem = buf.readItem();
        questEffects.itemCount = buf.readInt();
        return new StageCompletionPacket(questId, stageId, questEffects);
    }

    // Handle: Process the packet on the receiving side
    public static void handle(StageCompletionPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Player player = ctx.get().getSender();
            if(player != null) {
                player.getCapability(QuestCapabilities.PLAYER_QUESTS).ifPresent((questData) -> {

                    QuestInstance questInstance = questData.getQuestInstance(packet.questId);
                    QuestStage questStage = questInstance.getQuest().getStageById(packet.stageId);

                    if (questStage != null && questStage.stageRewards != null) {
                        questStage.onComplete(packet.effects, player, questInstance);
                    }
                });
            }
        });
        ctx.get().setPacketHandled(true);
    }
}