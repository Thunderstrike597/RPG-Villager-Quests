package net.kenji.rpg_villager_quests.quest_system.interfaces;

import net.kenji.rpg_villager_quests.quest_system.QuestEffects;
import net.minecraft.world.entity.player.Player;

public interface QuestObjective {
    void onStartObjective(Player player);
    void onRestartObjective(Player player);
    boolean canComplete(Player player);
    void onComplete(QuestEffects effects, Player player);
    boolean shouldRestartObjective(Player player);

}
