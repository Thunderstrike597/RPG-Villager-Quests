package net.kenji.rpg_villager_quests.events;

import net.kenji.rpg_villager_quests.RpgVillagerQuests;
import net.kenji.rpg_villager_quests.client.menu.VillagerQuestMenu;
import net.kenji.rpg_villager_quests.manager.VillagerQuestManager;
import net.kenji.rpg_villager_quests.quest_system.Quest;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
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
    public static String IS_QUEST_VILLAGER_TAG = "is_quest_villager";


    @SubscribeEvent
    public static void onVillagerSpawn(MobSpawnEvent event) {
        if (!(event.getEntity() instanceof Villager villager)) return;

        if (villager.getVillagerData().getLevel() > 1) return;

        CompoundTag data = villager.getPersistentData();

        // Already evaluated → stop
        if (data.contains(IS_QUEST_VILLAGER_TAG)) {
            // Restore quest mapping if needed
            if (data.getBoolean(IS_QUEST_VILLAGER_TAG)
                    && data.contains(QUEST_VILLAGER_TAG)
                    && !VillagerQuestManager.villagerQuestMap.containsKey(villager.getUUID())) {

                String questId = data.getString(QUEST_VILLAGER_TAG);
                Quest quest = VillagerQuestManager.getQuestByName(questId);
                VillagerQuestManager.villagerQuestMap.put(villager.getUUID(), quest);
            }
            return;
        }

        // First-time roll
        boolean roll = villager.getRandom().nextFloat() < 0.12F;
        data.putBoolean(IS_QUEST_VILLAGER_TAG, roll);

        if (!roll) return;

        VillagerQuestManager.assignRandomQuestToVillager(villager);
    }
    @Mod.EventBusSubscriber(modid = RpgVillagerQuests.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
    public static class VillagerClientEvents {

        @SubscribeEvent
        public static void onVillagerInteract(PlayerInteractEvent.EntityInteract event) {
            if (!(event.getTarget() instanceof Villager villager)) return;
            Player player = event.getEntity();
            if (!villager.getPersistentData().contains(QUEST_VILLAGER_TAG)) return;
            if (!villager.getPersistentData().getBoolean(IS_QUEST_VILLAGER_TAG)) return;

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
