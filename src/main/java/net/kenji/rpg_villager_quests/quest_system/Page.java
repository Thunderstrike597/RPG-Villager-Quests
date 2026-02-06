package net.kenji.rpg_villager_quests.quest_system;

import net.kenji.rpg_villager_quests.quest_system.enum_types.ChoiceType;
import net.kenji.rpg_villager_quests.quest_system.enum_types.LoadPageTypes;
import net.kenji.rpg_villager_quests.quest_system.quest_data.QuestInstance;
import net.kenji.rpg_villager_quests.quest_system.stage_types.DialogueStage;
import net.minecraft.world.entity.player.Player;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class Page {
    public DialogueStage.DialogueType dialogueType = DialogueStage.DialogueType.REGULAR;
    public QuestEffects effects;
    public String text;
    public String button1Text;
    public String button2Text;

    public String tag;
    public boolean startQuest = false;

    public static class PageSet{
        public final boolean loadNextPage;
        public final DialogueStage.DialogueType dialogueType;
        public final List<Page> nextPages;
        public PageSet(boolean loadNextPage, DialogueStage.DialogueType dialogueType, List<Page> nextPages){
            this.loadNextPage = loadNextPage;
            this.dialogueType = dialogueType;
            this.nextPages = nextPages;
        }
    }

    public LoadPageTypes loadNextPage(Player player, ChoiceType choiceType , QuestInstance questInstance, List<Page> pages, int currentPageIndex, UUID villager) {
        if (questInstance != null) {
            switch (choiceType) {
                case OPTION_1 -> {
                    if (pages.get(currentPageIndex).dialogueType == DialogueStage.DialogueType.CHOICE) {
                        return LoadPageTypes.CHOOSE_POSITIVE_DIALOGUE_OPTION;
                    } else {

                        if (questInstance.getQuest().reconsiderDialogueSet.positive != null && questInstance.getQuest().reconsiderDialogueSet.main != null) {
                            if (currentPageIndex >= pages.size() - 1) {
                                if (pages == questInstance.getQuest().reconsiderDialogueSet.main.pages) {
                                    return LoadPageTypes.RECONSIDER_POSITIVE;
                                }
                                if (pages == questInstance.getQuest().reconsiderDialogueSet.positive.pages) {
                                    return LoadPageTypes.RECONTINUE_DIALOGUE;
                                }
                            }
                        }

                        if (currentPageIndex < pages.size() - 1) {
                            return LoadPageTypes.NEXT_MAIN_PAGE;
                        }
                        if (currentPageIndex >= pages.size() - 1) {
                            if(questInstance.getCurrentStage().interruptedDialogue.main != null) {
                                if (pages == questInstance.getCurrentStage().interruptedDialogue.main.pages || pages == questInstance.getCurrentStage().interruptedDialogue.main.altPages){
                                    return LoadPageTypes.RESUME_DIALOGUE;
                                }
                            }
                            if (questInstance.getQuest().stages.get(0).dialogueSet.positive != null) {
                                if (pages == questInstance.getQuest().stages.get(0).dialogueSet.positive.pages)
                                    return LoadPageTypes.CLOSE_DIALOGUE;
                                if (pages == Objects.requireNonNull(questInstance.getQuest().stages.get(0).dialogueSet.main).pages)
                                    return LoadPageTypes.ACCEPT_DIALOGUE;
                            } else if (questInstance.getCurrentStage() == questInstance.getQuest().stages.get(0))
                                if (pages == Objects.requireNonNull(questInstance.getQuest().stages.get(0).dialogueSet.main).pages) {
                                    return LoadPageTypes.ACCEPT_QUEST;
                                }
                            if(!questInstance.isComplete(player)) {
                                if (questInstance.getCurrentStage().canCompleteStage(player, questInstance, villager)) {
                                    if (questInstance.getCurrentStage().getNextStage(player, questInstance) != null)
                                        return LoadPageTypes.COMPLETE_STAGE;
                                    else return LoadPageTypes.COMPLETE_QUEST;
                                }
                            }
                        }
                    }
                }
                case OPTION_2 -> {
                    if (pages.get(currentPageIndex).dialogueType == DialogueStage.DialogueType.CHOICE) {
                        return LoadPageTypes.CHOOSE_NEGATIVE_DIALOGUE_OPTION;
                    }
                    if(questInstance.getCurrentStage().interruptedDialogue.main != null) {
                        if (pages == questInstance.getCurrentStage().interruptedDialogue.main.pages || pages == questInstance.getCurrentStage().interruptedDialogue.main.altPages){
                            return LoadPageTypes.CLOSE_DIALOGUE;
                        }
                    }
                    if (questInstance.getQuest().reconsiderDialogueSet.main != null) {
                        if (pages == questInstance.getQuest().reconsiderDialogueSet.main.pages) {
                            if (currentPageIndex >= pages.size() - 1)
                                return LoadPageTypes.RECONSIDER_NEGATIVE;
                        }
                    }
                    if (currentPageIndex >= pages.size() - 1) {
                        if (questInstance.getQuest().stages.get(0).dialogueSet.negative != null) {
                            if (pages == questInstance.getQuest().stages.get(0).dialogueSet.main.pages)
                                return LoadPageTypes.DECLINE_DIALOGUE;
                        }
                    }
                    if(!questInstance.isComplete(player)) {
                        if(questInstance.getCurrentStage() != questInstance.getQuest().stages.get(0)) {
                            if (questInstance.getCurrentStage().canCompleteStage(player, questInstance, villager)) {
                               if(pages.get(currentPageIndex).button1Text == null) {
                                   if (questInstance.getCurrentStage().getNextStage(player, questInstance) != null)
                                       return LoadPageTypes.COMPLETE_STAGE;
                                   else return LoadPageTypes.COMPLETE_QUEST;
                               }
                            }
                        }
                    }
                    return LoadPageTypes.CLOSE_DIALOGUE;
                }
            }
        }
        return LoadPageTypes.CLOSE_DIALOGUE;
    }

}