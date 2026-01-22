package net.kenji.rpg_villager_quests.quest_system;

import net.kenji.rpg_villager_quests.quest_system.quest_data.QuestData;
import net.kenji.rpg_villager_quests.quest_system.quest_data.QuestInstance;
import net.minecraft.world.entity.player.Player;

import java.util.List;

public class Quest {
    public final String id;
    public final String displayName;
    public final String type;
    public final List<QuestStage> stages;

    private int currentStageIndex = 0;

    public Quest(String id, String displayName, String type, List<QuestStage> stages) {
        this.id = id;
        this.displayName = displayName;
        this.type = type;
        this.stages = stages;
    }

    public String getQuestId(){
        return this.id;
    }

    public QuestInstance StartQuest(Player player){
        QuestData questData = QuestData.get(player);
        return questData.addQuest(this);
    }

}

