package net.kenji.rpg_villager_quests.quest_system;

import net.kenji.rpg_villager_quests.quest_system.interfaces.QuestReward;
import net.minecraft.world.entity.player.Player;

import java.util.List;

public class QuestChoice {
    public final String id;
    public final String text;
    public final boolean endQuest;
    public final QuestEffects effects;
    public final List<QuestReward> rewards;

    public QuestChoice(
            String id,
            String text,
            boolean endQuest,
            QuestEffects effects,
            List<QuestReward> rewards
    ) {
        this.id = id;
        this.text = text;
        this.endQuest = endQuest;
        this.effects = effects;
        this.rewards = rewards;
    }
}