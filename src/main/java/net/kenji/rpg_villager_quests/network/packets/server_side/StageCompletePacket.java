package net.kenji.rpg_villager_quests.network.packets.server_side;

import net.kenji.rpg_villager_quests.quest_system.QuestEffects;
import net.kenji.rpg_villager_quests.quest_system.QuestStage;
import net.kenji.rpg_villager_quests.quest_system.quest_data.QuestData;
import net.kenji.rpg_villager_quests.quest_system.quest_data.QuestInstance;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import org.jline.utils.Log;

import java.util.UUID;
import java.util.function.Supplier;

public class StageCompletePacket {
    private final String questId;
    private final String stageId;
    private final QuestEffects effects;
    private final UUID villagerUuid;

    public StageCompletePacket(String questId, String stageId, QuestEffects questEffects, UUID villagerUuid) {
        this.questId = questId;
        this.stageId = stageId;
        this.effects = questEffects;

        this.villagerUuid = villagerUuid;
    }

    // Encode: Write data to buffer
    public static void encode(StageCompletePacket packet, FriendlyByteBuf buf) {
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
        buf.writeUUID(packet.villagerUuid);
    }

    // Decode: Read data from buffer
    public static StageCompletePacket decode(FriendlyByteBuf buf) {
        QuestEffects questEffects = new QuestEffects();
        String questId = buf.readUtf();
        String stageId = buf.readUtf();
        questEffects.giveReward = buf.readBoolean();
        questEffects.endQuest = buf.readBoolean();
        questEffects.removeItem = buf.readItem();
        questEffects.itemCount = buf.readInt();
        UUID villagerUuid = buf.readUUID();
        return new StageCompletePacket(questId, stageId, questEffects, villagerUuid);
    }

    // Handle: Process the packet on the receiving side
    public static void handle(StageCompletePacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if(player != null) {
                QuestData questData = QuestData.get(player);
                QuestInstance questInstance = questData.getQuestInstance(packet.questId, packet.villagerUuid, false);
                QuestStage questStage = questInstance.getQuest().getStageById(packet.stageId);

                if(questStage != null) {
                    questStage.onComplete(packet.effects, player, questInstance);
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}