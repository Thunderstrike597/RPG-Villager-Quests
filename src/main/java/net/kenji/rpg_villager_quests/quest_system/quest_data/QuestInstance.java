package net.kenji.rpg_villager_quests.quest_system.quest_data;

import net.kenji.rpg_villager_quests.manager.VillagerQuestManager;
import net.kenji.rpg_villager_quests.quest_system.Quest;
import net.kenji.rpg_villager_quests.quest_system.QuestEffects;
import net.kenji.rpg_villager_quests.quest_system.QuestStage;
import net.kenji.rpg_villager_quests.quest_system.Reputation;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;

import java.util.Objects;
import java.util.UUID;

public class QuestInstance {
    private final Quest questDefinition;

    private int currentStageIndex;
    private boolean completed;
    private Reputation questReputation;
    private UUID questVillagerUUID;

    public QuestInstance(Quest quest, UUID villager) {
        this.questDefinition = quest;
        this.currentStageIndex = 0;
        this.completed = false;
        this.questVillagerUUID = villager;
    }
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();

        // 🔑 Quest identity
        tag.putString("QuestId", questDefinition.getQuestId());

        // 📌 Progress state
        tag.putString("Stage", getCurrentStage().id);
        tag.putBoolean("Completed", completed);

        // 👤 Linked villager
        if (questVillagerUUID != null) {
            tag.putUUID("VillagerUUID", questVillagerUUID);
        }

        return tag;
    }

    public static QuestInstance fromNBT(CompoundTag tag) throws Exception {
        String questId = tag.getString("QuestId");

        Quest quest = VillagerQuestManager.getQuestByName(questId);


        QuestInstance instance = new QuestInstance(quest, null);

        instance.setCurrentStage(tag.getString("Stage"));
        instance.completed = tag.getBoolean("Completed");

        if (tag.hasUUID("VillagerUUID")) {
            instance.questVillagerUUID = tag.getUUID("VillagerUUID");
        }

        return instance;
    }
    public UUID getQuestVillager(){
        return questVillagerUUID;
    }

    public void advanceFromCurrentStage(Player player) {
        getCurrentStage().getNextStage(player, this).start(player, this);
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