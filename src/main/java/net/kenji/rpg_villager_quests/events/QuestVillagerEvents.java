package net.kenji.rpg_villager_quests.events;

import net.kenji.rpg_villager_quests.RpgVillagerQuests;
import net.kenji.rpg_villager_quests.client.menu.VillagerQuestMenu;
import net.kenji.rpg_villager_quests.manager.VillagerQuestManager;
import net.kenji.rpg_villager_quests.quest_system.Quest;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = RpgVillagerQuests.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class QuestVillagerEvents {

    public static String QUEST_VILLAGER_TAG = "quest_villager";


    @SubscribeEvent
    public static void onVillagerSpawn(MobSpawnEvent event){
        if(event.getEntity() instanceof Villager villager){
            //if(villager.getVillagerXp() > 0 || villager.getVillagerData().getLevel() > 0) return;

            if(villager.getRandom().nextFloat() < 0.4F){
                if(villager.getPersistentData().get(QUEST_VILLAGER_TAG) == null){
                    if(VillagerQuestManager.villagerQuestMap.get(villager.getUUID()) == null){
                        VillagerQuestManager.assignRandomQuestToVillager(villager);
                    }
                }
            }
            if(villager.getPersistentData().get(QUEST_VILLAGER_TAG) != null){
                if(VillagerQuestManager.villagerQuestMap.get(villager.getUUID()) == null){
                    VillagerQuestManager.assignRandomQuestToVillager(villager);
                }
                String assignedQuest = villager.getPersistentData().getString(QUEST_VILLAGER_TAG);
                Quest quest = VillagerQuestManager.getQuestByName(assignedQuest);
                VillagerQuestManager.villagerQuestMap.put(villager.getUUID(), quest);
            }
        }
    }
    @Mod.EventBusSubscriber(modid = RpgVillagerQuests.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
    public static class VillagerClientEvents {

        @SubscribeEvent
        public static void onVillagerInteract(PlayerInteractEvent.EntityInteract event) {
            if (!(event.getTarget() instanceof Villager villager)) return;
            Player player = event.getEntity();
            if (villager.getPersistentData().getString(QUEST_VILLAGER_TAG).isEmpty()) return;

            villager.setTradingPlayer(player);
            event.setCanceled(true);
            Minecraft.getInstance().execute(() -> {
                Minecraft.getInstance().setScreen(
                        new VillagerQuestMenu(Component.literal("Villager Quests"), villager)
                );
            });

        }
    }

}
