package net.kenji.rpg_villager_quests.quest_system.quest_data;

import net.kenji.rpg_villager_quests.manager.VillagerQuestManager;
import net.kenji.rpg_villager_quests.quest_system.Quest;
import net.kenji.rpg_villager_quests.quest_system.QuestEffects;
import net.kenji.rpg_villager_quests.quest_system.QuestStage;
import net.kenji.rpg_villager_quests.quest_system.Reputation;
import net.kenji.rpg_villager_quests.quest_system.quest_data.saved_data.QuestSavedData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.*;

public class QuestInstance {
    private final Quest questDefinition;

    private int currentStageIndex;
    private boolean completed;
    private Reputation questReputation;
    private UUID questVillagerUUID;
    public UUID currentSecondaryEntity;

    // ✅ Store which stages are complete for THIS instance
    private final Set<String> completedStageIds = new HashSet<>();

    public QuestInstance(Quest quest, UUID villager) {
        this.questDefinition = quest;
        this.currentStageIndex = 0;
        this.completed = false;
        this.questVillagerUUID = villager;
    }

    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();

        tag.putString("QuestId", questDefinition.getQuestId());
        tag.putString("Stage", getCurrentStage().id);
        tag.putBoolean("Completed", completed);

        if (questVillagerUUID != null) {
            tag.putUUID("VillagerUUID", questVillagerUUID);
        }

        if (currentSecondaryEntity != null) {
            tag.putUUID("SecondaryEntity", currentSecondaryEntity);
        }

        if (questReputation != null) {
            tag.putString("Reputation", questReputation.name());
        }

        // ✅ Save completed stages
        tag.putInt("CompletedStageCount", completedStageIds.size());
        int i = 0;
        for (String stageId : completedStageIds) {
            tag.putString("CompletedStage_" + i, stageId);
            i++;
        }

        return tag;
    }

    public static QuestInstance fromNBT(String questId, CompoundTag tag) throws Exception {
        Quest quest = VillagerQuestManager.getQuestByName(questId);

        QuestInstance instance = new QuestInstance(quest, null);

        // Use contains() instead of getString().isEmpty()
        if(tag.contains("Stage")) {
            instance.setCurrentStage(tag.getString("Stage"));
        }

        if(tag.contains("Completed")) {
            instance.completed = tag.getBoolean("Completed");
        }

        if(tag.hasUUID("VillagerUUID")) {
            instance.questVillagerUUID = tag.getUUID("VillagerUUID");
        }

        if(tag.hasUUID("SecondaryEntity")) {
            instance.currentSecondaryEntity = tag.getUUID("SecondaryEntity");
        }

        if(tag.contains("Reputation")) {
            instance.questReputation = Reputation.valueOf(tag.getString("Reputation"));
        }

        if(tag.contains("CompletedStageCount")) {
            int completedCount = tag.getInt("CompletedStageCount");
            for (int i = 0; i < completedCount; i++) {
                String stageId = tag.getString("CompletedStage_" + i);
                if (!stageId.isEmpty()) {
                    instance.completedStageIds.add(stageId);
                }
            }
        }

        return instance;
    }

    // ✅ Check if a stage is complete for THIS instance
    public boolean isStageComplete(String stageId) {
        return completedStageIds.contains(stageId);
    }

    public boolean isStageComplete(QuestStage stage) {
        return completedStageIds.contains(stage.id);
    }

    // ✅ Mark a stage as complete for THIS instance
    public void markStageComplete(QuestStage stage) {
        completedStageIds.add(stage.id);
    }

    public UUID getQuestVillager(){
        return questVillagerUUID;
    }

    public void advanceFromCurrentStage(ServerPlayer player) {
        QuestStage currentStage = getCurrentStage();
        markStageComplete(currentStage); // Mark current stage complete before advancing

        getCurrentStage().getNextStage(player, this).start(player, this);

        if (!player.level().isClientSide) {
            ServerLevel level = (ServerLevel) player.level();
            QuestSavedData.get(level).markDirty();
        }
        QuestData.syncToClient(player);
    }

    public boolean isComplete() {
        return completed;
    }

    public void triggerQuestComplete(QuestEffects effects, ServerPlayer player){
        completed = true;

        // ✅ Mark all stages complete
        for (QuestStage stage : questDefinition.stages) {
            markStageComplete(stage);
        }

        if(QuestData.get(player) != null) {
            QuestInstance questInstance = QuestData.get(player).getQuestInstance(questDefinition.getQuestId());
            QuestData.get(player).removeActiveQuest(questDefinition.getQuestId());
            QuestData.get(player).addCompletedQuest(questDefinition.getQuestId(), questInstance);
        }


            ServerLevel level = (ServerLevel) player.level();
            QuestSavedData.get(level).markDirty();
            QuestData.syncToClient(player);

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