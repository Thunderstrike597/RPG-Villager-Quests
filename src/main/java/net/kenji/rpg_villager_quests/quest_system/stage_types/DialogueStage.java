package net.kenji.rpg_villager_quests.quest_system.stage_types;

import net.kenji.rpg_villager_quests.network.ModPacketHandler;
import net.kenji.rpg_villager_quests.network.packets.StageStartPacket;
import net.kenji.rpg_villager_quests.quest_system.*;
import net.kenji.rpg_villager_quests.quest_system.interfaces.QuestReward;
import net.kenji.rpg_villager_quests.quest_system.objective_types.SecondaryVillagerQuestObjective;
import net.kenji.rpg_villager_quests.quest_system.quest_data.QuestInstance;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import org.jline.utils.Log;

import java.util.List;
import java.util.UUID;

public class DialogueStage extends QuestStage {

    public final List<QuestChoice> choices;
    public final List<Page> choice1Pages;
    public final List<Page> choice2Pages;

    public ChoiceType chosenDialogueOption = ChoiceType.UNCHOSEN;

    public List<Page> currentDialoguePages = pages;


    public enum DialogueType{
        REGULAR,
        CHOICE
    }

    public enum ChoiceType {
        UNCHOSEN,
        OPTION_1,
        OPTION_2
    }
    public DialogueStage(String id, List<Page> pages, String belongingQuest, String nextStageId, List<QuestChoice> choices, List<Page> choice1Pages, List<Page> choice2Pages, List<QuestReward> questReward, String tag) {
        super(id, QuestStageType.valueOf("dialogue".toUpperCase()), pages, belongingQuest, nextStageId, questReward, tag);
        this.choice1Pages = choice1Pages;
        this.choice2Pages = choice2Pages;
        this.choices = choices;
    }
    @Override
    public void start(Player player, QuestInstance questInstance) {
        ModPacketHandler.sendToServer(new StageStartPacket(belongingQuestId, id));
        questInstance.setCurrentStage(id);
    }

    @Override
    public boolean isComplete(Player player) {
        return isComplete; // dialogue finishes via UI
    }

    @Override
    public QuestStage getNextStage(Player player, QuestInstance questInstance) {
        Quest quest = questInstance.getQuest();

        // 1️⃣ Explicit override
        if (nextStageId != null) {
            return quest.getStageById(nextStageId);
        }

        // 2️⃣ Fallback: advance by index
        List<QuestStage> stages = quest.stages;
        QuestStage current = questInstance.getCurrentStage();

        int index = stages.indexOf(current);

        if (index == -1) {
            return null; // stage not found → fail safely
        }

        int nextIndex = index + 1;

        if (nextIndex >= stages.size()) {
            return null; // no more stages → quest complete
        }

        return stages.get(nextIndex);
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
            questInstance.advanceFromCurrentStage(player);
        } else {
            questInstance.triggerQuestComplete(completionEffects, player);
        }
    }
    public List<Page> getDialogue(Player player,QuestInstance questInstance, UUID interactVillager){
        if(!questInstance.isComplete()) {
            List<Page> pageList;
            if (choices != null && !choices.isEmpty()) {
                pageList = chosenDialogueOption == ChoiceType.OPTION_1 ? choice1Pages : chosenDialogueOption == ChoiceType.OPTION_2 ? choice2Pages : pages;
            } else {
                if (interactVillager.equals(questInstance.currentSecondaryEntity)) {
                    for (QuestStage stage : questInstance.getQuest().stages) {
                        if (!(stage instanceof ObjectiveStage objectiveStage)) continue;
                        if (!(objectiveStage.getObjective() instanceof SecondaryVillagerQuestObjective objective)) continue;
                        if (!objectiveStage.isComplete(player)) continue;
                        QuestStage next = objectiveStage.getNextStage(player, questInstance);


                        if (next == null || next.isComplete(player)) continue;

                        // Only play secondary-completed dialogue on the secondary villager
                        return objective.completedSecondaryVillagerDialogue;
                    }
                }

                pageList = pages;
            }

            return pageList;
        }
        Log.info("STAGE IS COMPLETE");

        return questInstance.getQuest().getCompletionDialogue(questInstance);
    }

    @Override
    public boolean canCompleteStage(Player player, QuestInstance questInstance, UUID villager) {
        for (QuestStage stage : questInstance.getQuest().stages) {
            if (!(stage instanceof ObjectiveStage objectiveStage)) continue;
            if (!(objectiveStage.getObjective() instanceof SecondaryVillagerQuestObjective objective)) continue;
            if(getDialogue(player, questInstance, villager) == objective.completedSecondaryVillagerDialogue){
                return false;
            }
        }
        return true;
    }

    public void setChosenDialogue(ChoiceType choiceType){
        chosenDialogueOption = choiceType;
        if(chosenDialogueOption == ChoiceType.OPTION_1 && choice1Pages != null)
            currentDialoguePages = choice1Pages;
        if(chosenDialogueOption == ChoiceType.OPTION_2 && choice2Pages != null)
            currentDialoguePages = choice2Pages;
    }

    @Override
    public boolean canCompleteStage(int currentPageIndex, Player player) {
        if(choices != null){
           ChoiceType option = chosenDialogueOption ;
           if(option == ChoiceType.OPTION_1){
               if(choices.get(0) != null) {
                   if(choices.get(0).effects != null) {
                       if (choices.get(0).effects.endQuest) {
                           return true;
                       }
                   }
               }

           }
            if(option == ChoiceType.OPTION_2){
                if(choices.get(1) != null) {
                    if(choices.get(0).effects != null) {

                        if (choices.get(1).effects.endQuest) {
                            return true;
                        }
                    }
                }
            }
        }
        return currentPageIndex >= currentDialoguePages.size() - 1;
    }

}
