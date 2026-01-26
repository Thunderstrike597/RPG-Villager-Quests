package net.kenji.rpg_villager_quests.quest_system.quest_data;

import net.kenji.rpg_villager_quests.network.packets.AddQuestPacket;
import net.kenji.rpg_villager_quests.network.ModPacketHandler;
import net.kenji.rpg_villager_quests.quest_system.Quest;
import net.minecraft.world.entity.npc.Villager;

import java.util.*;

public class QuestData {

    private static final Map<UUID, QuestData> questDataMap = new HashMap<>();
    private final Map<String, QuestInstance> activeQuests = new HashMap<>();
    private final Map<String, QuestInstance> completedQuests = new HashMap<>();

    public static QuestData get(UUID playerId) {
        return questDataMap.computeIfAbsent(playerId, id -> new QuestData());
    }

    public Collection<QuestInstance> getActiveQuests() {
        return activeQuests.values();
    }
    public void addCompetedQuest(String questName, QuestInstance questInstance){
        if(completedQuests.get(questName) == null){
            activeQuests.put(questName, questInstance);
        }
    }
    public void removeActiveQuest(String questName){
        if(activeQuests.get(questName) != null){
            activeQuests.remove(questName);
        }
    }

    public QuestInstance getQuestInstance(String questId) {
        return activeQuests.get(questId);
    }
    public void putQuest(Quest quest, Villager villager){
        QuestInstance instance = new QuestInstance(quest, villager);
        activeQuests.put(quest.getQuestId(), instance);
    }

    public QuestInstance startQuestClient(Quest quest, Villager villager) {
        QuestInstance instance = new QuestInstance(quest, villager);
        activeQuests.put(quest.getQuestId(), instance);
        villager.setGlowingTag(true);
        return instance;
    }
    public void startQuestServer(String questId, Villager villager) {
        ModPacketHandler.sendToServer(new AddQuestPacket(questId, villager.getUUID()));
    }
}

