package net.kenji.rpg_villager_quests.quest_system.quest_data;

import net.kenji.rpg_villager_quests.network.ModPacketHandler;
import net.kenji.rpg_villager_quests.network.packets.AddQuestPacket;
import net.kenji.rpg_villager_quests.quest_system.Quest;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class PlayerQuestData {

    private final Map<String, QuestInstance> activeQuests = new HashMap<>();
    private final Map<String, QuestInstance> completedQuests = new HashMap<>();

    public Collection<QuestInstance> getActiveQuests() {
        return activeQuests.values();
    }

    public Map<String, QuestInstance> getActiveQuestMap() {
        return activeQuests;
    }

    public Map<String, QuestInstance> getCompletedQuestMap() {
        return completedQuests;
    }


    public void addQuest(QuestInstance instance) {
        activeQuests.put(instance.getQuest().getQuestId(), instance);
    }

    public void completeQuest(String questId) {
        QuestInstance inst = activeQuests.remove(questId);
        if (inst != null) {
            completedQuests.put(questId, inst);
        }
    }
    public void addCompetedQuest(String questName, QuestInstance questInstance){
        if(completedQuests.get(questName) == null){
            activeQuests.put(questName, questInstance);
        }
    }
    public void removeActiveQuest(String questName){
        if(activeQuests.get(questName) != null){
            activeQuests.remove(questName);
        }
    }
    public QuestInstance getQuestInstance(String questId) {
        return activeQuests.get(questId);
    }

    public void putQuest(Quest quest, Villager villager){
        QuestInstance instance = new QuestInstance(quest, villager);
        activeQuests.put(quest.getQuestId(), instance);
    }

    public QuestInstance startQuestClient(Quest quest, Villager villager, Player player) {
        QuestInstance instance = new QuestInstance(quest, villager);
        activeQuests.put(quest.getQuestId(), instance);
        QuestInstance questInstance = getQuestInstance(quest.getQuestId());
        if(questInstance != null){
            questInstance.advanceFromCurrentStage(player);
        }
        villager.setGlowingTag(true);
        return instance;
    }
    public void startQuestServer(String questId, Villager villager) {
        ModPacketHandler.sendToServer(new AddQuestPacket(questId, villager.getUUID()));
    }

    /* ------------------ REQUIRED FOR CAP ------------------ */

    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();

        CompoundTag active = new CompoundTag();
        activeQuests.forEach((id, quest) ->
                active.put(id, quest.serializeNBT())
        );

        CompoundTag completed = new CompoundTag();
        completedQuests.forEach((id, questIntance) ->
                completed.put(id, questIntance.serializeNBT())
        );

        tag.put("ActiveQuests", active);
        tag.put("CompletedQuests", completed);
        return tag;
    }

    public void deserializeNBT(CompoundTag tag) throws Exception {
        activeQuests.clear();
        completedQuests.clear();

        CompoundTag active = tag.getCompound("ActiveQuests");
        for (String key : active.getAllKeys()) {
            activeQuests.put(key, QuestInstance.fromNBT(active.getCompound(key)));
        }

        CompoundTag completed = tag.getCompound("CompletedQuests");
        for (String key : completed.getAllKeys()) {
            completedQuests.put(key, QuestInstance.fromNBT(completed.getCompound(key)));
        }
    }

    public void copyFrom(PlayerQuestData other) throws Exception {
        deserializeNBT(other.serializeNBT());
    }
}
