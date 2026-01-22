package net.kenji.rpg_villager_quests.quest_system.stage_types;

import net.kenji.rpg_villager_quests.quest_system.Quest;
import net.kenji.rpg_villager_quests.quest_system.QuestChoice;
import net.kenji.rpg_villager_quests.quest_system.QuestStage;
import net.kenji.rpg_villager_quests.quest_system.QuestStageType;
import net.kenji.rpg_villager_quests.quest_system.quest_data.QuestData;
import net.kenji.rpg_villager_quests.quest_system.quest_data.QuestInstance;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;

public class DialogueStage extends QuestStage {

    public final List<QuestChoice> choices;

    public final List<Page> choice1Pages;
    public final List<Page> choice2Pages;

    public ChoiceType chosenDialogueOption = ChoiceType.UNCHOSEN;


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
    public void start(Player player) {
        // UI opens here, pages are displayed
    }

    @Override
    public boolean isComplete(Player player) {
        return true; // dialogue finishes via UI
    }

    @Override
    public QuestStage getNextStage(Player player) {
        Quest quest =  QuestData.get(player).getQuestInstance(belongingQuestId).getQuest();
        return quest.getStageById(nextStageId);
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
    }
    public List<String> getDialogue(){
        List<Page> pageList;
        if(choices != null){
            pageList = chosenDialogueOption == ChoiceType.OPTION_1 ? choice1Pages : chosenDialogueOption == ChoiceType.OPTION_2 ? choice2Pages : pages;
        }
        else pageList = pages;


        List<String> textList = new ArrayList<>();
        for(Page page : pageList){
            textList.add(page.text);
        }

        return textList;
    }

    public void setChosenDialogue(ChoiceType choiceType){
        chosenDialogueOption = choiceType;
    }

    @Override
    public boolean canCompleteStage(Player player) {
        return true;
    }

}
