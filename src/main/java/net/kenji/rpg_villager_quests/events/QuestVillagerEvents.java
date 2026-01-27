package net.kenji.rpg_villager_quests.events;

import net.kenji.rpg_villager_quests.RpgVillagerQuests;
import net.kenji.rpg_villager_quests.client.menu.VillagerQuestMenu;
import net.kenji.rpg_villager_quests.manager.VillagerQuestManager;
import net.kenji.rpg_villager_quests.quest_system.Quest;
import net.kenji.rpg_villager_quests.quest_system.capability.QuestCapabilities;
import net.kenji.rpg_villager_quests.quest_system.objective_types.PackageDeliverObjective;
import net.kenji.rpg_villager_quests.quest_system.quest_data.QuestInstance;
import net.kenji.rpg_villager_quests.quest_system.stage_types.ObjectiveStage;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jline.utils.Log;

@Mod.EventBusSubscriber(modid = RpgVillagerQuests.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class QuestVillagerEvents {

    public static String QUEST_VILLAGER_TAG = "quest_villager";
    public static String IS_QUEST_VILLAGER_TAG = "is_quest_villager";

    @SubscribeEvent
    public static void onVillagerJoin(EntityJoinLevelEvent event){
      if(event.getEntity() instanceof Villager villager) {
          if (!villager.getPersistentData().contains(QUEST_VILLAGER_TAG)) return;
          if (!villager.getPersistentData().getBoolean(IS_QUEST_VILLAGER_TAG)) return;

          villager.setGlowingTag(false);
      }
    }

    @SubscribeEvent
    public static void onVillagerSpawn(MobSpawnEvent event) throws Exception {
        if (!(event.getEntity() instanceof Villager villager)) return;
        if(villager.getSpawnType() == MobSpawnType.TRIGGERED) return;

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
        boolean roll = villager.getRandom().nextFloat() < 0.25F;
        data.putBoolean(IS_QUEST_VILLAGER_TAG, roll);

        if (!roll) return;
        VillagerQuestManager.assignRandomQuestToVillager(villager);
    }

    @Mod.EventBusSubscriber(modid = RpgVillagerQuests.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
    public static class VillagerClientEvents {
        @SubscribeEvent
        public static void onVillagerInteract(PlayerInteractEvent.EntityInteract event) {
            if (!(event.getTarget() instanceof Villager clickedVillager)) return;
            Player player = event.getEntity();

            player.getCapability(QuestCapabilities.PLAYER_QUESTS).ifPresent((questData) -> {

                // Check if this is either a quest villager OR a delivery target
                if (!clickedVillager.getPersistentData().contains(QUEST_VILLAGER_TAG)
                        && !clickedVillager.getPersistentData().contains(PackageDeliverObjective.objectiveEntityTag))
                    return;

                if (!clickedVillager.getPersistentData().getBoolean(IS_QUEST_VILLAGER_TAG)
                        && !clickedVillager.getPersistentData().contains(PackageDeliverObjective.objectiveEntityTag))
                    return;

                clickedVillager.setTradingPlayer(player);
                event.setCanceled(true);

                Villager questVillager = clickedVillager;
                Villager secondaryVillager = null;

                // If the clicked villager is a delivery target
                if (clickedVillager.getPersistentData().contains(PackageDeliverObjective.objectiveEntityTag)) {
                    if (questData.getActiveQuests() != null) {
                        for (QuestInstance questInstance : questData.getActiveQuests()) {
                            if (questInstance.getCurrentStage() instanceof ObjectiveStage objectiveStage) {
                                if (objectiveStage.getObjective() instanceof PackageDeliverObjective packageDeliverObjective) {
                                    // Check if this is the current delivery target
                                    if (packageDeliverObjective.currentDeliverEntity != null) {
                                        Log.info("Logging Package Deliver Entity: " + packageDeliverObjective.currentDeliverEntity);
                                        Log.info("Logging Clicked Entity: " + clickedVillager.getUUID());

                                        if (packageDeliverObjective.currentDeliverEntity == clickedVillager.getUUID()) {
                                            Log.info("Logging Package Quest3");

                                            questVillager = questInstance.getQuestVillager(player);  // The original quest giver
                                            secondaryVillager = clickedVillager;  // The delivery target
                                            break;  // Found the matching quest, stop searching
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // If no secondary villager was set (regular quest villager interaction)
                if (secondaryVillager == null) {
                    secondaryVillager = questVillager;
                }

                Villager finalQuestVillager = questVillager;
                Villager finalSecondaryVillager = secondaryVillager;
                Minecraft.getInstance().execute(() -> {
                    Minecraft.getInstance().setScreen(
                            new VillagerQuestMenu(Component.literal("Villager Quests"), finalQuestVillager, finalSecondaryVillager)
                    );
                });
            });
        }
    }

}
