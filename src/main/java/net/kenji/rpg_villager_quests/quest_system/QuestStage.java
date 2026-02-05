package net.kenji.rpg_villager_quests.quest_system;

import net.kenji.rpg_villager_quests.quest_system.interfaces.QuestReward;
import net.kenji.rpg_villager_quests.quest_system.quest_data.QuestInstance;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.List;
import java.util.UUID;

public abstract class QuestStage {
    public final String id;
    public final String displayName;
    public final QuestStageType type;
    public final Dialogue dialogue;
    public final String belongingQuestId;
    public final String nextStageId;
    public final List<QuestReward> stageRewards;
    public final String tag;
    public final int waypointColorIndex;

    public boolean isComplete;

    protected QuestStage(String id, String displayName, QuestStageType type, Dialogue pages, String belongingQuestId, String nextStageId, List<QuestReward> questReward, String tag, int waypointColorIndex) {
        this.id = id;
        this.type = type;
        this.dialogue = pages;
        this.belongingQuestId = belongingQuestId;
        this.nextStageId = nextStageId;
        this.stageRewards = questReward;
        this.tag = tag;
        this.displayName = displayName;
        this.waypointColorIndex = waypointColorIndex;
    }
    public abstract void start(ServerPlayer player, QuestInstance questInstance);
    public abstract boolean isComplete(Player player);
    public abstract QuestStage getNextStage(Player player, QuestInstance questInstance);
    public abstract void onComplete(QuestEffects completionEffects, ServerPlayer player, QuestInstance questInstance);

    public abstract List<Page> getDialogue(Player player, QuestInstance questInstance, UUID interactVillager);
    public abstract boolean canCompleteStage(Player player, QuestInstance questInstance, UUID villager);
    public abstract boolean canCompleteStage(int currentPageIndex,Player player);
    public abstract List<Page> getMainPages();

    public boolean hasTag(String tag){
       return this.tag != null && this.tag.equals(tag);
    }
}

