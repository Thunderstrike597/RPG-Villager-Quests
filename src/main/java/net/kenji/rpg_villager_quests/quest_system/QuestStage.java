package net.kenji.rpg_villager_quests.quest_system;

import net.minecraft.world.entity.player.Player;

import java.util.List;

public abstract class QuestStage {
    public final String id;
    public final QuestStageType type;
    public final List<Page> pages;
    public final String belongingQuestId;
    public final String nextStageId;

    protected boolean isComplete;

    public static class Page{
        public String text;
        public String button1Text;
        public String button2Text;

    }


    protected QuestStage(String id, QuestStageType type, List<Page> pages, String belongingQuestId, String nextStageId) {
        this.id = id;
        this.type = type;
        this.pages = pages;
        this.belongingQuestId = belongingQuestId;
        this.nextStageId = nextStageId;
    }
    public abstract void start(Player player);
    public abstract boolean isComplete(Player player);
    public abstract QuestStage getNextStage(Player player);
    public abstract void onComplete(Player player);
    public abstract List<String> getDialogue();
    public abstract boolean canCompleteStage(Player player);
}

