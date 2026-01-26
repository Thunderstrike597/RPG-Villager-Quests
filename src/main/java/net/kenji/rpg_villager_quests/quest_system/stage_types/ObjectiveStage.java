package net.kenji.rpg_villager_quests.quest_system.stage_types;

import net.kenji.rpg_villager_quests.quest_system.*;
import net.kenji.rpg_villager_quests.quest_system.interfaces.QuestObjective;
import net.kenji.rpg_villager_quests.quest_system.interfaces.QuestReward;
import net.kenji.rpg_villager_quests.quest_system.quest_data.QuestInstance;
import net.minecraft.world.entity.player.Player;

import java.util.List;

public class ObjectiveStage extends QuestStage {

    private final QuestObjective objective;
    private final QuestEffects effects;

    public ObjectiveStage(String id, QuestObjective objective, List<Page> pages, String belongingQuest, String nextStageId, QuestEffects stageEffects, List<QuestReward> questReward, String tag) {
        super(id, QuestStageType.valueOf("objective".toUpperCase()), pages, belongingQuest, nextStageId, questReward, tag);
        this.objective = objective;
        this.effects = stageEffects;
    }

    public QuestEffects getStageEffects(){
        return effects;
    }
    public QuestObjective getObjective(){
        return this.objective;
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
    public void onComplete(QuestEffects completionEffects, Player player, QuestInstance questInstance) {
        isComplete = true;
        QuestStage nextStage = getNextStage(player, questInstance);

        if (completionEffects != null) {
            if (completionEffects.giveReward) {
                if(stageRewards != null) {
                    for (QuestReward reward : stageRewards){
                        reward.apply(player);
                    }
                }
            }
            completionEffects.apply(player);
        }
        if (nextStage != null) {
            nextStage.start(player, questInstance);
        } else {
            questInstance.triggerQuestComplete(completionEffects, player);
        }
        objective.onComplete(completionEffects, player);
    }

}
