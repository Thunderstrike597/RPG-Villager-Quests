package net.kenji.rpg_villager_quests.quest_system.quest_data;

import net.kenji.rpg_villager_quests.manager.VillagerQuestManager;
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
    private final Map<String, List<QuestInstance>> inactiveQuests = new HashMap<>();

    private final Map<String, List<QuestInstance>> activeQuests = new HashMap<>();
    private final Map<String, List<QuestInstance>> completedQuests = new HashMap<>();


    //------------DATA MANAGEMENT---------------//

    public static QuestData get(Player player) {
        if (player.level().isClientSide) {
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
    public static QuestData loadClientData(CompoundTag tag) {
        Player player = Minecraft.getInstance().player;
        if (player == null) return null;

        QuestData data = fromNBT(tag);
        return questDataMap.put(player.getUUID(), data);
    }

    public static void updateClientQuest(String questId, CompoundTag instanceData) {
        Player player = Minecraft.getInstance().player;
        if (player == null) return;

        QuestData data = questDataMap.computeIfAbsent(
                player.getUUID(),
                id -> new QuestData()
        );

        try {
            QuestInstance incoming = QuestInstance.fromNBT(questId, instanceData);
            UUID villagerUuid = incoming.getQuestVillager();

            List<QuestInstance> list = data.activeQuests
                    .computeIfAbsent(questId, id -> new ArrayList<>());

            // Replace existing instance from same villager
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).getQuestVillager().equals(villagerUuid)) {
                    list.set(i, incoming);
                    return;
                }
            }

            // Otherwise, new quest instance from new villager
            list.add(incoming);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();

        tag.put("ActiveQuests", serializeQuestMap(activeQuests));
        tag.put("CompletedQuests", serializeQuestMap(completedQuests));

        return tag;
    }

    private CompoundTag serializeQuestMap(Map<String, List<QuestInstance>> map) {
        CompoundTag questsTag = new CompoundTag();

        for (Map.Entry<String, List<QuestInstance>> entry : map.entrySet()) {
            ListTag list = new ListTag();
            for (QuestInstance instance : entry.getValue()) {
                list.add(instance.serializeNBT());
            }
            questsTag.put(entry.getKey(), list);
        }

        return questsTag;
    }

    public static QuestData fromNBT(CompoundTag tag) {
        QuestData data = new QuestData();

        deserializeQuestMap(tag.getCompound("ActiveQuests"), data.activeQuests);
        deserializeQuestMap(tag.getCompound("CompletedQuests"), data.completedQuests);

        return data;
    }

    private static void deserializeQuestMap(
            CompoundTag questsTag,
            Map<String, List<QuestInstance>> target
    ) {
        for (String questId : questsTag.getAllKeys()) {
            ListTag list = questsTag.getList(questId, Tag.TAG_COMPOUND);
            List<QuestInstance> instances = new ArrayList<>();

            for (int i = 0; i < list.size(); i++) {
                CompoundTag instanceTag = list.getCompound(i);
                instances.add(QuestInstance.fromNBT(questId, instanceTag));
            }

            target.put(questId, instances);
        }
    }

    private void markDirtyAndSync(ServerPlayer player, String questId, QuestInstance instance) {
        ServerLevel level = (ServerLevel) player.level();
        QuestSavedData.get(level).markDirty();

        // Sync specific quest update to client
        ModPacketHandler.sendToPlayer(new UpdateQuestProgressPacket(questId, instance), player);
    }

    //------------QUEST MANAGEMENT---------------//

    public boolean hasCompletedQuest(String questId, UUID villagerUuid) {
        for (List<QuestInstance> instances : completedQuests.values()) {
            for (QuestInstance q : instances) {
                if (q.getQuest().getQuestId().equals(questId)) {
                    if(q.getQuestVillager().equals(villagerUuid))
                        return true;
                }
            }
        }
        return false;
    }

    public boolean hasActiveQuest(String questId, UUID villagerUuid) {
        for (List<QuestInstance> instances : activeQuests.values()) {
            for (QuestInstance q : instances) {
                if (q.getQuest().getQuestId().equals(questId)) {
                    if(q.getQuestVillager().equals(villagerUuid))
                        return true;
                }
            }
        }
        return false;
    }

    public Collection<QuestInstance> getActiveQuests(Player player) {
        List<QuestInstance> finalActiveQuests = new ArrayList<>();
        for(List<QuestInstance> questInstances : activeQuests.values()){
            for(QuestInstance questInstance : questInstances){
                if(!questInstance.isComplete(player))
                    finalActiveQuests.add(questInstance);
            }
        }
        return finalActiveQuests;
    }
    public Collection<QuestInstance> getInactiveQuests(Player player) {
        List<QuestInstance> finalActiveQuests = new ArrayList<>();
        for(List<QuestInstance> questInstances : inactiveQuests.values()){
            for(QuestInstance questInstance : questInstances){
                if(!questInstance.isComplete(player))
                    finalActiveQuests.add(questInstance);
            }
        }
        return finalActiveQuests;
    }
    public Collection<QuestInstance> getCompletedQuests(){
        List<QuestInstance> finalActiveQuests = new ArrayList<>();
        for(List<QuestInstance> questInstances : completedQuests.values()){
            finalActiveQuests.addAll(questInstances);
        }
        return finalActiveQuests;
    }
    public QuestInstance addInactiveQuest(UUID villager){
        Quest quest = VillagerQuestManager.getVillagerQuest(villager);
        QuestInstance questInstance = new QuestInstance(quest, villager);
        if(quest != null) {
            if (inactiveQuests.containsKey(quest.getQuestId())) {

                inactiveQuests.get(quest.getQuestId()).add(questInstance);
            } else {
                List<QuestInstance> questInstances = new ArrayList<>();
                questInstances.add(questInstance);
                inactiveQuests.putIfAbsent(quest.getQuestId(), questInstances);
            }
        }
        return  questInstance;
    }

    public void addCompletedQuest(String questName, QuestInstance questInstance){
        if(completedQuests.containsKey(questName)){
            completedQuests.get(questName).add(questInstance);
        }
        else {
            List<QuestInstance> questInstances = new ArrayList<>();
            questInstances.add(questInstance);
            completedQuests.putIfAbsent(questName, questInstances);
        }
    }


    public void removeActiveQuest(String questName, UUID villagerUuid){
        List<QuestInstance> instances = activeQuests.get(questName);
        if(instances != null){
            instances.removeIf(qi -> qi.getQuestVillager().equals(villagerUuid));
            // Only remove the key if list is now empty
            if(instances.isEmpty()) {
                activeQuests.remove(questName);
            }
        }
    }
    public List<QuestInstance> getQuestInstances(String questId, boolean includeInactive) {
        List<QuestInstance> questInstances = new ArrayList<>();

        // Always add active quests
        questInstances.addAll(activeQuests.getOrDefault(questId, new ArrayList<>()));

        // Add inactive quests if requested
        if(includeInactive) {
            questInstances.addAll(inactiveQuests.getOrDefault(questId, new ArrayList<>()));
        }

        return questInstances;
    }
    public QuestInstance getQuestInstance(String questId, UUID villagerUuid, boolean includeInactive) {
        for (QuestInstance questInstance : getQuestInstances(questId, includeInactive)){
            if(questInstance.getQuestVillager().equals(villagerUuid)){
                return questInstance;
            }
        }
        return null;
    }
    public QuestInstance getCompletedQuestInstance(String questId, UUID villagerUuid) {
        List<QuestInstance> completedInstances = completedQuests.get(questId);
        QuestInstance fianlQuestInstance = null;
        if(completedInstances != null) {
            for (QuestInstance questInstance : completedInstances) {
                if (questInstance.getQuestVillager().equals(villagerUuid)) {
                    fianlQuestInstance = questInstance;
                    break;
                }
            }
        }
        return fianlQuestInstance;
    }

    public QuestInstance startQuest(Quest quest, UUID villager, ServerPlayer player) {
        QuestInstance instance = null;

        if(inactiveQuests.containsKey(quest.getQuestId())){
            for (QuestInstance questInstance : inactiveQuests.get(quest.getQuestId())){
                if(questInstance.getQuestVillager() == villager){
                    instance = questInstance;
                }
                else {
                    instance = new QuestInstance(quest, villager);
                }
            }
            if(inactiveQuests.isEmpty()){
                instance = new QuestInstance(quest, villager);
            }
        }
        else {
            instance = new QuestInstance(quest, villager);
        }

        // FIX: Add to activeQuests, not completedQuests!
        activeQuests.computeIfAbsent(quest.getQuestId(), id -> new ArrayList<>()).add(instance);

        instance.advanceFromCurrentStage(player);

        markDirtyAndSync(player, quest.getQuestId(), instance);
        syncToClient(player);
        return instance;
    }
}

