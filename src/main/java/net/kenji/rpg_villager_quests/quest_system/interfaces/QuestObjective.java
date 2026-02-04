package net.kenji.rpg_villager_quests.quest_system.interfaces;

import net.kenji.rpg_villager_quests.quest_system.QuestEffects;
import net.kenji.rpg_villager_quests.quest_system.quest_data.QuestInstance;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;

public interface QuestObjective {
    void onStartObjective(Player player, QuestInstance questInstance);
    void onRestartObjective(Player player);
    boolean canComplete(Player player, QuestInstance questInstance, UUID villagerUuid);
    void onComplete(QuestEffects effects, Player player);
    boolean shouldRestartObjective(Player player);

}
