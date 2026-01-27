package net.kenji.rpg_villager_quests.quest_system.capability;

import net.kenji.rpg_villager_quests.quest_system.quest_data.PlayerQuestData;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;

public class QuestCapabilities {
    public static final Capability<PlayerQuestData> PLAYER_QUESTS =
            CapabilityManager.get(new CapabilityToken<>() {});
}