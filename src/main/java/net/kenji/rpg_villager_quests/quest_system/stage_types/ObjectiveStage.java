package net.kenji.rpg_villager_quests.quest_system.stage_types;

import net.kenji.rpg_villager_quests.quest_system.QuestStage;
import net.kenji.rpg_villager_quests.quest_system.interfaces.QuestObjective;
import net.minecraft.world.entity.player.Player;

import java.util.List;

public class ObjectiveStage extends QuestStage {

    private final QuestObjective objective;
    private final String nextStage;

    public ObjectiveStage(String id, QuestObjective objective, String nextStage, List<String> pages) {
        super(id, QuestStageTypes.valueOf("objective".toUpperCase()), pages);
        this.objective = objective;
        this.nextStage = nextStage;

    }

    @Override
    public void start(Player player) {}

    @Override
    public boolean isComplete(Player player) {
        return objective.isComplete(player);
    }

    public void turnIn(Player player) {
        objective.onTurnIn(player);
    }

    @Override
    public String getNextStageId() {
        return nextStage;
    }

    @Override
    public List<String> getDialogue() {
        return pages;
    }
}
