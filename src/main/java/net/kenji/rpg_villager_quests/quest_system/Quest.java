package net.kenji.rpg_villager_quests.quest_system;

import net.kenji.rpg_villager_quests.quest_system.interfaces.QuestReward;
import net.kenji.rpg_villager_quests.quest_system.quest_data.QuestData;
import net.kenji.rpg_villager_quests.quest_system.quest_data.QuestInstance;
import net.minecraft.world.entity.player.Player;

import java.util.List;
import java.util.Objects;

public class Quest {
    public final String id;
    public final String displayName;
    public final String type;
    public final List<QuestStage> stages;
    public final QuestReward questReward;

    private int currentStageIndex = 0;

    public Quest(String id, String displayName, String type, List<QuestStage> stages, QuestReward questReward) {
        this.id = id;
        this.displayName = displayName;
        this.type = type;
        this.stages = stages;
        this.questReward = questReward;
    }

    public String getQuestId(){
        return this.id;
    }

    public QuestStage getCurrentStage(){
        return stages.get(currentStageIndex);
    }

    public QuestStage getStageById(String id){
        for(QuestStage stage : stages){
            if(Objects.equals(stage.id, id)){
                return stage;
            }
        }
        return null;
    }
    public void onQuestComplete(Player player){
        if(questReward != null){
            questReward.apply(player);
        }
    }

    public void setCurrentStageIndex(String id){
        for(int i = 0; i < stages.size(); i++){
            if(Objects.equals(stages.get(i).id, id)){
                currentStageIndex = i;
                break;
            }
        }

    }

    public QuestInstance StartQuest(Player player){
        QuestData questData = QuestData.get(player);
        return questData.addQuest(this);
    }

}

