package net.kenji.rpg_villager_quests.quest_system.stage_types;

import net.kenji.rpg_villager_quests.quest_system.Quest;
import net.kenji.rpg_villager_quests.quest_system.QuestStage;
import net.kenji.rpg_villager_quests.quest_system.QuestStageType;
import net.kenji.rpg_villager_quests.quest_system.interfaces.QuestObjective;
import net.kenji.rpg_villager_quests.quest_system.quest_data.QuestData;
import net.kenji.rpg_villager_quests.quest_system.quest_data.QuestInstance;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;

public class ObjectiveStage extends QuestStage {

    private final QuestObjective objective;


    public ObjectiveStage(String id, QuestObjective objective, List<Page> pages, String belongingQuest, String nextStageId) {
        super(id, QuestStageType.valueOf("objective".toUpperCase()), pages, belongingQuest, nextStageId);
        this.objective = objective;
    }
    @Override
    public void start(Player player) {
        QuestInstance questInstance =  QuestData.get(player).getQuestInstance(belongingQuestId);
        questInstance.setCurrentStage(getNextStage(player).id);
    }

    @Override
    public boolean isComplete(Player player) {
        return isComplete;
    }

    @Override
    public QuestStage getNextStage(Player player) {
        Quest quest =  QuestData.get(player).getQuestInstance(belongingQuestId).getQuest();
        return quest.getStageById(nextStageId);
    }

    @Override
    public List<String> getDialogue() {
        List<String> textList = new ArrayList<>();

        for(Page page : pages){
            textList.add(page.text);
        }
        return textList;
    }

    @Override
    public boolean canCompleteStage(Player player) {
        return this.objective.canComplete(player);
    }

    @Override
    public void onComplete(Player player) {
        isComplete = true;
        QuestInstance questInstance = QuestData.get(player).getQuestInstance(belongingQuestId);
        QuestStage nextStage = questInstance.getCurrentStage().getNextStage(player);
        if(nextStage != null) {
            nextStage.start(player);
        }
        else{
            questInstance.triggerQuestComplete(player);
        }

        objective.onComplete(player);
    }
}
