package net.kenji.rpg_villager_quests.quest_system;

import net.kenji.rpg_villager_quests.quest_system.interfaces.QuestReward;
import net.kenji.rpg_villager_quests.quest_system.quest_data.QuestInstance;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;

import java.util.List;
import java.util.UUID;

public abstract class QuestStage {
    public final String id;
    public final QuestStageType type;
    public final List<Page> pages;
    public final String belongingQuestId;
    public final String nextStageId;
    public final List<QuestReward> stageRewards;
    protected boolean isComplete;
    public final String tag;


    protected QuestStage(String id, QuestStageType type, List<Page> pages, String belongingQuestId, String nextStageId, List<QuestReward> questReward, String tag) {
        this.id = id;
        this.type = type;
        this.pages = pages;
        this.belongingQuestId = belongingQuestId;
        this.nextStageId = nextStageId;
        this.stageRewards = questReward;
        this.tag = tag;
    }
    public abstract void start(Player player, QuestInstance questInstance);
    public abstract boolean isComplete(Player player);
    public abstract QuestStage getNextStage(Player player, QuestInstance questInstance);
    public abstract void onComplete(QuestEffects completionEffects, Player player, QuestInstance questInstance);

    public abstract List<Page> getDialogue(QuestInstance questInstance, UUID interactVillager);
    public abstract boolean canCompleteStage(Player player);
    public abstract boolean canCompleteStage(int currentPageIndex,Player player);


}

