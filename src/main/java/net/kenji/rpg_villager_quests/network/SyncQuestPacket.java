package net.kenji.rpg_villager_quests.network;

import net.kenji.rpg_villager_quests.manager.VillagerQuestManager;
import net.kenji.rpg_villager_quests.quest_system.Quest;
import net.kenji.rpg_villager_quests.quest_system.quest_data.QuestData;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SyncQuestPacket {

    private final String questId;

    public SyncQuestPacket(String questId) {
        this.questId = questId;
    }

    /* ---------------- ENCODE / DECODE ---------------- */

    public static void encode(SyncQuestPacket packet, FriendlyByteBuf buf) {
        buf.writeUtf(packet.questId);
    }

    public static SyncQuestPacket decode(FriendlyByteBuf buf) {
        return new SyncQuestPacket(buf.readUtf());
    }

    /* ---------------- HANDLE (CLIENT SIDE) ---------------- */

    public static void handle(SyncQuestPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            // CLIENT ONLY
            if (Minecraft.getInstance().player == null) return;

            QuestData questData = QuestData.get(Minecraft.getInstance().player.getUUID());
            Quest quest;
            try {
               quest = VillagerQuestManager.getQuestByName(packet.questId);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            questData.startQuestClient(quest);
        });

        ctx.get().setPacketHandled(true);
    }
}
