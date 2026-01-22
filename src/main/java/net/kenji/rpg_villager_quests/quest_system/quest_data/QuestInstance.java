package net.kenji.rpg_villager_quests.quest_system.quest_data;

import net.kenji.rpg_villager_quests.quest_system.Quest;
import net.kenji.rpg_villager_quests.quest_system.QuestStage;
import net.minecraft.world.entity.player.Player;

public class QuestInstance {
    private final Quest questDefinition;
    private int currentStageIndex;
    private boolean completed;

    public QuestInstance(Quest quest) {
        this.questDefinition = quest;
        this.currentStageIndex = 0;
        this.completed = false;
    }

    public void advanceFromCurrentStage(Player player) {
        QuestData questData = QuestData.get(player);
        QuestInstance questInstance = questData.getQuestInstance(questDefinition.getQuestId());
        if(questInstance != null) {
            for (int i = 0; i < questDefinition.stages.size(); i++) {
                if (questDefinition.stages.get(i) == questInstance.getCurrentStage() && i + 1 < questDefinition.stages.size()) {
                    currentStageIndex = i + 1;
                    return;
                }
            }
        }
    }

    public boolean isComplete() {
        return currentStageIndex >= questDefinition.stages.size() || completed;
    }
    public void triggerQuestComplete(Player player){
        completed = true;
        questDefinition.onQuestComplete(player);
    }

    public Quest getQuest() {
        return questDefinition;
    }
    public void setCurrentStage(String id) {
        questDefinition.setCurrentStageIndex(id);
    }

    public QuestStage getCurrentStage() {
        return getQuest().stages.get(currentStageIndex);
    }
    public int getCurrentStageIndex() {
       for(int i = 0; i < getQuest().stages.stream().count(); i++) {
           if (getQuest().stages.get(i) == getCurrentStage()) {
               return i;
           }
       }
        return 0;
    }
}