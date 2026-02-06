package net.kenji.rpg_villager_quests.events;

import net.kenji.rpg_villager_quests.RpgVillagerQuests;
import net.kenji.rpg_villager_quests.client.menu.VillagerQuestMenu;
import net.kenji.rpg_villager_quests.entity.goals.MoveToPlayerGoal;
import net.kenji.rpg_villager_quests.entity.villager.VillagerQuestTypes;
import net.kenji.rpg_villager_quests.manager.VillagerQuestManager;
import net.kenji.rpg_villager_quests.quest_system.Quest;
import net.kenji.rpg_villager_quests.quest_system.objective_types.PackageDeliverObjective;
import net.kenji.rpg_villager_quests.quest_system.quest_data.QuestData;
import net.kenji.rpg_villager_quests.quest_system.quest_data.QuestInstance;
import net.kenji.rpg_villager_quests.quest_system.stage_types.ObjectiveStage;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.MoveToBlockGoal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jline.utils.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
          villager.setVillagerData(
                  villager.getVillagerData().setType(VillagerQuestTypes.QUEST_VILLAGER)
          );
          Quest quest = VillagerQuestManager.getVillagerQuest(villager.getUUID());
          if(quest != null) {
              if (quest.questProfession != null) {
                  villager.setVillagerData(
                          villager.getVillagerData().setProfession(quest.questProfession)
                                  .setLevel(2)
                  );
              }
          }
      }
    }

    @SubscribeEvent
    public static void onVillagerFinalize(MobSpawnEvent.FinalizeSpawn event) {
        if (!(event.getEntity() instanceof Villager villager)) return;

        if (event.getSpawnType() == MobSpawnType.TRIGGERED) {
            villager.getPersistentData().putBoolean("ForceNoQuest", true);
        }
    }

        @SubscribeEvent
        public static void onVillagerSpawn(MobSpawnEvent event) {
            if (!(event.getEntity() instanceof Villager villager)) return;

            if(villager.getSpawnType() == MobSpawnType.EVENT || villager.getPersistentData().getBoolean(IS_QUEST_VILLAGER_TAG)){
                if(villager.getPersistentData().getBoolean("ForceNoQuest")){
                    villager.getPersistentData().remove("ForceNoQuest");
                }
            }
            if (villager.getPersistentData().getBoolean("ForceNoQuest")) return;

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

            if(villager.getSpawnType() == MobSpawnType.EVENT) return;
            Quest villagerQuest = VillagerQuestManager.getVillagerQuest(villager.getUUID());

            if (villager.getVillagerData().getLevel() > 1 && villagerQuest == null) return;


            boolean roll = villager.getRandom().nextFloat() < 0.18F;
            data.putBoolean(IS_QUEST_VILLAGER_TAG, roll);

            if (!roll) return;


            Quest quest = VillagerQuestManager.getRandomQuest(villager);
            VillagerQuestManager.assignQuestToVillager(quest, villager);

            if (quest.questProfession != null) {
                villager.setVillagerData(
                        villager.getVillagerData()
                                .setProfession(quest.questProfession)
                                .setLevel(2)
                );
            }
        }
    private static boolean hasFollowGoal(Villager villager) {
        for (WrappedGoal wrapped : villager.goalSelector.getAvailableGoals()) {
            if (wrapped.getGoal() instanceof MoveToPlayerGoal) {
                return true;
            }
        }
        return false;
    }

    @SubscribeEvent
    public static void onVillagerTick(LivingEvent.LivingTickEvent event) {
        if (!(event.getEntity() instanceof Villager villager)) return;

        Player player = villager.getTradingPlayer();

        if (player != null) {
            Log.info("Player Is NOT Null!");
            if (VillagerQuestManager.isQuestMenuOpen
                    .getOrDefault(player.getUUID(), false)) {
                Log.info("Logging MoveToPlayerGoal");
                if (!hasFollowGoal(villager)) {
                    villager.goalSelector.addGoal(
                            0,
                            new MoveToPlayerGoal(villager, 0.05D, 1.8F)
                    );
                }
            }
        }else {

            villager.goalSelector.getAvailableGoals().removeIf(
                    wrapped -> wrapped.getGoal() instanceof MoveToPlayerGoal
            );
        }
    }
        @Mod.EventBusSubscriber(modid = RpgVillagerQuests.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
    public static class VillagerClientEvents {
        @SubscribeEvent
        public static void onVillagerInteract(PlayerInteractEvent.EntityInteract event) {
            if (!(event.getTarget() instanceof Villager clickedVillager)) return;
            Player player = event.getEntity();

            // Check if this is either a quest villager OR a delivery target
            if (!clickedVillager.getPersistentData().contains(QUEST_VILLAGER_TAG)
                    && !clickedVillager.getPersistentData().contains(PackageDeliverObjective.objectiveEntityTag)) return;

            if (!clickedVillager.getPersistentData().getBoolean(IS_QUEST_VILLAGER_TAG)
                    && !clickedVillager.getPersistentData().contains(PackageDeliverObjective.objectiveEntityTag)) return;

            clickedVillager.setTradingPlayer(player);
            event.setCanceled(true);

            UUID questVillager = clickedVillager.getUUID();
            UUID secondaryVillager = null;

            // If the clicked villager is a delivery target
            if(clickedVillager.getPersistentData().contains(PackageDeliverObjective.objectiveEntityTag)){
                QuestData questData = QuestData.get(player);
                Quest quest = VillagerQuestManager.getVillagerQuest(clickedVillager.getPersistentData().getUUID(PackageDeliverObjective.objectiveEntityTag));
                if(quest != null) {
                    QuestInstance questInstance = questData.getQuestInstance(quest.getQuestId(), clickedVillager.getPersistentData().getUUID(PackageDeliverObjective.objectiveEntityTag), false);
                    questVillager = questInstance.getQuestVillager();
                    secondaryVillager = clickedVillager.getUUID();
                }
            }

            // If no secondary villager was set (regular quest villager interaction)
            if(secondaryVillager == null){
                secondaryVillager = questVillager;
            }

            UUID finalQuestVillager = questVillager;
            UUID finalSecondaryVillager = secondaryVillager;
            Minecraft.getInstance().execute(() -> {
                Minecraft.getInstance().setScreen(
                        VillagerQuestMenu.INSTANCE = new VillagerQuestMenu(Component.literal("Villager Quests"), finalQuestVillager, finalSecondaryVillager, clickedVillager.getId())
                );
            });
        }

        @SubscribeEvent
        public static void onClientTick(TickEvent.PlayerTickEvent event) {
            if (event.phase != TickEvent.Phase.END) return;

            Minecraft mc = Minecraft.getInstance();
            Player player = mc.player;

            if (player == null || mc.level == null || !player.level().isClientSide) return;

            QuestData questData = QuestData.get(player);
            if (questData.getActiveQuests(player) != null) {
                for (QuestInstance questInstance : questData.getActiveQuests(player)) {
                    if (questInstance.getCurrentStage() instanceof ObjectiveStage objectiveStage) {
                        if (objectiveStage.getObjective() instanceof PackageDeliverObjective packageDeliverObjective) {

                            if (questInstance.currentSecondaryEntity != null) {
                                for (Entity entity : mc.level.entitiesForRendering()) {
                                    if (entity instanceof Villager villager) {
                                        if (villager.getUUID().equals(questInstance.currentSecondaryEntity)) {
                                            if (!villager.hasGlowingTag()) {

                                                if(questInstance.getQuestVillager() != null) {
                                                    villager.setGlowingTag(true);
                                                    villager.getPersistentData().putUUID(
                                                            PackageDeliverObjective.objectiveEntityTag,
                                                            questInstance.getQuestVillager()
                                                    );
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

}
