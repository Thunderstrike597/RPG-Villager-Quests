package net.kenji.rpg_villager_quests.quest_system;

import net.kenji.rpg_villager_quests.client.menu.VillagerQuestMenu;

public enum CompletionType {
    STAGE(true, VillagerQuestMenu.INSTANCE::onCompleteStage),
    QUEST(true, VillagerQuestMenu.INSTANCE::onCompleteQuest),
    SKIP(false, null);

    public boolean trigger;
    public Runnable callMethod;

    private CompletionType(boolean trigger, Runnable callMethod){
        this.trigger = trigger;
        this.callMethod = callMethod;
    }

    public void triggerComplete(){
        if(this.trigger && this.callMethod != null){
            this.callMethod.run();
        }
    }
}
