package net.kenji.rpg_villager_quests.quest_system;

import java.util.List;

public class Quest {
    public final String id;
    public final String type;
    public final List<QuestStage> stages;

    private int currentStageIndex = 0;

    public Quest(String id, String type, List<QuestStage> stages) {
        this.id = id;
        this.type = type;
        this.stages = stages;
    }

    public QuestStage getCurrentStage() {
        return stages.get(currentStageIndex);
    }

    public void advanceTo(String stageId) {
        for (int i = 0; i < stages.size(); i++) {
            if (stages.get(i).id.equals(stageId)) {
                currentStageIndex = i;
                return;
            }
        }
        throw new IllegalStateException("Stage not found: " + stageId);
    }

    public boolean isComplete() {
        return currentStageIndex >= stages.size();
    }
}

