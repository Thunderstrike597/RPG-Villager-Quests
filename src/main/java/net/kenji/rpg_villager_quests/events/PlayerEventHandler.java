package net.kenji.rpg_villager_quests.events;

import com.google.gson.JsonObject;
import net.kenji.rpg_villager_quests.RpgVillagerQuests;
import net.kenji.rpg_villager_quests.manager.VillagerQuestManager;
import net.kenji.rpg_villager_quests.network.ModPacketHandler;
import net.kenji.rpg_villager_quests.network.packets.client_side.VillagerGlowPacket;
import net.kenji.rpg_villager_quests.quest_system.Quest;
import net.kenji.rpg_villager_quests.quest_system.quest_data.QuestData;
import net.kenji.rpg_villager_quests.quest_system.quest_data.QuestInstance;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;


@Mod.EventBusSubscriber(modid = RpgVillagerQuests.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class PlayerEventHandler {

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            QuestData questData = QuestData.get(player);

            for(String key : VillagerQuestManager.rawJsonFiles.keySet()) {
                Villager glowVillager = null;
                Quest quest = VillagerQuestManager.getQuestByName(key);
                QuestInstance questInstance = questData.getQuestInstance(quest.getQuestId());
                if (questInstance != null) {
                    UUID villagerUuid = questInstance.getQuestVillager();
                    Entity entity = player.serverLevel().getEntity(villagerUuid);
                    if (entity instanceof Villager villagerEntity) {
                        if(!questInstance.isComplete()) {
                            glowVillager = villagerEntity;
                        }
                    }
                }
                if(glowVillager != null){
                    ModPacketHandler.sendToPlayer(new VillagerGlowPacket(glowVillager.getId(), true), player);
                }
            }

            QuestData.syncToClient(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            // Re-sync on respawn
            QuestData.syncToClient(player);
        }
    }
}