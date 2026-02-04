package net.kenji.rpg_villager_quests.quest_system.stage_types;

import net.kenji.rpg_villager_quests.network.ModPacketHandler;
import net.kenji.rpg_villager_quests.network.packets.client_side.CompleteStageEventPacket;
import net.kenji.rpg_villager_quests.network.packets.client_side.StartStageEventPacket;
import net.kenji.rpg_villager_quests.quest_system.*;
import net.kenji.rpg_villager_quests.quest_system.events.QuestStageEvents;
import net.kenji.rpg_villager_quests.quest_system.interfaces.QuestObjective;
import net.kenji.rpg_villager_quests.quest_system.interfaces.QuestReward;
import net.kenji.rpg_villager_quests.quest_system.objective_types.SecondaryVillagerQuestObjective;
import net.kenji.rpg_villager_quests.quest_system.quest_data.QuestData;
import net.kenji.rpg_villager_quests.quest_system.quest_data.QuestInstance;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import org.jline.utils.Log;

import java.util.List;
import java.util.UUID;

public class ObjectiveStage extends QuestStage {

    private final QuestObjective objective;
    private final QuestEffects effects;

    public ObjectiveStage(String id, String displayName, QuestObjective objective, Dialogue pages, String belongingQuest, String nextStageId, QuestEffects stageEffects, List<QuestReward> questReward, String tag) {
        super(id, displayName, QuestStageType.valueOf("objective".toUpperCase()), pages, belongingQuest, nextStageId, questReward, tag, 6);
        this.objective = objective;
        this.effects = stageEffects;
    }

    public QuestEffects getStageEffects(){
        return effects;
    }
    public QuestObjective getObjective(){
        return this.objective;
    }

    @Override
    public void start(ServerPlayer player, QuestInstance questInstance) {
        questInstance.setCurrentStage(id);
        objective.onStartObjective(player, questInstance);

        QuestData.syncToClient(player);
        MinecraftForge.EVENT_BUS.post(new QuestStageEvents.StageStartEvent(questInstance));
        ModPacketHandler.sendToPlayer(new StartStageEventPacket(questInstance.getQuest().getQuestId(), this.id, questInstance.getQuestVillager()), player);
    }

    public void restartStage(Player player, QuestInstance questInstance) {
        if(objective.shouldRestartObjective(player)){
            questInstance.setCurrentStage(this.id);
            objective.onRestartObjective(player);
        };
    }

    @Override
    public boolean isComplete(Player player) {
        return isComplete;
    }

    @Override
    public QuestStage getNextStage(Player player, QuestInstance questInstance) {
        Quest quest = questInstance.getQuest();

        // 1️⃣ Explicit override
        if (nextStageId != null) {
            return quest.getStageById(nextStageId);
        }

        // 2️⃣ Fallback: advance by index
        List<QuestStage> stages = quest.stages;
        QuestStage current = questInstance.getCurrentStage();

        int index = stages.indexOf(current);

        if (index == -1) {
            return null; // stage not found → fail safely
        }

        int nextIndex = index + 1;

        if (nextIndex >= stages.size()) {
            return null; // no more stages → quest complete
        }

        return stages.get(nextIndex);
    }

    @Override
    public List<Page> getDialogue(Player player,QuestInstance questInstance, UUID interactingVillager) {
        if(!questInstance.isComplete(player)) {
            if(questInstance.currentSecondaryEntity != null) {
                if (objective instanceof SecondaryVillagerQuestObjective secondaryVillagerQuestObjective) {
                    if (interactingVillager.equals(questInstance.currentSecondaryEntity)) {
                        return secondaryVillagerQuestObjective.secondaryVillagerDialogue;
                    }
                }
            }
            return getMainPages();
        }

        return questInstance.getQuest().getCompletionDialogue(questInstance);

    }

    @Override
    public boolean canCompleteStage(Player player, QuestInstance questInstance, UUID villager) {
        return this.objective.canComplete(player, questInstance, villager);
    }

    @Override
    public boolean canCompleteStage(int currentPageIndex, Player player) {
        return currentPageIndex >= getMainPages().size() - 1;
    }

    @Override
    public void onComplete(QuestEffects completionEffects, ServerPlayer player, QuestInstance questInstance) {
        isComplete = true;
        QuestStage nextStage = getNextStage(player, questInstance);

        if (completionEffects != null) {
            if (completionEffects.giveReward) {
                if(stageRewards != null) {
                    for (QuestReward reward : stageRewards){
                        reward.apply(player);
                    }
                }
            }
            completionEffects.apply(player);
        }
        if (nextStage != null) {
            questInstance.advanceFromCurrentStage(player);
        } else {
            questInstance.triggerQuestComplete(completionEffects, player, questInstance.getQuestVillager());
        }
        objective.onComplete(completionEffects, player);
        QuestData.syncToClient(player);
        MinecraftForge.EVENT_BUS.post(new QuestStageEvents.StageCompleteEvent(questInstance, this));
        ModPacketHandler.sendToPlayer(new CompleteStageEventPacket(questInstance.getQuest().getQuestId(), this.id, questInstance.getQuestVillager()), player);
    }

    @Override
    public List<Page> getMainPages() {
        return this.dialogue.main.pages;
    }
}
