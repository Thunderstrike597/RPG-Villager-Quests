package net.kenji.rpg_villager_quests.quest_system.stage_types;

import net.kenji.rpg_villager_quests.network.ModPacketHandler;
import net.kenji.rpg_villager_quests.network.packets.client_side.CompleteStageEventPacket;
import net.kenji.rpg_villager_quests.network.packets.client_side.StartStageEventPacket;
import net.kenji.rpg_villager_quests.quest_system.*;
import net.kenji.rpg_villager_quests.quest_system.events.QuestStageEvents;
import net.kenji.rpg_villager_quests.quest_system.interfaces.QuestReward;
import net.kenji.rpg_villager_quests.quest_system.objective_types.SecondaryVillagerQuestObjective;
import net.kenji.rpg_villager_quests.quest_system.quest_data.QuestData;
import net.kenji.rpg_villager_quests.quest_system.quest_data.QuestInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;

import java.util.List;
import java.util.UUID;

public class DialogueStage extends QuestStage {

    public final List<QuestChoice> choices;
    public final List<Page> choice1Pages;
    public final List<Page> choice2Pages;

    public ChoiceType chosenDialogueOption = ChoiceType.UNCHOSEN;

    public List<Page> currentDialoguePages = dialogue.main.pages;


    public enum DialogueType{
        REGULAR,
        CHOICE
    }

    public enum ChoiceType {
        UNCHOSEN,
        OPTION_1,
        OPTION_2
    }
    public DialogueStage(String id, String displayName, Dialogue pages, String belongingQuest, String nextStageId, List<QuestChoice> choices, List<Page> choice1Pages, List<Page> choice2Pages, List<QuestReward> questReward, String tag) {
        super(id, displayName,QuestStageType.valueOf("dialogue".toUpperCase()), pages, belongingQuest, nextStageId, questReward, tag, 12);
        this.choice1Pages = choice1Pages;
        this.choice2Pages = choice2Pages;
        this.choices = choices;
    }
    @Override
    public void start(ServerPlayer player, QuestInstance questInstance) {
       // ModPacketHandler.sendToServer(new StageStartPacket(belongingQuestId, id,questInstance.getQuestVillager()));
        questInstance.setCurrentStage(id);
        BlockPos entityPos = questInstance.getEntityBlockPos(player);
        if(entityPos != null) {
            questInstance.setWaypoint(entityPos.getX(), entityPos.getY(), entityPos.getZ(), questInstance.getQuestVillager());
        }
        QuestData.syncToClient(player);
        MinecraftForge.EVENT_BUS.post(new QuestStageEvents.StageStartEvent(questInstance));
        ModPacketHandler.sendToPlayer(new StartStageEventPacket(questInstance.getQuest().getQuestId(), this.id, questInstance.getQuestVillager()), player);
    }

    @Override
    public boolean isComplete(Player player) {
        return isComplete; // dialogue finishes via UI
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
        QuestData.syncToClient(player);
        MinecraftForge.EVENT_BUS.post(new QuestStageEvents.StageCompleteEvent(questInstance, this));
        ModPacketHandler.sendToPlayer(new CompleteStageEventPacket(questInstance.getQuest().getQuestId(), this.id, questInstance.getQuestVillager()), player);
    }
    public List<Page> getDialogue(Player player,QuestInstance questInstance, UUID interactVillager) {

        List<Page> pageList;
        if (choices != null && !choices.isEmpty()) {
            pageList = chosenDialogueOption == ChoiceType.OPTION_1 ? choice1Pages : chosenDialogueOption == ChoiceType.OPTION_2 ? choice2Pages : getMainPages();
        } else {
            if (interactVillager.equals(questInstance.currentSecondaryEntity)) {
                for (QuestStage stage : questInstance.getQuest().stages) {
                    if (!(stage instanceof ObjectiveStage objectiveStage)) continue;
                    if (!(objectiveStage.getObjective() instanceof SecondaryVillagerQuestObjective objective)) continue;
                    if (!objectiveStage.isComplete(player)) continue;
                    QuestStage next = objectiveStage.getNextStage(player, questInstance);


                    if (next == null || next.isComplete(player)) continue;

                    // Only play secondary-completed dialogue on the secondary villager
                    return objective.completedSecondaryVillagerDialogue;
                }
            }

            pageList = getMainPages();
        }

        return pageList;
    }

    @Override
    public boolean canCompleteStage(Player player, QuestInstance questInstance, UUID villager) {
        for (QuestStage stage : questInstance.getQuest().stages) {
            if (!(stage instanceof ObjectiveStage objectiveStage)) continue;
            if (!(objectiveStage.getObjective() instanceof SecondaryVillagerQuestObjective objective)) continue;
            if(getDialogue(player, questInstance, villager) == objective.completedSecondaryVillagerDialogue){
                return false;
            }
        }
        return true;
    }

    public void setChosenDialogue(ChoiceType choiceType){
        chosenDialogueOption = choiceType;
        if(chosenDialogueOption == ChoiceType.OPTION_1 && choice1Pages != null)
            currentDialoguePages = choice1Pages;
        if(chosenDialogueOption == ChoiceType.OPTION_2 && choice2Pages != null)
            currentDialoguePages = choice2Pages;
    }

    @Override
    public boolean canCompleteStage(int currentPageIndex, Player player) {
        if(choices != null){
           ChoiceType option = chosenDialogueOption ;
           if(option == ChoiceType.OPTION_1){
               if(choices.get(0) != null) {
                   if(choices.get(0).effects != null) {
                       if (choices.get(0).effects.endQuest) {
                           return true;
                       }
                   }
               }

           }
            if(option == ChoiceType.OPTION_2){
                if(choices.get(1) != null) {
                    if(choices.get(1).effects != null) {

                        if (choices.get(1).effects.endQuest) {
                            return true;
                        }
                    }
                }
            }
        }
        return currentPageIndex >= currentDialoguePages.size() - 1;
    }

    @Override
    public List<Page> getMainPages() {
        return this.dialogue.main.pages;
    }

}
