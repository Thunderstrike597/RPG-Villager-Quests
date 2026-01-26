package net.kenji.rpg_villager_quests.quest_system.quest_data;

import net.kenji.rpg_villager_quests.quest_system.Quest;
import net.kenji.rpg_villager_quests.quest_system.QuestEffects;
import net.kenji.rpg_villager_quests.quest_system.QuestStage;
import net.kenji.rpg_villager_quests.quest_system.Reputation;
import net.kenji.rpg_villager_quests.quest_system.interfaces.QuestReward;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;

import java.util.List;
import java.util.Objects;

public class QuestInstance {
    private final Quest questDefinition;
    private int currentStageIndex;
    private boolean completed;
    private Reputation questReputation;
    private Villager questVillager;

    public QuestInstance(Quest quest, Villager villager) {
        this.questDefinition = quest;
        this.currentStageIndex = 0;
        this.completed = false;
        this.questVillager = villager;
    }

    public Villager getQuestVillager(){
        return questVillager;
    }

    public void advanceFromCurrentStage(Player player) {
        QuestData questData = QuestData.get(player.getUUID());
        QuestInstance questInstance = questData.getQuestInstance(questDefinition.getQuestId());
        if(questInstance != null) {
            if(getCurrentStage().getNextStage(player, this) == null) {
                for (int i = 0; i < questDefinition.stages.size(); i++) {
                    if (questDefinition.stages.get(i) == questInstance.getCurrentStage() && i + 1 < questDefinition.stages.size()) {
                        currentStageIndex = i + 1;
                        return;
                    }
                }
            }
            setCurrentStage(getCurrentStage().nextStageId);
        }
    }

    public boolean isComplete() {
        return completed;
    }
    public void triggerQuestComplete(QuestEffects effects, Player player){
        completed = true;
        if(QuestData.get(player.getUUID()) != null) {
            QuestInstance questInstance = QuestData.get(player.getUUID()).getQuestInstance(questDefinition.getQuestId());
            QuestData.get(player.getUUID()).removeActiveQuest(questDefinition.getQuestId());
            QuestData.get(player.getUUID()).addCompetedQuest(questDefinition.getQuestId(), questInstance);
        }
    }
    public Quest getQuest() {
        return questDefinition;
    }
    public void setCurrentStageIndexByName(String id){
        for(int i = 0; i < questDefinition.stages.size(); i++){
            if(Objects.equals(questDefinition.stages.get(i).id, id)){
                currentStageIndex = i;
                break;
            }
        }
    }
    public void setCurrentStage(String id) {
        setCurrentStageIndexByName(id);
    }

    public QuestStage getCurrentStage() {
        return getQuest().stages.get(currentStageIndex);
    }
    public int getCurrentStageIndex() {
       for(int i = 0; i < getQuest().stages.stream().count(); i++) {
           if (getQuest().stages.get(i) == getCurrentStage()) {
               return i;
           }
       }
        return 0;
    }

    public void setQuestReputation(Reputation questReputation) {
        this.questReputation = questReputation;
    }

    public Reputation getQuestReputation() {
        return questReputation;
    }
}