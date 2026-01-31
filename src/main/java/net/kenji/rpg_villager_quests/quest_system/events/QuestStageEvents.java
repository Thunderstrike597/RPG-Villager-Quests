package net.kenji.rpg_villager_quests.quest_system.events;

import net.kenji.rpg_villager_quests.quest_system.QuestStage;
import net.kenji.rpg_villager_quests.quest_system.quest_data.QuestInstance;
import net.minecraftforge.eventbus.api.Event;

public class QuestStageEvents {
    public static class StageStartEvent extends Event {

        private final QuestInstance questInstance;

        public StageStartEvent(QuestInstance quest) {
            this.questInstance = quest;
        }

        public QuestInstance getQuestInstance() {
            return questInstance;
        }
    }

    public static class StageCompleteEvent extends Event {
        private final QuestInstance questInstance;
        private final QuestStage completedStage; // Add this

        public StageCompleteEvent(QuestInstance questInstance, QuestStage completedStage) {
            this.questInstance = questInstance;
            this.completedStage = completedStage;
        }

        public QuestStage getCompletedStage() {
            return completedStage;
        }

        public QuestInstance getQuestInstance() {
            return questInstance;
        }
    }
}
