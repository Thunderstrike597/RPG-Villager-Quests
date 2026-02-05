package net.kenji.rpg_villager_quests.quest_system.stage_types;

import net.kenji.rpg_villager_quests.quest_system.*;
import net.kenji.rpg_villager_quests.quest_system.enum_types.DialogueTypes;
import net.kenji.rpg_villager_quests.quest_system.enum_types.QuestStageType;
import net.kenji.rpg_villager_quests.quest_system.quest_data.QuestInstance;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class IntroStage extends QuestStage {
    public List<Page> currentIntroDialogue = new ArrayList<>();

    public IntroStage(String displayName, Dialogue introDialogue, String belongingQuestId, String nextStageId) {
        super("intro", displayName, QuestStageType.INTRO, introDialogue, belongingQuestId, nextStageId, null, null, 0);
        setIntroDialogue(DialogueTypes.MAIN);
    }

    public void setIntroDialogue(DialogueTypes type){
       switch (type){
           case POSITIVE -> currentIntroDialogue = this.dialogue.positive.pages;
           case NEGATIVE -> currentIntroDialogue = this.dialogue.negative.pages;
           case MAIN -> {
               assert this.dialogue.main != null;
               currentIntroDialogue = this.dialogue.main.pages;
           }
       }
    }



    @Override
    public void start(ServerPlayer player, QuestInstance questInstance) {

    }

    @Override
    public boolean isComplete(Player player) {
        return isComplete;
    }

    @Override
    public QuestStage getNextStage(Player player, QuestInstance questInstance) {
        return questInstance.getQuest().stages.get(1);
    }

    @Override
    public void onComplete(QuestEffects completionEffects, ServerPlayer player, QuestInstance questInstance) {
        questInstance.advanceFromCurrentStage(player);
    }

    @Override
    public List<Page> getDialogue(Player player, QuestInstance questInstance, UUID interactVillager) {
        return currentIntroDialogue;
    }

    @Override
    public boolean canCompleteStage(Player player, QuestInstance questInstance, UUID villager) {
        return true;
    }

    @Override
    public boolean canCompleteStage(int currentPageIndex, Player player) {
        return true;
    }
    @Override
    public List<Page> getMainPages() {
        return this.dialogue.main.pages;
    }
}
