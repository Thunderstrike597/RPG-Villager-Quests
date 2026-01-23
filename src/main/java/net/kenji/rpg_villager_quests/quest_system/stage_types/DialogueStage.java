package net.kenji.rpg_villager_quests.quest_system.stage_types;

import net.kenji.rpg_villager_quests.quest_system.*;
import net.kenji.rpg_villager_quests.quest_system.quest_data.QuestInstance;
import net.minecraft.world.entity.player.Player;

import java.util.List;

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
    public DialogueStage(String id, List<Page> pages, String belongingQuest, String nextStageId, List<QuestChoice> choices, List<Page> choice1Pages, List<Page> choice2Pages) {
        super(id, QuestStageType.valueOf("dialogue".toUpperCase()), pages, belongingQuest, nextStageId);
        this.choice1Pages = choice1Pages;
        this.choice2Pages = choice2Pages;
        this.choices = choices;
    }
    @Override
    public void start(Player player, QuestInstance questInstance) {
        questInstance.setCurrentStage(this.id);
    }

    @Override
    public boolean isComplete(Player player) {
        return true; // dialogue finishes via UI
    }

    @Override
    public QuestStage getNextStage(Player player, QuestInstance questInstance) {
        Quest quest =  questInstance.getQuest();
        return quest.getStageById(nextStageId);
    }

    @Override
    public void onComplete(Player player, QuestInstance questInstance) {
        isComplete = true;
        QuestStage nextStage = questInstance.getCurrentStage().getNextStage(player, questInstance);
        if(nextStage != null) {
            nextStage.start(player, questInstance);
        }
        else{
            questInstance.triggerQuestComplete(player);
        }
    }
    public List<Page> getDialogue(QuestInstance questInstance){
        if(!questInstance.isComplete()) {
            List<Page> pageList;
            if (choices != null) {
                pageList = chosenDialogueOption == ChoiceType.OPTION_1 ? choice1Pages : chosenDialogueOption == ChoiceType.OPTION_2 ? choice2Pages : pages;
            } else pageList = pages;

            return pageList;
        }
        return questInstance.getQuest().getCompletionDialogue(questInstance);
    }

    @Override
    public boolean canCompleteStage(Player player) {
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
                   if (choices.get(0).endQuest) {
                       return true;
                   }
               }

           }
            if(option == ChoiceType.OPTION_2){
                if(choices.get(1) != null) {
                    if (choices.get(1).endQuest) {
                        return true;
                    }
                }
            }
        }
        return currentPageIndex >= currentDialoguePages.size() - 1;
    }

}
