package net.kenji.rpg_villager_quests.quest_system;

import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Quest {
    public final String id;
    public final String type;
    public final List<QuestStage> stages;

    private int currentStageIndex = 0;

    private static Map<UUID, Boolean> isQuestActiveMap = new HashMap<>();


    public Quest(String id, String type, List<QuestStage> stages) {
        this.id = id;
        this.type = type;
        this.stages = stages;
    }

    public QuestStage getCurrentStage() {
        return stages.get(currentStageIndex);
    }

    public void advanceFromCurrentStage() {
        for(int i = 0; i < stages.size(); i++) {
            if (stages.get(i) == getCurrentStage() && i + 1 < stages.size()) {
                currentStageIndex = i + 1;
                return;
            }
        }
    }
    public boolean isQuestActive(Player player){
       return isQuestActiveMap.getOrDefault(player.getUUID(), false);
    }

    public void StartQuest(Player player){
        isQuestActiveMap.put(player.getUUID(), true);
    }
    public boolean isComplete() {
        return currentStageIndex >= stages.size();
    }
}

