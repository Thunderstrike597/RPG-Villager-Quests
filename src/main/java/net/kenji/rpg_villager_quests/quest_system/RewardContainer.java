package net.kenji.rpg_villager_quests.quest_system;

import net.kenji.rpg_villager_quests.quest_system.interfaces.QuestReward;

import java.util.List;

public class RewardContainer {
    public static RewardContainer EMPTY = new RewardContainer("EMPTY");

    public final String rewardsId;
    RewardContainer(String rewardsId){
        this.rewardsId = rewardsId;
    }

    public List<QuestReward> rewards;
}
