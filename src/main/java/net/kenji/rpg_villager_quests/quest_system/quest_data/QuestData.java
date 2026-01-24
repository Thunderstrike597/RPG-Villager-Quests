package net.kenji.rpg_villager_quests.quest_system.quest_data;

import net.kenji.rpg_villager_quests.network.AddQuestPacket;
import net.kenji.rpg_villager_quests.network.ModPacketHandler;
import net.kenji.rpg_villager_quests.quest_system.Quest;

import java.util.*;

public class QuestData {

    private static final Map<UUID, QuestData> questDataMap = new HashMap<>();
    private final Map<String, QuestInstance> activeQuests = new HashMap<>();

    public static QuestData get(UUID playerId) {
        return questDataMap.computeIfAbsent(playerId, id -> new QuestData());
    }

    public Collection<QuestInstance> getActiveQuests() {
        return activeQuests.values();
    }

    public QuestInstance getQuestInstance(String questId) {
        return activeQuests.get(questId);
    }
    public void putQuest(Quest quest){
        QuestInstance instance = new QuestInstance(quest);
        activeQuests.put(quest.getQuestId(), instance);
    }

    public QuestInstance startQuestClient(Quest quest) {
        QuestInstance instance = new QuestInstance(quest);
        activeQuests.put(quest.getQuestId(), instance);
        return instance;
    }
    public void startQuestServer(String questId) {
        ModPacketHandler.sendToServer(new AddQuestPacket(questId));

    }
}

