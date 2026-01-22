package net.kenji.rpg_villager_quests.quest_system.interfaces;

import net.minecraft.world.entity.player.Player;

public interface QuestObjective {
    boolean isComplete(Player player);
    void onTurnIn(Player player);
}
