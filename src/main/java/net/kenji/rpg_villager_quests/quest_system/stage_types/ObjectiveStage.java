package net.kenji.rpg_villager_quests.quest_system.stage_types;

import net.kenji.rpg_villager_quests.quest_system.Page;
import net.kenji.rpg_villager_quests.quest_system.Quest;
import net.kenji.rpg_villager_quests.quest_system.QuestStage;
import net.kenji.rpg_villager_quests.quest_system.QuestStageType;
import net.kenji.rpg_villager_quests.quest_system.interfaces.QuestObjective;
import net.kenji.rpg_villager_quests.quest_system.quest_data.QuestInstance;
import net.minecraft.world.entity.player.Player;

import java.util.List;

public class ObjectiveStage extends QuestStage {

    private final QuestObjective objective;


    public ObjectiveStage(String id, QuestObjective objective, List<Page> pages, String belongingQuest, String nextStageId) {
        super(id, QuestStageType.valueOf("objective".toUpperCase()), pages, belongingQuest, nextStageId);
        this.objective = objective;
    }
    @Override
    public void start(Player player, QuestInstance questInstance) {
        questInstance.setCurrentStage(this.id);
    }

    @Override
    public boolean isComplete(Player player) {
        return isComplete;
    }

    @Override
    public QuestStage getNextStage(Player player, QuestInstance questInstance) {
        Quest quest =  questInstance.getQuest();
        return quest.getStageById(nextStageId);
    }

    @Override
    public List<Page> getDialogue(QuestInstance questInstance) {
        if(!questInstance.isComplete()) {
            return pages;
        }
        return questInstance.getQuest().getCompletionDialogue(questInstance);

    }

    @Override
    public boolean canCompleteStage(Player player) {
        return this.objective.canComplete(player);
    }

    @Override
    public boolean canCompleteStage(int currentPageIndex, Player player) {
        return currentPageIndex >= pages.size() - 1;
    }

    @Override
    public void onComplete(Player player, QuestInstance questInstance) {
        isComplete = true;
        QuestStage nextStage = getNextStage(player, questInstance);
        if(nextStage != null) {
            nextStage.start(player, questInstance);
        }
        else{
            questInstance.triggerQuestComplete(player);
        }

        objective.onComplete(player);
    }
}
