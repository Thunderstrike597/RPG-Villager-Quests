package net.kenji.rpg_villager_quests.quest_system.stage_types;

import net.kenji.rpg_villager_quests.quest_system.QuestStage;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;

public class DialogueStage extends QuestStage {

    private final String nextStage;

    public DialogueStage(String id, List<Page> pages, String nextStage) {
        super(id, QuestStageTypes.valueOf("dialogue".toUpperCase()), pages);
        this.nextStage = nextStage;
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
    public String getNextStageId() {
        return nextStage;
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
        return true;
    }
}
