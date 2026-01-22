package net.kenji.rpg_villager_quests.quest_system.stage_types;

import net.kenji.rpg_villager_quests.quest_system.QuestChoice;
import net.kenji.rpg_villager_quests.quest_system.QuestStage;
import net.minecraft.world.entity.player.Player;

import java.util.List;

public class ChoiceStage extends QuestStage {

    public final List<QuestChoice> choices;
    public final List<String> choice1Pages;
    public final List<String> choice2Pages;

    public ChoiceType chosenDialogueOption = ChoiceType.UNCHOSEN;

    public enum ChoiceType {
        UNCHOSEN,
        OPTION_1,
        OPTION_2
    }

    public ChoiceStage(String id, List<QuestChoice> choices, List<String> pages, List<String> choice1Pages, List<String> choice2Pages) {
        super(id, QuestStageTypes.valueOf("dialogue_with_choice".toUpperCase()), pages);
        this.choices = choices;
        this.choice1Pages = choice1Pages;
        this.choice2Pages = choice2Pages;
    }

    public List<String> getDialogue(){
       return chosenDialogueOption == ChoiceType.OPTION_1 ? choice1Pages : chosenDialogueOption == ChoiceType.OPTION_2 ? choice2Pages : pages;
    }

    public void setChosenDialogue(ChoiceType choiceType){
        chosenDialogueOption = choiceType;
    }

    public boolean ShowChoices(int pageIndex){
        return pageIndex >= pages.size() - 1;
    }

    @Override
    public void start(Player player) {
        // UI renders choices
    }

    @Override
    public boolean isComplete(Player player) {
        return false; // completed by choice selection
    }

    @Override
    public String getNextStageId() {
        return null; // handled by choice
    }
}
