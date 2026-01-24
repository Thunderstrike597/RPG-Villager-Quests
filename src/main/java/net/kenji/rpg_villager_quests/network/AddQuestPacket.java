package net.kenji.rpg_villager_quests.network;

import net.kenji.rpg_villager_quests.manager.VillagerQuestManager;
import net.kenji.rpg_villager_quests.quest_system.Quest;
import net.kenji.rpg_villager_quests.quest_system.QuestStage;
import net.kenji.rpg_villager_quests.quest_system.interfaces.QuestReward;
import net.kenji.rpg_villager_quests.quest_system.quest_data.QuestData;
import net.kenji.rpg_villager_quests.quest_system.quest_data.QuestInstance;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class AddQuestPacket {
    private final String questId;

    public AddQuestPacket(String questId) {
        this.questId = questId;
    }

    // Encode: Write data to buffer
    public static void encode(AddQuestPacket packet, FriendlyByteBuf buf) {
        buf.writeUtf(packet.questId);
    }

    // Decode: Read data from buffer
    public static AddQuestPacket decode(FriendlyByteBuf buf) {
        String questId = buf.readUtf();
        return new AddQuestPacket(questId);
    }

    // Handle: Process the packet on the receiving side
    public static void handle(AddQuestPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            Quest quest = null;
            try {
                quest = VillagerQuestManager.getQuestByName(packet.questId);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            QuestData questData = QuestData.get(player.getUUID());

            if (questData.getQuestInstance(packet.questId) != null) return;

            questData.putQuest(quest);

        });
        ctx.get().setPacketHandled(true);
    }
}