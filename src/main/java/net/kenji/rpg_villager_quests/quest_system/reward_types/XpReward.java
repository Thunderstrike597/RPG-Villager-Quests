package net.kenji.rpg_villager_quests.quest_system.reward_types;

import net.kenji.rpg_villager_quests.quest_system.interfaces.QuestReward;
import net.minecraft.world.entity.player.Player;

public class XpReward implements QuestReward {
    public final int amount;

    public XpReward(int amount) {
        this.amount = amount;
    }

    @Override
    public void apply(Player player) {
        player.giveExperiencePoints(amount);
    }
}