package net.kenji.rpg_villager_quests.quest_system.quest_data;

import net.kenji.rpg_villager_quests.quest_system.Quest;
import net.minecraft.world.entity.player.Player;

import java.util.*;

public class QuestData {

    private static final Map<UUID, QuestData> questDataMap = new HashMap<>();
    private final Map<String, QuestInstance> activeQuests = new HashMap<>();

    public static QuestData get(Player player) {
        return questDataMap.computeIfAbsent(player.getUUID(), id -> new QuestData());
    }

    public Collection<QuestInstance> getActiveQuests() {
        return activeQuests.values();
    }

    public QuestInstance getQuestInstance(String questId) {
        return activeQuests.get(questId);
    }

    public QuestInstance addQuest(Quest quest) {
       QuestInstance instance = new QuestInstance(quest);
       activeQuests.put(quest.getQuestId(), instance);
       return instance;
    }
}

