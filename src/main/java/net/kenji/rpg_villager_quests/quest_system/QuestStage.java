package net.kenji.rpg_villager_quests.quest_system;

import net.minecraft.world.entity.player.Player;

import java.util.List;

public abstract class QuestStage {
    public final String id;
    public final QuestStageTypes type;
    public final List<String> pages;


    public enum QuestStageTypes{
        DIALGOUGUE,
        OBJECTIVE,
        DIALGOGUE_WITH_CHOICE
    }

    protected QuestStage(String id, QuestStageTypes type, List<String> pages) {
        this.id = id;
        this.type = type;
        this.pages = pages;
    }

    public abstract void start(Player player);
    public abstract boolean isComplete(Player player);
    public abstract String getNextStageId();
    public abstract List<String> getDialogue();

}

