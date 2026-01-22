package net.kenji.rpg_villager_quests.quest_system.interfaces;

import net.minecraft.world.entity.player.Player;

public interface QuestObjective {
    boolean canComplete(Player player);
    void onComplete(Player player);
}
