package net.kenji.rpg_villager_quests.quest_system.enum_types;

import net.kenji.rpg_villager_quests.client.menu.VillagerQuestMenu;

public enum LoadPageTypes {
    NEXT_MAIN_PAGE(VillagerQuestMenu.INSTANCE::onNextPage),
    RECONSIDER_POSITIVE(() -> VillagerQuestMenu.INSTANCE.onReconsiderChoose(ChoiceType.OPTION_1)),
    RECONSIDER_NEGATIVE(() -> VillagerQuestMenu.INSTANCE.onReconsiderChoose(ChoiceType.OPTION_2)),
    CHOOSE_POSITIVE_DIALOGUE_OPTION(() -> VillagerQuestMenu.INSTANCE.onChoiceSelect(ChoiceType.OPTION_1)),
    CHOOSE_NEGATIVE_DIALOGUE_OPTION(() -> VillagerQuestMenu.INSTANCE.onChoiceSelect(ChoiceType.OPTION_2)),
    RECONTINUE_DIALOGUE(VillagerQuestMenu.INSTANCE::onRecontinueDialogue),
    ACCEPT_QUEST(() -> VillagerQuestMenu.INSTANCE.queQuestAccept(true)),
    ACCEPT_DIALOGUE(() -> VillagerQuestMenu.INSTANCE.onIntroDialgogueSelectDialogue(true)),
    DECLINE_DIALOGUE(() -> VillagerQuestMenu.INSTANCE.onIntroDialgogueSelectDialogue(false)),
    COMPLETE_STAGE(VillagerQuestMenu.INSTANCE::onCompleteStage),
    COMPLETE_QUEST(VillagerQuestMenu.INSTANCE::onCompleteQuest),
    CLOSE_DIALOGUE(VillagerQuestMenu.INSTANCE::onClose);

    public Runnable pressMethod;


    LoadPageTypes(Runnable pressMethod){
        this.pressMethod = pressMethod;
    }

}
