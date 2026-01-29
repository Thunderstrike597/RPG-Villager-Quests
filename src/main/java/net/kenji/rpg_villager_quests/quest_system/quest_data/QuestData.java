package net.kenji.rpg_villager_quests.quest_system.quest_data;

import net.kenji.rpg_villager_quests.network.ModPacketHandler;
import net.kenji.rpg_villager_quests.network.packets.SyncQuestDataPacket;
import net.kenji.rpg_villager_quests.network.packets.UpdateQuestProgressPacket;
import net.kenji.rpg_villager_quests.quest_system.Quest;
import net.kenji.rpg_villager_quests.quest_system.quest_data.saved_data.QuestSavedData;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.jline.utils.Log;

import java.util.*;

public class QuestData {

    private static final Map<UUID, QuestData> questDataMap = new HashMap<>();
    private final Map<String, QuestInstance> activeQuests = new HashMap<>();
    private final Map<String, QuestInstance> completedQuests = new HashMap<>();


    //------------DATA MANAGEMENT---------------//

    public static QuestData get(Player player) {
        if (player.level().isClientSide) {
            // Client just uses static map
            return questDataMap.computeIfAbsent(player.getUUID(), id -> new QuestData());
        } else {
            ServerLevel level = (ServerLevel) player.level();
            QuestSavedData savedData = QuestSavedData.get(level);
            QuestData data = savedData.getQuestData(player.getUUID());

            // Mirror server_side QuestData in static map for consistency
            questDataMap.put(player.getUUID(), data);

            return data;
        }
    }
    public static void syncToClient(ServerPlayer player) {
        QuestData data = get(player);
        CompoundTag tag = data.serializeNBT();
        ModPacketHandler.sendToPlayer(new SyncQuestDataPacket(tag), player);
    }

    // Client-side: load synced data
    public static void loadClientData(CompoundTag tag) {
        Player player = Minecraft.getInstance().player;
        if (player == null) return;

        QuestData data = fromNBT(tag);
        questDataMap.put(player.getUUID(), data);
    }

    // Client-side: update specific quest
    public static void updateClientQuest(String questId, CompoundTag instanceData) {
        Player player = Minecraft.getInstance().player;
        if (player == null) return;

        QuestData data = questDataMap.computeIfAbsent(player.getUUID(), id -> new QuestData());
        try {
            QuestInstance instance = QuestInstance.fromNBT(questId, instanceData);
            data.activeQuests.put(questId, instance);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();

        ListTag activeList = new ListTag();
        for (QuestInstance instance : activeQuests.values()) {
            activeList.add(instance.serializeNBT());
        }
        tag.put("ActiveQuests", activeList);

        ListTag completedList = new ListTag();
        for (QuestInstance instance : completedQuests.values()) {
            completedList.add(instance.serializeNBT());
        }
        tag.put("CompletedQuests", completedList);

        return tag;
    }

    public static QuestData fromNBT(CompoundTag tag) {
        QuestData data = new QuestData();

        ListTag activeList = tag.getList("ActiveQuests", Tag.TAG_COMPOUND);
        for (int i = 0; i < activeList.size(); i++) {
            try {
                CompoundTag questTag = activeList.getCompound(i);
                // Get the questId from the tag itself
                String questId = questTag.getString("QuestId");

                if (questId == null || questId.isEmpty()) {
                    System.err.println("Skipping quest with missing QuestId");
                    continue;
                }

                QuestInstance instance = QuestInstance.fromNBT(questId, questTag);
                data.activeQuests.put(instance.getQuest().getQuestId(), instance);
            } catch (Exception e) {
                System.err.println("Failed to load active quest " + i);
                e.printStackTrace();
            }
        }

        ListTag completedList = tag.getList("CompletedQuests", Tag.TAG_COMPOUND);
        for (int i = 0; i < completedList.size(); i++) {
            try {
                CompoundTag questTag = completedList.getCompound(i);
                // Get the questId from the tag itself
                String questId = questTag.getString("QuestId");

                if (questId == null || questId.isEmpty()) {
                    System.err.println("Skipping completed quest with missing QuestId");
                    continue;
                }

                QuestInstance instance = QuestInstance.fromNBT(questId, questTag);
                data.completedQuests.put(instance.getQuest().getQuestId(), instance);
            } catch (Exception e) {
                System.err.println("Failed to load completed quest " + i);
                e.printStackTrace();
            }
        }

        return data;
    }

    private void markDirtyAndSync(ServerPlayer player, String questId, QuestInstance instance) {
        ServerLevel level = (ServerLevel) player.level();
        QuestSavedData.get(level).markDirty();

        // Sync specific quest update to client
        ModPacketHandler.sendToPlayer(new UpdateQuestProgressPacket(questId, instance), player);
    }

    //------------QUEST MANAGEMENT---------------//

    public boolean hasCompletedQuest(String questId) {
        for (QuestInstance q : completedQuests.values()) {
            if (q.getQuest().getQuestId().equals(questId)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasActiveQuest(String questId) {
        for (QuestInstance q : activeQuests.values()) {
            if (q.getQuest().getQuestId().equals(questId)) {
                return true;
            }
        }
        return false;
    }

    public Collection<QuestInstance> getActiveQuests() {
        return activeQuests.values();
    }
    public Collection<QuestInstance> getCompletedQuests(){
        return completedQuests.values();
    }
    public void addCompletedQuest(String questName, QuestInstance questInstance){
        completedQuests.putIfAbsent(questName, questInstance);
    }
    public void removeActiveQuest(String questName){
        if(activeQuests.get(questName) != null){
            activeQuests.remove(questName);
        }
    }

    public QuestInstance getQuestInstance(String questId) {
        QuestInstance activeInstance = activeQuests.get(questId);
        return activeInstance;
    }
    public QuestInstance getCompletedQuestInstance(String questId) {
        QuestInstance activeInstance = completedQuests.get(questId);
        return activeInstance;
    }

    public QuestInstance startQuest(Quest quest, UUID villager, ServerPlayer player) {
        QuestInstance instance = new QuestInstance(quest, villager);
        activeQuests.put(quest.getQuestId(), instance);
        QuestInstance questInstance = getQuestInstance(quest.getQuestId());

        if(questInstance != null){
            questInstance.advanceFromCurrentStage(player);
        }
        markDirtyAndSync((ServerPlayer) player, quest.getQuestId(), instance);
        syncToClient(player);
        return instance;
    }
}

