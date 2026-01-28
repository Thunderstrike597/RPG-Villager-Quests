package net.kenji.rpg_villager_quests.quest_system.objective_types;

import net.kenji.rpg_villager_quests.quest_system.Page;
import net.kenji.rpg_villager_quests.quest_system.interfaces.QuestObjective;

import java.util.List;

public abstract class SecondaryVillagerQuestObjective implements QuestObjective {
    public final List<Page> secondaryVillagerDialogue;
    public final List<Page> completedSecondaryVillagerDialogue;


    SecondaryVillagerQuestObjective(List<Page> secondaryVillagerDialogue, List<Page> completedSecondaryVillagerDialogue){
        this.secondaryVillagerDialogue = secondaryVillagerDialogue;
        this.completedSecondaryVillagerDialogue = completedSecondaryVillagerDialogue;
    }

}
